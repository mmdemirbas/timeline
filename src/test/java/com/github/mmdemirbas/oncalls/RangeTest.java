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
    void of_NullStart() {
        assertThrows(NullPointerException.class, () -> Range.of(null, 1));
    }

    @Test
    void of_NullEnd() {
        assertThrows(NullPointerException.class, () -> Range.of(null, 1));
    }

    @Test
    void of_InvalidOrder() {
        assertThrows(IllegalArgumentException.class, () -> Range.of(2, 1));
    }

    private static void assertCreated(int expectedStart, int expectedEnd, Range<Integer> range) {
        assertEquals(expectedStart, (int) range.getStartInclusive());
        assertEquals(expectedEnd, (int) range.getEndExclusive());
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
}