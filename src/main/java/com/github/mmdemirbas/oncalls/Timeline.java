package com.github.mmdemirbas.oncalls;

/**
 * Base interface for all timelines.
 *
 * @param <C> type of time points on the time line
 * @param <V> type of arbitrary values associated with time intervals
 */
@FunctionalInterface
public interface Timeline<C extends Comparable<? super C>, V> {
    /**
     * Converts the specified {@code calculationRange} part of this Timeline into a {@link StaticTimeline}
     */
    StaticTimeline<C, V> toStaticTimeline(Range<? extends C> calculationRange);
}
