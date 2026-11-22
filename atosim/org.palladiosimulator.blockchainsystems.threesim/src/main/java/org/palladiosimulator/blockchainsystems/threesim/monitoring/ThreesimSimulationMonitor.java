package org.palladiosimulator.blockchainsystems.threesim.monitoring;

import org.palladiosimulator.blockchainsystems.core.behavior.AttackPhase;
import org.palladiosimulator.blockchainsystems.core.behavior.AttackPhaseTransitionTraceEvent;
import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.block.abstractions.BlockType;
import org.palladiosimulator.blockchainsystems.core.block.BlockBroadcastTraceEvent;
import org.palladiosimulator.blockchainsystems.core.block.BlockValidationFinishedTraceEvent;
import org.palladiosimulator.blockchainsystems.core.block.BlockValidationStartedTraceEvent;
import org.palladiosimulator.blockchainsystems.core.blockchain.BlockAppendedTraceEvent;
import org.palladiosimulator.blockchainsystems.core.blockchain.BlockTypeChangedTraceEvent;
import org.palladiosimulator.blockchainsystems.core.blockchain.ChainReorganizedTraceEvent;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.TraceEvent;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.TraceEventLogOrigin;
import org.palladiosimulator.blockchainsystems.core.crashes.events.CrashPlan;
import org.palladiosimulator.blockchainsystems.core.geography.GeographicalRegions;
import org.palladiosimulator.blockchainsystems.core.mining.BlockMinedTraceEvent;
import org.palladiosimulator.blockchainsystems.core.monitoring.abstractions.SimulationMonitor;
import org.palladiosimulator.blockchainsystems.core.simulation.termination.LongestChainExceededMaxLengthCondition;
import org.palladiosimulator.blockchainsystems.core.transaction.TransactionSubmittedTraceEvent;
import org.palladiosimulator.blockchainsystems.core.utils.CounterMap;
import org.palladiosimulator.blockchainsystems.threesim.behavior.BlockUtils;
import org.palladiosimulator.blockchainsystems.threesim.metrics.calculators.TransactionThroughputCalculator;
import org.palladiosimulator.blockchainsystems.threesim.simulation.AttackType;
import org.palladiosimulator.blockchainsystems.threesim.simulation.ThreesimSimulationParameters;
import org.palladiosimulator.blockchainsystems.threesim.simulation.results.ChainReorganizationOccurrence;
import org.palladiosimulator.blockchainsystems.threesim.simulation.results.EpisodeStatus;
import org.palladiosimulator.blockchainsystems.threesim.utils.BlockchainSystemFailureLog;
import org.palladiosimulator.blockchainsystems.threesim.utils.BlocksMap;
import org.palladiosimulator.blockchainsystems.core.system.BlockchainSystem;
import org.palladiosimulator.blockchainsystems.core.system.BlockchainSystemNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ThreesimSimulationMonitor implements SimulationMonitor {

    private final LongestChainExceededMaxLengthCondition _maxBlockchainLengthCondition;
    private final double _failureThroughputThreshold;
    private final ThreesimSimulationParameters _simulationParameters;

    private Set<BlockchainSystemNode> _nodes;
    private GeographicalRegions _geographicalRegions;
    private int _numberOfSubmittedTransactions = 0;
    private Double _blockReward;

    private boolean _finneyAttackSucceeded = false;
    private Long _finneyCandidateMinedTime = null;
    private String _finneyCandidateBlockHash = null;
    private boolean _raceAttackSucceeded = false;
    private boolean _selfishMiningAttackSucceeded = false;

    private final Map<String, Set<String>> _confirmedByPrevHash = new HashMap<>();
    private final Map<String, Set<String>> _staleByPrevHash = new HashMap<>();
    private final Map<String, Set<String>> _confirmedChildrenByParentHash = new HashMap<>();

    private String _raceWinningPrevHash = null;
    private String _raceWinningAttackerBlockHash = null;
    private Long _raceProvisionalSuccessTime = null;

    private CounterMap<String> _blocksProposedPerNode;
    private BlocksMap _includedBlocks;
    private BlocksMap _confirmedBlocks;
    private BlocksMap _staleBlocks;
    private BlocksMap _forkedBlocks;

    // Item 7: actual packed size (bytes) and transaction count of every block mined this round,
    // recorded regardless of whether the block ends up confirmed/stale/forked -- these describe
    // the packing process itself (item 7's stopping-rule fix), not chain-selection outcomes, so
    // they're collected on every BlockMinedTraceEvent rather than filtered to _confirmedBlocks.
    private final List<Integer> _minedBlockActualSizes = new ArrayList<>();
    private final List<Integer> _minedBlockTransactionCounts = new ArrayList<>();

    // Counts Lead-stubborn's defining PRIVATE_LEAD -> TIED_CONTEST phase transitions this round
    // (see LeadStubbornLostLeadTransitionCount's doc for the granularity caveat and why this is
    // the correct transition, not the AttackForkState.reset()-triggered SYNCED transition).
    // Stays 0 for every other attack type -- see the AttackPhaseTransitionTraceEvent branch below.
    private int _leadStubbornLostLeadTransitions = 0;

    // Colleague review item 6: propagation time = mining-to-network-coverage per block (not
    // per-message -- see PropagationTimeMean's doc and the design note above getForkDurations()
    // for why). Sourced entirely from the EXISTING BlockAppendedTraceEvent stream this class
    // already consumes below (every node's first append of a given block hash, in ANY of the
    // three classifications a first append can land in -- Included/Forking/Stale -- since full
    // network AWARENESS of the raw block, not its eventual chain-selection outcome, is what's
    // being measured): no new trace event or network-layer threading needed.
    //
    // Follow-up investigation (topology/forwarding/timing correctness review) confirmed: on a
    // high-diameter, low-degree topology (e.g. validator_count=1000/node_degree=2, a ring with
    // diameter 500) requiring literally 100% of nodes to have received a block before recording
    // anything structurally never completes within a round's time budget -- not a forwarding or
    // connectivity defect (both independently confirmed correct), just an artifact of the
    // all-or-nothing 100% completion criterion. Per the colleague's own stated condition ("only
    // if the protocol intentionally allows losing branches never to reach all nodes should we
    // use alternatives such as time to 90%/95% coverage" -- confirmed satisfied), three coverage
    // thresholds are now tracked in parallel: 90%, 95%, 100% (100% kept alongside, not replaced,
    // as its own separate metric trio below -- unchanged in definition/values from before).
    //
    // _blockAppearedAtNodes: block hash -> set of node ids that have appeared (appended) so far.
    // _blockThresholdFinalized: block hash -> boolean[3], whether each of the 90%/95%/100%-of-
    // nodes thresholds has already had its sample recorded (so a later call doesn't double-count
    // it). Events are processed in strictly non-decreasing timestamp order (this class's own
    // existing invariant -- see _lastEventTimestamp above: single-threaded within a round,
    // events dispatched in non-decreasing time order), so the timestamp of the call that first
    // brings a block's seen-node count up to a given threshold count IS that threshold's
    // completion time -- no separate running-max needed (the previous single-threshold version's
    // Math.max was therefore always redundant, not just simplified away here).
    // Each threshold is finalized into its OWN _propagationTimesPxx list independently, the
    // instant it is individually reached -- not batched until 100% is reached -- so a block that
    // reaches 90%/95% coverage but never 100% before the round ends still correctly contributes
    // to the 90%/95% lists (only its 100% sample is missing). A block's bookkeeping is discarded
    // (to bound memory) only once ALL thresholds, including 100%, have been reached. A block that
    // never reaches even the lowest (90%) threshold before the round ends is simply left in these
    // maps and never contributes a sample to any of the three lists -- right-censored/excluded,
    // not zero-filled (matches conditional_reorg_depth's established null-when-undefined
    // convention elsewhere).
    private static final double[] COVERAGE_THRESHOLDS = {0.90, 0.95, 1.00};
    private final Map<String, Set<String>> _blockAppearedAtNodes = new HashMap<>();
    private final Map<String, boolean[]> _blockThresholdFinalized = new HashMap<>();
    private final List<Long> _propagationTimesP90Coverage = new ArrayList<>();
    private final List<Long> _propagationTimesP95Coverage = new ArrayList<>();
    private final List<Long> _propagationTimesP100Coverage = new ArrayList<>();

    // block hash -> true network-broadcast timestamp (BlockBroadcastTraceEvent.getOccurrenceTime()),
    // recorded only for blocks that went through one of the four attack behavior classes'
    // publishOneHiddenBlock() -- i.e. blocks that were privately withheld before being revealed.
    // Absent for every other block (honest-mined, or broadcast via a path that doesn't raise this
    // event -- see recordBlockPropagation's fallback to getBlockMinedTimestamp() below), by
    // design: those blocks never accumulated a mined-to-broadcast gap in the first place.
    private final Map<String, Long> _blockBroadcastTimestamps = new HashMap<>();

    private final BlockchainSystemFailureLog _failureLog = new BlockchainSystemFailureLog();
    private final BlockRewardMonitor _blockRewardMonitor = new BlockRewardMonitor();

    private final List<Double> _throughputsDuringFailure = new ArrayList<>();
    private final List<Long> _confirmationLatenciesDuringFailure = new ArrayList<>();
    private final List<Double> _throughputsWithoutFailure = new ArrayList<>();
    private final List<Long> _confirmationLatenciesWithoutFailure = new ArrayList<>();

    private long _lastThroughputCheckTimestamp = 0;
    private Long _attackSuccessTime = null;

    private Integer _lastTermDebugConfirmedBlocks = null;
    private Boolean _lastTermDebugRaceSucceeded = null;
    private Boolean _lastTermDebugReachedDepth = null;

    private final Set<String> _throughputAccountedHashes = new HashSet<>();
    private final Set<String> _rewardAccountedHashes = new HashSet<>();

    private final List<ChainReorganizationOccurrence> _chainReorganizations = new ArrayList<>();

    // Quiescence-wait termination state. _lastEventTimestamp tracks the occurrence time of the
    // most recently processed trace event of any kind -- the closest available proxy for
    // "current simulated time" from here, since this class has no direct SystemClock access and
    // (per EventCoordinatorImpl.processEvents) events are always dispatched strictly in
    // non-decreasing timestamp order, single-threaded within one round, so this is safe to use.
    // _lastReorgTimestamp is the occurrence time of the most recent ChainReorganizedTraceEvent;
    // both start at 0 but _lastReorgTimestamp is only ever read once _chainReorganizations is
    // non-empty, at which point it has always already been set by that same first reorg.
    private long _lastEventTimestamp = 0L;
    private long _lastReorgTimestamp = 0L;

    // D5 episode-resolution state: true once shouldTerminate() has determined the round reached
    // a genuine converged stopping point on its own terms (the reorg quiescence-wait window, or
    // -- for AttackType.RACE -- its own confirmation-depth criterion), as opposed to being cut
    // off by the H_max cap while still contested. See getEpisodeStatus(). Never set back to
    // false; the round loop stops at the first shouldTerminate()==true, so this is set at most
    // once, right where the existing termination condition it records already fires.
    private boolean _quiescenceReached = false;

    // _replicationId: this round's index, threaded through from ThreesimSimulationRound purely
    // so the diagnostic dump (see handleDiagnosticPropagationEvent below) can gate on "is this
    // one of the first few rounds", per DiagnosticPropagationDumpWriter's round-based sampling
    // window -- not used anywhere in decision logic, event handling, or timestamp computation.
    private final int _replicationId;
    
    private boolean _nodesWereUnavailable = false;
    private long _allNodesOperationalSince = -1L;
    
    private CrashPlan _crashPlan;
    

    public ThreesimSimulationMonitor(
            LongestChainExceededMaxLengthCondition maxBlockchainLengthCondition,
            double failureThroughputThreshold,
            ThreesimSimulationParameters simulationParameters,
            int replicationId) {
        _maxBlockchainLengthCondition = maxBlockchainLengthCondition;
        _failureThroughputThreshold = failureThroughputThreshold;
        _simulationParameters = simulationParameters;
        _replicationId = replicationId;
    }

    @Override
    public void initialize(BlockchainSystem blockchainSystem) {
        _nodes = blockchainSystem.getNodes();
        _geographicalRegions = blockchainSystem.getGeographicalRegions();
        _blockReward = blockchainSystem.getBlockReward();

        _blocksProposedPerNode = CounterMap.create(
                _nodes.stream().map(BlockchainSystemNode::getId).collect(Collectors.toList()));

        int majorityThreshold = (_nodes.size() / 2) + 1;
        _includedBlocks = new BlocksMap(majorityThreshold);
        _confirmedBlocks = new BlocksMap(majorityThreshold);
        _staleBlocks = new BlocksMap(majorityThreshold);
        _forkedBlocks = new BlocksMap(majorityThreshold);
        
        _crashPlan = blockchainSystem.getCrashPlan();
    }

    public ThreesimSimulationMonitorState getFinalState(long finalSystemTime) {
        return new ThreesimSimulationMonitorState(
                _nodes.size(),
                _nodes.stream().map(BlockchainSystemNode::getResourcePower).collect(Collectors.toList()),
                _blocksProposedPerNode.getValues(),
                _geographicalRegions.getNumberOfRegions(),
                calculateNumberOfNodesPerRegion(),
                _numberOfSubmittedTransactions,
                calculateNumberOfConfirmedTransactions(),
                calculateTokensHeldPerNode(),
                calculateBlockProposalAndConfirmationTimes(),
                calculateMeanTimeBetweenFailures(finalSystemTime),
                _failureLog.calculateMeanFailureDuration(),
                _staleBlocks.getNumberOfValidBlocks(),
                _confirmedBlocks.getNumberOfValidBlocks(),
                calculateAverageThroughputDuringFailure(),
                calculateAverageThroughputWithoutFailure(),
                calculateAverageConfirmationLatencyDuringFailure(),
                calculateAverageConfirmationLatencyWithoutFailure()
        );
    }

    private List<long[]> calculateBlockProposalAndConfirmationTimes() {
        return _confirmedBlocks.getValidBlocks().stream()
                .map(e -> new long[]{e.getKey().getBlockMinedTimestamp(), e.getValue()})
                .collect(Collectors.toList());
    }

    @Override
    public void onTraceEventOccurred(TraceEvent event, TraceEventLogOrigin logOrigin) {
        // Diagnostic-only dump (off by default -- DiagnosticPropagationDumpWriter.ENABLED
        // reads -Dthreesim.diagnosticPropagationDump=true once at class-init). Intercepted and
        // returned before _lastEventTimestamp/any other state below is touched, so this branch
        // has zero effect on quiescence timing or any other already-verified logic in this
        // class, whether or not the flag is enabled. These two event types are only ever
        // raised by BlockValidatorImpl when the same flag is enabled (see that class); when
        // disabled, onTraceEventOccurred never even receives them, so this check is a cheap
        // no-op string comparison in the normal/production path either way.
        if (DiagnosticPropagationDumpWriter.ENABLED
                && (BlockValidationStartedTraceEvent.EVENT_TYPE.equals(event.getEventType())
                        || BlockValidationFinishedTraceEvent.EVENT_TYPE.equals(event.getEventType()))) {
            handleDiagnosticPropagationEvent(event, logOrigin);
            return;
        }

        _lastEventTimestamp = event.getOccurrenceTime();

        if (BlockMinedTraceEvent.EVENT_TYPE.equals(event.getEventType())) {
            BlockMinedTraceEvent e = (BlockMinedTraceEvent) event;
            Block block = e.getBlock();
            if (_finneyCandidateBlockHash == null && isAttacker(block.getOriginId())) {
                _finneyCandidateBlockHash = block.getHash();
                _finneyCandidateMinedTime = e.getOccurrenceTime();
            }
            if (BlockUtils.isBlockForked(block)) {
                _forkedBlocks.addNodeToBlock(block, logOrigin.getId(), e.getOccurrenceTime());
            }
            _blocksProposedPerNode.increment(logOrigin.getId());
            _minedBlockActualSizes.add(block.getSize());
            _minedBlockTransactionCounts.add(block.getTransactions().size());

        } else if (BlockAppendedTraceEvent.EVENT_TYPE.equals(event.getEventType())) {
            BlockAppendedTraceEvent e = (BlockAppendedTraceEvent) event;
            addBlock(e.getAppendedBlockType(), e.getAppendedBlock(), logOrigin.getId(), e.getOccurrenceTime());
            recordBlockPropagation(e.getAppendedBlock(), logOrigin.getId(), e.getOccurrenceTime());
            if (e.getAppendedBlockType() == BlockType.ConfirmedBlock || e.getAppendedBlockType() == BlockType.StaleBlock) {
                updateRaceOutcomeIfRelevant(e.getAppendedBlock(), e.getAppendedBlockType(), e.getOccurrenceTime());
            }
            _maxBlockchainLengthCondition.onBlockAppended(e.getBlockPosition());
            if (e.getAppendedBlockType() == BlockType.ConfirmedBlock && _throughputAccountedHashes.add(e.getAppendedBlock().getHash())) {
                monitorThroughput(e.getAppendedBlock(), e.getOccurrenceTime());
            }
            if (e.getAppendedBlockType() == BlockType.ConfirmedBlock && _rewardAccountedHashes.add(e.getAppendedBlock().getHash())) {
                _blockRewardMonitor.recordBlockReward(e.getAppendedBlock());
            }
            if (e.getAppendedBlockType() == BlockType.ConfirmedBlock
                    && _simulationParameters.getAttackType() == AttackType.FINNEY
                    && e.getAppendedBlock().getHash().equals(_finneyCandidateBlockHash)
                    && _finneyCandidateMinedTime != null
                    && e.getOccurrenceTime() > _finneyCandidateMinedTime) {
                _finneyAttackSucceeded = true;
            }

        } else if (BlockTypeChangedTraceEvent.EVENT_TYPE.equals(event.getEventType())) {
            BlockTypeChangedTraceEvent e = (BlockTypeChangedTraceEvent) event;
            removeBlock(e.getOldBlockType(), e.getBlock().getHash(), logOrigin.getId());
            addBlock(e.getNewBlockType(), e.getBlock(), logOrigin.getId(), e.getOccurrenceTime());
            if (e.getNewBlockType() == BlockType.ConfirmedBlock || e.getNewBlockType() == BlockType.StaleBlock) {
                updateRaceOutcomeIfRelevant(e.getBlock(), e.getNewBlockType(), e.getOccurrenceTime());
            }
            if (e.getNewBlockType() == BlockType.ConfirmedBlock && _throughputAccountedHashes.add(e.getBlock().getHash())) {
                monitorThroughput(e.getBlock(), e.getOccurrenceTime());
            }
            if (e.getNewBlockType() == BlockType.ConfirmedBlock && _rewardAccountedHashes.add(e.getBlock().getHash())) {
                _blockRewardMonitor.recordBlockReward(e.getBlock());
            }
            if (e.getNewBlockType() == BlockType.ConfirmedBlock
                    && _simulationParameters.getAttackType() == AttackType.FINNEY
                    && e.getBlock().getHash().equals(_finneyCandidateBlockHash)
                    && _finneyCandidateMinedTime != null
                    && e.getOccurrenceTime() > _finneyCandidateMinedTime) {
                _finneyAttackSucceeded = true;
            }

        } else if (TransactionSubmittedTraceEvent.EVENT_TYPE.equals(event.getEventType())) {
            _numberOfSubmittedTransactions++;

        } else if (ChainReorganizedTraceEvent.EVENT_TYPE.equals(event.getEventType())) {
            ChainReorganizedTraceEvent reorgEvent = (ChainReorganizedTraceEvent) event;
            
            boolean duringRecovery = _nodes.stream()
                    .filter(node -> node.getId().equals(logOrigin.getId()))
                    .findFirst()
                    .map(BlockchainSystemNode::isRecovering)
                    .orElse(false);
            
            _chainReorganizations.add(new ChainReorganizationOccurrence(
            		logOrigin.getId(), 
            		reorgEvent, 
            		isAttackerCaused(reorgEvent), 
            		duringRecovery));
            
            if(!duringRecovery) _lastReorgTimestamp = reorgEvent.getOccurrenceTime();

        } else if (AttackPhaseTransitionTraceEvent.EVENT_TYPE.equals(event.getEventType())) {
            AttackPhaseTransitionTraceEvent e = (AttackPhaseTransitionTraceEvent) event;
            if (_simulationParameters.getAttackType() == AttackType.LEAD_STUBBORN_MINING
                    && isAttacker(logOrigin.getId())
                    && e.getOldPhase() == AttackPhase.PRIVATE_LEAD
                    && e.getNewPhase() == AttackPhase.TIED_CONTEST) {
                _leadStubbornLostLeadTransitions++;
            }

        } else if (BlockBroadcastTraceEvent.EVENT_TYPE.equals(event.getEventType())) {
            // Not diagnostic-only (unlike BlockValidationStarted/FinishedTraceEvent above): this
            // event is now raised unconditionally by the four attack behavior classes'
            // publishOneHiddenBlock(), and recordBlockPropagation() below depends on it for the
            // production P90/95/100-coverage metrics. putIfAbsent, not put: a block hash is only
            // ever broadcast once (each of the four classes calls this exactly once per hidden
            // block, from its own single owning attacker node), so the first (and only expected)
            // write wins; putIfAbsent is defensive, not a designed-for overwrite-prevention path.
            BlockBroadcastTraceEvent e = (BlockBroadcastTraceEvent) event;
            String hash = e.getBlock().getHash();
            _blockBroadcastTimestamps.putIfAbsent(hash, e.getOccurrenceTime());
            if (DiagnosticPropagationDumpWriter.ENABLED && DiagnosticPropagationDumpWriter.shouldTrack(_replicationId)) {
                DiagnosticPropagationDumpWriter.recordBroadcast(hash, e.getOccurrenceTime());
            }
        }
    }

    @Override
    public boolean shouldTerminate() {
        // Quiescence-wait termination: a reorg no longer ends the round on its own -- it must
        // first go quiet (no further reorg, from any node) for 0.5 x block_creation_interval of
        // simulated time before the current state is accepted as converged. Any new reorg resets
        // the wait by advancing _lastReorgTimestamp, so this checks the gap since the MOST
        // RECENT one, not the first. Falls through to the existing maxAllowedBlockchainLength
        // check below as a backstop if quiescence is never reached.
    	
    	boolean allNodesOperational = _nodes.stream()
    	        .noneMatch(node -> node.isRecovering() || node.isCrashed());
    	
    	if (!allNodesOperational) {
            _nodesWereUnavailable = true;
            _allNodesOperationalSince = -1L;
        } else if (_nodesWereUnavailable && _allNodesOperationalSince < 0) {
            _allNodesOperationalSince = _lastEventTimestamp;
        }
    	
    	// No termination when a node is crashed or still recovering
    	if (!allNodesOperational) {
            return false;
        }
    
    	// No termination when there are crash events
    	if (_crashPlan != null && _lastEventTimestamp < _crashPlan.getCrashTime()) {
    	    return false;
    	}

    	
        if (!getNonRecoveryChainReorganizations().isEmpty()) {
            long quiescenceWindow = (long) (0.5 * _simulationParameters.getBlockInterval());
            long observationStart = _lastReorgTimestamp;

            if (_allNodesOperationalSince >= 0) {
                observationStart = Math.max(
                        observationStart,
                        _allNodesOperationalSince
                );
            }
            
            if (_lastEventTimestamp - observationStart >= quiescenceWindow) {
                _quiescenceReached = true;
                return true;
            }

            
        }

        boolean maxExceeded = _maxBlockchainLengthCondition.hasLengthExceeded();

        if (_simulationParameters.getAttackType() == AttackType.RACE) {
            int confirmedBlocks = _confirmedBlocks.getNumberOfValidBlocks();
            boolean reachedDepth = confirmedBlocks >= _simulationParameters.getConfirmationDepth();

            if (!Integer.valueOf(confirmedBlocks).equals(_lastTermDebugConfirmedBlocks)
                    || !Boolean.valueOf(_raceAttackSucceeded).equals(_lastTermDebugRaceSucceeded)
                    || !Boolean.valueOf(reachedDepth).equals(_lastTermDebugReachedDepth)) {
                _lastTermDebugConfirmedBlocks = confirmedBlocks;
                _lastTermDebugRaceSucceeded = _raceAttackSucceeded;
                _lastTermDebugReachedDepth = reachedDepth;
            }

            if (reachedDepth) {
                _quiescenceReached = true;
                return true;
            }
        }

        if (maxExceeded && !getNonRecoveryChainReorganizations().isEmpty()) {
            // Cap reached before quiescence was ever achieved (reorgs kept resetting the wait):
            // treat identically to "no reorg ever occurred" (D_r=0, loss) per the simplified
            // termination rule, rather than reporting a still-contested, possibly-transient
            // result as final.
        	// Only non recovery reorgs are removed
        	_chainReorganizations.removeIf(occurrence -> !occurrence.isDuringRecovery());
        }

        return maxExceeded;
    }
 

    // Colleague review item 6 (extended per the topology/forwarding/timing investigation):
    // records this node's first appearance of this block (any classification), and records a
    // completion sample the instant EACH of the 90%/95%/100%-of-nodes coverage thresholds is
    // first individually reached -- see the field doc above for the full design rationale and
    // why no running-max is needed (the current call's `time` IS the threshold-crossing time, by
    // the non-decreasing-timestamp-order invariant). Each threshold is finalized independently
    // and immediately upon being crossed (not batched until 100% is reached), so a block that
    // reaches 90%/95% coverage but never 100% before the round ends still correctly contributes
    // to the 90%/95% lists -- only the 100% sample (if any) is missing for that block.
    //
    // Broadcast-moment fix (follow-up to the colleague review): the delay's start point is now
    // _blockBroadcastTimestamps.get(hash) when available -- the true network-broadcast moment,
    // via BlockBroadcastTraceEvent -- falling back to getBlockMinedTimestamp() otherwise. The
    // fallback is not a special case for edge conditions: it is the ONLY available timestamp for
    // every block that never went through one of the four attack behavior classes'
    // publishOneHiddenBlock() (every honest-mined block, every block from an attack strategy or
    // path that doesn't raise BlockBroadcastTraceEvent, e.g. Race/Finney/EqualFork's own
    // adopt-and-reforward path -- see BlockBroadcastTraceEvent's own doc for which sites raise
    // it and why the four adoptPublicBlockAndAbandonPrivateState() re-forward sites deliberately
    // do not). For those blocks mined-time and broadcast-time already coincide by construction
    // (immediate, unconditional distribute() at mining time), so the fallback changes nothing
    // for them -- only privately-withheld attacker blocks, where mined-time and broadcast-time
    // can differ by an arbitrary amount, are affected by this fix.
    private void recordBlockPropagation(Block block, String nodeId, long time) {
        String hash = block.getHash();
        Set<String> seenAt = _blockAppearedAtNodes.computeIfAbsent(hash, k -> new HashSet<>());
        if (!seenAt.add(nodeId)) {
            return;
        }
        boolean[] finalized = _blockThresholdFinalized.computeIfAbsent(hash, k -> new boolean[COVERAGE_THRESHOLDS.length]);
        int seenCount = seenAt.size();
        int totalNodes = _nodes.size();
        Long broadcastAt = _blockBroadcastTimestamps.get(hash);
        long baseTime = broadcastAt != null ? broadcastAt : block.getBlockMinedTimestamp();

        if (!finalized[0] && seenCount >= (int) Math.ceil(COVERAGE_THRESHOLDS[0] * totalNodes)) {
            finalized[0] = true;
            _propagationTimesP90Coverage.add(time - baseTime);
        }
        if (!finalized[1] && seenCount >= (int) Math.ceil(COVERAGE_THRESHOLDS[1] * totalNodes)) {
            finalized[1] = true;
            _propagationTimesP95Coverage.add(time - baseTime);
        }
        if (!finalized[2] && seenCount >= (int) Math.ceil(COVERAGE_THRESHOLDS[2] * totalNodes)) {
            finalized[2] = true;
            _propagationTimesP100Coverage.add(time - baseTime);
        }

        if (finalized[2]) {
            _blockAppearedAtNodes.remove(hash);
            _blockThresholdFinalized.remove(hash);
        }
    }

    // Diagnostic-only (see onTraceEventOccurred's early-return guard above). Both event types
    // key off the block's own getBlockMinedTimestamp() -- the same field the existing
    // (production) recordBlockPropagation() already treats as the propagation-time start point
    // (see that method below), so this dump uses no new/different notion of "publish time".
    // BlockValidationStartedTraceEvent buffers this node's reception timestamp;
    // BlockValidationFinishedTraceEvent completes and writes the one row for this (block, node)
    // -- see DiagnosticPropagationDumpWriter for why exactly one combined row per node, not two
    // partial ones, and why the started-before-finished ordering it relies on is guaranteed.
    private void handleDiagnosticPropagationEvent(TraceEvent event, TraceEventLogOrigin logOrigin) {
        if (event instanceof BlockValidationStartedTraceEvent started) {
            if (!DiagnosticPropagationDumpWriter.shouldTrack(_replicationId)) {
                return;
            }
            String blockHash = started.getBlock().getHash();
            DiagnosticPropagationDumpWriter.recordReception(blockHash, logOrigin.getId(), started.getOccurrenceTime());
        } else if (event instanceof BlockValidationFinishedTraceEvent finished) {
            if (!DiagnosticPropagationDumpWriter.shouldTrack(_replicationId)) {
                return;
            }
            String blockHash = finished.getBlock().getHash();
            DiagnosticPropagationDumpWriter.recordValidationComplete(
                    blockHash, finished.getBlock().getBlockMinedTimestamp(), logOrigin.getId(), finished.getOccurrenceTime());
        }
    }

    private void addBlock(BlockType type, Block block, String nodeId, long time) {
        getMap(type).addNodeToBlock(block, nodeId, time);
    }

    private void removeBlock(BlockType type, String hash, String nodeId) {
        getMap(type).removeNodeFromBlock(hash, nodeId);
    }

    private BlocksMap getMap(BlockType type) {
        return switch (type) {
            case IncludedBlock -> _includedBlocks;
            case ConfirmedBlock -> _confirmedBlocks;
            case StaleBlock -> _staleBlocks;
            case ForkingBlock -> _forkedBlocks;
        };
    }

    private void monitorThroughput(Block confirmedBlock, long occurrenceTime) {
        if (_lastThroughputCheckTimestamp == 0L) { _lastThroughputCheckTimestamp = occurrenceTime; return; }
        long observationTime = occurrenceTime - _lastThroughputCheckTimestamp;
        double throughput = new TransactionThroughputCalculator(confirmedBlock.getTransactions().size(), observationTime)
                .calculate().getValue();
        long confirmationLatency = occurrenceTime - confirmedBlock.getBlockMinedTimestamp();

        if (_failureLog.isFailureOngoing()) {
            if (throughput > _failureThroughputThreshold) _failureLog.failureEnded(occurrenceTime);
        } else {
            if (throughput <= _failureThroughputThreshold) _failureLog.failureStarted(occurrenceTime);
        }
        if (_failureLog.isFailureOngoing()) {
            _throughputsDuringFailure.add(throughput);
            _confirmationLatenciesDuringFailure.add(confirmationLatency);
        } else {
            _throughputsWithoutFailure.add(throughput);
            _confirmationLatenciesWithoutFailure.add(confirmationLatency);
        }
        _lastThroughputCheckTimestamp = occurrenceTime;
    }

    private static final Set<AttackType> FORK_MERGE_ATTACK_TYPES = Set.of(
            AttackType.RACE,
            AttackType.SELFISH_MINING,
            AttackType.LEAD_STUBBORN_MINING,
            AttackType.EQUAL_FORK_STUBBORN_MINING,
            AttackType.TRAIL_STUBBORN_MINING,
            AttackType.COMBINED_SELFISH_RACE,
            AttackType.COMBINED_SELFISH_FINNEY,
            AttackType.COMBINED_SELFISH_LEAD_STUBBORN,
            AttackType.COMBINED_SELFISH_TRAIL_STUBBORN);

    private void updateRaceOutcomeIfRelevant(Block block, BlockType newType, long occurrenceTime) {
        AttackType attackType = _simulationParameters.getAttackType();
        boolean alreadySucceeded = attackType == AttackType.RACE ? _raceAttackSucceeded : _selfishMiningAttackSucceeded;
        if (alreadySucceeded || !FORK_MERGE_ATTACK_TYPES.contains(attackType)) return;
        String hash = block.getHash(), prev = block.getPreviousHash();
        boolean attacker = isAttacker(block.getOriginId());

        if (newType == BlockType.ConfirmedBlock) {
            if (prev != null) addTo(_confirmedChildrenByParentHash, prev, hash);
            if (attacker && prev != null) addTo(_confirmedByPrevHash, prev, hash);
        } else if (newType == BlockType.StaleBlock) {
            if (!attacker && prev != null) addTo(_staleByPrevHash, prev, hash);
        }

        if (_raceWinningAttackerBlockHash == null && prev != null) {
            Set<String> ac = _confirmedByPrevHash.getOrDefault(prev, Set.of());
            Set<String> hs = _staleByPrevHash.getOrDefault(prev, Set.of());
            String winner = ac.stream().filter(ah -> hs.stream().anyMatch(hh -> !hh.equals(ah))).findFirst().orElse(null);
            if (winner != null) {
                _raceWinningPrevHash = prev;
                _raceWinningAttackerBlockHash = winner;
                _raceProvisionalSuccessTime = occurrenceTime;
            }
        }

        String winningHash = _raceWinningAttackerBlockHash;
        if (winningHash == null) return;
        if (countConfirmedDepthFrom(winningHash) >= _simulationParameters.getConfirmationDepth()) {
            if (attackType == AttackType.RACE) {
                _raceAttackSucceeded = true;
            } else {
                _selfishMiningAttackSucceeded = true;
            }
            if (_attackSuccessTime == null) _attackSuccessTime = _raceProvisionalSuccessTime != null ? _raceProvisionalSuccessTime : occurrenceTime;
        }
    }

    private int countConfirmedDepthFrom(String rootHash) {
        int depth = 1;
        String current = rootHash;
        while (true) {
            Set<String> children = _confirmedChildrenByParentHash.getOrDefault(current, Set.of());
            if (children.isEmpty()) break;
            current = children.iterator().next();
            depth++;
        }
        return depth;
    }

    private boolean isAttacker(String originId) {
        return originId != null && _simulationParameters.getAttackerNodeIds().contains(originId);
    }
    
    private List<ChainReorganizationOccurrence> getNonRecoveryChainReorganizations() {
    	return _chainReorganizations
    			.stream()
    			.filter(occurrance -> !occurrance.isDuringRecovery())
    			.toList();
    }

    // Branch-lineage attribution: a reorg counts as attacker-caused when the winning branch's
    // earliest block past the common ancestor (i.e. where it first diverges from the shared
    // history) is attacker-mined -- regardless of who mined the later, decisive block that
    // actually pushed the chain past its previous length. This correctly credits the attacker
    // for a tie it forced even when gamma-recruited honest hashpower finishes extending it, and
    // correctly excludes an honest-vs-honest natural fork that the attacker merely happened to
    // finish (see GammaAwareHonestBlockchainSystemNodeBehavior / SelfishMiningNodeBehavior's
    // honest-fallback path for the two respective mechanisms).
    private boolean isAttackerCaused(ChainReorganizedTraceEvent e) {
        List<ChainReorganizedTraceEvent.ChainBlock> chain = e.getReplacingChainBlocks();
        return !chain.isEmpty() && isAttacker(chain.get(0).getBlock().getOriginId());
    }

    // LinkedHashSet, not HashSet: dispatchEvent processes trace events strictly in
    // non-decreasing timestamp order, single-threaded within one round (see _lastEventTimestamp
    // above), so insertion order into these sets already IS validation/confirmation arrival
    // order -- callers that need to break a tie among multiple entries (countConfirmedDepthFrom,
    // updateRaceOutcomeIfRelevant) must retain the first-established one deterministically,
    // never an arbitrary HashSet iteration order.
    private void addTo(Map<String, Set<String>> map, String key, String value) {
        map.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(value);
    }

    private double calculateMeanTimeBetweenFailures(long observationTime) {
        int n = _failureLog.getNumberOfFailures();
        if (n <= 0) return Double.POSITIVE_INFINITY;
        return (double) observationTime / n;
    }

    private int calculateNumberOfConfirmedTransactions() {
        return _confirmedBlocks.getValidBlocks().stream()
                .mapToInt(e -> e.getKey().getTransactions().size()).sum();
    }

    private List<Integer> calculateNumberOfNodesPerRegion() {
        Map<String, Integer> counts = new HashMap<>();
        for (BlockchainSystemNode node : _nodes) {
            String region = node.getGeographicalRegion().getRegion();
            counts.merge(region, 1, Integer::sum);
        }
        return new ArrayList<>(counts.values());
    }

    private List<Double> calculateTokensHeldPerNode() {
        if (_blockReward == null) throw new IllegalStateException("Block reward is not set");
        Map<String, List<Map.Entry<Block, Long>>> byOrigin = _confirmedBlocks.getValidBlocks().stream()
                .filter(e -> e.getKey().getOriginId() != null)
                .collect(Collectors.groupingBy(e -> e.getKey().getOriginId()));
        return _nodes.stream().map(node -> {
            List<Map.Entry<Block, Long>> blocks = byOrigin.get(node.getId());
            if (blocks == null) return 0.0;
            return blocks.stream().mapToDouble(e -> _blockReward + e.getKey().getTransactions().stream().mapToDouble(t -> t.getFee()).sum()).sum();
        }).collect(Collectors.toList());
    }

    private double avg(List<? extends Number> list) {
        if (list.isEmpty()) return -1.0;
        return list.stream().mapToDouble(Number::doubleValue).average().orElse(-1.0);
    }

    // Colleague review item 6: linear-interpolation-between-closest-ranks percentile (matches
    // numpy's default 'linear' method, for consistency with this project's Python-side analysis
    // scripts), -1.0 for an empty list matching avg()'s existing "no data" convention.
    private double percentile(List<Long> list, double p) {
        if (list.isEmpty()) return -1.0;
        List<Long> sorted = new ArrayList<>(list);
        java.util.Collections.sort(sorted);
        int n = sorted.size();
        if (n == 1) return sorted.get(0);
        double rank = (p / 100.0) * (n - 1);
        int lo = (int) Math.floor(rank);
        int hi = (int) Math.ceil(rank);
        if (lo == hi) return sorted.get(lo);
        double frac = rank - lo;
        return sorted.get(lo) + frac * (sorted.get(hi) - sorted.get(lo));
    }

    private double median(List<Long> list) { return percentile(list, 50.0); }

    private double calculateAverageThroughputDuringFailure() { return avg(_throughputsDuringFailure); }
    private double calculateAverageConfirmationLatencyDuringFailure() { return avg(new ArrayList<>(_confirmationLatenciesDuringFailure)); }
    private double calculateAverageThroughputWithoutFailure() { return avg(_throughputsWithoutFailure); }
    private double calculateAverageConfirmationLatencyWithoutFailure() { return avg(new ArrayList<>(_confirmationLatenciesWithoutFailure)); }

    // Item 7: mean actual packed size / transaction count across every block mined this round
    // (-1.0 if none were mined, matching this class's existing "no data" convention -- see avg()).
    public double getMeanActualBlockSize() { return avg(_minedBlockActualSizes); }
    public double getMeanBlockTransactionCount() { return avg(_minedBlockTransactionCounts); }

    public int getLeadStubbornLostLeadTransitions() { return _leadStubbornLostLeadTransitions; }

    // Colleague review item 6: mean/median/p95 of this round's completed (full-network-coverage)
    // per-block propagation times -- see the field doc above for what's included/excluded.
    // Unchanged in definition/values from before this task: mean/median/95th-percentile ACROSS
    // blocks of the 100%-coverage completion time (a distributional summary of one threshold,
    // not to be confused with the new PxxCoverage accessors below, which are per-threshold MEANS
    // across blocks at three different coverage levels).
    public double getMeanPropagationTime() { return avg(_propagationTimesP100Coverage); }
    public double getMedianPropagationTime() { return median(_propagationTimesP100Coverage); }
    public double getP95PropagationTime() { return percentile(_propagationTimesP100Coverage, 95.0); }

    // New: mean, across blocks, of time-to-X%-node-coverage, for X=90/95/100 -- see the field
    // doc above recordBlockPropagation for full rationale. getMeanPropagationTimeP100Coverage()
    // is numerically identical to getMeanPropagationTime() above (both are the mean of the same
    // 100%-coverage completion times) -- kept as its own explicitly-named accessor so all three
    // thresholds are available side by side under one consistent naming scheme, not because the
    // two 100% quantities differ.
    public double getMeanPropagationTimeP90Coverage() { return avg(_propagationTimesP90Coverage); }
    public double getMeanPropagationTimeP95Coverage() { return avg(_propagationTimesP95Coverage); }
    public double getMeanPropagationTimeP100Coverage() { return avg(_propagationTimesP100Coverage); }

    // Colleague review item 6: one duration per DISTINCT resolved fork/reorg contest this round --
    // NOT one per entry in _chainReorganizations, which can contain multiple node-local
    // observations of the SAME logical contest (one ChainReorganizedTraceEvent per node whose own
    // local view reorganizes because of it -- see this class's existing doc on
    // chainReorganizationDepths/attackerCausedChainReorganizationDepths above
    // getDecisiveAttackerReorg). Distinct contests are identified by the new canonical tip's hash,
    // and (mirroring getDecisiveAttackerReorg's hashToEarliestOccurrence pattern) the EARLIEST
    // occurrence per distinct hash is used as authoritative.
    //
    // Duration = (that occurrence's own ChainReorganizedTraceEvent.getOccurrenceTime(), i.e. the
    // moment the replacing branch became canonical somewhere) - (the MINED time of the first
    // block of that event's replacing chain, i.e. when the competing branch first came into
    // existence -- getReplacingChainBlocks() is ordered oldest-to-newest per its own doc, so
    // index 0 is that first block). This chosen definition of "fork start" is the competing
    // block's OWN mined timestamp, not the time some node first observed/received it (which would
    // require new per-node-arrival tracking analogous to propagation time above, not something
    // this task adds) -- flagged explicitly since "first detected" is ambiguous between the two.
    //
    // NOTE on "quiescence": this is the resolution moment for THIS SPECIFIC contest, not the same
    // concept as this class's round-level quiescence-wait mechanism (_quiescenceReached /
    // _lastReorgTimestamp, see shouldTerminate()), which decides when the WHOLE ROUND has gone
    // quiet enough to terminate -- there is no per-contest quiescence timestamp maintained
    // anywhere in this codebase; a specific contest's own reorg event occurrence time is the
    // natural (and only available) per-contest resolution instant.
    public List<Long> getForkDurations() {
        Map<String, ChainReorganizationOccurrence> earliestByTipHash = new LinkedHashMap<>();
        for (ChainReorganizationOccurrence occurrence : getNonRecoveryChainReorganizations()) {
            String tipHash = occurrence.getEvent().getNewCanonicalTip().getHash();
            ChainReorganizationOccurrence existing = earliestByTipHash.get(tipHash);
            if (existing == null
                    || occurrence.getEvent().getOccurrenceTime() < existing.getEvent().getOccurrenceTime()) {
                earliestByTipHash.put(tipHash, occurrence);
            }
        }
        List<Long> durations = new ArrayList<>();
        for (ChainReorganizationOccurrence occurrence : earliestByTipHash.values()) {
            ChainReorganizedTraceEvent event = occurrence.getEvent();
            List<ChainReorganizedTraceEvent.ChainBlock> replacing = event.getReplacingChainBlocks();
            if (replacing.isEmpty()) continue;
            long forkStart = replacing.get(0).getBlock().getBlockMinedTimestamp();
            durations.add(event.getOccurrenceTime() - forkStart);
        }
        return durations;
    }

    public double getMeanForkDuration() { return avg(getForkDurations()); }
    public double getMedianForkDuration() { return median(getForkDurations()); }

    public void recordBlockReward(Block block) { _blockRewardMonitor.recordBlockReward(block); }
    public int getTotalBlockRewards() { return _blockRewardMonitor.getTotalRewards(); }
    public int getBlockRewardsForNode(String nodeId) { return _blockRewardMonitor.getRewardsForNode(nodeId); }
    public boolean hasFinneyAttackSucceeded() { return _finneyAttackSucceeded; }
    public boolean hasRaceAttackSucceeded() { return _raceAttackSucceeded; }
    public boolean hasSelfishMiningAttackSucceeded() { return _selfishMiningAttackSucceeded; }
    public Long getAttackSuccessTime() { return _attackSuccessTime; }
    public List<ChainReorganizationOccurrence> getChainReorganizations() { return _chainReorganizations; }

    // D5 episode-resolution classification (see EpisodeStatus). UNRESOLVED whenever the round
    // never reached a genuine converged stopping point (_quiescenceReached, set in
    // shouldTerminate()) and was instead cut off by the H_max cap. Otherwise SUCCESS requires a
    // *genuine fork resolution*: an attacker-mined block that was part of some recorded reorg's
    // replacing chain (i.e. it displaced a then-canonical public branch when it was appended --
    // see BlockchainImpl.appendIncludedBlock/logChainReorganized, which only fires when a
    // competing branch that had actually been mined on gets replaced, never on ordinary linear
    // growth) AND that same block (matched by hash, not merely "some attacker block") still sits
    // on the canonical chain once the round has converged. An attacker block that was simply
    // appended on top of the existing tip without ever winning a contested fork does not count --
    // that is FAILURE, not SUCCESS, however far up the chain it sits.
    public EpisodeStatus getEpisodeStatus() {
        if (!_quiescenceReached) return EpisodeStatus.UNRESOLVED;

        Set<String> displacingAttackerBlockHashes = new HashSet<>();
        for (ChainReorganizationOccurrence occurrence : getNonRecoveryChainReorganizations()) {
            for (ChainReorganizedTraceEvent.ChainBlock chainBlock : occurrence.getEvent().getReplacingChainBlocks()) {
                if (isAttacker(chainBlock.getBlock().getOriginId())) {
                    displacingAttackerBlockHashes.add(chainBlock.getBlock().getHash());
                }
            }
        }
        if (displacingAttackerBlockHashes.isEmpty()) return EpisodeStatus.FAILURE;

        // Any node's local view is a valid witness for "the" canonical chain at quiescence --
        // by design all nodes converge once the network has gone quiet -- but _nodes is picked
        // deterministically (insertion-ordered LinkedHashSet, see BlockchainSystem._nodes) rather
        // than via arbitrary iteration, consistent with this codebase's determinism conventions.
        BlockchainSystemNode representativeNode = _nodes.iterator().next();
        Block current = representativeNode.getBlockchain().getPreferredTipOfLongestChains();
        while (current != null) {
            if (displacingAttackerBlockHashes.contains(current.getHash())) {
                return EpisodeStatus.SUCCESS;
            }
            String previousHash = current.getPreviousHash();
            current = previousHash != null ? representativeNode.getBlockchain().getBlock(previousHash) : null;
        }
        return EpisodeStatus.FAILURE;
    }

    // Item: chainReorganizationDepths/attackerCausedChainReorganizationDepths redefinition.
    // Identifies WHICH recorded reorg occurrence produced the surviving attacker-origin block
    // that getEpisodeStatus() (untouched, called as-is below) already used to decide SUCCESS --
    // this method never independently re-decides success/failure, it only identifies which
    // occurrence once that verdict is already known. Necessarily mirrors getEpisodeStatus()'s own
    // hash-matching criterion and canonical-chain walk (that method discards which specific hash
    // matched, and cannot be modified to retain it -- out of scope for this task), so this is
    // intentionally coupled to getEpisodeStatus()'s definition of "attacker-caused displacement":
    // if that definition ever changes, this method needs the same change alongside it.
    //
    // When multiple recorded reorgs' replacing chains include the same surviving hash (a later
    // reorg's walk-back can re-traverse blocks an earlier reorg already introduced, if the later
    // reorg's common ancestor sits before the earlier reorg's new tip), the EARLIEST occurrence
    // that introduced the hash is reported as decisive, not the one closest to the tip -- that
    // earlier occurrence is the one that actually put the block on the chain; a later occurrence
    // re-listing the same hash is just re-confirming it was already there, not a new event. This
    // matches "the decisive win" reading of D5's success definition.
    public ChainReorganizationOccurrence getDecisiveAttackerReorg() {
        if (getEpisodeStatus() != EpisodeStatus.SUCCESS) return null;

        Map<String, ChainReorganizationOccurrence> hashToEarliestOccurrence = new LinkedHashMap<>();
        for (ChainReorganizationOccurrence occurrence : getNonRecoveryChainReorganizations()) {
            for (ChainReorganizedTraceEvent.ChainBlock chainBlock : occurrence.getEvent().getReplacingChainBlocks()) {
                if (isAttacker(chainBlock.getBlock().getOriginId())) {
                    hashToEarliestOccurrence.putIfAbsent(chainBlock.getBlock().getHash(), occurrence);
                }
            }
        }

        BlockchainSystemNode representativeNode = _nodes.iterator().next();
        Block current = representativeNode.getBlockchain().getPreferredTipOfLongestChains();
        ChainReorganizationOccurrence decisive = null;
        while (current != null) {
            ChainReorganizationOccurrence candidate = hashToEarliestOccurrence.get(current.getHash());
            if (candidate != null) {
                // Keep walking toward genesis: a later match overwrites an earlier (closer-to-tip)
                // one, so the final value is the occurrence closest to genesis among all that match
                // -- i.e. the earliest one, per the reasoning above.
                decisive = candidate;
            }
            String previousHash = current.getPreviousHash();
            current = previousHash != null ? representativeNode.getBlockchain().getBlock(previousHash) : null;
        }
        return decisive;
    }
}
