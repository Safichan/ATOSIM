package org.palladiosimulator.blockchainsystems.core.recovery;

import org.palladiosimulator.blockchainsystems.core.block.abstractions.Block;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.EventDispatchable;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetworkEndpoint;

public interface Synchronizable {
	void startSynchronization();
	void handleNeighborTimeout(P2PNetworkEndpoint neighbor);
	void setEventOrigin(EventDispatchable origin);
	void setOnSynchronizationCompleteCallback(Runnable callback);
	void onSyncBlockValidated(Block block);
}