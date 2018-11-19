package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

/**
 * Represents a recurrence on the time line.
 * <p>
 * This class is immutable.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-19 14:48
 */
@Value
public final class Recurrence {
    Range<ZonedDateTime> recurrenceRange;
    Duration             iterationDuration;
    Ranges<Instant>      subRanges;

    public Recurrence(Range<ZonedDateTime> recurrenceRange, Duration iterationDuration, Ranges<Instant> subRanges) {
        this.recurrenceRange = recurrenceRange;
        this.iterationDuration = iterationDuration;
        this.subRanges = ((subRanges == null) || subRanges.isEmpty())
                         ? Ranges.of(Range.of(Instant.EPOCH,
                                              Instant.ofEpochSecond(0,
                                                                    iterationDuration.toNanos())))
                         : subRanges;
    }

    /**
     * Calculates a timeline at the specified time range mapping the ranges to corresponding iteration indices.
     */
    public <R> Timeline<ZonedDateTime, R> toTimeline(Range<ZonedDateTime> calculationRange,
                                                     LongFunction<? extends R> iterationValueMapper) {
        ZonedDateTime        recurrenceStart        = recurrenceRange.getStartInclusive();
        Range<ZonedDateTime> effectiveRange         = recurrenceRange.intersectedBy(calculationRange);
        ZonedDateTime        calculationStart       = effectiveRange.getStartInclusive();
        ZonedDateTime        calculationEnd         = effectiveRange.getEndExclusive();
        Duration             elapsedDurationAtStart = Duration.between(recurrenceStart, calculationStart);
        Duration             elapsedDurationAtEnd   = Duration.between(recurrenceStart, calculationEnd);
        long                 elapsedNanosAtStart    = elapsedDurationAtStart.toNanos();
        long                 elapsedNanosAtEnd      = elapsedDurationAtEnd.toNanos();
        long                 iterationPeriodNanos   = iterationDuration.toNanos();
        long                 iterationNoAtStart     = elapsedNanosAtStart / iterationPeriodNanos;
        long                 iterationNoAtEnd       = elapsedNanosAtEnd / iterationPeriodNanos;

        List<Event<R, ZonedDateTime>> events = new ArrayList<>();
        for (long iterationNo = iterationNoAtStart; iterationNo <= iterationNoAtEnd; iterationNo++) {
            ZonedDateTime iterationStart = recurrenceStart.plus(iterationDuration.multipliedBy(iterationNo));
            R             iterationValue = iterationValueMapper.apply(iterationNo);
            subRanges.getDisjointRanges()
                     .stream()
                     .map(range -> new Event<>(iterationValue,
                                               range.map(instant -> iterationStart.plus(Utils.nanosOf(instant),
                                                                                        ChronoUnit.NANOS))
                                                    .after(calculationStart)
                                                    .before(calculationEnd)))
                     .forEach(events::add);
        }
        return Timeline.of(events);
    }
}
