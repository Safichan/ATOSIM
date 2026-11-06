package org.palladiosimulator.blockchainsystems.core.crashes.nodemodes;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.crashes.recovery.RecoveryStrategy;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeBehavior;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeContext;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.Transaction;

public class RecoveryMode implements NodeMode{
	
	private final RecoveryStrategy recoveryStrategy;
	private final BlockchainSystemNodeBehavior behavior;
	
	public RecoveryMode(RecoveryStrategy recoveryStrategy, BlockchainSystemNodeBehavior behavior) {
		this.recoveryStrategy = recoveryStrategy;
		this.behavior = behavior;
	}

	@Override
	public void onTransactionReceived(Transaction transaction, BlockchainSystemNodeContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBlockReceived(Block block, BlockchainSystemNodeContext context) {
		behavior.onBlockReceived(block, context);
		recoveryStrategy.onBlockReceived(block.getHash());
	}

	@Override
	public void onBlockValidated(Block block, boolean isValid, BlockchainSystemNodeContext context) {
		behavior.onBlockValidated(block, isValid, context);
		
	}

	@Override
	public void onBlockMined(Block block, BlockchainSystemNodeContext context) {
		throw new IllegalStateException("A recovering node must not mine blocks");
		
	}

	@Override
	public String onPreviousBlockSelection(BlockchainSystemNodeContext context) {
		throw new IllegalStateException("A recovering node must not select a block for mining.");
	}

	@Override
	public Block onCreatingBlock(long blockMinedAt, String previousBlockHash, BlockchainSystemNodeContext context) {
		throw new IllegalStateException("A crashed node must not create blocks.");
	}

}
