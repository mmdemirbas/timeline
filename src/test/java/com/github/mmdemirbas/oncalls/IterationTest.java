package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Muhammed Demirbaş
 * @since 2018-12-02 13:38
 */
public final class IterationTest {
    @Test
    public void splitByIteration() {
        assertEquals(Iterations.of(10,
                                   ValuedRange.of(Range.of(3, 4), 0),
                                   ValuedRange.of(Range.of(5, 6), 0),
                                   ValuedRange.of(Range.of(6, 8), 1)),
                     Iteration.of(10, Range.of(3, 4), Range.of(5, 8))
                              .split(Iteration.of(4, Range.of(0, 4)), 2, (x, y) -> x + y));
    }

    @Test
    public void splitByIterations() {
        assertEquals(Iterations.of(10,
                                   ValuedRange.of(Range.of(3, 4), 1),
                                   ValuedRange.of(Range.of(5, 6), 3),
                                   ValuedRange.of(Range.of(6, 7), 4),
                                   ValuedRange.of(Range.of(7, 8), 5)),
                     Iteration.of(10, Range.of(3, 4), Range.of(5, 8))
                              .split(Iterations.of(4,
                                                   ValuedRange.of(Range.of(0, 1), 0),
                                                   ValuedRange.of(Range.of(1, 2), 1),
                                                   ValuedRange.of(Range.of(2, 3), 2),
                                                   ValuedRange.of(Range.of(3, 4), 3)), 2, (x, y) -> x + y));
    }

    @Test
    public void toIterations() {
        assertEquals(Iterations.of(10, ValuedRange.of(Range.of(2, 4), 0), ValuedRange.of(Range.of(5, 8), 0)),
                     Iteration.of(10, Range.of(2, 4), Range.of(5, 8)).toIterations());
    }

    @Test
    public void multiply() {
        assertEquals(Iteration.of(30,
                                  Range.of(2, 4),
                                  Range.of(5, 8),
                                  Range.of(12, 14),
                                  Range.of(15, 18),
                                  Range.of(22, 24),
                                  Range.of(25, 28)),
                     Iteration.of(10, Range.of(2, 4), Range.of(5, 8)).multiply(3, (x, y) -> x + y));
    }

    @Test
    public void concat() {
        assertEquals(Iteration.of(15, Range.of(2, 4), Range.of(5, 8), Range.of(11, 13), Range.of(14, 15)),
                     Iteration.of(10, Range.of(2, 4), Range.of(5, 8))
                              .concat(Iteration.of(5, Range.of(1, 3), Range.of(4, 5)), (x, y) -> x + y));
    }
}