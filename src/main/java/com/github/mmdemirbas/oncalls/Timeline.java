package com.github.mmdemirbas.oncalls;

import java.util.List;
import java.util.function.BiFunction;

import static com.github.mmdemirbas.oncalls.Utils.map;

/**
 * Base interface for all timelines.
 *
 * @param <C> type of time points on the time line
 * @param <V> type of arbitrary values associated with time intervals
 */
@FunctionalInterface
public interface Timeline<C extends Comparable<? super C>, V> {
    /**
     * Merges this and other {@code timelines} into one {@link TimelineSegment} on the specified
     * {@code calculationRange} using the provided {@code mergeFunction}.
     *
     * @param <A> type of the values of the other timelines
     */
    default <A> TimelineSegment<C, V> mergeWith(List<Timeline<C, A>> timelines,
                                                Range<? extends C> calculationRange,
                                                BiFunction<List<V>, List<A>, List<V>> mergeFunction) {
        return toTimelineSegment(calculationRange).mergeWith(map(timelines,
                                                                 timeline -> timeline.toTimelineSegment(calculationRange)),
                                                             mergeFunction);
    }

    /**
     * Converts the specified {@code calculationRange} part of this Timeline into a {@link TimelineSegment}
     */
    TimelineSegment<C, V> toTimelineSegment(Range<? extends C> calculationRange);
}
