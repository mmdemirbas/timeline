package com.github.mmdemirbas.oncalls;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.github.mmdemirbas.oncalls.Utils.buildIntervalMap;
import static com.github.mmdemirbas.oncalls.Utils.nextOrNull;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * Represents an ordered line of {@link Event}s which are {@link Range}-{@link Value} associations.
 * This is a generalization of timeline of events.
 * <p>
 * This class is immutable if the generic types {@link Point} and {@link Value} is immutable.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-18 09:55
 */
@ToString
@EqualsAndHashCode
public final class Timeline<Point extends Comparable<? super Point>, Value> {
    private final transient List<Event<Value, Point>>                      events;
    @Getter private final   NavigableMap<Point, List<Event<Value, Point>>> intervalMap;

    public static <Point extends Comparable<? super Point>, Value> Timeline<Point, Value> of(Event<Value, Point>... events) {
        return new Timeline<>(asList(events));
    }

    public static <Point extends Comparable<? super Point>, Value> Timeline<Point, Value> of(Collection<Event<Value, Point>> events) {
        return new Timeline<>(events);
    }

    private Timeline(Collection<Event<Value, Point>> events) {
        this.events = unmodifiableList(new ArrayList<>(events));
        intervalMap = buildIntervalMap(events, Event::getRange);
    }

    public Timeline<Point, Value> withPatches(Iterable<Timeline<Point, Patch<Value>>> patchTimelines) {
        Timeline<Point, Value> current = this;
        for (Timeline<Point, Patch<Value>> patch : patchTimelines) {
            current = withPatch(patch);
        }
        return current;
    }

    public Timeline<Point, Value> withPatch(Timeline<Point, ? extends Patch<Value>> patchTimeline) {
        return merge(patchTimeline, (values, patches) -> {
            List<Value> result = values;
            for (Patch<Value> patch : patches) {
                result = patch.patch(result);
            }
            return result;
        });
    }

    public <T, U> Timeline<Point, U> merge(Timeline<Point, T> patchTimeline,
                                           BiFunction<? super List<Value>, ? super List<T>, ? extends List<U>> merge) {
        Collection<Point> keyPoints = new TreeSet<>();
        keyPoints.addAll(intervalMap.keySet());
        keyPoints.addAll(patchTimeline.intervalMap.keySet());

        List<Event<U, Point>> mergedEvents = new ArrayList<>();
        Iterator<Point>       iterator     = keyPoints.iterator();
        Point                 current      = nextOrNull(iterator);
        Point                 next         = nextOrNull(iterator);

        List<U> bufferedValues = new ArrayList<>();
        Point   bufferedStart  = null;

        while (next != null) {
            List<Value> left   = getValues(current);
            List<T>     right  = patchTimeline.getValues(current);
            List<U>     merged = merge.apply(left, right);

            if (!bufferedValues.equals(merged)) {
                for (U u : bufferedValues) {
                    mergedEvents.add(new Event<>(u, Range.of(bufferedStart, current)));
                }
                bufferedValues = merged;
                bufferedStart = current;
            }

            current = next;
            next = nextOrNull(iterator);
        }
        for (U u : bufferedValues) {
            mergedEvents.add(new Event<>(u, Range.of(bufferedStart, current)));
        }
        return new Timeline<>(mergedEvents);
    }

    private List<Value> getValues(Point point) {
        Entry<Point, List<Event<Value, Point>>> entry = intervalMap.floorEntry(point);
        if (entry == null) {
            return emptyList();
        }

        List<Event<Value, Point>> events = entry.getValue();
        if ((events == null) || events.isEmpty()) {
            return emptyList();
        }
        return events.stream()
                     .map(Event::getValue)
                     .collect(Collectors.toList());
    }

    public List<Event<Value, Point>> findCurrent(Point point) {
        return Utils.getValueOrNull(intervalMap.floorEntry(point));
    }

    public List<Event<Value, Point>> findNext(Point point) {
        return Utils.getValueOrNull(intervalMap.higherEntry(point));
    }

    public Timeline<Point, Value> limitToRange(Range<Point> calculationRange) {
        Point start = calculationRange.getStartInclusive();
        Point end   = calculationRange.getEndExclusive();
        return new Timeline<>(events.stream()
                                    .map(event -> event.mapRange(range -> range.after(start)
                                                                               .before(end)))
                                    .collect(Collectors.toList()));
    }

    public interface Patch<V> {
        List<V> patch(List<V> existing);

        static <V> Patch<V> override(V value) {
            return ignored -> asList(value);
        }

        static <V> Patch<V> forward(V forwarder, V forwardedTo) {
            return existing -> existing.stream()
                                       .map(x -> x.equals(forwarder) ? forwardedTo : x)
                                       .collect(Collectors.toList());
        }
    }
}
