package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.github.mmdemirbas.oncalls.Utils.reduce;
import static java.util.Collections.emptyList;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-19 17:51
 */
@Value
public final class Rotation<C extends Comparable<? super C>, V> {
    Recurrence<C>                             recurrence;
    List<V>                                   recipients; // todo:ensure this is immutable
    List<Timeline<C, UnaryOperator<List<V>>>> patches; // // todo: ensure this is immutable

    // todo: should I write tests for Rotation even Rotations have tests?

    public Timeline<C, V> toTimeline(Range<? extends C> calculationRange) {
        Timeline<C, V> timeline;
        if (recipients.isEmpty()) {
            timeline = Timeline.of(emptyList());
        } else {
            Timeline<C, Long> iterations = recurrence.toTimeline(calculationRange);
            timeline = iterations.mapWith(index -> recipients.get((int) (index % recipients.size())));
        }
        timeline = reduce(timeline, patches, (acc, patch) -> acc.patchWith(patch.limitWith(calculationRange)));
        return timeline;
    }
}
