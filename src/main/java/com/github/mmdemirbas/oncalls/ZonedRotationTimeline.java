package com.github.mmdemirbas.oncalls;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;

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
    private final Iteration<Instant>   iteration;
    private final LongFunction<V>      recipientSupplier;

    public ZonedRotationTimeline(Range<ZonedDateTime> rotationRange,
                                 Duration iterationDuration,
                                 Collection<Range<Instant>> iterationRanges,
                                 List<V> recipients) {
        this.rotationRange = rotationRange;

        // todo: write test to show that empty subranges handled
        // todo: write javadoc to explain that empty subranges handled
        List<Range<Instant>> disjointIterationRanges = Range.toDisjointRanges(iterationRanges);
        iteration = Iteration.of(Instant.ofEpochSecond(0, iterationDuration.toNanos()),
                                 disjointIterationRanges.isEmpty()
                                 ? singletonList(Range.of(Instant.EPOCH,
                                                          Instant.ofEpochSecond(0L,
                                                                                iterationDuration.toNanos())))
                                 : disjointIterationRanges);

        List<V> recipientsList = unmodifiableList(new ArrayList<>(recipients));
        recipientSupplier = index -> recipientsList.get((int) (index % recipientsList.size()));
    }

    @Override
    public TimelineSegment<ZonedDateTime, V> toSegment(Range<ZonedDateTime> calculationRange) {
        List<ValuedRange<ZonedDateTime, V>> valuedRanges        = new ArrayList<>();
        Range<ZonedDateTime>                effectiveRange      = rotationRange.intersect(calculationRange);
        long                                startIndex          = indexOfIterationAt(effectiveRange.getStartInclusive());
        long                                endIndex            = indexOfIterationAt(effectiveRange.getEndExclusive());
        ZonedDateTime                       rotationStart       = rotationRange.getStartInclusive();
        Instant                             iterationDuration   = iteration.getDuration();
        long                                iterationStartNanos = nanosOf(iterationDuration) * startIndex;
        ZonedDateTime                       offset              = addNanos(rotationStart, iterationStartNanos);

        for (long index = startIndex; index <= endIndex; index++) {
            V recipient = recipientSupplier.apply(index);
            for (Range<Instant> range : iteration.getRanges()) {
                valuedRanges.add(ValuedRange.of(Range.of(add(offset, range.getStartInclusive()),
                                                         add(offset, range.getEndExclusive()))
                                                     .intersect(effectiveRange), recipient));
            }
            offset = add(offset, iterationDuration);
        }
        return StaticTimeline.ofIntervals(valuedRanges);
    }

    private long indexOfIterationAt(ZonedDateTime point) {
        ZonedDateTime rotationStart   = rotationRange.getStartInclusive();
        Duration      elapsedDuration = Duration.between(rotationStart, point);
        return elapsedDuration.toNanos() / nanosOf(iteration.getDuration());
    }

    private static ZonedDateTime add(ZonedDateTime offset, Instant instant) {
        return addNanos(offset, nanosOf(instant));
    }

    private static ZonedDateTime addNanos(ZonedDateTime rotationStart, long nanos) {
        return rotationStart.plus(nanos, ChronoUnit.NANOS);
    }

    private static long nanosOf(Instant instant) {
        long seconds       = instant.getEpochSecond();
        int  nanosOfSecond = instant.getNano();
        return TimeUnit.SECONDS.toNanos(seconds) + nanosOfSecond;
    }
}
