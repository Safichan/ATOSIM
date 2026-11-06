package org.palladiosimulator.blockchainsystems.core.crashes.events;

import org.palladiosimulator.blockchainsystems.core.common.abstractions.Event;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.EventDispatchable;

public class CrashEvent implements Event{
	
	public static final String EVENT_TYPE = "CrashEvent";
	
	private final long occurrenceTime;
	private final EventDispatchable target;
	private final long crashDuration;
	
	public CrashEvent(long occurrenceTime, EventDispatchable target, long crashDuration) {
		this.target = target;
		this.occurrenceTime = occurrenceTime;
		this.crashDuration = crashDuration;
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
	
	public long getCrashDuration() {
		return this.crashDuration;
	}

}
