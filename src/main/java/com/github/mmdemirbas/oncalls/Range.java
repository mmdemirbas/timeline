package com.github.mmdemirbas.oncalls;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.function.Function;

import static com.github.mmdemirbas.oncalls.Utils.maxOf;
import static com.github.mmdemirbas.oncalls.Utils.minOf;

/**
 * Represents a range between two {@link Comparable} types with an inclusive start point
 * and exclusive end point as in the following mathematical notation:
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
     * Compares {@code this} range to {@code other} range by comparing firstly their start points,
     * then the end points if the starts points are equal.
     */
    @Override
    public int compareTo(Range<Point> other) {
        int cmp = startInclusive.compareTo(other.startInclusive);
        return (cmp == 0) ? endExclusive.compareTo(other.endExclusive) : cmp;
    }

    /**
     * Returns {@code true} if this range is empty which means start and end points equal to each other.
     */
    public boolean isEmpty() {
        return startInclusive.compareTo(endExclusive) == 0;
    }

    /**
     * Returns {@code true} if and only if the given {@code point} is a member of this {@code Range}.
     * Note that the endpoint of the range is NOT considered as a member.
     */
    public boolean covers(Point point) {
        return (startInclusive.compareTo(point) <= 0) && (point.compareTo(endExclusive) < 0);
    }

    /**
     * Returns intersection of {@code this} range and the {@code other} range.
     */
    public Range<Point> intersectedBy(Range<? extends Point> other) {
        Point start  = maxOf(startInclusive, other.startInclusive);
        Point end    = minOf(endExclusive, other.endExclusive);
        Point finalS = minOf(start, end);
        return new Range<>(finalS, end);
    }

    /**
     * Transforms this range to another range applying provided {@code transformation} to the boundaries.
     *
     * @throws IllegalArgumentException if {@code endExclusive < startInclusive} after applying transformation
     */
    public <R extends Comparable<? super R>> Range<R> map(Function<? super Point, ? extends R> transformation) {
        return new Range<>(transformation.apply(startInclusive), transformation.apply(endExclusive));
    }

    /**
     * Returns a narrowed copy of this range whose the greatest member is strictly less than the specified {@code point}.
     */
    public Range<Point> before(Point point) {
        return new Range<>(startInclusive, findSplitPoint(point));
    }

    /**
     * Returns a narrowed copy of this range whose the smallest member is greater equal than the specified {@code point}.
     */
    public Range<Point> after(Point point) {
        return new Range<>(findSplitPoint(point), endExclusive);
    }

    private Point findSplitPoint(Point point) {
        return minOf(endExclusive, maxOf(startInclusive, point));
    }
}
