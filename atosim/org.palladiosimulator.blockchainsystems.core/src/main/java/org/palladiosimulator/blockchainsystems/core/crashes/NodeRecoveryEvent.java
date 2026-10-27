package org.palladiosimulator.blockchainsystems.core.crashes;

import org.palladiosimulator.blockchainsystems.core.common.abstractions.Event;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.EventDispatchable;

public class NodeRecoveryEvent implements Event {
	
	public static final String EVENT_TYPE = "NodeRecoveryEvent";
	
	private final long occurrenceTime;
	private final EventDispatchable target;

	public NodeRecoveryEvent(long occurrenceTime, EventDispatchable target) {
		this.target = target;
		this.occurrenceTime = occurrenceTime;
	}

	@Override
	public long getOccurrenceTime() {
		return this.occurrenceTime;
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}

	@Override
	public EventDispatchable getOrigin() {
		return this.target;
	}

	@Override
	public String getValueFormatted() {
		return this.toString();
	}
	
	
}