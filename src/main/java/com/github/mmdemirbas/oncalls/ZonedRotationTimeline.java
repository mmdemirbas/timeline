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
    private final RotationTimeline<ZonedDateTime, Instant, V> rotationTimeline;
    private final List<V>                                     recipients;

    public ZonedRotationTimeline(Range<ZonedDateTime> rotationRange,
                                 Duration iterationDuration,
                                 Collection<Range<Instant>> iterationRanges,
                                 List<V> recipients) {
        this.recipients = unmodifiableList(new ArrayList<>(recipients));
        rotationTimeline = new RotationTimeline<>(rotationRange,
                                                  buildIterations(iterationDuration, iterationRanges),
                                                  (offset, unitDuration, point) ->
                                                          Duration.between(offset, point).toNanos() / nanosOf(
                                                                  unitDuration),
                                                  (offset, unitDuration, index) -> offset.plus(
                                                          nanosOf(unitDuration) * index, ChronoUnit.NANOS),
                                                  index -> this.recipients.get((int) (index % this.recipients.size())));
    }

    private static Iterations<Instant> buildIterations(Duration iterationDuration,
                                                       Collection<Range<Instant>> iterationRanges) {

        // todo: write test to show that empty subranges handled
        // todo: write javadoc to explain that empty subranges handled
        List<Range<Instant>> disjointIterationRanges = Range.toDisjointRanges(iterationRanges);
        return Iteration.of(Instant.ofEpochSecond(0, iterationDuration.toNanos()),
                            disjointIterationRanges.isEmpty()
                            ? singletonList(Range.of(Instant.EPOCH,
                                                     Instant.ofEpochSecond(0L,
                                                                           iterationDuration.toNanos())))
                            : disjointIterationRanges).toIterations();
    }

    private static long nanosOf(Instant instant) {
        long seconds       = instant.getEpochSecond();
        int  nanosOfSecond = instant.getNano();
        return TimeUnit.SECONDS.toNanos(seconds) + nanosOfSecond;
    }

    @Override
    public TimelineSegment<ZonedDateTime, V> toSegment(Range<ZonedDateTime> calculationRange) {
        return rotationTimeline.toSegment(calculationRange);
    }
}
