package org.palladiosimulator.blockchainsystems.core.system;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.block.abstractions.BlockFactory;
import org.palladiosimulator.blockchainsystems.core.block.abstractions.BlockValidator;
import org.palladiosimulator.blockchainsystems.core.common.BlockchainSimulationObject;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.Event;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.Taggable;
import org.palladiosimulator.blockchainsystems.core.crashes.BlockSyncTimeoutEvent;
import org.palladiosimulator.blockchainsystems.core.crashes.NodeCrashEvent;
import org.palladiosimulator.blockchainsystems.core.crashes.NodeRecoveryEvent;
import org.palladiosimulator.blockchainsystems.core.geography.GeographicalRegion;
import org.palladiosimulator.blockchainsystems.core.propagation.PropagationStrategy;
import org.palladiosimulator.blockchainsystems.core.recovery.Synchronizable;
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
    private final Synchronizable _blockSynchronization;

    private final BlockchainSystemNodeContext _context;
    
    private BlockchainSystemNodeState _state = BlockchainSystemNodeState.ACTIVE;
    
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
        
        if (blockPropagationStrategy instanceof Synchronizable synchronization) {
            _blockSynchronization = synchronization;
            _blockSynchronization.setEventOrigin(this);
            _blockSynchronization.setOnSynchronizationCompleteCallback(
            	    this::finishRecovering
            	);
        } else {
            throw new IllegalArgumentException(
                    "Block propagation must support synchronization."
            );
        }

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
                _geographicalRegion,
                () -> _state
        );
    }

    @Override
    public void onInitialize() {
        _blockchain.initialize(getSimulationContext());
        _blockchain.initializeLogger(this);

        _transactionPropagationStrategy.setNetworkInterface(_networkInterface);
        _transactionPropagationStrategy.setBlockchainSystemNodeContext(_context);
        _transactionPropagationStrategy.setOnReceivedCallback(t -> _behavior.onTransactionReceived(t, _context));
        _transactionPropagationStrategy.initialize(getSimulationContext());
        _transactionPropagationStrategy.initializeLogger(this);

        _blockPropagationStrategy.setNetworkInterface(_networkInterface);
        _blockPropagationStrategy.setBlockchainSystemNodeContext(_context);
        _blockPropagationStrategy.setOnReceivedCallback(b -> _behavior.onBlockReceived(b, _context));
        _blockPropagationStrategy.initialize(getSimulationContext());
        _blockPropagationStrategy.initializeLogger(this);

        _blockValidator.setOnBlockValidatedCallback(
        		(block, isValid) -> {
        			_behavior.onBlockValidated(block, isValid, _context);
        			if (_state == BlockchainSystemNodeState.RECOVERING) {
        		        _blockSynchronization.onSyncBlockValidated(block);
        		    }
        		});
        _blockValidator.initialize(getSimulationContext());
        _blockValidator.initializeLogger(this);

        _miningProcess.setOnBlockMinedCallback(b -> _behavior.onBlockMined(b, _context));
        _miningProcess.setPreviousBlockSelectionCallback(() -> _behavior.onPreviousBlockSelection(_context));
        _miningProcess.setOnCreatingBlockCallback((blockMinedAt, prevHash) -> _behavior.onCreatingBlock(blockMinedAt, prevHash, _context));
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
    	if (NodeCrashEvent.EVENT_TYPE.equals(event.getEventType())) {
    		
            crash();
            
            NodeCrashEvent crashEvent;
            if(event instanceof NodeCrashEvent) {
            	crashEvent = (NodeCrashEvent) event;
            } else {
            	System.out.println("Not a crash event was passed in.");
            	return;
            }
            
            long recoveryTime = crashEvent.getOccurrenceTime() + crashEvent.getCrashDuration();

            NodeRecoveryEvent recoveryEvent = new NodeRecoveryEvent(recoveryTime, this);
            getSimulationContext().getEventCoordinator().raiseEvent(recoveryEvent);
        }
    	
    	else if(NodeRecoveryEvent.EVENT_TYPE.equals(event.getEventType())) {
    		recover();
    	}
    	
    	else if (BlockSyncTimeoutEvent.EVENT_TYPE.equals(event.getEventType())) {
    	    handleBlockSyncTimeoutEvent((BlockSyncTimeoutEvent) event);
    	}
    }
    
    

	private void handleBlockSyncTimeoutEvent(BlockSyncTimeoutEvent event) {
	    if (_state != BlockchainSystemNodeState.RECOVERING) {
	        return;
	    }
	
	    _blockSynchronization.handleNeighborTimeout(event.getNeighbor());
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
    
    public BlockchainSystemNodeState getState() {
    	return _state;
    }
    
    
    
    public void crash() {
    	_state = BlockchainSystemNodeState.CRASHED;
    	_miningProcess.stopMining();
    	
    	System.out.println("Node " + getId() + " crashed.");
    }
    
    public void recover() {
    	_state = BlockchainSystemNodeState.RECOVERING;
    	 _blockSynchronization.startSynchronization();
    	
    	System.out.println("Node " + getId() + " is recovering.");
    }
    
    public void finishRecovering() {
    	
    	if (_state != BlockchainSystemNodeState.RECOVERING) return;
    	
    	_state = BlockchainSystemNodeState.ACTIVE;
    	_miningProcess.restartMining();
    	
    	System.out.println("Node " + getId() + " has recovered.");
    }
}
