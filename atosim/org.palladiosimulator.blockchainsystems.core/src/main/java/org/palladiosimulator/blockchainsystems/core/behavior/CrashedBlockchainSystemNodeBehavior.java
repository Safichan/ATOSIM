package org.palladiosimulator.blockchainsystems.core.behavior;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.common.BlockchainNodeObject;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.Event;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeBehavior;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeContext;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.Transaction;

import java.util.random.RandomGenerator;

public class CrashedBlockchainSystemNodeBehavior extends BlockchainNodeObject
        implements BlockchainSystemNodeBehavior, BlockHashSeedable {

    private RandomGenerator _blockHashGenerator;
    private int crashDuration;

    @Override
    public void setBlockHashGenerator(RandomGenerator generator) {
        _blockHashGenerator = generator;
    }

    @Override
    public void onBlockReceived(Block block, BlockchainSystemNodeContext context) {
        if (context.getBlockchain().hasBlockWithHash(block.getHash()) ||
                context.getOrphanBlockPool().hasBlockWithHash(block.getHash())) {
            return;
        }
        context.getBlockValidator().validateBlock(block);
    }

    @Override
    public void onTransactionReceived(Transaction transaction, BlockchainSystemNodeContext context) {
        context.getTrxMemPool().storeTransaction(transaction);
        context.getTransactionPropagationStrategy().distribute(transaction);
    }

    @Override
    public void onBlockValidated(Block block, boolean isValid, BlockchainSystemNodeContext context) {
        if (!isValid) return;

        AppendOutcome outcome = BehaviorUtils.INSTANCE.appendBlockToBlockchainDetailed(block, context);
        if (outcome == AppendOutcome.INCLUDED || outcome == AppendOutcome.FORKING) {
            context.getTrxMemPool().removeTransactions(block.getTransactions());
            context.getMiningProcess().restartMining();
            context.getBlockPropagationStrategy().distribute(block);
        }
    }

    @Override
    public void onBlockMined(Block block, BlockchainSystemNodeContext context) {
        AppendOutcome outcome = BehaviorUtils.INSTANCE.appendBlockToBlockchainDetailed(block, context);
        if (outcome == AppendOutcome.INCLUDED || outcome == AppendOutcome.FORKING) {
            context.getTrxMemPool().removeTransactions(block.getTransactions());
            context.getBlockPropagationStrategy().distribute(block);
        }
    }

    @Override
    public Block onCreatingBlock(long blockMinedAt, String previousBlockHash, BlockchainSystemNodeContext context) {
        var selectedTrxsResult = context.getTransactionSelectionProcess().selectTransactionsForBlock(context);
        return context.getBlockFactory().createBlock(
                String.format("%016x%016x", _blockHashGenerator.nextLong(), _blockHashGenerator.nextLong()),
                previousBlockHash,
                context.getId(),
                blockMinedAt,
                selectedTrxsResult.getTotalSize(),
                selectedTrxsResult.getTransactions()
        );
    }

    @Override
    public String onPreviousBlockSelection(BlockchainSystemNodeContext context) {
        return context.getBlockchain().getPreferredTipOfLongestChains().getHash();
    }

    @Override
    public void onNodeInitialized(BlockchainSystemNodeContext context) {
        context.getMiningProcess().startMining();
    }

    @Override
    public void dispatchEvent(Event event) {
    }
    
    public int getCrashDuration() {
    	return crashDuration;
    }
    
    public void setCrashDuration(int crashDuration) {
    	this.crashDuration = crashDuration;
    }
}
