package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.github.mmdemirbas.oncalls.Utils.maxOf;
import static com.github.mmdemirbas.oncalls.Utils.minOf;
import static com.github.mmdemirbas.oncalls.Utils.sortedBy;
import static java.util.Collections.unmodifiableList;

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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a set of disjoint ranges by joining intersecting, overlapping and successive ranges.
     * Empty ranges will not appear in the result set.
     */
    public static <C extends Comparable<? super C>> List<Range<C>> toDisjointRanges(Collection<Range<C>> ranges) {
        List<Range<C>> disjointRanges = new ArrayList<>();
        C              start          = null;
        C              end            = null;

        Set<Range<C>> rangesInStartOrder = sortedBy(Range::getStartInclusive, ranges);
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

    private static <C extends Comparable<? super C>> void addRange(Collection<? super Range<C>> output,
                                                                   C start,
                                                                   C end) {
        if ((start != null) && (end != null)) {
            Range<C> range = of(start, end);
            if (!range.isEmpty()) {
                output.add(range);
            }
        }
    }
}
