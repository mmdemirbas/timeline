package com.github.mmdemirbas.oncalls;

import lombok.Value;

/**
 * Represents a range between two {@link Comparable} types with an inclusive start point
 * and exclusive end point. For example:
 * <pre>
 *     new Range<>(20, 80) => [20,80)
 * </pre>
 * <p>
 * This class is immutable if the {@code T} type is immutable.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-17 11:55
 */
@Value
public final class Range<T extends Comparable<? super T>> implements Comparable<Range<T>> {
    T startInclusive;
    T endExclusive;

    /**
     * Creates a {@link Range} from the given start and end points.
     *
     * @throws IllegalArgumentException if {@code endExclusive < startInclusive}
     */
    public static <T extends Comparable<? super T>> Range<T> of(T startInclusive, T endExclusive) {
        if (startInclusive.compareTo(endExclusive) > 0) {
            throw new IllegalArgumentException(String.format("start must be <= end, but was: %s > %s",
                                                             startInclusive,
                                                             endExclusive));
        }
        return new Range<>(startInclusive, endExclusive);
    }

    private Range(T startInclusive, T endExclusive) {
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
    }

    /**
     * Returns {@code true} if and only if the given {@code point} is a member of this {@code Range}.
     * Note that the endpoint of the range is NOT considered as a member.
     */
    public boolean covers(T point) {
        return (startInclusive.compareTo(point) <= 0) && (point.compareTo(endExclusive) < 0);
    }

    @Override
    public int compareTo(Range<T> o) {
        int cmp = startInclusive.compareTo(o.startInclusive);
        return (cmp == 0) ? endExclusive.compareTo(o.endExclusive) : cmp;
    }
}
