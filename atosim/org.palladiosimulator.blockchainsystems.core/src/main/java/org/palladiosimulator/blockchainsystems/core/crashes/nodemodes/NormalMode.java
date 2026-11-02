package org.palladiosimulator.blockchainsystems.core.crashes.nodemodes;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeBehavior;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeContext;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.Transaction;

public class NormalMode implements NodeMode{

	private final BlockchainSystemNodeBehavior behavior;
	public NormalMode(BlockchainSystemNodeBehavior behavior) {
        this.behavior = behavior;
    }

    @Override
    public void onBlockReceived(Block block, BlockchainSystemNodeContext context) {
        behavior.onBlockReceived(block, context);
    }

	@Override
	public void onTransactionReceived(Transaction transaction, BlockchainSystemNodeContext context) {
		behavior.onTransactionReceived(transaction, context);
		
	}

	@Override
	public void onBlockValidated(Block block, boolean isValid, BlockchainSystemNodeContext context) {
		behavior.onBlockValidated(block, isValid, context);	
	}

	@Override
	public void onBlockMined(Block block, BlockchainSystemNodeContext context) {
		behavior.onBlockMined(block, context);
	}

	@Override
	public String onPreviousBlockSelection(BlockchainSystemNodeContext context) {
		return behavior.onPreviousBlockSelection(context);
	}

	@Override
	public Block onCreatingBlock(long blockMinedAt, String previousBlockHash, BlockchainSystemNodeContext context) {
		return behavior.onCreatingBlock(blockMinedAt, previousBlockHash, context);
	}

}
