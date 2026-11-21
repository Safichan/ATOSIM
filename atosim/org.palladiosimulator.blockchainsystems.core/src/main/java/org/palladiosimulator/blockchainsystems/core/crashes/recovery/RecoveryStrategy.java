package org.palladiosimulator.blockchainsystems.core.crashes.recovery;

import java.util.function.BiConsumer;

import org.palladiosimulator.blockchainsystems.core.crashes.events.RecoveryTimeoutType;

public interface RecoveryStrategy extends P2PMessageHandler {
	void startRecovery();
	void onBlockReceived(String blockId);
	void onBlockValidated(String blockId);
	void setOnRecoveryCompletedCallback(Runnable callback);
	void setOnRecoveryTimeoutRequestedCallback(BiConsumer<RecoveryTimeoutType, Long> callback);
	void handleSyncResponseTimeout(long attemptId);
}
