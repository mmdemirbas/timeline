package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

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
public final class ValuedRange<C extends Comparable<? super C>, V> {
    private final Range<C> range;
    private final V        value;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Builds an interval map which can be considered as another form of an "interval tree".
     */
    public static <C extends Comparable<? super C>, V> NavigableMap<C, List<V>> buildIntervalMap(List<ValuedRange<C, V>> intervals) {
        NavigableMap<C, List<V>> add           = indexBy(intervals, Range::getStartInclusive);
        NavigableMap<C, List<V>> remove        = indexBy(intervals, Range::getEndExclusive);
        NavigableMap<C, List<V>> intervalMap   = new TreeMap<>();
        List<V>                  ongoingEvents = new ArrayList<>();

        Set<C> sorted = new TreeSet<>(Comparator.comparing((Function<? super C, ? extends C>) it -> it));
        sorted.addAll(add.keySet());
        sorted.addAll(remove.keySet());

        sorted.forEach(point -> {
            ongoingEvents.addAll(orEmpty(add.get(point)));
            ongoingEvents.removeAll(orEmpty(remove.get(point)));
            intervalMap.put(point, unmodifiableList(new ArrayList<>(ongoingEvents)));
        });
        return unmodifiableNavigableMap(intervalMap);
    }

    private static <C extends Comparable<? super C>, V> NavigableMap<C, List<V>> indexBy(Iterable<ValuedRange<C, V>> intervals,
                                                                                         Function<? super Range<C>, C> fn) {
        NavigableMap<C, List<V>> index = new TreeMap<>();
        intervals.forEach(interval -> {
            Range<C> range = interval.getRange();
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
