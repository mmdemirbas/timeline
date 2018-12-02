package com.github.mmdemirbas.oncalls;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.unmodifiableList;

/**
 * A {@link Timeline} implementation which represents a recurring period in a finite interval
 * of {@link ZonedDateTime}s.
 * <p>
 * This class is immutable, if the generic type {@link V} is immutable.
 */
public final class ZonedRotationTimeline<V> extends RotationTimeline<ZonedDateTime, Instant, V> {
    private final List<V> recipients;

    public ZonedRotationTimeline(Range<ZonedDateTime> rotationRange, Iterations<Instant> iterations,
                                 List<V> recipients) {
        super(rotationRange, iterations);
        this.recipients = unmodifiableList(new ArrayList<>(Objects.requireNonNull(recipients, "recipients")));
    }

    @Override
    protected long indexAtPoint(ZonedDateTime offset, Instant unitDuration, ZonedDateTime point) {
        return Duration.between(offset, point).toNanos() / nanosOf(unitDuration);
    }

    @Override
    protected ZonedDateTime pointAtIndex(ZonedDateTime offset, Instant unitDuration, long iterationIndex) {
        return offset.plus(nanosOf(unitDuration) * iterationIndex, ChronoUnit.NANOS);
    }

    @Override
    protected V recipientAtIndex(long recipientIndex) {
        return recipients.get((int) (recipientIndex % recipients.size()));
    }

    private static long nanosOf(Instant instant) {
        long seconds       = instant.getEpochSecond();
        int  nanosOfSecond = instant.getNano();
        return TimeUnit.SECONDS.toNanos(seconds) + nanosOfSecond;
    }
}
