package com.github.mmdemirbas.oncalls;

import com.github.mmdemirbas.oncalls.Timeline.Interval;
import lombok.Value;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;

/**
 * An immutable representation of a recurring period on the time line of {@link ZonedDateTime} values.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-19 14:48
 */
@Value
public final class ZonedDateTimeRecurrence implements Recurrence<ZonedDateTime> {
    Range<ZonedDateTime> recurrenceRange;
    Duration             iterationDuration;
    List<Range<Instant>> disjointRanges;

    public ZonedDateTimeRecurrence(Range<ZonedDateTime> recurrenceRange,
                                   Duration iterationDuration,
                                   Collection<Range<Instant>> subRanges) {
        this.recurrenceRange = recurrenceRange;
        this.iterationDuration = iterationDuration;
        // todo: write test to show that empty subranges handled
        // todo: write javadoc to explain that empty subranges handled
        List<Range<Instant>> disjointRanges = Range.toDisjointRanges(subRanges);
        this.disjointRanges = !disjointRanges.isEmpty()
                              ? disjointRanges
                              : singletonList(Range.of(Instant.EPOCH, durationToInstant(iterationDuration)));
    }

    @Override
    public Timeline<ZonedDateTime, Long> toTimeline(Range<? extends ZonedDateTime> calculationRange) {
        Range<ZonedDateTime> effectiveRange = recurrenceRange.intersect(calculationRange);
        long                 startIndex     = iterationIndexAt(effectiveRange.getStartInclusive());
        long                 endIndex       = iterationIndexAt(effectiveRange.getEndExclusive());
        ZonedDateTime        offset         = getRecurrenceStart().plus(iterationDuration.multipliedBy(startIndex));

        List<Interval<ZonedDateTime, Long>> intervals = new ArrayList<>();
        for (long index = startIndex; index <= endIndex; index++) {
            for (Range<Instant> range : disjointRanges) {
                intervals.add(new Interval<>(Range.of(sum(offset, range.getStartInclusive()),
                                                      sum(offset, range.getEndExclusive()))
                                                  .intersect(effectiveRange), index));
            }
            offset = offset.plus(iterationDuration);
        }
        return Timeline.of(intervals);
    }

    private long iterationIndexAt(ZonedDateTime point) {
        Duration elapsedDuration = Duration.between(getRecurrenceStart(), point);
        return elapsedDuration.toNanos() / iterationDuration.toNanos();
    }

    private ZonedDateTime getRecurrenceStart() {
        return recurrenceRange.getStartInclusive();
    }

    private static ZonedDateTime sum(ZonedDateTime zonedDateTime, Instant instant) {
        long nanos = TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
        return zonedDateTime.plus(nanos, ChronoUnit.NANOS);
    }

    private static Instant durationToInstant(Duration duration) {
        return Instant.ofEpochSecond(0L, duration.toNanos());
    }
}
