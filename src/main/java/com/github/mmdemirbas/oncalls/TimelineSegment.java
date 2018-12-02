package com.github.mmdemirbas.oncalls;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

/**
 * Represent a finite interval of a {@link Timeline}. Used to merge multiple timelines and query details.
 */
public interface TimelineSegment<C extends Comparable<? super C>, V> {
    /**
     * Merges this and other {@code segments} into one {@link TimelineSegment} using the provided {@code mergeFunction}.
     *
     * @param <A> value type of the other segments
     */
    default <A> TimelineSegment<C, V> mergeWith(List<TimelineSegment<C, A>> segments,
                                                BiFunction<List<V>, List<A>, List<V>> mergeFunction) {
        TimelineSegment<C, V> result = this;
        for (TimelineSegment<C, A> segment : requireNonNull(segments, "segments")) {
            result = result.mergeWith(segment, mergeFunction);
        }
        return result;
    }

    default <A> TimelineSegment<C, V> mergeWith(TimelineSegment<C, A> segment,
                                                BiFunction<List<V>, List<A>, List<V>> mergeFunction) {
        requireNonNull(segment, "segment");
        requireNonNull(mergeFunction, "mergeFunction");

        List<ValuedRange<C, V>> valuedRanges = new ArrayList<>();
        List<V>                 values       = emptyList();
        C                       start        = null;
        C                       end          = null;

        Set<C> sorted = new TreeSet<>();
        sorted.addAll(getKeyPoints());
        sorted.addAll(segment.getKeyPoints());

        // todo: merge & deduplication may be separated

        for (C point : sorted) {
            end = point;
            List<V> mergedValues = mergeFunction.apply(findCurrentValues(point), segment.findCurrentValues(point));
            if (!values.equals(mergedValues)) {
                if (!values.isEmpty()) {
                    Range<C> range = Range.of(start, end);
                    values.forEach(value -> valuedRanges.add(ValuedRange.of(range, value)));
                }
                values = mergedValues;
                start = end;
            }
        }

        if (!values.isEmpty()) {
            Range<C> range = Range.of(start, end);
            values.forEach(value -> valuedRanges.add(ValuedRange.of(range, value)));
        }
        return newSegment(valuedRanges);
    }

    /**
     * Creates a new {@link TimelineSegment} instance from the given {@code valuedRanges}.
     */
    TimelineSegment<C, V> newSegment(List<ValuedRange<C, V>> valuedRanges);

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
    ValuedRange<C, List<V>> findCurrentInterval(C point);

    /**
     * Returns the interval coming just after the interval containing the specified {@code point}.
     */
    ValuedRange<C, List<V>> findNextInterval(C point);

    /**
     * Returns an interval map representation.
     */
    NavigableMap<C, List<V>> toIntervalMap();
}
