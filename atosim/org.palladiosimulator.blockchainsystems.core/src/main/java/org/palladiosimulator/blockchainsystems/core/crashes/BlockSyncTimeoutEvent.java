package org.palladiosimulator.blockchainsystems.core.crashes;

import org.palladiosimulator.blockchainsystems.core.common.abstractions.Event;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.EventDispatchable;
import org.palladiosimulator.blockchainsystems.core.system.abstractions.P2PNetworkEndpoint;

public class BlockSyncTimeoutEvent implements Event {

	public static final String EVENT_TYPE = "BlockSyncTimeoutEvent";

    private final long _occurrenceTime;
    private final EventDispatchable _origin;
    private final P2PNetworkEndpoint _neighbor;

    public BlockSyncTimeoutEvent(
            long occurrenceTime,
            EventDispatchable origin,
            P2PNetworkEndpoint neighbor) {
        _occurrenceTime = occurrenceTime;
        _origin = origin;
        _neighbor = neighbor;
    }

    @Override
    public long getOccurrenceTime() {
        return _occurrenceTime;
    }

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public EventDispatchable getOrigin() {
        return _origin;
    }

    @Override
    public String getValueFormatted() {
        return toString();
    }

    public P2PNetworkEndpoint getNeighbor() {
        return _neighbor;
    }
	
}