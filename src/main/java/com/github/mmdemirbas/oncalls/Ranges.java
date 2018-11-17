package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.Collection;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-17 16:16
 */
@Value
public final class Ranges<T extends Comparable<? super T>> {
    private final NavigableSet<Range<T>> ranges;

    public Ranges(Collection<Range<T>> ranges) {
        this.ranges = Collections.unmodifiableNavigableSet(combine(ranges));
    }

    private static <T extends Comparable<? super T>> NavigableSet<Range<T>> combine(Collection<Range<T>> ranges) {
        NavigableSet<Range<T>> combined = new TreeSet<>();
        T                      start    = null;
        T                      end      = null;

        for (Range<T> range : new TreeSet<>(ranges)) {
            T nextStart = range.getStartInclusive();
            T nextEnd   = range.getEndExclusive();
            if (start == null) {
                // first
                start = nextStart;
                end = nextEnd;
            } else if (end.compareTo(nextStart) < 0) {
                combined.add(new Range<>(start, end));
                start = nextStart;
                end = nextEnd;
            } else if (end.compareTo(nextEnd) < 0) {
                // overlapping of successive
                end = nextEnd;
            }
        }
        combined.add(new Range<>(start, end));
        return combined;
    }
}
