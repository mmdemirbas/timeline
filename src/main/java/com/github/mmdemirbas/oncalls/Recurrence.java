package com.github.mmdemirbas.oncalls;

/**
 * Represents a recurring period on a time line of values of type {@link C}.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-21 04:04
 */
public interface Recurrence<C extends Comparable<? super C>> extends ToTimeline<C, Long> {
    /**
     * Builds a timeline for the specified range by associating
     * the ranges with the corresponding iteration indices.
     */
    @Override
    Timeline<C, Long> toTimeline(Range<? extends C> calculationRange);
}
