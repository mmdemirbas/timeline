package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import java.util.TreeSet;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-17 16:21
 */
public final class RangesTest {
    @Test
    public void ofSingleRange() {
        assertEquals(treeSetOf(range(2, 4)), ranges(range(2, 4)).getRanges());
    }

    @Test
    public void ofDisjointRanges() {
        assertEquals(treeSetOf(range(2, 4), range(8, 10)), ranges(range(2, 4), range(8, 10)).getRanges());
    }

    @Test
    public void ofSuccessiveRanges() {
        assertEquals(treeSetOf(range(2, 8)), ranges(range(2, 4), range(4, 8)).getRanges());
    }

    @Test
    public void ofIntersectingRanges() {
        assertEquals(treeSetOf(range(2, 8)), ranges(range(2, 6), range(4, 8)).getRanges());
    }

    @Test
    public void ofOverlappingRanges() {
        assertEquals(treeSetOf(range(2, 8)), ranges(range(2, 8), range(4, 6)).getRanges());
    }

    private static Ranges<Integer> ranges(Range<Integer>... ranges) {
        return new Ranges<>(asList(ranges));
    }

    private static TreeSet<Range<Integer>> treeSetOf(Range<Integer>... ranges) {
        return new TreeSet<>(asList(ranges));
    }

    private static Range<Integer> range(int startInclusive, int endExclusive) {
        return new Range<>(startInclusive, endExclusive);
    }
}