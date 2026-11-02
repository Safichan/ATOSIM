package org.palladiosimulator.blockchainsystems.core.crashes.recovery;

public class BlockSyncRequest {
	
	private final String lastKnownBlockHash;

    public BlockSyncRequest(String lastKnownBlockHash) {
        this.lastKnownBlockHash = lastKnownBlockHash;
    }

    public String getLastKnownBlockHash() {
        return lastKnownBlockHash;
    }

}
