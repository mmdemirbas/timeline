package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
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

    public static <C extends Comparable<? super C>, V> ValuedRange<C, V> of(Range<C> range, V value) {
        return new ValuedRange<>(range, value);
    }

    private ValuedRange(Range<C> range, V value) {
        this.range = range;
        this.value = value;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static <C extends Comparable<? super C>, V> List<ValuedRange<C, V>> toDisjointIntervals(Collection<ValuedRange<C, V>> intervals) {
        return Range.toDisjointRanges(intervals,
                                      (ValuedRange<C, V> interval) -> interval.getRange(),
                                      (current, joining) -> Objects.equals(current.value, joining.value),
                                      (range, joining) -> of(range, joining.value));
    }

    /**
     * Builds an interval map which can be considered as another form of an "interval tree".
     */
    public static <C extends Comparable<? super C>, V> NavigableMap<C, List<V>> buildIntervalMap(Collection<ValuedRange<C, V>> intervals) {
        List<ValuedRange<C, V>> disjointIntervals = toDisjointIntervals(intervals);

        NavigableMap<C, List<V>> add    = index(disjointIntervals, Range::getStartInclusive);
        NavigableMap<C, List<V>> remove = index(disjointIntervals, Range::getEndExclusive);

        Set<C> sorted = new TreeSet<>();
        sorted.addAll(add.keySet());
        sorted.addAll(remove.keySet());

        NavigableMap<C, List<V>> intervalMap   = new TreeMap<>();
        List<V>                  ongoingEvents = new ArrayList<>();

        sorted.forEach(point -> {
            ongoingEvents.addAll(orEmpty(add.get(point)));
            ongoingEvents.removeAll(orEmpty(remove.get(point)));
            intervalMap.put(point, unmodifiableList(new ArrayList<>(ongoingEvents)));
        });
        return unmodifiableNavigableMap(intervalMap);
    }

    private static <C extends Comparable<? super C>, V> NavigableMap<C, List<V>> index(Iterable<ValuedRange<C, V>> items,
                                                                                       Function<Range<C>, C> keyExtractor) {
        NavigableMap<C, List<V>> index = new TreeMap<>();
        items.forEach(item -> {
            Range<C> range = item.range;
            if (!range.isEmpty()) {
                C key = keyExtractor.apply(range);
                index.computeIfAbsent(key, x -> new ArrayList<>()).add(item.value);
            }
        });
        return index;
    }

    private static <V> List<V> orEmpty(List<V> list) {
        return (list == null) ? emptyList() : list;
    }
}
