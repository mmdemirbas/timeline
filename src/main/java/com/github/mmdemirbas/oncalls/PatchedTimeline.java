package com.github.mmdemirbas.oncalls;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static java.util.Collections.unmodifiableList;

/**
 * A {@link Timeline} implementation which applies patches on top of a base timeline.
 * <p>
 * This class is immutable, if the generic types {@link C} and {@link V} are immutable.
 */
public final class PatchedTimeline<C extends Comparable<? super C>, V> implements Timeline<C, V> {
    private final Timeline<C, V>                            baseTimeline;
    private final List<Timeline<C, UnaryOperator<List<V>>>> patchTimelines;

    public PatchedTimeline(Timeline<C, V> baseTimeline, List<Timeline<C, UnaryOperator<List<V>>>> patchTimelines) {
        this.baseTimeline = baseTimeline;
        this.patchTimelines = unmodifiableList(new ArrayList<>(patchTimelines));
    }

    @Override
    public TimelineSegment<C, V> toSegment(Range<C> calculationRange) {
        return baseTimeline.mergeWith(patchTimelines, calculationRange, (values, patches) -> {
            List<V> result = new ArrayList<>(values);
            for (UnaryOperator<List<V>> patch : patches) {
                result = patch.apply(result);
            }
            return result;
        });
    }
}