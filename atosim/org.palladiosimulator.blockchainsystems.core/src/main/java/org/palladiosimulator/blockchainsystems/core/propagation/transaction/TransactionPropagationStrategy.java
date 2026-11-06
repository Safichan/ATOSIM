package org.palladiosimulator.blockchainsystems.core.propagation.transaction;

import org.palladiosimulator.blockchainsystems.core.network.MessageDroppedTraceEvent;
import org.palladiosimulator.blockchainsystems.core.propagation.GossipPropagationStrategy;
import org.palladiosimulator.blockchainsystems.core.propagation.MessageImpl;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.Message;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetworkEndpoint;
import org.palladiosimulator.blockchainsystems.core.transaction.abstractions.Transaction;

import java.util.HashSet;
import java.util.Set;

public class TransactionPropagationStrategy extends GossipPropagationStrategy<Transaction> {

    private static final String INV_MESSAGE_KEY = "TRX_INV";
    private static final String GET_DATA_MESSAGE_KEY = "TRX_GET_DATA";
    private static final String ELEMENT_MESSAGE_KEY = "TRX_MSG";

    private static final int MESSAGE_HEADER_BYTE_SIZE = 24;
    private static final int INV_MESSAGE_BYTE_SIZE = 20;
    private static final int GET_DATA_MESSAGE_BYTE_SIZE = 20;

    private final Set<String> _requestedTransactionIds = new HashSet<>();

    @Override protected String getInvMessageKey() { return INV_MESSAGE_KEY; }
    @Override protected String getGetDataMessageKey() { return GET_DATA_MESSAGE_KEY; }
    @Override protected String getElementMessageKey() { return ELEMENT_MESSAGE_KEY; }

    @Override
    protected Message createInvMessage(Transaction element) {
        return new MessageImpl(element.getTxId(), INV_MESSAGE_KEY, MESSAGE_HEADER_BYTE_SIZE + INV_MESSAGE_BYTE_SIZE);
    }

    @Override
    protected Message createGetDataMessage(String elementId) {
        return new MessageImpl(elementId, GET_DATA_MESSAGE_KEY, MESSAGE_HEADER_BYTE_SIZE + GET_DATA_MESSAGE_BYTE_SIZE);
    }

    @Override
    protected Message createElementMessage(Transaction element) {
        return new MessageImpl(element, ELEMENT_MESSAGE_KEY, MESSAGE_HEADER_BYTE_SIZE + element.getSize());
    }

    @Override
    protected void handleMessageDropped(Message message, P2PNetworkEndpoint recipientNetworkEndpoint) {
        if (_networkInterface == null) {
            throw new IllegalStateException("Network interface is not set for TransactionPropagationStrategy.");
        }
        getTraceEventLogger().logEvent(new MessageDroppedTraceEvent(
                message,
                getSimulationContext().getSystemClock().getCurrentTime(),
                recipientNetworkEndpoint,
                _networkInterface
        ));
    }

    @Override
    protected void handleInvMessageReceived(Message message, P2PNetworkEndpoint senderNetworkEndpoint) {
        String txId = (String) message.getContent();
        if (_context == null || _context.getTrxMemPool() == null) return;

        Transaction trx = _context.getTrxMemPool().getTransactionById(txId);
        if (trx != null) {
            _requestedTransactionIds.remove(txId);
            return;
        }

        if (!_requestedTransactionIds.add(txId)) return;

        if (_networkInterface != null) {
            _networkInterface.send(createGetDataMessage(txId), senderNetworkEndpoint);
        }
    }

    @Override
    protected void handleGetDataMessageReceived(Message message, P2PNetworkEndpoint senderNetworkEndpoint) {
        String txId = (String) message.getContent();
        if (_context == null || _context.getTrxMemPool() == null) return;

        Transaction trx = _context.getTrxMemPool().getTransactionById(txId);
        if (trx != null && _networkInterface != null) {
            _networkInterface.send(createElementMessage(trx), senderNetworkEndpoint);
            logTrxSent(trx, senderNetworkEndpoint);
        }
    }

    @Override
    protected void handleElementMessageReceived(Message message, P2PNetworkEndpoint senderNetworkEndpoint) {
        Transaction trx = (Transaction) message.getContent();
        _requestedTransactionIds.remove(trx.getTxId());
        logTrxReceived(trx, senderNetworkEndpoint);
        notifyTrxReceived(trx);
    }

    private void notifyTrxReceived(Transaction transaction) {
        if (_onReceivedCallback != null) _onReceivedCallback.accept(transaction);
    }

    private void logTrxSent(Transaction trx, P2PNetworkEndpoint receiverNetworkEndpoint) {
        getTraceEventLogger().logEvent(new TransactionSentTraceEvent(
                getSimulationContext().getSystemClock().getCurrentTime(),
                trx, receiverNetworkEndpoint));
    }

    private void logTrxReceived(Transaction trx, P2PNetworkEndpoint senderNetworkEndpoint) {
        getTraceEventLogger().logEvent(new TransactionReceivedTraceEvent(
                getSimulationContext().getSystemClock().getCurrentTime(),
                trx, senderNetworkEndpoint));
    }
    
	@Override
	public void requestElement(String elementId, P2PNetworkEndpoint peer) {
		// TODO Auto-generated method stub
		
	}
}
