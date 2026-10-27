package org.palladiosimulator.blockchainsystems.core.system;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.block.abstractions.BlockFactory;
import org.palladiosimulator.blockchainsystems.core.block.abstractions.BlockValidator;
import org.palladiosimulator.blockchainsystems.core.geography.GeographicalRegion;
import org.palladiosimulator.blockchainsystems.core.propagation.PropagationStrategy;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.Blockchain;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeContext;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.MiningProcess;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.NodeP2PNetworkInterface;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.OrphanBlockPool;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.Transaction;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.TransactionSelectionProcess;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.TrxMemPool;

public class BlockchainSystemNodeContextImpl implements BlockchainSystemNodeContext {

    private final String _id;
    private final PropagationStrategy<Block> _blockPropagationStrategy;
    private final PropagationStrategy<Transaction> _transactionPropagationStrategy;
    private final NodeP2PNetworkInterface _networkInterface;
    private final double _resourcePower;
    private final MiningProcess _miningProcess;
    private final TransactionSelectionProcess _transactionSelectionProcess;
    private final Blockchain _blockchain;
    private final BlockValidator _blockValidator;
    private final TrxMemPool _trxMemPool;
    private final OrphanBlockPool _orphanBlockPool;
    private final BlockFactory _blockFactory;
    private final GeographicalRegion _geographicalRegion;
    private final Supplier<BlockchainSystemNodeState> _stateSupplier;
    

    public BlockchainSystemNodeContextImpl(
            String id,
            PropagationStrategy<Block> blockPropagationStrategy,
            PropagationStrategy<Transaction> transactionPropagationStrategy,
            NodeP2PNetworkInterface networkInterface,
            double resourcePower,
            MiningProcess miningProcess,
            TransactionSelectionProcess transactionSelectionProcess,
            Blockchain blockchain,
            BlockValidator blockValidator,
            TrxMemPool trxMemPool,
            OrphanBlockPool orphanBlockPool,
            BlockFactory blockFactory,
            GeographicalRegion geographicalRegion,
            Supplier<BlockchainSystemNodeState> stateSupplier
    ) {
        _id = id;
        _blockPropagationStrategy = blockPropagationStrategy;
        _transactionPropagationStrategy = transactionPropagationStrategy;
        _networkInterface = networkInterface;
        _resourcePower = resourcePower;
        _miningProcess = miningProcess;
        _transactionSelectionProcess = transactionSelectionProcess;
        _blockchain = blockchain;
        _blockValidator = blockValidator;
        _trxMemPool = trxMemPool;
        _orphanBlockPool = orphanBlockPool;
        _blockFactory = blockFactory;
        _geographicalRegion = geographicalRegion;
        _stateSupplier = stateSupplier;
    }

    @Override public String getId() { return _id; }
    @Override public PropagationStrategy<Block> getBlockPropagationStrategy() { return _blockPropagationStrategy; }
    @Override public PropagationStrategy<Transaction> getTransactionPropagationStrategy() { return _transactionPropagationStrategy; }
    @Override public NodeP2PNetworkInterface getNetworkInterface() { return _networkInterface; }
    @Override public double getResourcePower() { return _resourcePower; }
    @Override public MiningProcess getMiningProcess() { return _miningProcess; }
    @Override public TransactionSelectionProcess getTransactionSelectionProcess() { return _transactionSelectionProcess; }
    @Override public Blockchain getBlockchain() { return _blockchain; }
    @Override public BlockValidator getBlockValidator() { return _blockValidator; }
    @Override public TrxMemPool getTrxMemPool() { return _trxMemPool; }
    @Override public OrphanBlockPool getOrphanBlockPool() { return _orphanBlockPool; }
    @Override public BlockFactory getBlockFactory() { return _blockFactory; }
    @Override public GeographicalRegion getGeographicalRegion() { return _geographicalRegion; }

	@Override
	public BlockchainSystemNodeState getState() {
		return _stateSupplier.get();
	}
	

}
