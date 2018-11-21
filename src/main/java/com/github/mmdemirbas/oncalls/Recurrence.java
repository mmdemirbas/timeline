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
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.github.mmdemirbas.oncalls.Utils.maxOf;
import static com.github.mmdemirbas.oncalls.Utils.sortedBy;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

/**
 * An immutable representation of a recurring period on the time line.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-19 14:48
 */
@Value
public final class Recurrence {
    Range<ZonedDateTime> recurrenceRange;
    Duration             iterationDuration;
    List<Range<Instant>> disjointRanges;

    public Recurrence(Range<ZonedDateTime> recurrenceRange,
                      Duration iterationDuration,
                      Collection<Range<Instant>> subRanges) {
        this.recurrenceRange = recurrenceRange;
        this.iterationDuration = iterationDuration;
        // todo: write test to show that empty subranges handled
        // todo: write javadoc to explain that empty subranges handled
        disjointRanges = toDisjointRanges(((subRanges != null) && !subRanges.isEmpty())
                                          ? subRanges
                                          : singletonList(Range.of(Instant.EPOCH,
                                                                   durationToInstant(iterationDuration))));
    }

    private static Instant durationToInstant(Duration duration) {
        return Instant.ofEpochSecond(0L, duration.toNanos());
    }

    /**
     * Returns a set of disjoint ranges by joining intersecting, overlapping and successive ranges.
     * Empty ranges will not appear in the result set.
     */
    public static <C extends Comparable<? super C>> List<Range<C>> toDisjointRanges(Collection<Range<C>> ranges) {
        List<Range<C>> disjointRanges = new ArrayList<>();
        C              start          = null;
        C              end            = null;

        Set<Range<C>> rangesInStartOrder = sortedBy(Range::getStartInclusive, ranges);
        for (Range<C> range : rangesInStartOrder) {
            if ((end == null) || (end.compareTo(range.getStartInclusive()) < 0)) {
                addRange(disjointRanges, start, end);
                start = range.getStartInclusive();
                end = range.getEndExclusive();
            } else {
                end = maxOf(end, range.getEndExclusive());
            }
        }
        addRange(disjointRanges, start, end);
        return unmodifiableList(disjointRanges);
    }

    private static <C extends Comparable<? super C>> void addRange(Collection<? super Range<C>> output,
                                                                   C start,
                                                                   C end) {
        if ((start != null) && (end != null)) {
            Range<C> range = Range.of(start, end);
            if (!range.isEmpty()) {
                output.add(range);
            }
        }
    }

    /**
     * Calculates a timeline at the specified time range mapping the ranges to corresponding iteration indices.
     */
    public Timeline<ZonedDateTime, Long> toTimeline(Range<ZonedDateTime> calculationRange) {
        Range<ZonedDateTime> effectiveRange = recurrenceRange.intersect(calculationRange);
        long                 startIndex     = iterationIndexAt(effectiveRange.getStartInclusive());
        long                 endIndex       = iterationIndexAt(effectiveRange.getEndExclusive());
        ZonedDateTime        offset         = getRecurrenceStart().plus(iterationDuration.multipliedBy(startIndex));

        List<Interval<ZonedDateTime, Long>> intervals = new ArrayList<>();
        for (long index = startIndex; index <= endIndex; index++) {
            for (Range<Instant> range : disjointRanges) {
                intervals.add(Interval.of(sum(range, offset).intersect(effectiveRange), index));
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

    private static Range<ZonedDateTime> sum(Range<Instant> range, ZonedDateTime offset) {
        return Range.of(sum(offset, range.getStartInclusive()), sum(offset, range.getEndExclusive()));
    }

    private static ZonedDateTime sum(ZonedDateTime zonedDateTime, Instant instant) {
        return zonedDateTime.plus(instantToNanos(instant), ChronoUnit.NANOS);
    }

    private static long instantToNanos(Instant instant) {
        return TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
    }
}
