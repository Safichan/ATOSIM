package org.palladiosimulator.blockchainsystems.core.propagation.block;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.network.MessageDroppedTraceEvent;
import org.palladiosimulator.blockchainsystems.core.propagation.GossipPropagationStrategy;
import org.palladiosimulator.blockchainsystems.core.propagation.MessageImpl;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.Message;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetworkEndpoint;

import java.util.HashSet;
import java.util.Set;

public class BlockPropagationStrategy extends GossipPropagationStrategy<Block> {

    private static final String INV_MESSAGE_KEY = "BLOCK_INV";
    private static final String GET_DATA_MESSAGE_KEY = "BLOCK_GET_DATA";
    private static final String ELEMENT_MESSAGE_KEY = "BLOCK_MSG";

    private static final int MESSAGE_HEADER_BYTE_SIZE = 24;
    private static final int INV_MESSAGE_BYTE_SIZE = 20;
    private static final int GET_DATA_MESSAGE_BYTE_SIZE = 20;

    private final Set<String> _requestedBlockIds = new HashSet<>();
    
    @Override
    public void requestElement(String blockHash, P2PNetworkEndpoint syncPeer) {
    	if(!_requestedBlockIds.contains(blockHash) && !_context.getBlockchain().hasBlockWithHash(blockHash)) {
    		_requestedBlockIds.add(blockHash);
    		Message message = createGetDataMessage(blockHash);
    		_networkInterface.send(message, syncPeer);
    	}
    }

    @Override protected String getInvMessageKey() { return INV_MESSAGE_KEY; }
    @Override protected String getGetDataMessageKey() { return GET_DATA_MESSAGE_KEY; }
    @Override protected String getElementMessageKey() { return ELEMENT_MESSAGE_KEY; }

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

        if (_context.getBlockchain().hasBlockWithHash(hash) ||
                _context.getOrphanBlockPool().hasBlockWithHash(hash)) {
            _requestedBlockIds.remove(hash);
            return;
        }

        if (!_requestedBlockIds.add(hash)) return;

        if (_networkInterface != null) {
            _networkInterface.send(createGetDataMessage(hash), senderNetworkEndpoint);
        }
    }

    @Override
    protected void handleGetDataMessageReceived(Message message, P2PNetworkEndpoint senderNetworkEndpoint) {
        String hash = (String) message.getContent();
        if (_context == null) return;
        Block block = _context.getBlockchain().getBlock(hash);
        if (block != null && _networkInterface != null) {
            _networkInterface.send(createElementMessage(block), senderNetworkEndpoint);
            logBlockSent(block, senderNetworkEndpoint);
        }
    }

    @Override
    protected void handleElementMessageReceived(Message message, P2PNetworkEndpoint senderNetworkEndpoint) {
        Block block = (Block) message.getContent();
        if (_context == null) return;

        _requestedBlockIds.remove(block.getHash());

        if (_context.getBlockchain().hasBlockWithHash(block.getHash()) ||
                _context.getOrphanBlockPool().hasBlockWithHash(block.getHash())) {
            return;
        }

        String previousHash = block.getPreviousHash();
        if (previousHash != null &&
                !_context.getBlockchain().hasBlockWithHash(previousHash) &&
                !_context.getOrphanBlockPool().hasBlockWithHash(previousHash) &&
                _requestedBlockIds.add(previousHash)) {
            if (_networkInterface != null) {
                _networkInterface.send(createGetDataMessage(previousHash), senderNetworkEndpoint);
            }
        }

        logBlockReceived(block, senderNetworkEndpoint);
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
}
