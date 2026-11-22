package org.palladiosimulator.blockchainsystems.core.crashes.recovery;
import java.util.function.Consumer;

public interface RecoveryStrategy extends P2PMessageHandler {
	void startRecovery();
	void onBlockReceived(String blockId);
	void onBlockValidated(String blockId);
	void setOnRecoveryCompletedCallback(Runnable callback);
	void setOnRecoveryTimeoutRequestedCallback(Consumer<RecoveryTimeoutRequest> callback);
	void handleSyncResponseTimeout(long attemptId);
	void handleBlockResponseTimeout(String blockHash, long attemptId);
}
