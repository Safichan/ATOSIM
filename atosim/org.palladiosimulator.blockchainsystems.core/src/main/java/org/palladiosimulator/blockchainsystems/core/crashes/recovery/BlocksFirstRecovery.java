package org.palladiosimulator.blockchainsystems.core.crashes.recovery;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.crashes.events.RecoveryTimeoutType;
import org.palladiosimulator.blockchainsystems.core.propagation.MessageImpl;
import org.palladiosimulator.blockchainsystems.core.propagation.PropagationStrategy;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeContext;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.Message;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.NodeP2PNetworkInterface;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetworkEndpoint;

public class BlocksFirstRecovery implements RecoveryStrategy {
	
	private final NodeP2PNetworkInterface _networkInterface;
	private final BlockchainSystemNodeContext _context;
	private final PropagationStrategy<Block> _blockPropagationStrategy;
	
	private static final int MESSAGE_HEADER_BYTE_SIZE = 24;
	private static final int BLOCK_HASH_BYTE_SIZE = 20;
	
	private static final int MAX_NUMBER_OF_BLOCKS = 500;
	private static final int MAX_NUMBER_OF_REQUESTS = 128;
	
	// Stores all block hashes that need to send a request
	// A maximum of 128 requests can be active at the same time
	private Queue<String> pendingRequestQueue = new ArrayDeque<>();
	// Stores all block hashes that await a response from the sync peer
	private Map<String, PendingBlockRequest> pendingBlockRequests = new HashMap<>();
	// All block hashes that have to be validated
	private Set<String> awaitingValidations = new HashSet<>();
	
	private Set<P2PNetworkEndpoint> attemptedPeers = new HashSet<>();
	
	
	private P2PNetworkEndpoint syncPeer;
	private Runnable onRecoveryCompletedCallback;
	private long _recoveryGeneration = 0;
	private long _syncAttemptId = 0;
	private long _blockAttemptId = 0;
	
	private Consumer<RecoveryTimeoutRequest> onRecoveryTimeoutRequestedCallback;
	
	private boolean waitingForSyncResponse = false;
	
	public BlocksFirstRecovery(NodeP2PNetworkInterface networkInterface, BlockchainSystemNodeContext context,
    		PropagationStrategy<Block> blockPropagationStrategy) {
        _networkInterface = networkInterface;
        _context = context;
        _blockPropagationStrategy = blockPropagationStrategy;
    } 
	
    
    @Override
    public void startRecovery() {
    	
    	_recoveryGeneration++;
    	    	
    	cleanUp();
    	
    	syncPeer = selectSyncPeer();

        if (syncPeer == null) {
        	onRecoveryCompletedCallback.run();
            return;
        }
        sendSyncRequest(syncPeer); 
    }
    
    private void cleanUp() {	
    	pendingRequestQueue.clear();
    	pendingBlockRequests.clear();
    	awaitingValidations.clear();
    	attemptedPeers.clear();
    	waitingForSyncResponse = false;
    }
    
    private void sendSyncRequest(P2PNetworkEndpoint syncPeer) {
    	List<String> blockLocator = createBlockLocator();
        Message syncRequest = createSyncRequestMessage(blockLocator);
        
        _syncAttemptId++;
        waitingForSyncResponse = true;
        attemptedPeers.add(syncPeer); 
        
        onRecoveryTimeoutRequestedCallback.accept(
        		new RecoveryTimeoutRequest(RecoveryTimeoutType.SYNC_RESPONSE,
        	    _syncAttemptId, null)
        );
        _networkInterface.send(syncRequest, syncPeer); 
        
    }
    
    private void requestNextSyncBatch() {
	    if (syncPeer == null) {
	        return;
	    }
	    sendSyncRequest(syncPeer);
	}
    
    private Message createSyncRequestMessage(List<String> blockLocator) {
        int messageSize = MESSAGE_HEADER_BYTE_SIZE
        		+ Long.BYTES
                + blockLocator.size() * BLOCK_HASH_BYTE_SIZE;
        
        BlockSyncRequest request = new BlockSyncRequest(_recoveryGeneration, blockLocator);
       
        return new MessageImpl(
                request,
                RecoveryMessageTypes.BLOCK_SYNC_REQUEST,
                messageSize
        );
    }
    
    private Message createSyncResponseMessage(List<String> missingBlockHashes, long recoveryNumber) {
	    int messageSize = MESSAGE_HEADER_BYTE_SIZE
	    		+ Long.BYTES
	            + missingBlockHashes.size() * BLOCK_HASH_BYTE_SIZE;

	    BlockSyncResponse response = new BlockSyncResponse(recoveryNumber, missingBlockHashes);

		return new MessageImpl(
					response,
					RecoveryMessageTypes.BLOCK_SYNC_RESPONSE,
					messageSize
		);
	}
    
    private P2PNetworkEndpoint selectSyncPeer() {
    	P2PNetworkEndpoint syncPeer = _networkInterface.getNeighbors()
                .stream()
                .filter(peer -> !attemptedPeers.contains(peer))
                .findAny()
                .orElse(null);
        return syncPeer;
    }
    
    // This method creates a block locator.
    // The blocks are sorted from highest (tip) to lowest.
	// The first 10 blocks are included consecutively. After that, the step size doubles. 
	// The genesis block is always included as the last entry.
    private List<String> createBlockLocator() {
    	
    	List<String> blockLocator = new ArrayList<>();
        Block currentBlock = _context.getBlockchain().getPreferredTipOfLongestChains();
        int step = 1;

        while (currentBlock != null) {
            blockLocator.add(currentBlock.getHash());

            if (blockLocator.size() > 10) {
                step *= 2;
            }
            
         // Go backwards by 'step' blocks, skipping intermediate hashes.
         // If the genesis block is reached, add it to block locator
         // Stop if a predecessor block cannot be found.
            for (int i = 0; i < step; i++) {
                String previousHash = currentBlock.getPreviousHash();

                if (previousHash == null) {
                    String genesisHash = currentBlock.getHash();

                    if (!blockLocator.get(blockLocator.size() - 1).equals(genesisHash)) {
                        blockLocator.add(genesisHash);
                    }

                    return blockLocator;
                }

                currentBlock = _context.getBlockchain().getBlock(previousHash);

                if (currentBlock == null) {
                    return blockLocator;
                }
            }
        }
        
        return blockLocator;
    }
   
	
	private void handleSyncRequest(Message message, P2PNetworkEndpoint sender) {
		if (!(message.getContent() instanceof BlockSyncRequest request)) {
			System.err.println("Invalid BLOCK_SYNC_REQUEST content");
			return;
		}

		List<String> blockLocator = request.getBlockLocator();
		long recoveryGeneration = request.getRecoveryGeneration();
			 		
		
	    String commonAncestorHash = "";
	    Set<String> preferredChainHashes = _context.getBlockchain().getPreferredChainBlockHashes();
	    
	    // Find common ancestor hash
	    for (String hash : blockLocator) {

	        if (preferredChainHashes.contains(hash)) {
	            commonAncestorHash = hash;
	            break;
	        }
	    }
	    
	    if (commonAncestorHash.isEmpty()) {
	        System.err.println("No common ancestor found");
	        return;
	    }
	    
	    // Create list with all missing blocks and send this list to the recovering node
	    // Only up to 500 blocks will be transferred
	    List<Block> missingBlocks = new ArrayList<>();
	    Block currentBlock = _context.getBlockchain().getPreferredTipOfLongestChains();
	    
	    while(currentBlock != null && !currentBlock.getHash().equals(commonAncestorHash)) {
	    	
	    	missingBlocks.add(currentBlock);
	    	currentBlock = _context.getBlockchain().getBlock(currentBlock.getPreviousHash());
	    	
	    }
	    
	    // List is now sorted from lowest to highest
	    Collections.reverse(missingBlocks);
	    
	    List<String> missingBlockHashes = missingBlocks.stream()
				.limit(MAX_NUMBER_OF_BLOCKS)
				.map(Block::getHash)
				.toList();
	    
	    Message responseMessage = createSyncResponseMessage(missingBlockHashes, recoveryGeneration);
	    
	    _networkInterface.send(responseMessage, sender);
	}
	
	private void handleSyncResponse(Message message, P2PNetworkEndpoint sender) {
		
		if (!(message.getContent() instanceof BlockSyncResponse response)) {
			System.err.println("Invalid BLOCK_SYNC_RESPONSE content");
			return;
		}

		if (response.getRecoveryGeneration() != _recoveryGeneration) {
			return;
		}
		
		if (!sender.equals(syncPeer)) {
			return;
		}
		
		if (!waitingForSyncResponse) {
		    return;
		}
		
		waitingForSyncResponse = false;
		
		List<String> missingBlockHashes = response.getMissingBlockHashes(); 
		
		// Add all block hashes to the queue
	    for (String hash : missingBlockHashes) {

	        if (!_context.getBlockchain().hasBlockWithHash(hash)
	                && !pendingRequestQueue.contains(hash)
	                && !pendingBlockRequests.containsKey(hash)
	                && !awaitingValidations.contains(hash)) {

	            pendingRequestQueue.add(hash);
	        }
	    }
	    
	    // Up to 128 requests possible
	    // Takes the first element and requests the block through block propagation strategy
	    // Stores that hash in awaiting responses
	    int availableSlots = Math.max(
	            0,
	            MAX_NUMBER_OF_REQUESTS - pendingBlockRequests.size()
	    );
	    int numberOfRequests = Math.min(
	            availableSlots,
	            pendingRequestQueue.size()
	    );
	    for (int i = 0; i < numberOfRequests; i++) {
	        String hash = pendingRequestQueue.poll();
	        if (hash == null) {
	            break;
	        }
	        requestBlock(hash, sender, false);
	    }
	    
	    // If the sync peer's response is empty and checkSyncRoundCompleted() returns true
	    // -> End the recovery
	    // Otherwise start a new recovery round
	    if (missingBlockHashes.isEmpty() && checkSyncRoundCompleted()) {
	    	onRecoveryCompletedCallback.run();
	    } else if (checkSyncRoundCompleted()) {
	    	System.out.println(
	    		    "NEXT SYNC BATCH | missing=" + missingBlockHashes.size()
	    		    + " | pending=" + pendingRequestQueue.size()
	    		    + " | active=" + pendingBlockRequests.size()
	    		    + " | validating=" + awaitingValidations.size()
	    		);
	        requestNextSyncBatch();
	    }
		
	}
	
	private boolean checkSyncRoundCompleted() {
		return pendingRequestQueue.isEmpty() 
				&& awaitingValidations.isEmpty() 
				&& pendingBlockRequests.isEmpty();
	}
	
	private void requestBlock(String blockHash, P2PNetworkEndpoint peer, boolean retry) {
		if (blockHash == null) {
		    new Exception("NULL BLOCK HASH | retry=" + retry)
		        .printStackTrace();
		    return;
		}
		_blockAttemptId++;
		PendingBlockRequest pendingRequest = new PendingBlockRequest(_blockAttemptId, peer);
		pendingBlockRequests.put(blockHash, pendingRequest);
		
		RecoveryTimeoutRequest request = new RecoveryTimeoutRequest(RecoveryTimeoutType.BLOCK_RESPONSE, _blockAttemptId, blockHash);
		onRecoveryTimeoutRequestedCallback.accept(request);
		
		if (retry) {
	        _blockPropagationStrategy.retryElement(blockHash, peer);
	    } else {
	        _blockPropagationStrategy.requestElement(blockHash, peer);
	    }
	}
	
	@Override
	public void handleSyncResponseTimeout(long attemptId) {
	    if (attemptId != _syncAttemptId || !waitingForSyncResponse) {
	        return;
	    }

	    syncPeer = selectSyncPeer();
	    
	    // If all peers have been requested, request again
	    if (syncPeer == null) {
	        attemptedPeers.clear();
	        syncPeer = selectSyncPeer();
	    }
	    
	    // When there is no neighbors, end recovery
	    if (syncPeer == null) {
	        waitingForSyncResponse = false;
	        onRecoveryCompletedCallback.run();
	        return;
	    }
	    
	    sendSyncRequest(syncPeer);
	}
	
	
	@Override
	public void onP2PMessageReceived(Message message, P2PNetworkEndpoint sender) {
		
		switch(message.getContentType()) {
			case "BLOCK_SYNC_REQUEST" -> {
				handleSyncRequest(message, sender);
			}
			case "BLOCK_SYNC_RESPONSE" -> {
				handleSyncResponse(message, sender);
			}
		
		}
		
	}
	
	@Override
	public void onP2PMessageDropped(Message message, P2PNetworkEndpoint recipient) {
		// TODO 
	}

	@Override
	public void onBlockReceived(String blockHash) {
		if (pendingBlockRequests.remove(blockHash) != null) {
		    awaitingValidations.add(blockHash);

	        if (!pendingRequestQueue.isEmpty() && syncPeer != null) {

	            String hash = pendingRequestQueue.poll();

	            requestBlock(hash, syncPeer, false);
	        }
	    }
	}

	@Override
	public void onBlockValidated(String blockHash) {
		boolean blocksRemoved = awaitingValidations.removeIf(
	        hash -> _context.getBlockchain().hasBlockWithHash(hash)
	    );
		
		if (blocksRemoved && checkSyncRoundCompleted()) {
		    requestNextSyncBatch();
		}
	}

	@Override
	public void setOnRecoveryCompletedCallback(Runnable callback) {
		onRecoveryCompletedCallback = callback;
		
	}
	
	public void setOnRecoveryTimeoutRequestedCallback(Consumer<RecoveryTimeoutRequest> callback) {
	    this.onRecoveryTimeoutRequestedCallback = callback;
	}


	@Override
	public void handleBlockResponseTimeout(String blockHash, long attemptId) {
		PendingBlockRequest request = pendingBlockRequests.get(blockHash);

	    if (request == null) return;

	    if (request.getAttemptId() != attemptId) return;
	    
	    /*System.out.println(
	    	    "BLOCK RETRY | hash=" + blockHash
	    	    + " | attempt=" + attemptId
	    	);*/
	    
	    P2PNetworkEndpoint retryPeer = selectBlockRetryPeer(request.getPeer());

	    requestBlock(blockHash, retryPeer, true);
	}
		
	
	private P2PNetworkEndpoint selectBlockRetryPeer(P2PNetworkEndpoint previousPeer) {
	    return _networkInterface.getNeighbors().stream()
	            .filter(peer -> !peer.equals(previousPeer))
	            .findAny()
	            .orElse(previousPeer);
	}
}
