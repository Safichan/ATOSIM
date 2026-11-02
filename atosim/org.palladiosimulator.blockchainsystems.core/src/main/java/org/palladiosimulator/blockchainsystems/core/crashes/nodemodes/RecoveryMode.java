package org.palladiosimulator.blockchainsystems.core.crashes.nodemodes;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.crashes.recovery.RecoveryStrategy;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeContext;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.Transaction;

public class RecoveryMode implements NodeMode{
	
	private final RecoveryStrategy recoveryStrategy;
	
	public RecoveryMode(RecoveryStrategy recoveryStrategy) {
		this.recoveryStrategy = recoveryStrategy;
	}

	@Override
	public void onTransactionReceived(Transaction transaction, BlockchainSystemNodeContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBlockReceived(Block block, BlockchainSystemNodeContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBlockValidated(Block block, boolean isValid, BlockchainSystemNodeContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBlockMined(Block block, BlockchainSystemNodeContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String onPreviousBlockSelection(BlockchainSystemNodeContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Block onCreatingBlock(long blockMinedAt, String previousBlockHash, BlockchainSystemNodeContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
