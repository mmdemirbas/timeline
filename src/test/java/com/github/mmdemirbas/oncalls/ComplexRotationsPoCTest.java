package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ComplexRotationsPoCTest {

    @Test
    void officeHours() {
        Iteration<Integer> weekdays  = Iteration.of(7 * 24, Range.of(0, 5 * 24));
        Iteration<Integer> workHours = Iteration.of(24, Range.of(8, 18));

        Iterations<Integer> actual = weekdays.newBuilder((x, y) -> x + y).split(workHours, 0);
        Iterations<Integer> expected = Iterations.of(7 * 24,
                                                     ValuedRange.of(Range.of(8, 18), 0),
                                                     ValuedRange.of(Range.of(32, 42), 1),
                                                     ValuedRange.of(Range.of(56, 66), 2),
                                                     ValuedRange.of(Range.of(80, 90), 3),
                                                     ValuedRange.of(Range.of(104, 114), 4));

        assertEquals(expected, actual);
    }

    @Test
    void nonOfficeHours() {
        Iteration<Integer> day          = Iteration.of(24, Range.of(0, 24));
        Iteration<Integer> nonWorkHours = Iteration.of(24, Range.of(0, 8), Range.of(18, 24));
        Iterations<Integer> actual = nonWorkHours.newBuilder((x, y) -> x + y)
                                                 .repeat(5)
                                                 .concat(day, it -> it.repeat(2))
                                                 .split(day, 0);

        Iterations<Integer> expected = Iterations.of(7 * 24,
                                                     ValuedRange.of(Range.of(0, 8), 0),
                                                     ValuedRange.of(Range.of(18, 24), 0),
                                                     ValuedRange.of(Range.of(24, 32), 1),
                                                     ValuedRange.of(Range.of(42, 48), 1),
                                                     ValuedRange.of(Range.of(48, 56), 2),
                                                     ValuedRange.of(Range.of(66, 72), 2),
                                                     ValuedRange.of(Range.of(72, 80), 3),
                                                     ValuedRange.of(Range.of(90, 96), 3),
                                                     ValuedRange.of(Range.of(96, 104), 4),
                                                     ValuedRange.of(Range.of(114, 120), 4),
                                                     ValuedRange.of(Range.of(120, 144), 5),
                                                     ValuedRange.of(Range.of(144, 7 * 24), 6));

        assertEquals(expected, actual);
    }

    @Test
    void splittedOfficeHours() {
        Iteration<Integer> weekdays   = Iteration.of(7 * 24, Range.of(0, 120));
        Iteration<Integer> workHours  = Iteration.of(24, Range.of(8, 17));
        Iteration<Integer> threeHours = Iteration.of(3, Range.of(0, 3));

        Iterations<Integer> actual = weekdays.newBuilder((x, y) -> x + y)
                                             .split(workHours, 0, it -> it.split(threeHours, 8));
        Iterations<Integer> expected = Iterations.of(7 * 24,
                                                     ValuedRange.of(Range.of(8, 11), 0),
                                                     ValuedRange.of(Range.of(11, 14), 1),
                                                     ValuedRange.of(Range.of(14, 17), 2),
                                                     ValuedRange.of(Range.of(32, 35), 3),
                                                     ValuedRange.of(Range.of(35, 38), 4),
                                                     ValuedRange.of(Range.of(38, 41), 5),
                                                     ValuedRange.of(Range.of(56, 59), 6),
                                                     ValuedRange.of(Range.of(59, 62), 7),
                                                     ValuedRange.of(Range.of(62, 65), 8),
                                                     ValuedRange.of(Range.of(80, 83), 9),
                                                     ValuedRange.of(Range.of(83, 86), 10),
                                                     ValuedRange.of(Range.of(86, 89), 11),
                                                     ValuedRange.of(Range.of(104, 107), 12),
                                                     ValuedRange.of(Range.of(107, 110), 13),
                                                     ValuedRange.of(Range.of(110, 113), 14));

        assertEquals(expected, actual);
    }

    // todo: write test for non-standard schedules such as 'every ramadan', 'every leap day', 'first work day of month' etc.
}