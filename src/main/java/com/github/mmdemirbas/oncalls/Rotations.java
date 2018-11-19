package com.github.mmdemirbas.oncalls;

import com.github.mmdemirbas.oncalls.Timeline.Patch;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-19 17:50
 */
@Value
public final class Rotations<V> {
    Collection<Rotation<V>>                 rotations;
    List<Timeline<ZonedDateTime, Patch<V>>> globalPatches;

    public Timeline<ZonedDateTime, V> toTimeline(Range<ZonedDateTime> calculationRange) {
        Timeline<ZonedDateTime, V> timeline = Timeline.of();
        if (rotations != null) {
            for (Rotation<V> rotation : rotations) {
                Timeline<ZonedDateTime, V> rotationTimeline = rotation.toTimeline(calculationRange);
                timeline = timeline.merge(rotationTimeline, Utils::merge);
            }
        }
        if (globalPatches != null) {
            for (Timeline<ZonedDateTime, Patch<V>> patchTimeline : globalPatches) {
                Timeline<ZonedDateTime, Patch<V>> patch = patchTimeline.limitToRange(calculationRange);
                timeline = timeline.withPatch(patch);
            }
        }
        return timeline;
    }

}
