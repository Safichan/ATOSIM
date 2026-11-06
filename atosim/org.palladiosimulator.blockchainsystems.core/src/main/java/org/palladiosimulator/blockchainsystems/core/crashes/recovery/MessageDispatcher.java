package org.palladiosimulator.blockchainsystems.core.crashes.recovery;

import org.palladiosimulator.blockchainsystems.core.system.abstractions.Message;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.NodeP2PNetworkInterface;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetworkEndpoint;

// This class now handles the callback registration that was previously in GossipPropagationStrategy
// and also delegates messages to the PropagationStrategy or to the RecoveryStrategy 
// depending on the message type
public class MessageDispatcher {
	
	private NodeP2PNetworkInterface _networkInterface;
	private P2PMessageHandler _blockPropagationStrategy;
	private P2PMessageHandler _recoveryStrategy;
	
	public MessageDispatcher(NodeP2PNetworkInterface networkInterface) {
        _networkInterface = networkInterface;
    }
	
	public void onMessageReceived(Message message, P2PNetworkEndpoint sender) {

        switch (message.getContentType()) {
            case "BLOCK_INV", "BLOCK_GET_DATA", "BLOCK_MSG" -> {
            	if (_blockPropagationStrategy != null) {
            		_blockPropagationStrategy.onP2PMessageReceived(message, sender);
            	}
            }
            case RecoveryMessageTypes.BLOCK_SYNC_REQUEST, RecoveryMessageTypes.BLOCK_SYNC_RESPONSE -> {
            	if (_recoveryStrategy != null) {
                    _recoveryStrategy.onP2PMessageReceived(message, sender);
                }
            }
        }
    }

    public void onMessageDropped(Message message, P2PNetworkEndpoint recipient) {

        switch (message.getContentType()) {
            case "BLOCK_INV", "BLOCK_GET_DATA", "BLOCK_MSG" -> {
            	if (_blockPropagationStrategy != null) {
                    _blockPropagationStrategy.onP2PMessageDropped(message, recipient);
                }
            }
            case RecoveryMessageTypes.BLOCK_SYNC_REQUEST, RecoveryMessageTypes.BLOCK_SYNC_RESPONSE -> {
            	if (_recoveryStrategy != null) {
                    _recoveryStrategy.onP2PMessageDropped(message, recipient);
                }
            }
        }
    }

	public void setBlockPropagationStrategy(P2PMessageHandler blockPropagationStrategy) {
	    _blockPropagationStrategy = blockPropagationStrategy;
	}
	
	public void setRecoveryStrategy(P2PMessageHandler recoveryStrategy) {
	    _recoveryStrategy = recoveryStrategy;
	}


	public void setNetworkInterface(NodeP2PNetworkInterface networkInterface) {
	    if (_networkInterface != null) removeCurrentNetworkInterface();
	    setNewNetworkInterface(networkInterface);
	}
	
	private void removeCurrentNetworkInterface() {
	    _networkInterface.setOnMessageReceivedCallback(null);
	    _networkInterface.setOnMessageDroppedCallback(null);
	    _networkInterface = null;
	}
	
	private void setNewNetworkInterface(NodeP2PNetworkInterface networkInterface) {
	    _networkInterface = networkInterface;
	    _networkInterface.setOnMessageReceivedCallback(
	            (message, sender) -> this.onMessageReceived(message, sender));
	    _networkInterface.setOnMessageDroppedCallback(
	            (message, recipient) -> this.onMessageDropped(message, recipient));
	}
    
    
}
