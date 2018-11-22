package com.github.mmdemirbas.oncalls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;

/**
 * A {@link Timeline} implementation which works as a union of multiple {@link Timeline}s.
 * <p>
 * This class is immutable, if the generic types {@link C} and {@link V} are immutable.
 */
public final class UnionTimeline<C extends Comparable<? super C>, V> implements Timeline<C, V> {
    private final List<Timeline<C, V>> timelines;

    public UnionTimeline(List<Timeline<C, V>> timelines) {
        this.timelines = unmodifiableList(new ArrayList<>(timelines));
    }

    @Override
    public TimelineSegment<C, V> toSegment(Range<C> calculationRange) {
        Timeline<C, V> seed = StaticTimeline.ofIntervals(Collections.emptyList());
        return seed.mergeWith(timelines,
                              calculationRange,
                              (thisValues, otherValues) -> Stream.of(thisValues, otherValues)
                                                                 .flatMap(Collection::stream)
                                                                 .collect(Collectors.toList()));
    }
}
