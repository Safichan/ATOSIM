package org.palladiosimulator.blockchainsystems.threesim.creation;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.palladiosimulator.blockchainsystems.bscm.blockchainsystem.BlockchainSystem;
import org.palladiosimulator.blockchainsystems.bscm.p2pnetwork.NetworkTopology;
import org.palladiosimulator.blockchainsystems.core.block.BlockFactoryImpl;
import org.palladiosimulator.blockchainsystems.core.block.abstractions.BlockFactory;
import org.palladiosimulator.blockchainsystems.core.blockchain.BlockchainFactoryImpl;
import org.palladiosimulator.blockchainsystems.core.geography.GeographicalRegionsResolver;
import org.palladiosimulator.blockchainsystems.core.orphanblockpool.OrphanBlockPoolFactoryImpl;
import org.palladiosimulator.blockchainsystems.core.propagation.block.BlockPropagationStrategyFactoryImpl;
import org.palladiosimulator.blockchainsystems.core.propagation.transaction.RaceAwareTransactionPropagationStrategy;
import org.palladiosimulator.blockchainsystems.core.propagation.transaction.TransactionPropagationStrategy;
import org.palladiosimulator.blockchainsystems.core.propagation.transaction.TransactionPropagationStrategyFactoryImpl;
import org.palladiosimulator.blockchainsystems.core.system.BlockchainSystemNodeFactory;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.NodeP2PNetworkInterface;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetwork;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetworkCreationResult;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetworkFactory;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.ResourcePowerCalculator;
import org.palladiosimulator.blockchainsystems.core.transaction.TrxMemPoolFactoryImpl;
import org.palladiosimulator.blockchainsystems.threesim.behavior.ThreesimBlockchainSystemNodeBehaviorFactory;
import org.palladiosimulator.blockchainsystems.threesim.behavior.ThreesimBlockchainSystemNodeTagProvider;
import org.palladiosimulator.blockchainsystems.threesim.behavior.ThreesimTransactionSelectionProcessFactory;
import org.palladiosimulator.blockchainsystems.threesim.creation.abstractions.NodeAllocationResolver;
import org.palladiosimulator.blockchainsystems.threesim.creation.geography.ThreesimGeographicalRegionsResolver;
import org.palladiosimulator.blockchainsystems.threesim.behavior.NoOpTransactionSubmissionProcess;
import org.palladiosimulator.blockchainsystems.threesim.simulation.AttackType;
import org.palladiosimulator.blockchainsystems.threesim.simulation.DeterministicSeeds;
import org.palladiosimulator.blockchainsystems.threesim.simulation.ThreesimSimulationParameters;
import org.palladiosimulator.blockchainsystems.threesim.simulation.TransactionGenerationMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public abstract class ThreesimBlockchainSystemFactory {

    protected final BlockchainSystem designBlockchainSystem;
    protected final NetworkTopology networkTopology;

    protected ThreesimBlockchainSystemFactory(BlockchainSystem designBlockchainSystem,
            NetworkTopology networkTopology) {
        this.designBlockchainSystem = designBlockchainSystem;
        this.networkTopology = networkTopology;
    }

    protected abstract P2PNetworkFactory createP2PNetworkFactory(ThreesimSimulationParameters params, long rootSeed);
    protected abstract NodeAllocationResolver getNodeAllocationResolver(P2PNetworkCreationResult networkCreationResult);
    protected abstract ResourcePowerCalculator getResourcePowerCalculator(P2PNetworkCreationResult networkCreationResult);

    /** Stable 0..N-1 index per runtime node id, if the concrete topology factory assigns one (see
     * ConnectedSubgraphNetworkBlockchainSystemFactory). Empty for topology types that don't. */
    protected Map<String, Integer> getNodeIdToIndexMapping(P2PNetworkCreationResult networkCreationResult) {
        return Collections.emptyMap();
    }

    /** Identifier for the realized topology, if the concrete factory assigns one. Null otherwise. */
    protected String getTopologyId(P2PNetworkCreationResult networkCreationResult) {
        return null;
    }
    
    // Build the blockchain system with all parameters
    public BlockchainSystemWithParameters createBlockchainSystem(ThreesimSimulationParameters params, long replicationId) {
        long rootSeed = DeterministicSeeds.topologySeed(params.getConfigId(), replicationId);

        P2PNetworkFactory networkFactory = createP2PNetworkFactory(params, rootSeed);
        P2PNetworkCreationResult networkCreationResult = networkFactory.createP2PNetwork();

        NodeAllocationResolver nodeAllocationResolver = getNodeAllocationResolver(networkCreationResult);

        Set<String> runtimeAttackerNodeIds = networkCreationResult.getCreatedNetwork().getNodes().stream()
                .map(NodeP2PNetworkInterface::getEndpointId)
                .filter(runtimeNodeId -> {
                    var alloc = nodeAllocationResolver.getNodeAllocation(runtimeNodeId);
                    var nodeSystem = alloc != null ? alloc.getNodeSystem() : null;
                    String nodeSystemId = nodeSystem != null ? EcoreUtil.getID(nodeSystem) : null;
                    return nodeSystemId != null && params.getAttackerNodeIds().contains(nodeSystemId);
                })
                .collect(Collectors.toSet());

        TopologyDeterminismInfo topologyDeterminismInfo =
                buildTopologyDeterminismInfo(rootSeed, replicationId, networkCreationResult, runtimeAttackerNodeIds);

        ThreesimSimulationParameters effectiveParameters = new ThreesimSimulationParameters(
                params.getConfigId(),
                params.getFailureThroughputThreshold(),
                params.getShannonEntropyK(),
                params.getNakamotoCoefficientThreshold(),
                params.getReliabilityObservationTimespan(),
                params.getAttackType(),
                params.isCombinedAttackEnabled(),
                params.getSecondaryAttackType(),
                runtimeAttackerNodeIds,
                params.getAttackerHashPower(),
                params.getGamma(),
                params.getDeltaA(),
                params.getDeltaB(),
                params.getConfirmationDepth(),
                params.getBlockInterval(),
                params.getNodeDegree(),
                params.getMaxBlockSize(),
                params.getNetworkBandwidth(),
                params.getTransactionGenerationMode(),
                params.isStaticValidationDelayEnabled()
        );

        ResourcePowerCalculator baseResourcePowerCalculator = getResourcePowerCalculator(networkCreationResult);
        ResourcePowerCalculator resourcePowerCalculator =
                (!effectiveParameters.getAttackerNodeIds().isEmpty() && effectiveParameters.getAttackerHashPower() > 0.0)
                        ? new AttackAwareResourcePowerCalculator(baseResourcePowerCalculator,
                                effectiveParameters.getAttackerNodeIds(), effectiveParameters.getAttackerHashPower())
                        : baseResourcePowerCalculator;

        ThreesimGeographicalRegionsResolver geographicalRegionsResolver =
                new ThreesimGeographicalRegionsResolver(
                        designBlockchainSystem.getGeographicalRegionsSpecification(), nodeAllocationResolver);

        BlockFactory blockFactory = new BlockFactoryImpl();

        BlockchainSystemNodeFactory nodeFactory = createBlockchainSystemNodeFactory(
                nodeAllocationResolver, resourcePowerCalculator, blockFactory,
                geographicalRegionsResolver, effectiveParameters, rootSeed);

        org.palladiosimulator.blockchainsystems.core.system.BlockchainSystem system =
                createBlockchainSystemInstance(networkCreationResult.getCreatedNetwork(), blockFactory,
                        nodeFactory, geographicalRegionsResolver,
                        designBlockchainSystem.getSpecification().getBlockReward(),
                        effectiveParameters.getTransactionGenerationMode(), rootSeed,
                        getNodeIdToIndexMapping(networkCreationResult));

        return new BlockchainSystemWithParameters(system, effectiveParameters, topologyDeterminismInfo);
    }

    // Builds the verification-facing determinism metadata (item 1a) -- attacker indices and
    // adjacency by index, both derived from the node-index map the concrete topology factory
    // assigns (see ConnectedSubgraphNetworkBlockchainSystemFactory / getNodeIdToIndexMapping);
    // empty/absent for topology types that don't assign one (e.g. explicit topologies). Also
    // computes item 2's realized degree statistics (mean/min/max) straight from that same
    // adjacency map -- see TopologyDeterminismInfo's doc comment for why this rides along here
    // instead of new plumbing of its own.
    private TopologyDeterminismInfo buildTopologyDeterminismInfo(
            long rootSeed, long replicationId, P2PNetworkCreationResult networkCreationResult,
            Set<String> runtimeAttackerNodeIds) {

        Map<String, Integer> nodeIdToIndex = getNodeIdToIndexMapping(networkCreationResult);
        String topologyId = getTopologyId(networkCreationResult);
        if (nodeIdToIndex.isEmpty()) {
            return new TopologyDeterminismInfo(rootSeed, replicationId, topologyId, List.of(), Map.of(), 0.0, 0, 0);
        }

        List<Integer> attackerIndices = runtimeAttackerNodeIds.stream()
                .map(nodeIdToIndex::get)
                .filter(java.util.Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        Map<Integer, List<Integer>> adjacency = new TreeMap<>();
        for (NodeP2PNetworkInterface node : networkCreationResult.getCreatedNetwork().getNodes()) {
            Integer index = nodeIdToIndex.get(node.getEndpointId());
            if (index == null) continue;
            List<Integer> neighborIndices = new ArrayList<>();
            for (var neighbor : node.getNeighbors()) {
                Integer neighborIndex = nodeIdToIndex.get(((NodeP2PNetworkInterface) neighbor).getEndpointId());
                if (neighborIndex != null) neighborIndices.add(neighborIndex);
            }
            Collections.sort(neighborIndices);
            adjacency.put(index, neighborIndices);
        }

        List<Integer> degrees = adjacency.values().stream().map(List::size).collect(Collectors.toList());
        double meanDegree = degrees.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        int minDegree = degrees.stream().mapToInt(Integer::intValue).min().orElse(0);
        int maxDegree = degrees.stream().mapToInt(Integer::intValue).max().orElse(0);
        // Sanity check on the measurement itself (not a business-logic gate on realized vs.
        // declared d -- see item 2 phase 1): these three are computed from the same list, so
        // disagreement can only mean a bug in this method, not a topology-generation issue.
        if (!degrees.isEmpty() && !(minDegree <= meanDegree && meanDegree <= maxDegree)) {
            throw new IllegalStateException(
                    "Degree statistics inconsistent: min=" + minDegree + " mean=" + meanDegree + " max=" + maxDegree);
        }

        return new TopologyDeterminismInfo(rootSeed, replicationId, topologyId, attackerIndices, adjacency,
                meanDegree, minDegree, maxDegree);
    }

    private org.palladiosimulator.blockchainsystems.core.system.BlockchainSystem createBlockchainSystemInstance(
            P2PNetwork network, BlockFactory blockFactory, BlockchainSystemNodeFactory nodeFactory,
            GeographicalRegionsResolver geographicalRegionsResolver, double blockReward,
            TransactionGenerationMode transactionGenerationMode, long rootSeed,
            Map<String, Integer> nodeIdToIndex) {

        String id = DeterministicSeeds.randomHexId(DeterministicSeeds.seededGenerator(rootSeed, "systemId"));
        String name = "BlockchainSystem_" + id.substring(0, 8);

        var genesisBlock = blockFactory.createGenesisBlock(
                DeterministicSeeds.randomHexId(DeterministicSeeds.seededGenerator(rootSeed, "genesisBlock")));

        // Deterministic node order: sort by the stable per-node index (item 1a) when the
        // topology assigns one, falling back to the runtime node id (itself deterministic for
        // topology types that don't assign an index, e.g. explicit topologies use the model's
        // own stable node ids). network.getNodes() itself has no guaranteed iteration order
        // (JGraphT/HashSet-backed), and that order previously leaked into
        // BlockchainSystem.onInitialize()'s node-initialization order, and from there into
        // event-dispatch order for any two events sharing a timestamp -- so nodes are collected
        // into a LinkedHashSet here, in this pre-sorted order, rather than an unordered HashSet.
        var nodes = network.getNodes().stream()
                .sorted(Comparator
                        .comparing((NodeP2PNetworkInterface ni) -> nodeIdToIndex.getOrDefault(ni.getEndpointId(), Integer.MAX_VALUE))
                        .thenComparing(NodeP2PNetworkInterface::getEndpointId))
                .map(ni -> nodeFactory.createBlockchainSystemNode(ni, genesisBlock))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        var txSubmissionProcess = (transactionGenerationMode == TransactionGenerationMode.DETERMINISTIC_FULL_BLOCK)
                ? new NoOpTransactionSubmissionProcess(id, name)
                : createMempoolTransactionSubmissionProcess(id, name, rootSeed);

        var geoRegions = geographicalRegionsResolver.resolveGeographicalRegions();

        return new org.palladiosimulator.blockchainsystems.core.system.BlockchainSystem(
                id, name, network, geoRegions, nodes, txSubmissionProcess, blockReward,
                DeterministicSeeds.seededGenerator(rootSeed, "recipientSelection"));
    }

    private org.palladiosimulator.blockchainsystems.threesim.behavior.ThreesimTransactionSubmissionProcess
            createMempoolTransactionSubmissionProcess(String id, String name, long rootSeed) {
        var trxPropSpec = designBlockchainSystem.getTransactionsSpecification().getTransactionPropertiesSpecification();
        double meanTrxInterval = designBlockchainSystem.getTransactionsSpecification().getMeanTransactionCreationInterval();

        return new org.palladiosimulator.blockchainsystems.threesim.behavior.ThreesimTransactionSubmissionProcess(
                id, name, meanTrxInterval,
                TransactionPropertiesValueProviderAdapter.create(trxPropSpec,
                        DeterministicSeeds.seededGenerator(rootSeed, "transactionProperties")),
                DeterministicSeeds.seededGenerator(rootSeed, "transactionSubmission"),
                DeterministicSeeds.seededGenerator(rootSeed, "transactionSubmissionIds"));
    }

    private BlockchainSystemNodeFactory createBlockchainSystemNodeFactory(
            NodeAllocationResolver nodeAllocationResolver,
            ResourcePowerCalculator resourcePowerCalculator,
            BlockFactory blockFactory,
            ThreesimGeographicalRegionsResolver geographicalRegionsResolver,
            ThreesimSimulationParameters effectiveParameters,
            long rootSeed) {

        var blockchainFactory = new BlockchainFactoryImpl(
                designBlockchainSystem.getSpecification().getNumOfRequiredSecurityConfirmations());

        var txPropStrategyFactory = new TransactionPropagationStrategyFactoryImpl(
                effectiveParameters.getAttackType() == AttackType.RACE
                        ? () -> new RaceAwareTransactionPropagationStrategy(
                                effectiveParameters.getAttackerNodeIds(),
                                effectiveParameters.getDeltaA(),
                                effectiveParameters.getDeltaB())
                        : TransactionPropagationStrategy::new);

        var trxPropSpec = designBlockchainSystem.getTransactionsSpecification().getTransactionPropertiesSpecification();

        return new BlockchainSystemNodeFactory(
                blockFactory,
                blockchainFactory,
                new ThreesimMiningProcessFactory(effectiveParameters.getBlockInterval(), resourcePowerCalculator, rootSeed),
                new ThreesimTransactionSelectionProcessFactory(
                        effectiveParameters.getMaxBlockSize(),
                        effectiveParameters.getTransactionGenerationMode(),
                        trxPropSpec,
                        rootSeed),
                new ThreesimBlockValidatorFactory(nodeAllocationResolver, effectiveParameters.isStaticValidationDelayEnabled(), rootSeed),
                new BlockPropagationStrategyFactoryImpl(),
                txPropStrategyFactory,
                new TrxMemPoolFactoryImpl(),
                new OrphanBlockPoolFactoryImpl(),
                new ThreesimBlockchainSystemNodeBehaviorFactory(effectiveParameters, rootSeed),
                geographicalRegionsResolver,
                resourcePowerCalculator,
                new ThreesimBlockchainSystemNodeTagProvider()
        );
    }
}
