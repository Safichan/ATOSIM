package org.palladiosimulator.blockchainsystems.core.crashes.recovery;

import java.util.List;

public class BlockSyncRequest {

    private final long recoveryGeneration;
    private final List<String> blockLocator;
    
    public BlockSyncRequest(long recoveryGeneration, List<String> blockLocator) {
	   this.recoveryGeneration = recoveryGeneration;
	   this.blockLocator = List.copyOf(blockLocator);
    }
    
    public long getRecoveryGeneration() {
      return recoveryGeneration;
    }
    
    public List<String> getBlockLocator() {
       return blockLocator;
    }
}
