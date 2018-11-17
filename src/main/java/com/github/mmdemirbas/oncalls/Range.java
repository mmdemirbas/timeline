package com.github.mmdemirbas.oncalls;

import lombok.Value;

/**
 * Represents a range between two {@link Comparable} types where the start point is inclusive
 * and end point is exclusive.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-17 11:55
 */
@Value
public final class Range<T extends Comparable<? super T>> implements Comparable<Range<T>> {
    T startInclusive;
    T endExclusive;

    /**
     * Returns {@code true} if and only if the given {@code point} is a member of this {@code Range}.
     * Note that the endpoint of the range is considered as not a member.
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
