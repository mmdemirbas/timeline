package com.github.mmdemirbas.oncalls;

import com.github.mmdemirbas.oncalls.Timeline.Interval;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableNavigableSet;

/**
 * Represents a recurring period on the time line.
 * <p>
 * This class is immutable.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-19 14:48
 */
public final class Recurrence {
    @Getter private final Range<ZonedDateTime>         recurrenceRange;
    @Getter private final Duration                     iterationDuration;
    @Getter private final NavigableSet<Range<Instant>> disjointRanges;

    public Recurrence(Range<ZonedDateTime> recurrenceRange,
                      Duration iterationDuration,
                      Collection<Range<Instant>> subRanges) {
        this.recurrenceRange = recurrenceRange;
        this.iterationDuration = iterationDuration;

        Collection<Range<Instant>> effectiveSubRanges = ((subRanges != null) && !subRanges.isEmpty())
                                                        ? subRanges
                                                        : singletonList(Range.of(Instant.EPOCH,
                                                                                 Instant.ofEpochSecond(0L,
                                                                                                       iterationDuration.toNanos())));
        disjointRanges = toDisjointRanges(effectiveSubRanges);
    }

    /**
     * Returns a set of disjoint ranges by joining intersecting, overlapping and successive ranges.
     * Empty ranges will not appear in the result set.
     */
    static <C extends Comparable<? super C>> NavigableSet<Range<C>> toDisjointRanges(Collection<Range<C>> ranges) {
        List<Range<C>> rangesOrderedByStart = new ArrayList<>(ranges);
        rangesOrderedByStart.sort(Comparator.comparing(Range::getStartInclusive));

        Iterator<Range<C>> it      = rangesOrderedByStart.iterator();
        Range<C>           current = Utils.nextOrNull(it);
        Range<C>           next    = Utils.nextOrNull(it);

        NavigableSet<Range<C>> disjointRanges = new TreeSet<>();
        Consumer<Range<C>> yield = range -> {
            if (!range.isEmpty()) {
                disjointRanges.add(range);
            }
        };

        while (next != null) {
            C currentEnd = current.getEndExclusive();
            if (currentEnd.compareTo(next.getStartInclusive()) < 0) {
                yield.accept(current);
            } else {
                do {
                    if (currentEnd.compareTo(next.getEndExclusive()) < 0) {
                        currentEnd = next.getEndExclusive();
                    }
                    next = Utils.nextOrNull(it);
                } while ((next != null) && (currentEnd.compareTo(next.getStartInclusive()) >= 0));

                Range<C> range = Range.of(current.getStartInclusive(), currentEnd);
                yield.accept(range);
            }
            current = next;
            next = Utils.nextOrNull(it);
        }

        if ((current != null)) {
            yield.accept(current);
        }
        return unmodifiableNavigableSet(disjointRanges);
    }

    /**
     * Calculates a timeline at the specified time range mapping the ranges to corresponding iteration indices.
     */
    public Timeline<ZonedDateTime, Long> toTimeline(Range<ZonedDateTime> calculationRange) {
        ZonedDateTime        recurrenceStart        = recurrenceRange.getStartInclusive();
        Range<ZonedDateTime> effectiveRange         = recurrenceRange.intersectedBy(calculationRange);
        ZonedDateTime        calculationStart       = effectiveRange.getStartInclusive();
        ZonedDateTime        calculationEnd         = effectiveRange.getEndExclusive();
        Duration             elapsedDurationAtStart = Duration.between(recurrenceStart, calculationStart);
        Duration             elapsedDurationAtEnd   = Duration.between(recurrenceStart, calculationEnd);
        long                 elapsedNanosAtStart    = elapsedDurationAtStart.toNanos();
        long                 elapsedNanosAtEnd      = elapsedDurationAtEnd.toNanos();
        long                 iterationPeriodNanos   = iterationDuration.toNanos();
        long                 iterationIndexAtStart  = elapsedNanosAtStart / iterationPeriodNanos;
        long                 iterationIndexAtEnd    = elapsedNanosAtEnd / iterationPeriodNanos;

        List<Interval<ZonedDateTime, Long>> intervals      = new ArrayList<>();
        Duration                            passedDuration = iterationDuration.multipliedBy(iterationIndexAtStart);
        ZonedDateTime                       offset         = recurrenceStart.plus(passedDuration);
        for (long iterationIndex = iterationIndexAtStart; iterationIndex <= iterationIndexAtEnd; iterationIndex++) {
            long          finalIterationIndex = iterationIndex;
            ZonedDateTime finalOffset         = offset;
            intervals.addAll(Utils.map(disjointRanges,
                                       range -> Interval.of(sum(range, finalOffset).intersectedBy(effectiveRange),
                                                            finalIterationIndex)));
            offset = offset.plus(iterationDuration);
        }
        return Timeline.of(intervals);
    }

    private static Range<ZonedDateTime> sum(Range<Instant> range, ZonedDateTime offset) {
        return Range.of(sum(offset, range.getStartInclusive()), sum(offset, range.getEndExclusive()));
    }

    private static ZonedDateTime sum(ZonedDateTime zonedDateTime, Instant instant) {
        long instantNanos = TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
        return zonedDateTime.plus(instantNanos, ChronoUnit.NANOS);
    }
}
