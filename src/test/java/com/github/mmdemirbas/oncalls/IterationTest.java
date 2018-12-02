package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.github.mmdemirbas.oncalls.StaticTimelineTest.assertUnmodifiable;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class IterationTest {
    @Test
    void toIterations() {
        assertEquals(Iterations.of(10, ValuedRange.of(Range.of(2, 4), 0), ValuedRange.of(Range.of(5, 8), 0)),
                     Iteration.of(10, Range.of(2, 4), Range.of(5, 8)).toIterations());
    }

    @Test
    void immutability_input() {
        List<Range<Integer>> expected   = asList(Range.of(10, 20));
        List<Range<Integer>> input      = new ArrayList<>(expected);
        Iteration<Integer>   iterations = Iteration.of(100, input);

        assertEquals(expected, iterations.getRanges());

        input.clear();

        assertEquals(expected, iterations.getRanges());
    }

    @Test
    void immutability_getter() {
        Range<Integer>       input      = Range.of(10, 20);
        List<Range<Integer>> ranges     = new ArrayList<>(asList(input));
        Iteration<Integer>   iterations = Iteration.of(100, ranges);
        assertUnmodifiable(iterations.getRanges(), Range.of(1, 2));
    }
}