package com.github.mmdemirbas.oncalls;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.function.BiFunction;

import static com.github.mmdemirbas.oncalls.Utils.reduce;
import static com.github.mmdemirbas.oncalls.Utils.sorted;
import static java.util.Collections.emptyList;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-22 12:37
 */
public interface TimelineSegment<C extends Comparable<? super C>, V> {
    /**
     * Merges this and other {@code segments} into one {@link TimelineSegment} using the provided {@code mergeFunction}.
     *
     * @param <A> value type of the other segments
     */
    default <A> TimelineSegment<C, V> mergeWith(List<TimelineSegment<C, A>> segments,
                                                BiFunction<List<V>, List<A>, List<V>> mergeFunction) {
        return reduce(this, segments, (acc, segment) -> {
            List<Interval<C, V>> intervals = new ArrayList<>();
            List<V>              values    = emptyList();
            C                    start     = null;
            C                    end       = null;

            for (C point : sorted(acc.getKeyPoints(), segment.getKeyPoints())) {
                end = point;
                List<V> mergedValues = mergeFunction.apply(acc.findCurrentValues(point),
                                                           segment.findCurrentValues(point));
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
            return newSegment(intervals);
        });
    }

    /**
     * Creates a new {@link TimelineSegment} instance from the given {@code intervals}.
     */
    TimelineSegment<C, V> newSegment(List<Interval<C, V>> intervals);

    /**
     * Returns a set of change points. In other words all start and end points of sub-intervals.
     */
    Set<C> getKeyPoints();

    /**
     * Returns values of the interval containing the specified {@code point}.
     */
    List<V> findCurrentValues(C point);

    /**
     * Returns the interval containing the specified {@code point}.
     */
    Interval<C, List<V>> findCurrentInterval(C point);

    /**
     * Returns the interval coming just after the interval containing the specified {@code point}.
     */
    Interval<C, List<V>> findNextInterval(C point);

    /**
     * Returns an interval map representation.
     */
    NavigableMap<C, List<V>> toIntervalMap();
}
