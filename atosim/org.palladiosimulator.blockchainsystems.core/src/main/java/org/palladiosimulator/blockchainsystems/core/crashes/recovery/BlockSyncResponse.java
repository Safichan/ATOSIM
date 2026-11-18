package org.palladiosimulator.blockchainsystems.core.crashes.recovery;

import java.util.List;

public class BlockSyncResponse {

   private final long recoveryGeneration;
   private final List<String> missingBlockHashes;

   public BlockSyncResponse(long recoveryGeneration, List<String> missingBlockHashes) {
        this.recoveryGeneration = recoveryGeneration;
        this.missingBlockHashes = List.copyOf(missingBlockHashes);
    }

   public long getRecoveryGeneration() {
       return recoveryGeneration;
    }

   public List<String> getMissingBlockHashes() {
       return missingBlockHashes;
   }
   
}
