package org.palladiosimulator.blockchainsystems.threesim.simulation.results;

import org.palladiosimulator.blockchainsystems.core.blockchain.ChainReorganizedTraceEvent;

/**
 * A chain reorganization as observed by one node's local blockchain view during a simulation round.
 */
public class ChainReorganizationOccurrence {

    private final String nodeId;
    private final ChainReorganizedTraceEvent event;
    private final boolean attackerCaused;
    private final boolean duringRecovery;

    public ChainReorganizationOccurrence(String nodeId, ChainReorganizedTraceEvent event, 
    		boolean attackerCaused, boolean duringRecovery) {
        this.nodeId = nodeId;
        this.event = event;
        this.attackerCaused = attackerCaused;
        this.duringRecovery = duringRecovery;
    }

    public String getNodeId() { return nodeId; }
    public ChainReorganizedTraceEvent getEvent() { return event; }

    // True when the winning branch's earliest block past the common ancestor was attacker-mined
    // (branch-lineage attribution -- see ThreesimSimulationMonitor.isAttackerCaused).
    public boolean isAttackerCaused() { return attackerCaused; }
    
    public boolean isDuringRecovery() {
    	return duringRecovery;
    }
}
