package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.Objects;

import static com.github.mmdemirbas.oncalls.Utils.maxOf;
import static com.github.mmdemirbas.oncalls.Utils.minOf;

/**
 * Represents a range between two {@link Comparable} types.
 * <p>
 * Start point is inclusive, and end point is exclusive as in the mathematical notation {@code  [20,80)}.
 * <p>
 * This class is immutable if the generic type {@link C} is immutable.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-17 11:55
 */
@Value
public final class Range<C extends Comparable<? super C>> {
    private final C startInclusive;
    private final C endExclusive;

    /**
     * Creates a Range from the given start and end points.
     *
     * @throws IllegalArgumentException if {@code endExclusive < startInclusive}
     */
    public static <C extends Comparable<? super C>> Range<C> of(C startInclusive, C endExclusive) {
        return new Range<>(startInclusive, endExclusive);
    }

    private Range(C startInclusive, C endExclusive) {
        Objects.requireNonNull(startInclusive, "startInclusive");
        Objects.requireNonNull(endExclusive, "endExclusive");
        if (startInclusive.compareTo(endExclusive) > 0) {
            throw new IllegalArgumentException(String.format("must be start <= end, but was: %s > %s",
                                                             startInclusive,
                                                             endExclusive));
        }

        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
    }

    /**
     * Returns {@code true} if this range is empty which means start and end points equal to each other.
     */
    public boolean isEmpty() {
        return startInclusive.compareTo(endExclusive) == 0;
    }

    /**
     * Returns intersection of {@code this} range and the {@code other} range.
     */
    public Range<C> intersect(Range<? extends C> other) {
        C start  = maxOf(startInclusive, other.startInclusive);
        C end    = minOf(endExclusive, other.endExclusive);
        C finalS = minOf(start, end);
        return new Range<>(finalS, end);
    }
}
