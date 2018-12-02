package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.github.mmdemirbas.oncalls.StaticTimelineTest.assertUnmodifiable;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class IterationsTest {
    @Test
    void immutability_input() {
        List<ValuedRange<Integer, Integer>> expected   = asList(ValuedRange.of(Range.of(10, 20), 123));
        List<ValuedRange<Integer, Integer>> input      = new ArrayList<>(expected);
        Iterations<Integer>                 iterations = Iterations.of(100, input);

        assertEquals(expected, iterations.getRanges());

        input.clear();

        assertEquals(expected, iterations.getRanges());
    }

    @Test
    void immutability_getter() {
        ValuedRange<Integer, Integer>       input      = ValuedRange.of(Range.of(10, 20), 123);
        List<ValuedRange<Integer, Integer>> ranges     = new ArrayList<>(asList(input));
        Iterations<Integer>                 iterations = Iterations.of(100, ranges);
        assertUnmodifiable(iterations.getRanges(), ValuedRange.of(Range.of(1, 2), 3));
    }
}