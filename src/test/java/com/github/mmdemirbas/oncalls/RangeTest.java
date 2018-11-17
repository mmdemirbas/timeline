package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-17 11:57
 */
public final class RangeTest {
    @Test
    public void covers_smallerPoint() {
        assertFalse(new Range<>(1, 3).covers(0));
    }

    @Test
    public void covers_startPoint() {
        assertTrue(new Range<>(1, 3).covers(1));
    }

    @Test
    public void covers_innerPoint() {
        assertTrue(new Range<>(1, 3).covers(2));
    }

    @Test
    public void covers_endPoint() {
        assertFalse(new Range<>(1, 3).covers(3));
    }

    @Test
    public void covers_largerPoint() {
        assertFalse(new Range<>(1, 3).covers(4));
    }
}