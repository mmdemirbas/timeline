package com.github.mmdemirbas.oncalls;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableNavigableSet;

/**
 * Represents a disjoint set of {@link Range}s.
 * <p>
 * Note that the intersecting, overlapping and successive ranges will be merged into one range
 * when constructing a {@link Ranges} object, to ensure all the underlying {@link Range}s
 * are disjoint.
 * <p>
 * This class is immutable if the generic type {@link Point} is immutable.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-17 16:16
 */
@ToString
@EqualsAndHashCode
public final class Ranges<Point extends Comparable<? super Point>> {
    @Getter private final NavigableSet<Range<Point>> disjointRanges;

    @SafeVarargs
    public static <T extends Comparable<? super T>> Ranges<T> of(Range<T>... ranges) {
        return new Ranges<>(asList(ranges));
    }

    public static <T extends Comparable<? super T>> Ranges<T> of(Collection<Range<T>> ranges) {
        return new Ranges<>(ranges);
    }

    private Ranges(Collection<Range<Point>> ranges) {
        disjointRanges = unmodifiableNavigableSet(toDisjointRanges(ranges));
    }

    /**
     * Returns a set of disjoint {@link Range}s joining intersecting, overlapping and successive ranges.
     */
    private static <Point extends Comparable<? super Point>> NavigableSet<Range<Point>> toDisjointRanges(Collection<Range<Point>> ranges) {
        NavigableSet<Range<Point>> disjointRanges = new TreeSet<>();
        if (!ranges.isEmpty()) {
            List<Range<Point>> list = new ArrayList<>(ranges);
            list.sort(Comparator.comparing(Range::getStartInclusive));

            Iterator<Range<Point>> it      = list.iterator();
            Range<Point>           current = nextOrNull(it);
            Range<Point>           next    = nextOrNull(it);

            while (next != null) {
                Point currentEnd = current.getEndExclusive();
                if (currentEnd.compareTo(next.getStartInclusive()) < 0) {
                    disjointRanges.add(current);
                } else {
                    do {
                        if (currentEnd.compareTo(next.getEndExclusive()) < 0) {
                            currentEnd = next.getEndExclusive();
                        }
                        next = nextOrNull(it);
                    } while ((next != null) && (currentEnd.compareTo(next.getStartInclusive()) >= 0));
                    disjointRanges.add(Range.of(current.getStartInclusive(), currentEnd));
                }
                current = next;
                next = nextOrNull(it);
            }

            if (current != null) {
                disjointRanges.add(current);
            }
        }
        return disjointRanges;
    }

    private static <T> T nextOrNull(Iterator<? extends T> iterator) {
        return iterator.hasNext() ? iterator.next() : null;
    }
}
