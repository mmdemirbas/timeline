package com.github.mmdemirbas.oncalls;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableNavigableMap;
import static java.util.Objects.requireNonNull;

/**
 * A {@link Timeline} implementation which statically associates {@link Range}s with values of type {@link V}.
 * <p>
 * This class is immutable if the generic types {@link C} and {@link V} are immutable.
 */
public final class StaticTimeline<C extends Comparable<? super C>, V> implements Timeline<C, V>, TimelineSegment<C, V> {
    private final NavigableMap<C, List<V>> intervalMap;

    public static <C extends Comparable<? super C>, V> StaticTimeline<C, V> ofIntervals(Collection<ValuedRange<C, V>> intervals) {
        return new StaticTimeline<>(ValuedRange.buildIntervalMap(intervals));
    }

    private StaticTimeline(NavigableMap<C, List<V>> intervalMap) {
        // todo: ensure inner lists also immutable -- may be extracted to a unmodifiableCopyOf() utility method
        this.intervalMap = unmodifiableNavigableMap(requireNonNull(intervalMap, "intervalMap"));
    }

    @Override
    public TimelineSegment<C, V> toSegment(Range<C> calculationRange) {
        requireNonNull(calculationRange, "calculationRange");

        C                        start = calculationRange.getStartInclusive();
        C                        end   = calculationRange.getEndExclusive();
        NavigableMap<C, List<V>> map   = new TreeMap<>(intervalMap.subMap(start, end));

        Entry<C, List<V>> startEntry = intervalMap.floorEntry(start);
        List<V>           startValue = (startEntry == null) ? emptyList() : startEntry.getValue();
        if (!startValue.isEmpty()) {
            map.put(start, startValue);
        }

        Entry<C, List<V>> endEntry = intervalMap.lowerEntry(end);
        List<V>           endValue = (endEntry == null) ? emptyList() : endEntry.getValue();
        if (!endValue.isEmpty()) {
            map.put(end, emptyList());
        }

        return new StaticTimeline<>(map);
    }

    @Override
    public TimelineSegment<C, V> newSegment(List<ValuedRange<C, V>> valuedRanges) {
        return ofIntervals(valuedRanges);
    }

    @Override
    public Set<C> getKeyPoints() {
        return intervalMap.keySet();
    }

    @Override
    public List<V> findCurrentValues(C point) {
        ValuedRange<C, List<V>> interval = findCurrentInterval(point);
        return (interval == null) ? null : interval.getValue();
    }

    @Override
    public ValuedRange<C, List<V>> findCurrentInterval(C point) {
        return getValuesOrNull(intervalMap.floorEntry(point));
    }

    @Override
    public ValuedRange<C, List<V>> findNextInterval(C point) {
        return getValuesOrNull(intervalMap.higherEntry(point));
    }

    private ValuedRange<C, List<V>> getValuesOrNull(Entry<C, List<V>> entry) {
        if (entry != null) {
            C key     = entry.getKey();
            C nextKey = intervalMap.higherKey(key);
            if (nextKey != null) {
                return ValuedRange.of(Range.of(key, nextKey), entry.getValue());
            }
        }
        return null;
    }

    @Override
    public NavigableMap<C, List<V>> toIntervalMap() {
        return intervalMap;
    }
}
