package com.github.mmdemirbas.oncalls;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableNavigableMap;

/**
 * Represents an ordered line of {@link Event}s.
 * This is a generalization of timeline of events.
 * <p>
 * This class is immutable if the generic types {@link Point} and {@link Value} is immutable.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-18 09:55
 */
@ToString
@EqualsAndHashCode
public final class EventLine<Point extends Comparable<? super Point>, Value> {
    @Getter private final NavigableMap<Point, List<Event<Point, Value>>> intervalMap;

    public static <Point extends Comparable<? super Point>, Value> EventLine<Point, Value> of(Event<Point, Value>... events) {
        return new EventLine<>(asList(events));
    }

    public static <Point extends Comparable<? super Point>, Value> EventLine<Point, Value> of(Iterable<Event<Point, Value>> events) {
        return new EventLine<>(events);
    }

    private EventLine(Iterable<Event<Point, Value>> events) {
        intervalMap = unmodifiableNavigableMap(buildIntervalMap(events));
    }

    /**
     * Builds an interval map which can be considered as another form of an "interval tree".
     */
    private static <Point extends Comparable<? super Point>, Value> NavigableMap<Point, List<Event<Point, Value>>> buildIntervalMap(
            Iterable<Event<Point, Value>> events) {
        // firstly, detect change points and which changes occur at each point
        NavigableMap<Point, List<EventChange<Point, Value>>> changePoints = new TreeMap<>();
        events.forEach(event -> {
            Range<Point> range = event.getRange();
            changePoints.computeIfAbsent(range.getStartInclusive(), x -> new ArrayList<>())
                        .add(new EventChange<>(event, true));
            changePoints.computeIfAbsent(range.getEndExclusive(), x -> new ArrayList<>())
                        .add(new EventChange<>(event, false));
        });

        // then, build the interval map using a sweep line algorithm on the detected change points
        NavigableMap<Point, List<Event<Point, Value>>> intervalMap   = new TreeMap<>();
        List<Event<Point, Value>>                      ongoingEvents = new ArrayList<>();
        changePoints.forEach((point, changes) -> {
            changes.forEach(change -> {
                if (change.started) {
                    ongoingEvents.add(change.event);
                } else {
                    ongoingEvents.remove(change.event);
                }
            });
            intervalMap.put(point, unmodifiableList(new ArrayList<>(ongoingEvents)));
        });
        return intervalMap;
    }

    private static final class EventChange<Point extends Comparable<? super Point>, V> {
        final Event<Point, V> event;
        final boolean         started;

        EventChange(Event<Point, V> event, boolean started) {
            this.event = event;
            this.started = started;
        }
    }
}
