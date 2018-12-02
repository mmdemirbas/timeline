package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Muhammed Demirbaş
 * @since 2018-12-02 13:38
 */
final class IterationTest {
    @Test
    void toIterations() {
        assertEquals(Iterations.of(10, ValuedRange.of(Range.of(2, 4), 0), ValuedRange.of(Range.of(5, 8), 0)),
                     Iteration.of(10, Range.of(2, 4), Range.of(5, 8)).toIterations());
    }
}