package org.palladiosimulator.blockchainsystems.core.crashes.recovery;

import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetworkEndpoint;

public class PendingBlockRequest {

    private final long attemptId;
    private final P2PNetworkEndpoint peer;

    public PendingBlockRequest(long attemptId, P2PNetworkEndpoint peer) {
        this.attemptId = attemptId;
        this.peer = peer;
    }

    public long getAttemptId() {
        return attemptId;
    }

    public P2PNetworkEndpoint getPeer() {
        return peer;
    }
}
