package org.palladiosimulator.blockchainsystems.core.crashes.recovery;

public interface RecoveryStrategy extends P2PMessageHandler {
	void startRecovery();
	void onBlockReceived(String blockId);
	void onBlockValidated(String blockId);
	void setOnRecoveryCompletedCallback(Runnable callback);
}
