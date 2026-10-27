package org.palladiosimulator.blockchainsystems.core.propagation.block;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.EventDispatchable;
import org.palladiosimulator.blockchainsystems.core.crashes.BlockSyncTimeoutEvent;
import org.palladiosimulator.blockchainsystems.core.network.MessageDroppedTraceEvent;
import org.palladiosimulator.blockchainsystems.core.propagation.GossipPropagationStrategy;
import org.palladiosimulator.blockchainsystems.core.propagation.MessageImpl;
import org.palladiosimulator.blockchainsystems.core.recovery.Synchronizable;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.Message;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetworkEndpoint;

import java.util.HashSet;
import java.util.Set;

public class BlockPropagationStrategy extends GossipPropagationStrategy<Block> implements Synchronizable {

    private static final String INV_MESSAGE_KEY = "BLOCK_INV";
    private static final String GET_DATA_MESSAGE_KEY = "BLOCK_GET_DATA";
    private static final String ELEMENT_MESSAGE_KEY = "BLOCK_MSG";
    
    private static final String SYNC_REQUEST_MESSAGE_KEY = "BLOCK_SYNC_REQUEST";
    private static final String SYNC_RESPONSE_MESSAGE_KEY = "BLOCK_SYNC_RESPONSE";

    private static final int MESSAGE_HEADER_BYTE_SIZE = 24;
    private static final int INV_MESSAGE_BYTE_SIZE = 20;
    private static final int GET_DATA_MESSAGE_BYTE_SIZE = 20;
    
    private static final long SYNC_TIMEOUT_DURATION = 5_000L;
    private EventDispatchable _eventOrigin;

    private final Set<String> _requestedBlockIds = new HashSet<>();
    private final Set<P2PNetworkEndpoint> _pendingSyncResponses = new HashSet<>();
    private final Set<P2PNetworkEndpoint> _timedOutNeighbors = new HashSet<>();
    private final Set<String> _requestedSyncBlockIds = new HashSet<>();
    private final Set<String> _pendingSyncValidations = new HashSet<>();

    @Override protected String getInvMessageKey() { return INV_MESSAGE_KEY; }
    @Override protected String getGetDataMessageKey() { return GET_DATA_MESSAGE_KEY; }
    @Override protected String getElementMessageKey() { return ELEMENT_MESSAGE_KEY; }
    
    private Runnable _onSynchronizationCompleteCallback;
    private boolean _synchronizationCompleted = false;
    
    @Override
    public void startSynchronization() {
        if (_context == null || _networkInterface == null) {
            return;
        }
        
        _synchronizationCompleted = false;
        _pendingSyncValidations.clear();
        _timedOutNeighbors.clear();

        Block localTip =
                _context.getBlockchain().getPreferredTipOfLongestChains();

        Message request = new MessageImpl(
                localTip.getHash(),
                SYNC_REQUEST_MESSAGE_KEY,
                MESSAGE_HEADER_BYTE_SIZE + INV_MESSAGE_BYTE_SIZE
        );
        
        _pendingSyncResponses.clear();

        for (P2PNetworkEndpoint neighbor : _networkInterface.getNeighbors()) {
        	_pendingSyncResponses.add(neighbor);
            _networkInterface.send(request, neighbor);
            
            scheduleSyncTimeout(neighbor);
        }
        
        checkSynchronizationComplete();
    }
    
    private void scheduleSyncTimeout(P2PNetworkEndpoint neighbor) {
        long timeoutTime =
                getSimulationContext().getSystemClock().getCurrentTime()
                + SYNC_TIMEOUT_DURATION;

        BlockSyncTimeoutEvent event = new BlockSyncTimeoutEvent(
                timeoutTime,
                _eventOrigin,
                neighbor
        );

        getSimulationContext().getEventCoordinator().raiseEvent(event);
    }
    
    @Override
    protected void handleSynchronizationMessage(Message message, P2PNetworkEndpoint senderNetworkEndpoint) {
        String contentType = message.getContentType();

        if (SYNC_REQUEST_MESSAGE_KEY.equals(contentType)) {
            handleSyncRequest(senderNetworkEndpoint);
        }

        if (SYNC_RESPONSE_MESSAGE_KEY.equals(contentType)) {
            handleSyncResponse(message, senderNetworkEndpoint);
        }
    }
    
    @Override
    protected boolean isSynchronizationMessage(Message message) {
    	return message.getContentType().equals(SYNC_REQUEST_MESSAGE_KEY) 
    			|| message.getContentType().equals(SYNC_RESPONSE_MESSAGE_KEY);
    }
    
    
    private void handleSyncRequest(P2PNetworkEndpoint senderNetworkEndpoint) {

        if (_context == null || _networkInterface == null) {
            return;
        }

        Block localTip = _context.getBlockchain().getPreferredTipOfLongestChains();

        Message response = new MessageImpl(localTip.getHash(), SYNC_RESPONSE_MESSAGE_KEY,
                MESSAGE_HEADER_BYTE_SIZE + INV_MESSAGE_BYTE_SIZE
        );

        _networkInterface.send(response, senderNetworkEndpoint);
    }
    
    private void handleSyncResponse(Message message, P2PNetworkEndpoint senderNetworkEndpoint) {

        if (_context == null || _networkInterface == null) {
            return;
        }
        
        if (_timedOutNeighbors.contains(senderNetworkEndpoint)) {
            return;
        }
        
        _pendingSyncResponses.remove(senderNetworkEndpoint);

        String remoteTipHash = (String) message.getContent();

        if (_context.getBlockchain().hasBlockWithHash(remoteTipHash)
                || _context.getOrphanBlockPool().hasBlockWithHash(remoteTipHash)) {
        	checkSynchronizationComplete();
            return;
        }

        if (_requestedSyncBlockIds.add(remoteTipHash)) {
            _networkInterface.send(createGetDataMessage(remoteTipHash), senderNetworkEndpoint);
        }
    }
    
    @Override
    protected void handleMessageDuringRecovery(Message message, P2PNetworkEndpoint senderNetworkEndpoint) {
        handlePropagationMessage(message, senderNetworkEndpoint);
    }
    
    @Override
	public void handleNeighborTimeout(P2PNetworkEndpoint neighbor) {
    	if (!_pendingSyncResponses.remove(neighbor)) {
            return;
        }
    	 _timedOutNeighbors.add(neighbor);
    	 
    	 checkSynchronizationComplete();
	}
    

    private void checkSynchronizationComplete() {
    	if (_synchronizationCompleted) {
            return;
        }

        if (!_pendingSyncResponses.isEmpty()
                || !_requestedSyncBlockIds.isEmpty()
                || !_pendingSyncValidations.isEmpty()) {
            return;
        }

        _synchronizationCompleted = true;

        if (_onSynchronizationCompleteCallback != null) {
            _onSynchronizationCompleteCallback.run();
        }
    }
    
    @Override
    protected Message createInvMessage(Block element) {
        return new MessageImpl(element.getHash(), INV_MESSAGE_KEY, MESSAGE_HEADER_BYTE_SIZE + INV_MESSAGE_BYTE_SIZE);
    }

    @Override
    protected Message createGetDataMessage(String elementId) {
        return new MessageImpl(elementId, GET_DATA_MESSAGE_KEY, MESSAGE_HEADER_BYTE_SIZE + GET_DATA_MESSAGE_BYTE_SIZE);
    }

    @Override
    protected Message createElementMessage(Block element) {
        return new MessageImpl(element, ELEMENT_MESSAGE_KEY, MESSAGE_HEADER_BYTE_SIZE + element.getSize());
    }
    
    @Override
    protected void handleInvMessageReceived(Message message, P2PNetworkEndpoint senderNetworkEndpoint) {
        String hash = (String) message.getContent();
        if (_context == null) return;
        
        // Proof if the block is already known
        if (_context.getBlockchain().hasBlockWithHash(hash) ||
                _context.getOrphanBlockPool().hasBlockWithHash(hash)) {
            _requestedBlockIds.remove(hash);
            return;
        }
        
        // Proof if the block was already requested
        if (!_requestedBlockIds.add(hash)) return;
        
        // Actual block request 
        if (_networkInterface != null) {
            _networkInterface.send(createGetDataMessage(hash), senderNetworkEndpoint);
        }
    }
    
    // Handle block requests
    @Override
    protected void handleGetDataMessageReceived(Message message, P2PNetworkEndpoint senderNetworkEndpoint) {
        String hash = (String) message.getContent();
        if (_context == null) return;
        // Search for block in the blockchain
        Block block = _context.getBlockchain().getBlock(hash);
        
        // If block is found send it back to the requesting node
        if (block != null && _networkInterface != null) {
            _networkInterface.send(createElementMessage(block), senderNetworkEndpoint);
            logBlockSent(block, senderNetworkEndpoint);
        }
    }
    
    // Handle messages containing blocks
    @Override
    protected void handleElementMessageReceived(Message message, P2PNetworkEndpoint senderNetworkEndpoint) {
        Block block = (Block) message.getContent();
        if (_context == null) return;
        
        boolean isSyncBlock = _requestedSyncBlockIds.contains(block.getHash());
        
        // Remove requested block from list
        _requestedBlockIds.remove(block.getHash());
        _requestedSyncBlockIds.remove(block.getHash());
        
        // If block is already known do nothing
        if (_context.getBlockchain().hasBlockWithHash(block.getHash()) ||
                _context.getOrphanBlockPool().hasBlockWithHash(block.getHash())) {
        	checkSynchronizationComplete();
            return;
        }
        
        // Request previous block if it is not known
        String previousHash = block.getPreviousHash();
        if (previousHash != null &&
                !_context.getBlockchain().hasBlockWithHash(previousHash) &&
                !_context.getOrphanBlockPool().hasBlockWithHash(previousHash) &&
                _requestedBlockIds.add(previousHash)) {
        	
        	boolean requestAdded;

            if (isSyncBlock) {
                requestAdded = _requestedSyncBlockIds.add(previousHash);
            } else {
                requestAdded = _requestedBlockIds.add(previousHash);
            }
        	
            if (requestAdded && _networkInterface != null) {
                _networkInterface.send(createGetDataMessage(previousHash), senderNetworkEndpoint);
            }
        }

        logBlockReceived(block, senderNetworkEndpoint);
        if (isSyncBlock) {
            _pendingSyncValidations.add(block.getHash());
        }

        notifyBlockReceived(block);
    }

    @Override
    protected void handleMessageDropped(Message message, P2PNetworkEndpoint recipientNetworkEndpoint) {
        if (_networkInterface == null) {
            throw new IllegalStateException("Network interface is not set for BlockPropagationStrategy.");
        }
        getTraceEventLogger().logEvent(new MessageDroppedTraceEvent(
                message,
                getSimulationContext().getSystemClock().getCurrentTime(),
                recipientNetworkEndpoint,
                _networkInterface
        ));
    }

    private void notifyBlockReceived(Block block) {
        if (_onReceivedCallback != null) _onReceivedCallback.accept(block);
    }

    private void logBlockSent(Block block, P2PNetworkEndpoint receiverNetworkEndpoint) {
        getTraceEventLogger().logEvent(new BlockSentTraceEvent(
                getSimulationContext().getSystemClock().getCurrentTime(),
                block, receiverNetworkEndpoint));
    }

    private void logBlockReceived(Block block, P2PNetworkEndpoint senderNetworkEndpoint) {
        getTraceEventLogger().logEvent(new BlockReceivedTraceEvent(
                getSimulationContext().getSystemClock().getCurrentTime(),
                block, senderNetworkEndpoint));
    }
    
	@Override
	public void setEventOrigin(EventDispatchable origin) {
		_eventOrigin = origin;
		
	}
	@Override
	public void setOnSynchronizationCompleteCallback(Runnable callback) {
		_onSynchronizationCompleteCallback = callback;
	}
	
	@Override
	public void onSyncBlockValidated(Block block) {
		_pendingSyncValidations.remove(block.getHash());
	    checkSynchronizationComplete();
		
	}
		
	
}
