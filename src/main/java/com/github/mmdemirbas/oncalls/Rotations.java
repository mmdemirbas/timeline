package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.UnaryOperator;

import static com.github.mmdemirbas.oncalls.Utils.reduce;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-19 17:50
 */
@Value
public final class Rotations<V> {
    List<Rotation<V>>                                     rotations; // todo: ensure this is immutable
    List<Timeline<ZonedDateTime, UnaryOperator<List<V>>>> globalPatches; // todo: ensure this is immutable

    public Timeline<ZonedDateTime, V> toTimeline(Range<ZonedDateTime> calculationRange) {
        Timeline<ZonedDateTime, V> timeline = Timeline.of();
        timeline = reduce(timeline, rotations, (acc, rotation) -> acc.mergeWith(rotation.toTimeline(calculationRange)));
        timeline = reduce(timeline, globalPatches, (acc, patch) -> acc.patchWith(patch.limitWith(calculationRange)));
        return timeline;
    }
}