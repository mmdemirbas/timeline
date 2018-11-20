package com.github.mmdemirbas.oncalls;

import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.UnaryOperator;

import static com.github.mmdemirbas.oncalls.Utils.reduce;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-19 17:51
 */
public final class Rotation<V> {
    @Getter private final Recurrence                                            recurrence;
    @Getter private final List<V>                                               recipients;
    @Getter private final List<Timeline<ZonedDateTime, UnaryOperator<List<V>>>> patches;

    public Rotation(Recurrence recurrence,
                    List<V> recipients,
                    List<Timeline<ZonedDateTime, UnaryOperator<List<V>>>> patches) {
        this.recurrence = recurrence;
        this.recipients = recipients;
        this.patches = patches;
    }

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
