package com.github.mmdemirbas.oncalls;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * Represents a range between two {@link Comparable} types with an inclusive start point
 * and exclusive end point as in the following mathematical notation:
 * <pre>
 *     new Range<>(20, 80) => [20,80)
 * </pre>
 * <p>
 * This class is immutable if the generic {@link C} type is immutable.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-17 11:55
 */
@ToString
@EqualsAndHashCode
public final class Range<C extends Comparable<? super C>> implements Comparable<Range<C>> {
    @Getter private final C startInclusive;
    @Getter private final C endExclusive;

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
     * Compares {@code this} range to {@code other} range by comparing firstly their start points,
     * then the end points if the starts points are equal.
     */
    @Override
    public int compareTo(Range<C> o) {
        int cmp = startInclusive.compareTo(o.startInclusive);
        return (cmp == 0) ? endExclusive.compareTo(o.endExclusive) : cmp;
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
    public Range<C> intersectedBy(Range<? extends C> other) {
        C start  = maxOf(startInclusive, other.startInclusive);
        C end    = minOf(endExclusive, other.endExclusive);
        C finalS = minOf(start, end);
        return new Range<>(finalS, end);
    }

    private static <C extends Comparable<? super C>> C maxOf(C x, C y) {
        return (x.compareTo(y) > 0) ? x : y;
    }

    private static <C extends Comparable<? super C>> C minOf(C x, C y) {
        return (x.compareTo(y) < 0) ? x : y;
    }
}
