package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-17 11:57
 */
final class RangeTest {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void of_StartLessThanEnd() {
        assertCreated(1, 2, Range.of(1, 2));
    }

    @Test
    void of_StartEqualsEnd() {
        assertCreated(1, 1, Range.of(1, 1));
    }

    @Test
    void of_StartGreaterThanEnd() {
        assertThrows(IllegalArgumentException.class, () -> Range.of(2, 1));
    }

    private static void assertCreated(int expectedStart, int expectedEnd, Range<Integer> range) {
        assertEquals(expectedStart, (int) range.getStartInclusive());
        assertEquals(expectedEnd, (int) range.getEndExclusive());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void covers_SmallerPoint() {
        assertCovers(false, Range.of(1, 3), 0);
    }

    @Test
    void covers_StartPoint() {
        assertCovers(true, Range.of(1, 3), 1);
    }

    @Test
    void covers_InnerPoint() {
        assertCovers(true, Range.of(1, 3), 2);
    }

    @Test
    void covers_EndPoint() {
        assertCovers(false, Range.of(1, 3), 3);
    }

    @Test
    void covers_LargerPoint() {
        assertCovers(false, Range.of(1, 3), 4);
    }

    private static void assertCovers(boolean expected, Range<Integer> range, int testPoint) {
        assertEquals(expected, range.covers(testPoint));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void compareTo_Disjoint() {
        assertCompareTo(-1, Range.of(1, 3), Range.of(4, 6));
    }

    @Test
    void compareTo_Successive() {
        assertCompareTo(-1, Range.of(1, 3), Range.of(3, 5));
    }

    @Test
    void compareTo_Overlapping() {
        assertCompareTo(-1, Range.of(1, 4), Range.of(2, 5));
    }

    @Test
    void compareTo_OverlappingWithSameEnd() {
        assertCompareTo(-1, Range.of(1, 4), Range.of(2, 4));
    }

    @Test
    void compareTo_OverlappingWithSameStart() {
        assertCompareTo(1, Range.of(1, 4), Range.of(1, 3));
    }

    @Test
    void compareTo_Equal() {
        assertCompareTo(0, Range.of(1, 3), Range.of(1, 3));
    }

    private static void assertCompareTo(int expected, Range<Integer> left, Range<Integer> right) {
        assertEquals(expected, left.compareTo(right));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}