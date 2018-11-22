package com.github.mmdemirbas.oncalls;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.BiFunction;

import static com.github.mmdemirbas.oncalls.Utils.reduce;
import static com.github.mmdemirbas.oncalls.Utils.sorted;
import static java.util.Collections.emptyList;

/**
 * A {@link Timeline} implementation which statically associates {@link Range}s with values of type {@link V}.
 * <p>
 * This class is immutable if the generic types {@link C} and {@link V} are immutable.
 */
public final class StaticTimelineImp<C extends Comparable<? super C>, V> implements StaticTimeline<C, V> {
    private static final Interval       EMPTYLIST_INTERVAL = new Interval(null, emptyList());
    private static final StaticTimeline EMPTY_TIMELINE     = new StaticTimelineImp(emptyList());

    @Getter private final NavigableMap<C, List<V>> intervalMap;

    public static <C extends Comparable<? super C>, V> StaticTimeline<C, V> emptyTimeline() {
        return EMPTY_TIMELINE;
    }

    public StaticTimelineImp(Iterable<Interval<C, V>> intervals) {
        this(Interval.buildIntervalMap(intervals));
    }

    private StaticTimelineImp(NavigableMap<C, List<V>> intervalMap) {
        this.intervalMap = intervalMap;
    }

    @Override
    public StaticTimeline<C, V> toStaticTimeline(Range<? extends C> calculationRange) {
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

        return new StaticTimelineImp<>(map);
    }

    @Override
    public <A> StaticTimelineImp<C, V> combine(Iterable<? extends Timeline<C, A>> timelines,
                                               Range<? extends C> calculationRange,
                                               BiFunction<List<V>, List<A>, List<V>> mergeFunction) {
        return reduce(this, timelines, (result, timeline) -> {
            StaticTimeline<C, A> restricted = timeline.toStaticTimeline(calculationRange);
            List<Interval<C, V>> intervals  = new ArrayList<>();
            List<V>              values     = emptyList();
            C                    start      = null;
            C                    end        = null;

            for (C point : sorted(result.getIntervalMap()
                                        .keySet(),
                                  restricted.getIntervalMap()
                                            .keySet())) {
                end = point;
                List<V> mergedValues = mergeFunction.apply(result.findCurrentValues(point),
                                                           restricted.findCurrentValues(point));
                if (!values.equals(mergedValues)) {
                    if (!values.isEmpty()) {
                        Range<C> range = Range.of(start, end);
                        values.forEach(value -> intervals.add(new Interval<>(range, value)));
                    }
                    values = mergedValues;
                    start = end;
                }
            }

            if (!values.isEmpty()) {
                Range<C> range = Range.of(start, end);
                values.forEach(value -> intervals.add(new Interval<>(range, value)));
            }
            return new StaticTimelineImp<>(intervals);
        });
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
        return EMPTYLIST_INTERVAL;
    }
}
