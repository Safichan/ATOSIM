package org.palladiosimulator.blockchainsystems.core.crashes.nodemodes;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeContext;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.Transaction;

public class CrashMode implements NodeMode{

	@Override
	public void onTransactionReceived(Transaction transaction, BlockchainSystemNodeContext context) {
		// Ignore all transactions while node is crashed
	}

	@Override
	public void onBlockReceived(Block block, BlockchainSystemNodeContext context) {
		// Ignore all blocks while node is crashed
		
	}

	@Override
	public void onBlockValidated(Block block, boolean isValid, BlockchainSystemNodeContext context) {
		// Ignore block validation results
	}

	@Override
	public void onBlockMined(Block block, BlockchainSystemNodeContext context) {
		throw new IllegalStateException("A crashed node must not mine blocks");
	}

	@Override
	public String onPreviousBlockSelection(BlockchainSystemNodeContext context) {
		throw new IllegalStateException("A crashed node must not select a block for mining.");
	}

	@Override
	public Block onCreatingBlock(long blockMinedAt, String previousBlockHash, BlockchainSystemNodeContext context) {
		throw new IllegalStateException("A crashed node must not create blocks.");
	}

}
