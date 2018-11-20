package com.github.mmdemirbas.oncalls;

import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-19 17:50
 */
public final class Rotations<V> {
    @Getter private final Collection<Rotation<V>>                               rotations;
    @Getter private final List<Timeline<ZonedDateTime, UnaryOperator<List<V>>>> globalPatches;

    public Rotations(Collection<Rotation<V>> rotations,
                     List<Timeline<ZonedDateTime, UnaryOperator<List<V>>>> globalPatches) {
        this.rotations = rotations;
        this.globalPatches = globalPatches;
    }

    public Timeline<ZonedDateTime, V> toTimeline(Range<ZonedDateTime> calculationRange) {
        Timeline<ZonedDateTime, V> timeline = Timeline.of();
        timeline = Utils.reduce(timeline,
                                rotations,
                                (acc, rotation) -> acc.mergeWith(rotation.toTimeline(calculationRange)));

        timeline = Utils.reduce(timeline,
                                globalPatches,
                                (acc, globalPatch) -> acc.patchWith(globalPatch.limitWith(calculationRange)));
        return timeline;
    }
}