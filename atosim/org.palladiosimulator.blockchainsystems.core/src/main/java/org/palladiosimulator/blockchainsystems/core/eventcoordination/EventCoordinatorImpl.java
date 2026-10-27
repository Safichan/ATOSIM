package org.palladiosimulator.blockchainsystems.core.eventcoordination;

import org.palladiosimulator.blockchainsystems.core.common.abstractions.Event;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.EventCoordinator;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.EventDispatchable;
import org.palladiosimulator.blockchainsystems.core.common.abstractions.SystemClockControl;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.TreeMap;

public class EventCoordinatorImpl implements EventCoordinator {

    private final SystemClockControl _clock;
    private final TerminationCondition _terminationCondition;
    // Timetstamp and list of events
    private final TreeMap<Long, EffectsTimeSlice> _eventTimeSlices = new TreeMap<>();
    private final HashMap<EventDispatchable, LinkedHashSet<Event>> _eventsPerOrigin = new HashMap<>();

    public EventCoordinatorImpl(SystemClockControl clock, TerminationCondition terminationCondition) {
        _clock = clock;
        _terminationCondition = terminationCondition;
    }

    public void processEvents() {
        while (hasUnprocessedEvents() && !_terminationCondition.shouldTerminate()) {
            processCurrentSlice();
            purgeProcessedSlices();
            advanceCurrentTime();
        }
    }

    private boolean hasUnprocessedEvents() {
        return !_eventTimeSlices.isEmpty();
    }

    private void purgeProcessedSlices() {
        if (_eventTimeSlices.isEmpty()) return;

        Long firstTimestamp = _eventTimeSlices.firstKey();
        while (firstTimestamp != null && firstTimestamp <= _clock.getCurrentTime()) {
            removeEventsAt(firstTimestamp);
            if (_eventTimeSlices.isEmpty()) break;
            firstTimestamp = _eventTimeSlices.firstKey();
        }
    }

    private void removeEventsAt(long timestamp) {
        EffectsTimeSlice eventTimeSlice = _eventTimeSlices.get(timestamp);
        if (eventTimeSlice != null) {
            for (Event event : eventTimeSlice.getEvents()) {
                LinkedHashSet<Event> events = _eventsPerOrigin.get(event.getOrigin());
                if (events != null) events.remove(event);
            }
            _eventTimeSlices.remove(_clock.getCurrentTime());
        }
    }

    private void processCurrentSlice() {
        long eventOccurrenceTime = _clock.getCurrentTime();
        EffectsTimeSlice currentSlice = _eventTimeSlices.get(eventOccurrenceTime);
        if (currentSlice == null) return;

        for (Event event : currentSlice.getEvents()) {
            dispatchEvent(event);
        }
    }

    private void advanceCurrentTime() {
        if (_eventTimeSlices.isEmpty()) return;
        long nextEventTime = _eventTimeSlices.firstKey();
        _clock.progressClockTo(nextEventTime);
    }

    @Override
    public void raiseEvent(Event event) {
        if (event.getOccurrenceTime() > _clock.getCurrentTime()) {
            scheduleEvent(event);
        } else if (event.getOccurrenceTime() == _clock.getCurrentTime()) {
            dispatchEvent(event);
        }
    }

    private void scheduleEvent(Event event) {
        long eventOccurrenceTime = event.getOccurrenceTime();
        EffectsTimeSlice timeSlice = _eventTimeSlices.computeIfAbsent(
                eventOccurrenceTime, EffectsTimeSlice::new);
        timeSlice.addEvent(event);

        _eventsPerOrigin.computeIfAbsent(event.getOrigin(), k -> new LinkedHashSet<>())
                .add(event);
    }

    @Override
    public void cancelEventsFor(EventDispatchable eventOrigin) {
        LinkedHashSet<Event> events = _eventsPerOrigin.get(eventOrigin);
        if (events != null && !events.isEmpty()) {
            for (Event event : events) {
                if (event.getOccurrenceTime() <= _clock.getCurrentTime()) continue;
                EffectsTimeSlice slice = _eventTimeSlices.get(event.getOccurrenceTime());
                if (slice != null) slice.removeEvent(event);
            }
            events.clear();
        }
    }

    private void dispatchEvent(Event event) {
        event.getOrigin().dispatchEvent(event);
    }
}
