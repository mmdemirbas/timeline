package com.github.mmdemirbas.oncalls;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableNavigableMap;
import static java.util.Collections.unmodifiableNavigableSet;

/**
 * Represents a disjoint set of {@link Range}s.
 * <p>
 * Note that the intersecting, overlapping and successive ranges will be merged into one range
 * when constructing a {@link Ranges} object, to ensure all the underlying {@link Range}s
 * are disjoint. Empty ranges will not appear in the resulting {@link #getDisjointRanges() disjoint ranges}.
 * <p>
 * This class is immutable if the generic type {@link Point} is immutable.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-17 16:16
 */
@ToString
@EqualsAndHashCode
public final class Ranges<Point extends Comparable<? super Point>> {
    @Getter private final           NavigableSet<Range<Point>>        disjointRanges;
    @Getter private final transient NavigableMap<Point, Range<Point>> intervalMap;

    @SafeVarargs
    public static <T extends Comparable<? super T>> Ranges<T> of(Range<T>... ranges) {
        return new Ranges<>(asList(ranges));
    }

    public static <T extends Comparable<? super T>> Ranges<T> of(Collection<Range<T>> ranges) {
        return new Ranges<>(ranges);
    }

    private Ranges(Collection<Range<Point>> ranges) {
        disjointRanges = unmodifiableNavigableSet(toDisjointRanges(ranges));

        NavigableMap<Point, Range<Point>> intervalMap = new TreeMap<>();
        disjointRanges.forEach(range -> intervalMap.put(range.getStartInclusive(), range));
        disjointRanges.forEach(range -> intervalMap.put(range.getEndExclusive(), null));
        this.intervalMap = unmodifiableNavigableMap(intervalMap);
    }

    /**
     * Returns a set of disjoint {@link Range}s joining intersecting, overlapping and successive ranges.
     */
    private static <Point extends Comparable<? super Point>> NavigableSet<Range<Point>> toDisjointRanges(Collection<Range<Point>> ranges) {
        NavigableSet<Range<Point>> disjointRanges       = new TreeSet<>();
        List<Range<Point>>         rangesOrderedByStart = new ArrayList<>(ranges);
        rangesOrderedByStart.sort(Comparator.comparing(Range::getStartInclusive));

        Iterator<Range<Point>> it      = rangesOrderedByStart.iterator();
        Range<Point>           current = Utils.nextOrNull(it);
        Range<Point>           next    = Utils.nextOrNull(it);

        while (next != null) {
            Point currentEnd = current.getEndExclusive();
            if (currentEnd.compareTo(next.getStartInclusive()) < 0) {
                if (!current.isEmpty()) {
                    disjointRanges.add(current);
                }
            } else {
                do {
                    if (currentEnd.compareTo(next.getEndExclusive()) < 0) {
                        currentEnd = next.getEndExclusive();
                    }
                    next = Utils.nextOrNull(it);
                } while ((next != null) && (currentEnd.compareTo(next.getStartInclusive()) >= 0));

                Range<Point> range = Range.of(current.getStartInclusive(), currentEnd);
                if (!range.isEmpty()) {
                    disjointRanges.add(range);
                }
            }
            current = next;
            next = Utils.nextOrNull(it);
        }

        if ((current != null) && !current.isEmpty()) {
            disjointRanges.add(current);
        }
        return disjointRanges;
    }

    /**
     * Returns {@code true} if this Ranges contains no Range.
     */
    public boolean isEmpty() {
        return disjointRanges.isEmpty();
    }

    /**
     * Transforms this Ranges to another Ranges applying provided {@code transformation} to the range boundaries.
     *
     * @throws IllegalArgumentException if {@code endExclusive < startInclusive} for a range after applying transformation
     */
    public <R extends Comparable<? super R>> Ranges<R> map(Function<? super Point, R> transformation) {
        return new Ranges<>(mapRanges(range -> range.map(transformation)));
    }

    /**
     * Returns a narrowed copy of this Ranges whose the greatest member is strictly less than the provided {@code point}.
     */
    public Ranges<Point> before(Point point) {
        return new Ranges<>(mapRanges(range -> range.before(point)));
    }

    private <R> List<R> mapRanges(Function<? super Range<Point>, ? extends R> mapper) {
        return disjointRanges.stream()
                             .map(mapper)
                             .collect(Collectors.toList());
    }

    /**
     * Returns the {@link Range} containing the specified {@code point},
     * or {@code null} if the specified {@code point} not a member of a range.
     */
    public Range<Point> findRangeAt(Point point) {
        Entry<Point, Range<Point>> entry = intervalMap.floorEntry(point);
        return (entry == null) ? null : entry.getValue();
    }
}
