package org.palladiosimulator.blockchainsystems.core.crashes.recovery;

import org.palladiosimulator.blockchainsystems.core.system.abstractions.Message;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetworkEndpoint;

// This interface determines which classes can handle messages coming from the P2PNetwork
public interface P2PMessageHandler {
	void onP2PMessageReceived(Message message, P2PNetworkEndpoint sender);
	void onP2PMessageDropped(Message message, P2PNetworkEndpoint recipient);
}
