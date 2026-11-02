package org.palladiosimulator.blockchainsystems.core.crashes.nodemodes;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeContext;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.Transaction;

public interface NodeMode {
    void onTransactionReceived(Transaction transaction, BlockchainSystemNodeContext context);
    void onBlockReceived(Block block, BlockchainSystemNodeContext context);
    void onBlockValidated(Block block, boolean isValid, BlockchainSystemNodeContext context);
    void onBlockMined(Block block, BlockchainSystemNodeContext context);
    String onPreviousBlockSelection(BlockchainSystemNodeContext context);
    Block onCreatingBlock(long blockMinedAt, String previousBlockHash, BlockchainSystemNodeContext context);
}
