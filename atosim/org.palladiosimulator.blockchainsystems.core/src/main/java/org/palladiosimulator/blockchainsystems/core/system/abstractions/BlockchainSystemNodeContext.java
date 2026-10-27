package org.palladiosimulator.blockchainsystems.core.system.abstractions;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.block.abstractions.BlockFactory;
import org.palladiosimulator.blockchainsystems.core.block.abstractions.BlockValidator;
import org.palladiosimulator.blockchainsystems.core.geography.GeographicalRegion;
import org.palladiosimulator.blockchainsystems.core.propagation.PropagationStrategy;
import org.palladiosimulator.blockchainsystems.core.system.BlockchainSystemNodeState;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.Transaction;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.TransactionSelectionProcess;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.TrxMemPool;

public interface BlockchainSystemNodeContext {
    String getId();
    PropagationStrategy<Block> getBlockPropagationStrategy();
    PropagationStrategy<Transaction> getTransactionPropagationStrategy();
    TrxMemPool getTrxMemPool();
    NodeP2PNetworkInterface getNetworkInterface();
    double getResourcePower();
    MiningProcess getMiningProcess();
    TransactionSelectionProcess getTransactionSelectionProcess();
    Blockchain getBlockchain();
    OrphanBlockPool getOrphanBlockPool();
    BlockFactory getBlockFactory();
    BlockValidator getBlockValidator();
    GeographicalRegion getGeographicalRegion();
    BlockchainSystemNodeState getState();
}
