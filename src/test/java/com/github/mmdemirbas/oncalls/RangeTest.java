package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-17 11:57
 */
public final class RangeTest {
    @Test
    public void covers_smallerPoint() {
        assertFalse(range(1, 3).covers(0));
    }

    @Test
    public void covers_startPoint() {
        assertTrue(range(1, 3).covers(1));
    }

    @Test
    public void covers_innerPoint() {
        assertTrue(range(1, 3).covers(2));
    }

    @Test
    public void covers_endPoint() {
        assertFalse(range(1, 3).covers(3));
    }

    @Test
    public void covers_largerPoint() {
        assertFalse(range(1, 3).covers(4));
    }

    @Test
    public void compareTo_Disjoint() {
        assertEquals(-1, range(1, 3).compareTo(range(4, 6)));
    }

    @Test
    public void compareTo_Successive() {
        assertEquals(-1, range(1, 3).compareTo(range(3, 5)));
    }

    @Test
    public void compareTo_Overlapping() {
        assertEquals(-1, range(1, 4).compareTo(range(2, 5)));
    }

    @Test
    public void compareTo_OverlappingWithSameEnd() {
        assertEquals(-1, range(1, 4).compareTo(range(2, 4)));
    }

    @Test
    public void compareTo_OverlappingWithSameStart() {
        assertEquals(1, range(1, 4).compareTo(range(1, 3)));
    }

    @Test
    public void compareTo_Equal() {
        assertEquals(0, range(1, 3).compareTo(range(1, 3)));
    }

    private static Range<Integer> range(int startInclusive, int endExclusive) {
        return new Range<>(startInclusive, endExclusive);
    }
}