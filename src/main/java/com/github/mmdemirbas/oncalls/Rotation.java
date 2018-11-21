package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.UnaryOperator;

import static com.github.mmdemirbas.oncalls.Utils.reduce;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-19 17:51
 */
@Value
public final class Rotation<V> {
    Recurrence                                            recurrence;
    List<V>                                               recipients; // todo:ensure this is immutable
    List<Timeline<ZonedDateTime, UnaryOperator<List<V>>>> patches; // // todo: ensure this is immutable

    // todo: should I write tests for Rotation even Rotations have tests?

    public Timeline<ZonedDateTime, V> toTimeline(Range<ZonedDateTime> calculationRange) {
        Timeline<ZonedDateTime, V> timeline;
        if (recipients.isEmpty()) {
            timeline = Timeline.of();
        } else {
            Timeline<ZonedDateTime, Long> iterations = recurrence.toTimeline(calculationRange);
            timeline = iterations.mapWith(index -> recipients.get((int) (index % recipients.size())));
        }
        timeline = reduce(timeline, patches, (acc, patch) -> acc.patchWith(patch.limitWith(calculationRange)));
        return timeline;
    }
}
