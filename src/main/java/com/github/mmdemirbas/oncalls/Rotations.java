package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.github.mmdemirbas.oncalls.Utils.reduce;
import static java.util.Collections.emptyList;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-19 17:50
 */
@Value
public final class Rotations<C extends Comparable<? super C>, V> {
    List<Rotation<C, V>>                      rotations; // todo: ensure this is immutable
    List<Timeline<C, UnaryOperator<List<V>>>> globalPatches; // todo: ensure this is immutable

    public Timeline<C, V> toTimeline(Range<? extends C> calculationRange) {
        Timeline<C, V> timeline = Timeline.of(emptyList());
        timeline = reduce(timeline, rotations, (acc, rotation) -> acc.mergeWith(rotation.toTimeline(calculationRange)));
        timeline = reduce(timeline, globalPatches, (acc, patch) -> acc.patchWith(patch.limitWith(calculationRange)));
        return timeline;
    }
}