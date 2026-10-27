package org.palladiosimulator.blockchainsystems.core.propagation;

import org.palladiosimulator.blockchainsystems.core.common.BlockchainNodeObject;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.Event;
import org.palladiosimulator.blockchainsystems.core.system.BlockchainSystemNodeState;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.BlockchainSystemNodeContext;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.Message;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.NodeP2PNetworkInterface;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetworkEndpoint;

import java.util.Set;
import java.util.function.Consumer;

public abstract class GossipPropagationStrategy<E extends Propagatable>
        extends BlockchainNodeObject implements PropagationStrategy<E> {

    protected NodeP2PNetworkInterface _networkInterface;
    protected Consumer<E> _onReceivedCallback;
    protected BlockchainSystemNodeContext _context;

    protected abstract String getInvMessageKey();
    protected abstract String getGetDataMessageKey();
    protected abstract String getElementMessageKey();

    protected abstract Message createInvMessage(E element);
    protected abstract void handleInvMessageReceived(Message message, P2PNetworkEndpoint senderNetworkEndpoint);

    protected abstract Message createGetDataMessage(String elementId);
    protected abstract void handleGetDataMessageReceived(Message message, P2PNetworkEndpoint senderNetworkEndpoint);

    protected abstract Message createElementMessage(E element);
    protected abstract void handleElementMessageReceived(Message message, P2PNetworkEndpoint senderNetworkEndpoint);

    protected abstract void handleMessageDropped(Message message, P2PNetworkEndpoint recipientNetworkEndpoint);
    

    @Override
    public void setBlockchainSystemNodeContext(BlockchainSystemNodeContext context) {
        _context = context;
    }

    @Override
    public void distribute(E element) {
        if (_networkInterface != null) _networkInterface.multicast(createInvMessage(element));
    }

    @Override
    public void distribute(E element, Set<P2PNetworkEndpoint> neighborEndpoints) {
        for (P2PNetworkEndpoint endpoint : neighborEndpoints) {
            if (_networkInterface != null) _networkInterface.send(createInvMessage(element), endpoint);
        }
    }

    private void onMessageReceivedFromNetworkInterface(Message message, P2PNetworkEndpoint senderNetworkEndpoint) {
    	if(_context == null ) return;
    	
    	BlockchainSystemNodeState _state = _context.getState();
    	
    	if(_state == BlockchainSystemNodeState.CRASHED) return;
    	
    	if (isSynchronizationMessage(message)) {
    		handleSynchronizationMessage(message, senderNetworkEndpoint);
            return;
        }
    	
    	if (_state == BlockchainSystemNodeState.RECOVERING) {
            handleMessageDuringRecovery(message, senderNetworkEndpoint);
            return;
        }
    	
    	handlePropagationMessage(message, senderNetworkEndpoint);
   
    }
    
    protected void handlePropagationMessage(Message message, P2PNetworkEndpoint senderNetworkEndpoint) {
    	String ct = message.getContentType();
        if (ct.equals(getInvMessageKey())) {
            handleInvMessageReceived(message, senderNetworkEndpoint);
        } else if (ct.equals(getGetDataMessageKey())) {
            handleGetDataMessageReceived(message, senderNetworkEndpoint);
        } else if (ct.equals(getElementMessageKey())) {
            handleElementMessageReceived(message, senderNetworkEndpoint);
        }
    }
    
    protected boolean isSynchronizationMessage(Message message) {
    	return false;
    }
    
    protected void handleSynchronizationMessage(Message message, P2PNetworkEndpoint senderNetworkEndpoint) {
       
    }

    protected void handleMessageDuringRecovery(Message message, P2PNetworkEndpoint senderNetworkEndpoint) {
    	
    }

    private void failedToSendMessageToNetworkInterface(Message message, P2PNetworkEndpoint recipientNetworkEndpoint) {
        handleMessageDropped(message, recipientNetworkEndpoint);
    }

    @Override
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
                (message, sender) -> this.onMessageReceivedFromNetworkInterface(message, sender));
        _networkInterface.setOnMessageDroppedCallback(
                (message, recipient) -> this.failedToSendMessageToNetworkInterface(message, recipient));
    }

    @Override
    public void setOnReceivedCallback(Consumer<E> onReceivedCallback) {
        _onReceivedCallback = onReceivedCallback;
    }

    @Override
    public void dispatchEvent(Event event) {
    }
}
