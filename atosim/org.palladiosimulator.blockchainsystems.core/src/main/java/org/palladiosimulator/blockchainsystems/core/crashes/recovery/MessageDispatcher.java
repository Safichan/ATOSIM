package org.palladiosimulator.blockchainsystems.core.crashes.recovery;

import org.palladiosimulator.blockchainsystems.core.system.abstractions.Message;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.NodeP2PNetworkInterface;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetworkEndpoint;

public class MessageDispatcher {
	
	private final NodeP2PNetworkInterface _networkInterface;

	private MessageHandler _blockHandler;
	
	public MessageDispatcher(NodeP2PNetworkInterface networkInterface) {
        _networkInterface = networkInterface;

        _networkInterface.setOnMessageReceivedCallback(
                this::onMessageReceived
        );

        _networkInterface.setOnMessageDroppedCallback(
                this::onMessageDropped
        );
    }

	public void setBlockHandler(MessageHandler handler) {
	    _blockHandler = handler;
	}

    public void onMessageReceived(Message message, P2PNetworkEndpoint sender) {

        switch (message.getContentType()) {
            case "BLOCK_INV", "BLOCK_GET_DATA", "BLOCK_MSG" -> {
            	if (_blockHandler != null) {
            		_blockHandler.handleMessage(message, sender);
            	}
            }

            case "BLOCK_SYNC_REQUEST", "BLOCK_SYNC_RESPONSE" -> {
            	// TODO Recovery
            }
        }
    }

    public void onMessageDropped(Message message, P2PNetworkEndpoint recipient) {

        switch (message.getContentType()) {
            case "BLOCK_INV", "BLOCK_GET_DATA", "BLOCK_MSG" -> {
            	if (_blockHandler != null) {
                    _blockHandler.handleDroppedMessage(message, recipient);
                }
            }
                    

            case "BLOCK_SYNC_REQUEST", "BLOCK_SYNC_RESPONSE" -> {
                // TODO Recovery
            }
        }
    }
}
