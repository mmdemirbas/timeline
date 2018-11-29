package com.github.mmdemirbas.oncalls;

import lombok.Value;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-28 22:18
 */
final class ComplexRotationsPoCTest {

    private static final int WEEK = 168;

    @Test
    void officeHours() {
        SingleIteration<Integer> weekdays         = iteration(WEEK, Range.of(0, 120));
        SingleIteration<Integer> officeHoursOfDay = iteration(24, Range.of(8, 18));

        assertEquals(iterations(WEEK,
                                ValuedRange.of(Range.of(8, 18), 0),
                                ValuedRange.of(Range.of(32, 42), 1),
                                ValuedRange.of(Range.of(56, 66), 2),
                                ValuedRange.of(Range.of(80, 90), 3),
                                ValuedRange.of(Range.of(104, 114), 4)), multiply(weekdays, officeHoursOfDay));
    }

    @Test
    void nonOfficeHours() {
        SingleIteration<Integer> weekdays            = iteration(WEEK, Range.of(0, 120));
        SingleIteration<Integer> weekends            = iteration(WEEK, Range.of(120, WEEK));
        SingleIteration<Integer> wholeDay            = iteration(24, Range.of(0, 24));
        SingleIteration<Integer> nonOfficeHoursOfDay = iteration(24, Range.of(0, 8), Range.of(18, 24));

        assertEquals(iterations(WEEK,
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
                                ValuedRange.of(Range.of(144, WEEK), 6)),
                     plus(multiply(weekdays, nonOfficeHoursOfDay), multiply(weekends, wholeDay)));
    }

    @Disabled("not implemented yet!")
    @Test
    void splittedOfficeHours() {
        SingleIteration<Integer> weekdays         = iteration(WEEK, Range.of(0, 120));
        SingleIteration<Integer> officeHoursOfDay = iteration(24, Range.of(8, 17));
        SingleIteration<Integer> threeHours       = iteration(3, Range.of(0, 3));

        MultipleIterations<Integer> actual = null; //  multiply(weekdays, multiply(officeHoursOfDay, threeHours));
        assertEquals(iterations(WEEK,
                                ValuedRange.of(Range.of(8, 18), 0),
                                ValuedRange.of(Range.of(32, 42), 1),
                                ValuedRange.of(Range.of(56, 66), 2),
                                ValuedRange.of(Range.of(80, 90), 3),
                                ValuedRange.of(Range.of(104, 114), 4)), actual);
    }

    @SafeVarargs
    static SingleIteration<Integer> iteration(int duration, Range<Integer>... ranges) {
        return new SingleIteration<>(duration, asList(ranges));
    }

    @SafeVarargs
    private static MultipleIterations<Integer> iterations(int duration, ValuedRange<Integer, Integer>... iterations) {
        return new MultipleIterations<>(duration, asList(iterations));
    }

    private static MultipleIterations<Integer> multiply(SingleIteration<Integer> x, SingleIteration<Integer> y) {
        SingleIteration<Integer> big;
        SingleIteration<Integer> small;

        if (x.getDuration().compareTo(y.getDuration()) > 0) {
            big = x;
            small = y;
        } else {
            big = y;
            small = x;
        }

        Integer                             bigDuration   = big.getDuration();
        Integer                             smallDuration = small.getDuration();
        List<ValuedRange<Integer, Integer>> iterations    = new ArrayList<>();

        int offset         = 0;
        int iterationIndex = 0;

        while (offset < bigDuration) {
            int finalOffset         = offset;
            int finalIterationIndex = iterationIndex;
            iterations.addAll(small.getRanges()
                                   .stream()
                                   .flatMap(range -> big.getRanges()
                                                        .stream()
                                                        .map(bigRange -> Range.of(
                                                                finalOffset + range.getStartInclusive(),
                                                                finalOffset + range.getEndExclusive())
                                                                              .intersect(bigRange))
                                                        .filter(it -> !it.isEmpty()))
                                   .map(it -> ValuedRange.of(it, finalIterationIndex))
                                   .collect(Collectors.toList()));
            offset += smallDuration;
            iterationIndex++;
        }

        return new MultipleIterations<>(bigDuration, iterations);
    }

    private static MultipleIterations<Integer> plus(Iteration<Integer> multiply, Iteration<Integer> multiply1) {
        // todo: assumed that durations are same
        List<ValuedRange<Integer, Integer>> iterations = new ArrayList<>();
        iterations.addAll(multiply.getIterations());
        iterations.addAll(multiply1.getIterations());
        return new MultipleIterations<>(multiply.getDuration(), iterations);
    }

    interface Iteration<C extends Comparable<? super C>> {
        C getDuration();

        List<ValuedRange<C, Integer>> getIterations();
    }

    @Value
    public static final class SingleIteration<C extends Comparable<? super C>> implements Iteration<C> {
        private final C              duration;
        private final List<Range<C>> ranges;

        SingleIteration(C duration, Collection<Range<C>> ranges) {
            this.duration = duration;
            this.ranges = Range.toDisjointRanges(ranges);

            C maxSubrangeEnd = ranges.stream().map(Range::getEndExclusive).max(Comparator.naturalOrder()).orElse(null);
            if (maxSubrangeEnd == null) {
                throw new NullPointerException("Iteration has no sub-ranges.");
            } else if (duration.compareTo(maxSubrangeEnd) < 0) {
                throw new RuntimeException("Iteration duration couldn't be smaller than sub-ranges.");
            }
        }

        @Override
        public List<ValuedRange<C, Integer>> getIterations() {
            return ranges.stream().map(range -> ValuedRange.of(range, 0)).collect(Collectors.toList());
        }
    }

    @Value
    private static final class MultipleIterations<C extends Comparable<? super C>> implements Iteration<C> {
        private final C                             duration;
        private final List<ValuedRange<C, Integer>> iterations;
    }
}