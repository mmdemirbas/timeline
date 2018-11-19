package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.TreeSet;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void getDisjointRanges_EmptyRange() {
        assertDisjointRanges(asList(), Ranges.of(Range.of(2, 2)));
    }

    @Test
    void getDisjointRanges_EmptyRanges() {
        assertDisjointRanges(asList(), Ranges.of(Range.of(1, 1), Range.of(5, 5)));
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

    @Test
    void getDisjointRanges_DuplicateRanges() {
        assertDisjointRanges(asList(Range.of(2, 8)), Ranges.of(Range.of(2, 8), Range.of(2, 8)));
    }

    private static void assertDisjointRanges(List<Range<Integer>> expected, Ranges<Integer> ranges) {
        assertEquals(new TreeSet<>(expected), ranges.getDisjointRanges());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void isEmpty_NonEmpty() {
        assertFalse(Ranges.of(Range.of(1, 3), Range.of(5, 7))
                          .isEmpty());
    }

    @Test
    void isEmpty_Empty() {
        assertTrue(Ranges.of(Range.of(1, 1))
                         .isEmpty());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void map_ToNonEmpty() {
        Ranges<Integer> range = Ranges.of(Range.of(1, 3), Range.of(5, 7));
        assertEquals(Ranges.of(Range.of(10, 30), Range.of(50, 70)), range.map(i -> i * 10));
    }

    @Test
    void map_ToEmpty() {
        Ranges<Integer> range = Ranges.of(Range.of(1, 3), Range.of(5, 7));
        assertEquals(Ranges.of(), range.map(i -> 0));
    }

    @Test
    void map_ToInvalid() {
        Ranges<Integer> range = Ranges.of(Range.of(1, 3), Range.of(5, 7));
        assertThrows(IllegalArgumentException.class, () -> range.map(i -> -i));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void before_SmallerPoint() {
        assertBefore(Ranges.of(), Ranges.of(Range.of(1, 3), Range.of(5, 7)), 0);
    }

    @Test
    void before_StartPoint1() {
        assertBefore(Ranges.of(), Ranges.of(Range.of(1, 3), Range.of(5, 7)), 1);
    }

    @Test
    void before_InnerPoint1() {
        assertBefore(Ranges.of(Range.of(1, 2)), Ranges.of(Range.of(1, 3), Range.of(5, 7)), 2);
    }

    @Test
    void before_EndPoint1() {
        assertBefore(Ranges.of(Range.of(1, 3)), Ranges.of(Range.of(1, 3), Range.of(5, 7)), 3);
    }

    @Test
    void before_LargerPoint1() {
        assertBefore(Ranges.of(Range.of(1, 3)), Ranges.of(Range.of(1, 3), Range.of(5, 7)), 4);
    }

    @Test
    void before_StartPoint2() {
        assertBefore(Ranges.of(Range.of(1, 3)), Ranges.of(Range.of(1, 3), Range.of(5, 7)), 5);
    }

    @Test
    void before_InnerPoint2() {
        assertBefore(Ranges.of(Range.of(1, 3), Range.of(5, 6)), Ranges.of(Range.of(1, 3), Range.of(5, 7)), 6);
    }

    @Test
    void before_EndPoint2() {
        assertBefore(Ranges.of(Range.of(1, 3), Range.of(5, 7)), Ranges.of(Range.of(1, 3), Range.of(5, 7)), 7);
    }

    @Test
    void before_LargerPoint2() {
        assertBefore(Ranges.of(Range.of(1, 3), Range.of(5, 7)), Ranges.of(Range.of(1, 3), Range.of(5, 7)), 8);
    }

    @Test
    void before_EmptyRange() {
        assertBefore(Ranges.of(), Ranges.of(), 0);
    }

    private static void assertBefore(Ranges<Integer> expected, Ranges<Integer> ranges, int testPoint) {
        assertEquals(expected, ranges.before(testPoint));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void findRangeAt_SmallerPoint() {
        assertFindRangeAt(null, Ranges.of(Range.of(1, 3), Range.of(5, 7)), 0);
    }

    @Test
    void findRangeAt_StartPoint1() {
        assertFindRangeAt(Range.of(1, 3), Ranges.of(Range.of(1, 3), Range.of(5, 7)), 1);
    }

    @Test
    void findRangeAt_InnerPoint1() {
        assertFindRangeAt(Range.of(1, 3), Ranges.of(Range.of(1, 3), Range.of(5, 7)), 2);
    }

    @Test
    void findRangeAt_EndPoint1() {
        assertFindRangeAt(null, Ranges.of(Range.of(1, 3), Range.of(5, 7)), 3);
    }

    @Test
    void findRangeAt_LargerPoint1() {
        assertFindRangeAt(null, Ranges.of(Range.of(1, 3), Range.of(5, 7)), 4);
    }

    @Test
    void findRangeAt_StartPoint2() {
        assertFindRangeAt(Range.of(5, 7), Ranges.of(Range.of(1, 3), Range.of(5, 7)), 5);
    }

    @Test
    void findRangeAt_InnerPoint2() {
        assertFindRangeAt(Range.of(5, 7), Ranges.of(Range.of(1, 3), Range.of(5, 7)), 6);
    }

    @Test
    void findRangeAt_EndPoint2() {
        assertFindRangeAt(null, Ranges.of(Range.of(1, 3), Range.of(5, 7)), 7);
    }

    @Test
    void findRangeAt_LargerPoint2() {
        assertFindRangeAt(null, Ranges.of(Range.of(1, 3), Range.of(5, 7)), 8);
    }

    @Test
    void findRangeAt_EmptyRange() {
        assertFindRangeAt(null, Ranges.of(), 0);
    }

    private static void assertFindRangeAt(Range<Integer> expected, Ranges<Integer> ranges, int testPoint) {
        assertEquals(expected, ranges.findRangeAt(testPoint));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}