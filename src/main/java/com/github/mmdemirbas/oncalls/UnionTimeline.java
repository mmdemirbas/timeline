package com.github.mmdemirbas.oncalls;

import com.github.mmdemirbas.oncalls.StaticTimeline.Interval;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.mmdemirbas.oncalls.Utils.unmodifiableCopyOf;

/**
 * A {@link Timeline} implementation which works as a union of multiple {@link Timeline}s.
 * <p>
 * This class is immutable, if the generic types {@link C} and {@link V} are immutable.
 */
public final class UnionTimeline<C extends Comparable<? super C>, V> implements Timeline<C, V> {
    private final List<Timeline<C, V>> timelines;

    public UnionTimeline(List<Timeline<C, V>> timelines) {
        this.timelines = unmodifiableCopyOf(timelines);
    }

    @Override
    public StaticTimeline<C, V> toStaticTimeline(Range<? extends C> calculationRange) {
        return StaticTimeline.of(Collections.<Interval<C, V>>emptyList())
                             .combine(timelines,
                                      calculationRange,
                                      (thisValues, otherValues) -> Stream.of(thisValues, otherValues)
                                                                         .flatMap(Collection::stream)
                                                                         .collect(Collectors.toList()));
    }
}
