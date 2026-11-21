package org.palladiosimulator.blockchainsystems.core.crashes.events;

import org.palladiosimulator.blockchainsystems.core.common.abstractions.Event;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.EventDispatchable;

public class RecoveryTimeoutEvent implements Event {

    public static final String EVENT_TYPE = "RecoveryTimeoutEvent";

    private final long occurrenceTime;
    private final EventDispatchable target;
    
    private final RecoveryTimeoutType timeoutType;
    private final long attemptId;
    
    private final String blockHash;

    public RecoveryTimeoutEvent(EventDispatchable target, long occurrenceTime, 
    		RecoveryTimeoutType timeoutType, long attemptId) {
    	this(target, occurrenceTime, timeoutType, attemptId, null);
    }
    
    public RecoveryTimeoutEvent(EventDispatchable target, long occurrenceTime, 
    		RecoveryTimeoutType timeoutType, long attemptId, String blockHash) {
        this.occurrenceTime = occurrenceTime;
        this.target = target;
        this.timeoutType = timeoutType;
        this.attemptId = attemptId;
        this.blockHash = blockHash;
    }

    @Override
    public long getOccurrenceTime() {
        return occurrenceTime;
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public EventDispatchable getOrigin() {
        return target;
    }

    @Override
    public String getValueFormatted() {
        return toString();
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