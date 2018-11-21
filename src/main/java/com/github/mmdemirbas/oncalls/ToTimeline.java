package com.github.mmdemirbas.oncalls;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-21 07:37
 */
public interface ToTimeline<C extends Comparable<? super C>, V> {
    Timeline<C, V> toTimeline(Range<? extends C> calculationRange);
}
