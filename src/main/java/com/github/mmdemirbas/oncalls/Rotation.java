package com.github.mmdemirbas.oncalls;

import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.UnaryOperator;

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
            Timeline<ZonedDateTime, Long> iterationsTimeline = recurrence.toTimeline(calculationRange);
            timeline = iterationsTimeline.mapWith(iterationIndex -> recipients.get((int) (iterationIndex
                                                                                          % recipients.size())));
        }
        timeline = Utils.reduce(timeline, patches, Timeline::patchWith);
        return timeline;
    }
}
