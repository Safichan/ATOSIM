package org.palladiosimulator.blockchainsystems.core.system;

import org.palladiosimulator.blockchainsystems.core.common.BlockchainSimulationObject;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.Event;
import org.palladiosimulator.blockchainsystems.core.crashes.events.CrashEvent;
import org.palladiosimulator.blockchainsystems.core.crashes.events.CrashPlan;
import org.palladiosimulator.blockchainsystems.core.geography.GeographicalRegions;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetwork;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.TransactionSubmissionProcess;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

public class BlockchainSystem extends BlockchainSimulationObject {

    private final P2PNetwork _network;
    private final GeographicalRegions _geographicalRegions;
    // LinkedHashSet, not HashSet: onInitialize() below iterates this set to start each node's
    // mining process, and that iteration order determines which node's first event lands first
    // in the event coordinator whenever two events share a timestamp (see
    // EventCoordinatorImpl/EffectsTimeSlice, which dispatch same-timestamp events in the order
    // they were raised). A HashSet's iteration order depends on BlockchainSystemNode's identity
    // hashCode, which is not reproducible across separate JVM runs; the caller must therefore
    // populate this set in a deterministic order (see ThreesimBlockchainSystemFactory).
    private final LinkedHashSet<BlockchainSystemNode> _nodes;
    private final TransactionSubmissionProcess _transactionSubmissionProcess;
    private final double _blockReward;
    // Network-wide (not per-node) recipient-selection draw: one seeded stream consumed once per
    // submitted transaction, matching the tagged-generator pattern used for the other
    // transaction-submission mechanics (see ThreesimBlockchainSystemFactory's "recipientSelection"
    // tag) rather than a per-node-scoped generator, since this is a single routing decision, not a
    // per-node draw.
    private final RandomGenerator _recipientSelectionGenerator;
    
    private final CrashPlan _crashPlan;

    public BlockchainSystem(
            String id,
            String name,
            P2PNetwork network,
            GeographicalRegions geographicalRegions,
            LinkedHashSet<BlockchainSystemNode> nodes,
            TransactionSubmissionProcess transactionSubmissionProcess,
            double blockReward,
            RandomGenerator recipientSelectionGenerator,
            CrashPlan crashPlan
    ) {
        super(id, name);
        _network = network;
        _geographicalRegions = geographicalRegions;
        _nodes = nodes;
        _transactionSubmissionProcess = transactionSubmissionProcess;
        _blockReward = blockReward;
        _recipientSelectionGenerator = recipientSelectionGenerator;
        _crashPlan = crashPlan;
    }

    @Override
    public void onInitialize() {
        _network.initialize(getSimulationContext());

        for (BlockchainSystemNode node : _nodes) {
            node.initialize(getSimulationContext());
            _transactionSubmissionProcess.addOnTransactionSubmittedCallbackSubscriber(node);
        }
        
        if(_crashPlan != null) {
        	Set<String> crashingNodeIds = _crashPlan.getCrashingNodeIds();
        	for (BlockchainSystemNode node : _nodes) {
                if (crashingNodeIds.contains(node.getId())) {
                    CrashEvent crashEvent = 
                    		new CrashEvent(node, _crashPlan.getCrashTime(), _crashPlan.getCrashDuration());
                    getSimulationContext().getEventCoordinator().raiseEvent(crashEvent);
                }
            }
        }

        _transactionSubmissionProcess.setOnSelectRecipientNodeIdCallback(this::selectRecipientNodeId);
        _transactionSubmissionProcess.initialize(getSimulationContext());
        _transactionSubmissionProcess.startTransactionSubmissionProcess();
    }

    @Override
    public void onCleanup() {
        _network.cleanup();
        for (BlockchainSystemNode node : _nodes) node.cleanup();
        _transactionSubmissionProcess.cleanup();
    }

    @Override
    public void dispatchEvent(Event event) {
    }

    private String selectRecipientNodeId() {
        List<BlockchainSystemNode> nodeList = new ArrayList<>(_nodes);
        return nodeList.get(_recipientSelectionGenerator.nextInt(nodeList.size())).getId();
    }

    public P2PNetwork getNetwork() { return _network; }
    public GeographicalRegions getGeographicalRegions() { return _geographicalRegions; }
    public LinkedHashSet<BlockchainSystemNode> getNodes() { return _nodes; }
    public TransactionSubmissionProcess getTransactionSubmissionProcess() { return _transactionSubmissionProcess; }
    public double getBlockReward() { return _blockReward; }

	public CrashPlan getCrashPlan() {
		return _crashPlan;
	}
}
