package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * Represents a range between two {@link Comparable} types.
 * <p>
 * Start point is inclusive, and end point is exclusive as in the mathematical notation {@code  [20,80)}.
 * <p>
 * It is considered an empty range, if start and end points equal to each other.
 * <p>
 * Attempt to create a range with an end value smaller than the start value results with an exception.
 * <p>
 * This class is immutable if the generic type {@link C} is immutable.
 *
 * @param <C> type of the {@link Comparable} values
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
        this.startInclusive = requireNonNull(startInclusive, "startInclusive");
        this.endExclusive = requireNonNull(endExclusive, "endExclusive");

        if (startInclusive.compareTo(endExclusive) > 0) {
            throw new IllegalArgumentException(String.format("must be start <= end, but was: %s > %s",
                                                             startInclusive,
                                                             endExclusive));
        }
    }

    /**
     * Returns {@code true} if this range is empty which means start and end points equal to each other.
     */
    public boolean isEmpty() {
        return startInclusive.compareTo(endExclusive) == 0;
    }

    /**
     * Returns intersection of this range with the given range.
     */
    public Range<C> intersect(Range<C> other) {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a set of disjoint ranges by joining intersecting, overlapping and successive ranges.
     * Empty ranges will not appear in the result set.
     */
    public static <C extends Comparable<? super C>> List<Range<C>> toDisjointRanges(Collection<Range<C>> ranges) {
        if ((ranges == null) || ranges.isEmpty()) {
            return emptyList();
        }

        List<Range<C>> disjointRanges = new ArrayList<>();
        C              start          = null;
        C              end            = null;

        Set<Range<C>> rangesInStartOrder = new TreeSet<>(Comparator.comparing(Range::getStartInclusive));
        rangesInStartOrder.addAll(ranges);

        for (Range<C> range : rangesInStartOrder) {
            if ((end == null) || (end.compareTo(range.getStartInclusive()) < 0)) {
                addRange(disjointRanges, start, end);
                start = range.getStartInclusive();
                end = range.getEndExclusive();
            } else {
                end = maxOf(end, range.getEndExclusive());
            }
        }
        addRange(disjointRanges, start, end);
        return unmodifiableList(disjointRanges);
    }

    private static <C extends Comparable<? super C>> void addRange(Collection<Range<C>> output, C start, C end) {
        if ((start != null) && (end != null)) {
            Range<C> range = of(start, end);
            if (!range.isEmpty()) {
                output.add(range);
            }
        }
    }
}
