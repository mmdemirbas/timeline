package com.github.mmdemirbas.oncalls;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

/**
 * A {@link Timeline} implementation which represents a recurring period in a finite interval
 * of {@link ZonedDateTime}s.
 * <p>
 * This class is immutable, if the generic type {@link V} is immutable.
 */
public final class ZonedRotationTimeline<V> implements Timeline<ZonedDateTime, V> {
    // todo: try to split. 4 props are too much!
    private final Range<ZonedDateTime> rotationRange;
    private final Duration             iterationDuration;
    private final List<Range<Instant>> iterationRanges;
    private final List<V>              recipients;

    public ZonedRotationTimeline(Range<ZonedDateTime> rotationRange,
                                 Duration iterationDuration,
                                 Collection<Range<Instant>> iterationRanges,
                                 List<V> recipients) {
        this.rotationRange = rotationRange;
        this.iterationDuration = iterationDuration;
        // todo: write test to show that empty subranges handled
        // todo: write javadoc to explain that empty subranges handled
        List<Range<Instant>> disjointIterationRanges = Range.toDisjointRanges(iterationRanges);
        this.iterationRanges = !disjointIterationRanges.isEmpty()
                               ? disjointIterationRanges
                               : singletonList(Range.of(Instant.EPOCH,
                                                        Instant.ofEpochSecond(0L, iterationDuration.toNanos())));
        this.recipients = unmodifiableList(new ArrayList<>(recipients));
    }

    @Override
    public TimelineSegment<ZonedDateTime, V> toSegment(Range<ZonedDateTime> calculationRange) {
        List<ValuedRange<ZonedDateTime, V>> valuedRanges = new ArrayList<>();
        if (!recipients.isEmpty()) {
            Range<ZonedDateTime> effectiveRange = rotationRange.intersect(calculationRange);
            long                 startIndex     = indexOfIterationAt(effectiveRange.getStartInclusive());
            long                 endIndex       = indexOfIterationAt(effectiveRange.getEndExclusive());
            ZonedDateTime        rotationStart  = rotationRange.getStartInclusive();
            ZonedDateTime        offset         = rotationStart.plus(iterationDuration.multipliedBy(startIndex));

            for (long index = startIndex; index <= endIndex; index++) {
                V recipient = recipients.get((int) (index % recipients.size()));
                for (Range<Instant> range : iterationRanges) {
                    valuedRanges.add(ValuedRange.of(Range.of(sum(offset, range.getStartInclusive()),
                                                             sum(offset, range.getEndExclusive()))
                                                         .intersect(effectiveRange), recipient));
                }
                offset = offset.plus(iterationDuration);
            }
        }
        return StaticTimeline.ofIntervals(valuedRanges);
    }

    private long indexOfIterationAt(ZonedDateTime point) {
        ZonedDateTime rotationStart   = rotationRange.getStartInclusive();
        Duration      elapsedDuration = Duration.between(rotationStart, point);
        return elapsedDuration.toNanos() / iterationDuration.toNanos();
    }

    private static ZonedDateTime sum(ZonedDateTime zonedDateTime, Instant instant) {
        long nanos = TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
        return zonedDateTime.plus(nanos, ChronoUnit.NANOS);
    }
}
