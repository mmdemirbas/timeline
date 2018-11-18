package com.github.mmdemirbas.oncalls;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents a range between two {@link Comparable} types with an inclusive start point
 * and exclusive end point. For example:
 * <pre>
 *     new Range<>(20, 80) => [20,80)
 * </pre>
 * <p>
 * This class is immutable if the generic type {@link Point} is immutable.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-17 11:55
 */
@ToString
@EqualsAndHashCode
public final class Range<Point extends Comparable<? super Point>> implements Comparable<Range<Point>> {
    @Getter private final Point startInclusive;
    @Getter private final Point endExclusive;

    /**
     * Creates a {@link Range} from the given start and end points.
     *
     * @throws IllegalArgumentException if {@code endExclusive < startInclusive}
     */
    public static <T extends Comparable<? super T>> Range<T> of(T startInclusive, T endExclusive) {
        return new Range<>(startInclusive, endExclusive);
    }

    private Range(Point startInclusive, Point endExclusive) {
        if (startInclusive.compareTo(endExclusive) > 0) {
            throw new IllegalArgumentException(String.format("start must be <= end, but was: %s > %s",
                                                             startInclusive,
                                                             endExclusive));
        }
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
    }

    /**
     * Returns {@code true} if and only if the given {@code point} is a member of this {@code Range}.
     * Note that the endpoint of the range is NOT considered as a member.
     */
    public boolean covers(Point point) {
        return (startInclusive.compareTo(point) <= 0) && (point.compareTo(endExclusive) < 0);
    }

    @Override
    public int compareTo(Range<Point> o) {
        int cmp = startInclusive.compareTo(o.startInclusive);
        return (cmp == 0) ? endExclusive.compareTo(o.endExclusive) : cmp;
    }
}
