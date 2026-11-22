package org.palladiosimulator.blockchainsystems.core.crashes.recovery;

import org.palladiosimulator.blockchainsystems.core.crashes.events.RecoveryTimeoutType;

public class RecoveryTimeoutRequest {
	
	private final RecoveryTimeoutType timeoutType;
	private final long attemptId;
	private final String blockHash;
	
	public RecoveryTimeoutRequest(RecoveryTimeoutType timeoutType, 
			long attemptId, String blockHash) {
		this.timeoutType = timeoutType;
		this.attemptId = attemptId;
		this.blockHash = blockHash;
	}

	public RecoveryTimeoutType getTimeoutType() {
	    return timeoutType;
	}

	public long getAttemptId() {
	    return attemptId;
	}

	public String getBlockHash() {
	    return blockHash;
	}
}
