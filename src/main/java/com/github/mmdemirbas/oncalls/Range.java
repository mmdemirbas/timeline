package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

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
     * Returns a sorted list of unique disjoint ranges by joining intersecting, overlapping and successive ranges.
     * Empty ranges will not appear in the result set.
     */
    public static <C extends Comparable<? super C>> List<Range<C>> toDisjointRanges(Collection<Range<C>> ranges) {
        return toDisjointRanges(ranges,
                                (Range<C> range) -> range,
                                (current, joining) -> true,
                                (newRange, joining) -> newRange);
    }

    public static <C extends Comparable<? super C>, R> List<R> toDisjointRanges(Collection<? extends R> items,
                                                                                Function<? super R, Range<C>> getRange,
                                                                                BiPredicate<? super R, ? super R> canJoinIfSuccessive,
                                                                                BiFunction<? super Range<C>, ? super R, ? extends R> create) {
        if ((items == null) || items.isEmpty()) {
            return emptyList();
        }

        List<R> disjointRanges = new ArrayList<>();
        C       start          = null;
        C       end            = null;
        R       joiningItem    = null;

        List<R> sorted = new ArrayList<>(items);
        sorted.sort(Comparator.comparing(r -> getRange.apply(r).getStartInclusive()));

        for (R item : sorted) {
            Range<C> range    = getRange.apply(item);
            C        newStart = range.getStartInclusive();
            C        newEnd   = range.getEndExclusive();

            if ((end == null) || (end.compareTo(newStart) < 0) || !canJoinIfSuccessive.test(item, joiningItem)) {
                addRange(disjointRanges, start, end, create, joiningItem);
                start = newStart;
                end = newEnd;
                joiningItem = item;
            } else if (end.compareTo(newEnd) < 0) {
                end = newEnd;
            }
        }
        addRange(disjointRanges, start, end, create, joiningItem);
        return unmodifiableList(disjointRanges);
    }

    private static <R, C extends Comparable<? super C>> void addRange(Collection<? super R> output,
                                                                      C start,
                                                                      C end,
                                                                      BiFunction<? super Range<C>, ? super R, ? extends R> create,
                                                                      R joiningItem) {
        if ((start != null) && (end != null)) {
            Range<C> range = of(start, end);
            if (!range.isEmpty()) {
                output.add(create.apply(range, joiningItem));
            }
        }
    }
}
