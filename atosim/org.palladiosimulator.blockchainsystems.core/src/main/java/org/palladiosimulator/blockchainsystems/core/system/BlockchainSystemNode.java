package org.palladiosimulator.blockchainsystems.core.system;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.block.abstractions.BlockFactory;
import org.palladiosimulator.blockchainsystems.core.block.abstractions.BlockValidator;
import org.palladiosimulator.blockchainsystems.core.common.BlockchainSimulationObject;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.Event;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.Taggable;
import org.palladiosimulator.blockchainsystems.core.crashes.nodemodes.CrashMode;
import org.palladiosimulator.blockchainsystems.core.crashes.nodemodes.NodeMode;
import org.palladiosimulator.blockchainsystems.core.crashes.nodemodes.NormalMode;
import org.palladiosimulator.blockchainsystems.core.crashes.nodemodes.RecoveryMode;
import org.palladiosimulator.blockchainsystems.core.crashes.recovery.BlocksFirstRecovery;
import org.palladiosimulator.blockchainsystems.core.crashes.recovery.MessageDispatcher;
import org.palladiosimulator.blockchainsystems.core.crashes.recovery.RecoveryStrategy;
import org.palladiosimulator.blockchainsystems.core.geography.GeographicalRegion;
import org.palladiosimulator.blockchainsystems.core.propagation.PropagationStrategy;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.Blockchain;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeBehavior;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeContext;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.MiningProcess;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.NodeP2PNetworkInterface;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.OrphanBlockPool;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.Transaction;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.TransactionSelectionProcess;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.TransactionSubmittedCallbackSubscriber;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.TrxMemPool;

import java.util.Objects;
import java.util.Set;

public class BlockchainSystemNode extends BlockchainSimulationObject implements Taggable, TransactionSubmittedCallbackSubscriber {

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
    private final BlockchainSystemNodeBehavior _behavior;
    private final GeographicalRegion _geographicalRegion;
    private final Set<String> _tags;

    private final BlockchainSystemNodeContext _context;
    
    private NodeMode _currentMode;
    private RecoveryStrategy _recoveryStrategy = new BlocksFirstRecovery();
    private MessageDispatcher _messageDispatcher;
    

    public BlockchainSystemNode(
            String id,
            String name,
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
            BlockchainSystemNodeBehavior behavior,
            GeographicalRegion geographicalRegion,
            Set<String> tags
    ) {
        super(id, name);
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
        _behavior = behavior;
        _geographicalRegion = geographicalRegion;
        _tags = tags;

        _context = new BlockchainSystemNodeContextImpl(
                id,
                _blockPropagationStrategy,
                _transactionPropagationStrategy,
                _networkInterface,
                _resourcePower,
                _miningProcess,
                _transactionSelectionProcess,
                _blockchain,
                _blockValidator,
                _trxMemPool,
                _orphanBlockPool,
                _blockFactory,
                _geographicalRegion
        );
        
        _currentMode = new NormalMode(_behavior);
    }

    @Override
    public void onInitialize() {
        _blockchain.initialize(getSimulationContext());
        _blockchain.initializeLogger(this);

        _transactionPropagationStrategy.setNetworkInterface(_networkInterface);
        _transactionPropagationStrategy.setBlockchainSystemNodeContext(_context);
        _transactionPropagationStrategy.setOnReceivedCallback(t -> _currentMode.onTransactionReceived(t, _context));
        _transactionPropagationStrategy.initialize(getSimulationContext());
        _transactionPropagationStrategy.initializeLogger(this);

        _messageDispatcher = new MessageDispatcher(_networkInterface);

        _blockPropagationStrategy.setNetworkInterface(_networkInterface);

        _messageDispatcher.setBlockHandler(_blockPropagationStrategy);
        
        _blockPropagationStrategy.setBlockchainSystemNodeContext(_context);
        _blockPropagationStrategy.setOnReceivedCallback(b -> _currentMode.onBlockReceived(b, _context));
        _blockPropagationStrategy.initialize(getSimulationContext());
        _blockPropagationStrategy.initializeLogger(this);

        _blockValidator.setOnBlockValidatedCallback((block, isValid) -> _currentMode.onBlockValidated(block, isValid, _context));
        _blockValidator.initialize(getSimulationContext());
        _blockValidator.initializeLogger(this);

        _miningProcess.setOnBlockMinedCallback(b -> _currentMode.onBlockMined(b, _context));
        _miningProcess.setPreviousBlockSelectionCallback(() -> _currentMode.onPreviousBlockSelection(_context));
        _miningProcess.setOnCreatingBlockCallback((blockMinedAt, prevHash) -> _currentMode.onCreatingBlock(blockMinedAt, prevHash, _context));
        _miningProcess.initialize(getSimulationContext());
        _miningProcess.initializeLogger(this);

        _transactionSelectionProcess.initialize(getSimulationContext());
        _transactionSelectionProcess.initializeLogger(this);

        _trxMemPool.initialize(getSimulationContext());
        _trxMemPool.initializeLogger(this);

        _orphanBlockPool.initialize(getSimulationContext());
        _orphanBlockPool.initializeLogger(this);

        _behavior.initialize(getSimulationContext());
        _behavior.initializeLogger(this);
        _behavior.onNodeInitialized(_context);
    }

    @Override
    public void onCleanup() {
        _trxMemPool.cleanup();
        _orphanBlockPool.cleanup();
        _miningProcess.cleanup();
        _blockValidator.cleanup();
        _blockPropagationStrategy.cleanup();
        _blockchain.cleanup();
    }

    @Override
    public void dispatchEvent(Event event) {
    	
    	
    }
    
    
    public void crash() {
        _miningProcess.stopMining();
        _currentMode = new CrashMode();
    }
    
    public void recover() {
    	_miningProcess.restartMining();
    	_currentMode = new RecoveryMode(_recoveryStrategy);
    }
    
    public void finishRecovery() {
    	_currentMode = new NormalMode(_behavior);
    }
    

    @Override
    public boolean hasTag(String tag) {
        return _tags.contains(tag);
    }

    @Override
    public void onTransactionSubmitted(Transaction transaction) {
        if (!transaction.getRecipientId().equals(this.getId())) return;
        _behavior.onTransactionReceived(transaction, _context);
    }

    public double getResourcePower() { return _resourcePower; }
    public GeographicalRegion getGeographicalRegion() { return _geographicalRegion; }
    public Blockchain getBlockchain() { return _blockchain; }
    
    public void setRecoveryStrategy(RecoveryStrategy recoveryStrategy) {
        _recoveryStrategy = Objects.requireNonNull(recoveryStrategy); 
    }
    
}
