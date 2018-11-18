package com.github.mmdemirbas.oncalls;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import static java.util.Collections.unmodifiableNavigableMap;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-18 09:55
 */
@ToString
@EqualsAndHashCode
public final class Timeline<Point extends Comparable<? super Point>, V> {
    @Getter private final NavigableMap<Point, List<Event<Point, V>>> intervalMap;

    public Timeline(Iterable<Event<Point, V>> events) {
        intervalMap = indexIntervals(events);
    }

    private NavigableMap<Point, List<Event<Point, V>>> indexIntervals(Iterable<Event<Point, V>> events) {
        NavigableMap<Point, List<Event<Point, V>>> index         = new TreeMap<>();
        List<Event<Point, V>>                      currentEvents = new ArrayList<>();
        indexChangePoints(events).forEach((point, changes) -> {
            changes.forEach(change -> {
                if (change.started) {
                    currentEvents.add(change.event);
                } else {
                    currentEvents.remove(change.event);
                }
            });
            index.put(point, Collections.unmodifiableList(new ArrayList<>(currentEvents)));
        });
        return unmodifiableNavigableMap(index);
    }

    private NavigableMap<Point, List<EventChange<Point, V>>> indexChangePoints(Iterable<Event<Point, V>> events) {
        NavigableMap<Point, List<EventChange<Point, V>>> changePoints = new TreeMap<>();
        events.forEach(event -> {
            Range<Point> range = event.getRange();
            changePoints.computeIfAbsent(range.getStartInclusive(), x -> new ArrayList<>())
                        .add(new EventChange<>(event, true));
            changePoints.computeIfAbsent(range.getEndExclusive(), x -> new ArrayList<>())
                        .add(new EventChange<>(event, false));
        });
        return changePoints;
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
