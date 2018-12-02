package com.github.mmdemirbas.oncalls;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

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
                                                Range<C> calculationRange,
                                                BiFunction<List<V>, List<A>, List<V>> mergeFunction) {
        return toSegment(calculationRange).mergeWith(requireNonNull(timelines, "timelines").stream()
                                                                                           .map(timeline -> timeline.toSegment(
                                                                                                   calculationRange))
                                                                                           .collect(Collectors.toList()),
                                                     mergeFunction);
    }

    /**
     * Creates a {@link TimelineSegment} on the specified {@code calculationRange} of this Timeline.
     */
    TimelineSegment<C, V> toSegment(Range<C> calculationRange);
}
