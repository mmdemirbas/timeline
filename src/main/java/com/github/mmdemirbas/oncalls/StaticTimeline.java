package com.github.mmdemirbas.oncalls;

import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import static java.util.Collections.emptyList;

/**
 * A {@link Timeline} implementation which statically associates {@link Range}s with values of type {@link V}.
 * <p>
 * This class is immutable if the generic types {@link C} and {@link V} are immutable.
 */
public final class StaticTimeline<C extends Comparable<? super C>, V> implements Timeline<C, V>, TimelineSegment<C, V> {

    private final NavigableMap<C, List<V>> intervalMap;

    public static <C extends Comparable<? super C>, V> StaticTimeline<C, V> ofIntervals(Iterable<? extends Interval<? extends C, ? extends V>> intervals) {
        return new StaticTimeline<>(Interval.buildIntervalMap(intervals));
    }

    private StaticTimeline(NavigableMap<C, List<V>> intervalMap) {
        this.intervalMap = intervalMap;
    }

    @Override
    public TimelineSegment<C, V> toTimelineSegment(Range<? extends C> calculationRange) {
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
    public TimelineSegment<C, V> newSegment(List<Interval<C, V>> intervals) {
        return ofIntervals(intervals);
    }

    @Override
    public Set<C> getKeyPoints() {
        return intervalMap.keySet();
    }

    @Override
    public List<V> findCurrentValues(C point) {
        return findCurrentInterval(point).getValue();
    }

    @Override
    public Interval<C, List<V>> findCurrentInterval(C point) {
        return getValuesOrEmpty(intervalMap.floorEntry(point));
    }

    @Override
    public Interval<C, List<V>> findNextInterval(C point) {
        return getValuesOrEmpty(intervalMap.higherEntry(point));
    }

    private Interval<C, List<V>> getValuesOrEmpty(Entry<C, List<V>> entry) {
        if (entry != null) {
            C key     = entry.getKey();
            C nextKey = intervalMap.higherKey(key);
            if (nextKey != null) {
                return new Interval<>(Range.of(key, nextKey), entry.getValue());
            }
        }
        return new Interval(null, emptyList());
    }

    @Override
    public NavigableMap<C, List<V>> toIntervalMap() {
        return intervalMap;
    }
}
