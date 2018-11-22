package com.github.mmdemirbas.oncalls;

import java.util.List;
import java.util.NavigableMap;
import java.util.function.BiFunction;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-22 09:47
 */
public interface StaticTimeline<C extends Comparable<? super C>, V> extends Timeline<C, V> {
    /**
     * Creates a new StaticTimeline combining this timeline with the given timeline using
     * the provided {@code mergeFunction}.
     *
     * @param timelines        other timelines to combine
     * @param calculationRange range to calculate combination
     * @param mergeFunction    merge function to use to decide final values for each interval
     * @param <A>              type of the values of the other timelines
     */
    <A> StaticTimeline<C, V> combine(Iterable<? extends Timeline<C, A>> timelines,
                                     Range<? extends C> calculationRange,
                                     BiFunction<List<V>, List<A>, List<V>> mergeFunction);

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

    NavigableMap<C, List<V>> getIntervalMap();
}
