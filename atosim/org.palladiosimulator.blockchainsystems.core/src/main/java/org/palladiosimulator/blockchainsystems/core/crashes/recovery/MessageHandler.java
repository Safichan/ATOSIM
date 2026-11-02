package org.palladiosimulator.blockchainsystems.core.crashes.recovery;

import org.palladiosimulator.blockchainsystems.core.system.abstractions.Message;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetworkEndpoint;

public interface MessageHandler {
	
	void handleMessage(Message message, P2PNetworkEndpoint sender);
	
	void handleDroppedMessage(Message message, P2PNetworkEndpoint recipient);

}
