package com.github.mmdemirbas.oncalls;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static com.github.mmdemirbas.oncalls.Utils.unmodifiableCopyOf;
import static java.util.Objects.requireNonNull;

/**
 * A {@link Timeline} implementation which applies patches on top of a base timeline.
 * <p>
 * This class is immutable, if the generic types {@link C} and {@link V} are immutable.
 */
public final class PatchedTimeline<C extends Comparable<? super C>, V> implements Timeline<C, V> {
    private final Timeline<C, V>                            baseTimeline;
    private final List<Timeline<C, UnaryOperator<List<V>>>> patchTimelines;

    public static <C extends Comparable<? super C>, V> PatchedTimeline<C, V> of(Timeline<C, V> baseTimeline,
                                                                                List<Timeline<C, UnaryOperator<List<V>>>> patchTimelines) {
        return new PatchedTimeline<>(baseTimeline, patchTimelines);
    }

    private PatchedTimeline(Timeline<C, V> baseTimeline, List<Timeline<C, UnaryOperator<List<V>>>> patchTimelines) {
        this.baseTimeline = requireNonNull(baseTimeline, "baseTimeline");
        this.patchTimelines = unmodifiableCopyOf(patchTimelines);
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