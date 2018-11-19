package com.github.mmdemirbas.oncalls;

import com.github.mmdemirbas.oncalls.Timeline.Patch;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.List;

import static com.github.mmdemirbas.oncalls.Utils.getModuloIndex;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-19 17:51
 */
@Value
public final class Rotation<V> {
    Recurrence                              recurrence;
    List<V>                                 recipients;
    List<Timeline<ZonedDateTime, Patch<V>>> localPatches;

    public Timeline<ZonedDateTime, V> toTimeline(Range<ZonedDateTime> calculationRange) {
        return recipients.isEmpty()
               ? Timeline.<ZonedDateTime, V>of().withPatches(localPatches)
               : recurrence.toTimeline(calculationRange, iterationIndex -> getModuloIndex(recipients, iterationIndex))
                           .withPatches(localPatches);

    }
}
