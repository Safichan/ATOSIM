package org.palladiosimulator.blockchainsystems.core.crashes.recovery;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
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
	private List<String> awaitingResponses = new ArrayList<>();
	private Set<String> awaitingValidations = new HashSet<>();
	
	private P2PNetworkEndpoint syncPeer;
	private Runnable onRecoveryCompletedCallback;
	
	public BlocksFirstRecovery(NodeP2PNetworkInterface networkInterface, BlockchainSystemNodeContext context,
    		PropagationStrategy<Block> blockPropagationStrategy) {
        _networkInterface = networkInterface;
        _context = context;
        _blockPropagationStrategy = blockPropagationStrategy;
    } 
	
    
    @Override
    public void startRecovery() {
    	
    	syncPeer = selectSyncPeer();

        if (syncPeer == null) {
        	// TODO Handle when there is no available neighbor
        	// TODO Handle when neighbor does not respond
        	// TODO Handle when a message gets a timeout
            return;
        }
        
        List<String> blockLocator = createBlockLocator();
        
        Message syncRequest = createSyncRequestMessage(blockLocator);
        _networkInterface.send(syncRequest, syncPeer);
    }
    
    private void requestNextSyncBatch() {
	    if (syncPeer == null) {
	        return;
	    }

	    List<String> blockLocator = createBlockLocator();
	    Message syncRequest = createSyncRequestMessage(blockLocator);

	    _networkInterface.send(syncRequest, syncPeer);
	}
    
    private Message createSyncRequestMessage(List<String> blockLocator) {
        int messageSize = MESSAGE_HEADER_BYTE_SIZE
                + blockLocator.size() * BLOCK_HASH_BYTE_SIZE;

        return new MessageImpl(
                blockLocator,
                RecoveryMessageTypes.BLOCK_SYNC_REQUEST,
                messageSize
        );
    }
    
    private Message createSyncResponseMessage(List<String> missingBlockHashes) {
	    int messageSize = MESSAGE_HEADER_BYTE_SIZE
	            + missingBlockHashes.size() * BLOCK_HASH_BYTE_SIZE;

	    return new MessageImpl(
	            missingBlockHashes,
	            RecoveryMessageTypes.BLOCK_SYNC_RESPONSE,
	            messageSize
	    );
	}
    
    // Selects any sync peer
    private P2PNetworkEndpoint selectSyncPeer() {
        return _networkInterface.getNeighbors()
                .stream()
                .findAny()
                .orElse(null);
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
		List<String> blockLocator;
		
		// Check if message content is indeed a List of Strings extract the content
		if (message.getContent() instanceof List<?> list 
				&& list.stream().allMatch(String.class::isInstance)) {

			blockLocator = list.stream()
		            .map(String.class::cast)
		            .toList();
		} else {
			System.err.println("The block locator has to be a list of strings");
			return;
		}
		
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
	    
	    Message responseMessage = createSyncResponseMessage(missingBlockHashes);
	    
	    _networkInterface.send(responseMessage, sender);
	}
	
	private void handleSyncResponse(Message message, P2PNetworkEndpoint sender) {
		List<String> missingBlockHashes;
		
		// Check if message content is indeed a List of Strings extract the content
		if (message.getContent() instanceof List<?> list 
				&& list.stream().allMatch(String.class::isInstance)) {

			missingBlockHashes = list.stream()
		            .map(String.class::cast)
		            .toList();
		} else {
			System.err.println("Sync response has not a list of blocks");
			return;
		}
		
		// Add all block hashes to the queue
	    for (String hash : missingBlockHashes) {

	        if (!_context.getBlockchain().hasBlockWithHash(hash)
	                && !pendingRequestQueue.contains(hash)
	                && !awaitingResponses.contains(hash)
	                && !awaitingValidations.contains(hash)) {

	            pendingRequestQueue.add(hash);
	        }
	    }
	    
	    // Up to 128 requests possible
	    // Takes the first element and requests the block through block propagation strategy
	    // Stores that hash in awaiting responses
	    int availableSlots = Math.max(
	            0,
	            MAX_NUMBER_OF_REQUESTS - awaitingResponses.size()
	    );
	    int numberOfRequests = Math.min(
	            availableSlots,
	            pendingRequestQueue.size()
	    );
	    for (int i = 0; i < numberOfRequests; i++) {
	        String hash = pendingRequestQueue.poll();
	        _blockPropagationStrategy.requestElement(hash, sender);

	        awaitingResponses.add(hash);
	    }
	    
	    // If the sync peer's response is empty and checkSyncRoundCompleted() returns true
	    // -> End the recovery
	    // Otherwise start a new recovery round
	    if (missingBlockHashes.isEmpty() && checkSyncRoundCompleted()) {
	    	onRecoveryCompletedCallback.run();
	    } else if (checkSyncRoundCompleted()) {
	        requestNextSyncBatch();
	    }
		
	}
	
	private boolean checkSyncRoundCompleted() {
		return pendingRequestQueue.isEmpty() 
				&& awaitingValidations.isEmpty() 
				&& awaitingResponses.isEmpty();
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
		if (awaitingResponses.contains(blockHash)) {
	        awaitingResponses.remove(blockHash);
	        awaitingValidations.add(blockHash);

	        if (!pendingRequestQueue.isEmpty() && syncPeer != null) {

	            String hash = pendingRequestQueue.poll();

	            _blockPropagationStrategy.requestElement(hash, syncPeer);
	            awaitingResponses.add(hash);
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
}
