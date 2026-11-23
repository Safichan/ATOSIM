package org.palladiosimulator.blockchainsystems.core.system;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.block.abstractions.BlockFactory;
import org.palladiosimulator.blockchainsystems.core.block.abstractions.BlockValidatorFactory;
import org.palladiosimulator.blockchainsystems.core.geography.GeographicalRegionsResolver;
import org.palladiosimulator.blockchainsystems.core.propagation.PropagationStrategyFactory;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainFactory;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeBehaviorFactory;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeTagProvider;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.MiningProcessFactory;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.NodeP2PNetworkInterface;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.OrphanBlockPoolFactory;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.ResourcePowerCalculator;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.Transaction;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.TransactionSelectionProcessFactory;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.TrxMemPoolFactory;

public class BlockchainSystemNodeFactory {

    private final BlockFactory _blockFactory;
    private final BlockchainFactory _blockchainFactory;
    private final MiningProcessFactory _miningProcessFactory;
    private final TransactionSelectionProcessFactory _transactionSelectionProcessFactory;
    private final BlockValidatorFactory _blockValidatorFactory;
    private final PropagationStrategyFactory<Block> _blockPropagationStrategyFactory;
    private final PropagationStrategyFactory<Transaction> _transactionPropagationStrategyFactory;
    private final TrxMemPoolFactory _trxMemPoolFactory;
    private final OrphanBlockPoolFactory _orphanBlockPoolFactory;
    private final BlockchainSystemNodeBehaviorFactory _behaviorFactory;
    private final GeographicalRegionsResolver _geographicalRegionsResolver;
    private final ResourcePowerCalculator _resourcePowerCalculator;
    private final BlockchainSystemNodeTagProvider _tagProvider;
    private final long _recoveryTimeoutDuration;

    public BlockchainSystemNodeFactory(
            BlockFactory blockFactory,
            BlockchainFactory blockchainFactory,
            MiningProcessFactory miningProcessFactory,
            TransactionSelectionProcessFactory transactionSelectionProcessFactory,
            BlockValidatorFactory blockValidatorFactory,
            PropagationStrategyFactory<Block> blockPropagationStrategyFactory,
            PropagationStrategyFactory<Transaction> transactionPropagationStrategyFactory,
            TrxMemPoolFactory trxMemPoolFactory,
            OrphanBlockPoolFactory orphanBlockPoolFactory,
            BlockchainSystemNodeBehaviorFactory behaviorFactory,
            GeographicalRegionsResolver geographicalRegionsResolver,
            ResourcePowerCalculator resourcePowerCalculator,
            BlockchainSystemNodeTagProvider tagProvider,
            long recoveryTimeoutDuration
    ) {
        _blockFactory = blockFactory;
        _blockchainFactory = blockchainFactory;
        _miningProcessFactory = miningProcessFactory;
        _transactionSelectionProcessFactory = transactionSelectionProcessFactory;
        _blockValidatorFactory = blockValidatorFactory;
        _blockPropagationStrategyFactory = blockPropagationStrategyFactory;
        _transactionPropagationStrategyFactory = transactionPropagationStrategyFactory;
        _trxMemPoolFactory = trxMemPoolFactory;
        _orphanBlockPoolFactory = orphanBlockPoolFactory;
        _behaviorFactory = behaviorFactory;
        _geographicalRegionsResolver = geographicalRegionsResolver;
        _resourcePowerCalculator = resourcePowerCalculator;
        _tagProvider = tagProvider;
        _recoveryTimeoutDuration = recoveryTimeoutDuration;
    }

    public BlockchainSystemNode createBlockchainSystemNode(NodeP2PNetworkInterface networkInterface, Block genesisBlock) {
        String nodeId = networkInterface.getEndpointId();

        return new BlockchainSystemNode(
                nodeId,
                "BlockchainSystemNode_" + nodeId,
                _blockPropagationStrategyFactory.createPropagationStrategy(),
                _transactionPropagationStrategyFactory.createPropagationStrategy(),
                networkInterface,
                _resourcePowerCalculator.getResourcePowerOfNode(nodeId),
                _miningProcessFactory.createMiningProcess(nodeId),
                _transactionSelectionProcessFactory.createTransactionSelectionProcess(nodeId),
                _blockchainFactory.createBlockchain(genesisBlock, nodeId),
                _blockValidatorFactory.createBlockValidator(nodeId),
                _trxMemPoolFactory.createEmptyTransactionMemPool(nodeId),
                _orphanBlockPoolFactory.createOrphanBlockPool(nodeId),
                _blockFactory,
                _behaviorFactory.create(nodeId),
                _geographicalRegionsResolver.getGeographicalRegionOfNode(nodeId),
                _tagProvider.getTags(nodeId),
                _recoveryTimeoutDuration
        );
    }
}
