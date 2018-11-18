package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.TreeSet;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-17 16:21
 */
final class RangesTest {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void getDisjointRanges_NoRange() {
        assertDisjointRanges(asList(), Ranges.of());
    }

    @Test
    void getDisjointRanges_SingleRange() {
        assertDisjointRanges(asList(Range.of(2, 4)), Ranges.of(Range.of(2, 4)));
    }

    @Test
    void getDisjointRanges_DisjointRanges() {
        assertDisjointRanges(asList(Range.of(2, 4), Range.of(8, 10)), Ranges.of(Range.of(2, 4), Range.of(8, 10)));
    }

    @Test
    void getDisjointRanges_SuccessiveRanges() {
        assertDisjointRanges(asList(Range.of(2, 8)), Ranges.of(Range.of(2, 4), Range.of(4, 8)));
    }

    @Test
    void getDisjointRanges_IntersectingRanges() {
        assertDisjointRanges(asList(Range.of(2, 8)), Ranges.of(Range.of(2, 6), Range.of(4, 8)));
    }

    @Test
    void getDisjointRanges_OverlappingRanges() {
        assertDisjointRanges(asList(Range.of(2, 8)), Ranges.of(Range.of(2, 8), Range.of(4, 6)));
    }

    private static void assertDisjointRanges(List<Range<Integer>> expected, Ranges<Integer> ranges) {
        assertEquals(new TreeSet<>(expected), ranges.getDisjointRanges());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}