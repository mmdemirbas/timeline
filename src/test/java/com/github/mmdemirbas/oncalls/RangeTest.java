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
    void of_NonEmpty() {
        assertCreated(1, 2, Range.of(1, 2));
    }

    @Test
    void of_Empty() {
        assertCreated(1, 1, Range.of(1, 1));
    }

    @Test
    void of_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> Range.of(2, 1));
    }

    private static void assertCreated(int expectedStart, int expectedEnd, Range<Integer> range) {
        assertEquals(expectedStart, (int) range.getStartInclusive());
        assertEquals(expectedEnd, (int) range.getEndExclusive());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void compareTo_Disjoint() {
        assertCompareTo(-1, Range.of(1, 3), Range.of(5, 7));
    }

    @Test
    void compareTo_Successive() {
        assertCompareTo(-1, Range.of(1, 3), Range.of(3, 5));
    }

    @Test
    void compareTo_Intersecting() {
        assertCompareTo(-1, Range.of(1, 5), Range.of(3, 7));
    }

    @Test
    void compareTo_OverlappingWithSameEnd() {
        assertCompareTo(-1, Range.of(1, 5), Range.of(3, 5));
    }

    @Test
    void compareTo_OverlappingWithSameStart() {
        assertCompareTo(1, Range.of(1, 5), Range.of(1, 3));
    }

    @Test
    void compareTo_Containing() {
        assertCompareTo(-1, Range.of(1, 7), Range.of(3, 5));
    }

    @Test
    void compareTo_Equal() {
        assertCompareTo(0, Range.of(1, 3), Range.of(1, 3));
    }

    @Test
    void compareTo_EmptyRange() {
        assertCompareTo(-1, Range.of(1, 1), Range.of(2, 2));
    }

    private static void assertCompareTo(int expected, Range<Integer> left, Range<Integer> right) {
        assertEquals(expected, left.compareTo(right));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void isEmpty_NonEmpty() {
        assertIsEmpty(false, Range.of(1, 2));
    }

    @Test
    void isEmpty_Empty() {
        assertIsEmpty(true, Range.of(1, 1));
    }

    private static void assertIsEmpty(boolean expected, Range<Integer> range) {
        assertEquals(expected, range.isEmpty());
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

    @Test
    void covers_EmptyRange() {
        assertCovers(false, Range.of(1, 1), 1);
    }

    private static void assertCovers(boolean expected, Range<Integer> range, int testPoint) {
        assertEquals(expected, range.covers(testPoint));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void intersectedBy_Disjoint() {
        assertIntersectedBy(Range.of(3, 3), Range.of(1, 3), Range.of(5, 7));
    }

    @Test
    void intersectedBy_Successive() {
        assertIntersectedBy(Range.of(3, 3), Range.of(1, 3), Range.of(3, 5));
    }

    @Test
    void intersectedBy_Intersecting() {
        assertIntersectedBy(Range.of(3, 5), Range.of(1, 5), Range.of(3, 7));
    }

    @Test
    void intersectedBy_OverlappingWithSameEnd() {
        assertIntersectedBy(Range.of(3, 5), Range.of(1, 5), Range.of(3, 5));
    }

    @Test
    void intersectedBy_OverlappingWithSameStart() {
        assertIntersectedBy(Range.of(1, 3), Range.of(1, 5), Range.of(1, 3));
    }

    @Test
    void intersectedBy_Containing() {
        assertIntersectedBy(Range.of(3, 5), Range.of(1, 7), Range.of(3, 5));
    }

    @Test
    void intersectedBy_Equal() {
        assertIntersectedBy(Range.of(1, 3), Range.of(1, 3), Range.of(1, 3));
    }

    @Test
    void intersectedBy_EmptyRange() {
        assertIntersectedBy(Range.of(1, 1), Range.of(1, 1), Range.of(2, 2));
    }

    private static void assertIntersectedBy(Range<Integer> expected, Range<Integer> left, Range<Integer> right) {
        assertEquals(expected, left.intersectedBy(right));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void map_ToNonEmpty() {
        Range<Integer> range = Range.of(1, 3);
        assertEquals(Range.of(10, 30), range.map(i -> i * 10));
    }

    @Test
    void map_ToEmpty() {
        Range<Integer> range = Range.of(1, 3);
        assertEquals(Range.of(0, 0), range.map(i -> 0));
    }

    @Test
    void map_ToInvalid() {
        Range<Integer> range = Range.of(1, 3);
        assertThrows(IllegalArgumentException.class, () -> range.map(i -> -i));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void before_SmallerPoint() {
        assertBefore(Range.of(1, 1), Range.of(1, 3), 0);
    }

    @Test
    void before_StartPoint() {
        assertBefore(Range.of(1, 1), Range.of(1, 3), 1);
    }

    @Test
    void before_InnerPoint() {
        assertBefore(Range.of(1, 2), Range.of(1, 3), 2);
    }

    @Test
    void before_EndPoint() {
        assertBefore(Range.of(1, 3), Range.of(1, 3), 3);
    }

    @Test
    void before_LargerPoint() {
        assertBefore(Range.of(1, 3), Range.of(1, 3), 4);
    }

    @Test
    void before_EmptyRange_SmallerPoint() {
        assertBefore(Range.of(1, 1), Range.of(1, 1), 0);
    }

    @Test
    void before_EmptyRange_EqualPoint() {
        assertBefore(Range.of(1, 1), Range.of(1, 1), 1);
    }

    @Test
    void before_EmptyRange_LargerPoint() {
        assertBefore(Range.of(1, 1), Range.of(1, 1), 2);
    }

    private static void assertBefore(Range<Integer> expected, Range<Integer> range, int testPoint) {
        assertEquals(expected, range.before(testPoint));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void after_SmallerPoint() {
        assertAfter(Range.of(1, 3), Range.of(1, 3), 0);
    }

    @Test
    void after_StartPoint() {
        assertAfter(Range.of(1, 3), Range.of(1, 3), 1);
    }

    @Test
    void after_InnerPoint() {
        assertAfter(Range.of(2, 3), Range.of(1, 3), 2);
    }

    @Test
    void after_EndPoint() {
        assertAfter(Range.of(3, 3), Range.of(1, 3), 3);
    }

    @Test
    void after_LargerPoint() {
        assertAfter(Range.of(3, 3), Range.of(1, 3), 4);
    }

    @Test
    void after_EmptyRange_SmallerPoint() {
        assertAfter(Range.of(1, 1), Range.of(1, 1), 0);
    }

    @Test
    void after_EmptyRange_EqualPoint() {
        assertAfter(Range.of(1, 1), Range.of(1, 1), 1);
    }

    @Test
    void after_EmptyRange_LargerPoint() {
        assertAfter(Range.of(1, 1), Range.of(1, 1), 2);
    }

    private static void assertAfter(Range<Integer> expected, Range<Integer> range, int testPoint) {
        assertEquals(expected, range.after(testPoint));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}