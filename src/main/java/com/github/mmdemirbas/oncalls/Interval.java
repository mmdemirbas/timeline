package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;

import static com.github.mmdemirbas.oncalls.Utils.sorted;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableNavigableMap;

/**
 * Represents an association between a {@link Range} and an arbitrary value.
 * <p>
 * This class is immutable if the generic types {@link C} and {@link V} is immutable.
 *
 * @param <C> type of the {@link Comparable} values used as time points
 * @param <V> type of the value associated by the time range
 */
@Value
public final class Interval<C extends Comparable<? super C>, V> {
    private final Range<C> range;
    private final V        value;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Builds an interval map which can be considered as another form of an "interval tree".
     */
    public static <C extends Comparable<? super C>, V> NavigableMap<C, List<V>> buildIntervalMap(Iterable<? extends Interval<? extends C, ? extends V>> intervals) {
        NavigableMap<C, List<V>> add           = indexBy(intervals, Range::getStartInclusive);
        NavigableMap<C, List<V>> remove        = indexBy(intervals, Range::getEndExclusive);
        NavigableMap<C, List<V>> intervalMap   = new TreeMap<>();
        List<V>                  ongoingEvents = new ArrayList<>();

        sorted(add.keySet(), remove.keySet()).forEach(point -> {
            ongoingEvents.addAll(orEmpty(add.get(point)));
            ongoingEvents.removeAll(orEmpty(remove.get(point)));
            intervalMap.put(point, unmodifiableList(new ArrayList<>(ongoingEvents)));
        });
        return unmodifiableNavigableMap(intervalMap);
    }

    private static <C extends Comparable<? super C>, V> NavigableMap<C, List<V>> indexBy(Iterable<? extends Interval<? extends C, ? extends V>> intervals,
                                                                                         Function<? super Range<? extends C>, ? extends C> fn) {
        NavigableMap<C, List<V>> index = new TreeMap<>();
        intervals.forEach(interval -> {
            Range<? extends C> range = interval.getRange();
            if (!range.isEmpty()) {
                C       key    = fn.apply(range);
                List<V> values = index.computeIfAbsent(key, x -> new ArrayList<>());
                values.add(interval.getValue());
            }
        });
        return index;
    }

    private static <V> List<V> orEmpty(List<V> input) {
        return (input == null) ? emptyList() : input;
    }
}
