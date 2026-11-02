package org.palladiosimulator.blockchainsystems.core.crashes.recovery;

public interface RecoveryStrategy {
	
	void startRecovery();
	
	void onBlockReceived();
	
	boolean isRecoveryCompleted();

}
