package com.github.mmdemirbas.oncalls;

import java.util.ArrayList;
import java.util.List;

import static com.github.mmdemirbas.oncalls.Utils.reduce;
import static com.github.mmdemirbas.oncalls.Utils.unmodifiableCopyOf;
import static java.util.Collections.emptyList;

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
        StaticTimeline<C, V> result = StaticTimeline.of(emptyList());
        result = reduce(result,
                        timelines,
                        (acc, timeline) -> acc.combine(timeline.toStaticTimeline(calculationRange),
                                                       (thisValues, otherValues) -> {
                                                           List<V> values = new ArrayList<>();
                                                           values.addAll(thisValues);
                                                           values.addAll(otherValues);
                                                           return values;
                                                       }));
        return result;
    }
}
