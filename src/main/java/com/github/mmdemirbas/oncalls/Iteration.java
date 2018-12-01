package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * @author Muhammed Demirbaş
 * @since 2018-12-01 16:04
 */
@Value
public final class Iteration<C extends Comparable<? super C>> {
    private final C              duration;
    private final List<Range<C>> ranges;

    @SafeVarargs
    public static <C extends Comparable<? super C>> Iteration<C> of(C duration, Range<C>... ranges) {
        return of(duration, asList(ranges));
    }

    public static <C extends Comparable<? super C>> Iteration<C> of(C duration, List<Range<C>> subRanges) {
        return new Iteration<C>(duration, subRanges);
    }

    private Iteration(C duration, List<Range<C>> ranges) {
        this.duration = duration;
        this.ranges = Range.toDisjointRanges(ranges);

        C max = this.ranges.stream().map(it -> it.getEndExclusive()).max(Comparator.naturalOrder()).orElse(null);

        if (duration.compareTo(max) < 0) {
            throw new RuntimeException(String.format(
                    "Sub-ranges exceed the iteration duration. Iteration duration was: %s, sub-ranges ends at: %s",
                    duration,
                    max));
        }
    }

    public Iterations<C> split(Iteration<C> unit, C offset, BinaryOperator<C> sum) {
        return split(unit.toIterations(), offset, sum);
    }

    public Iterations<C> split(Iterations<C> units, C startOffset, BinaryOperator<C> sum) {
        C                             unitDuration         = units.getDuration();
        List<ValuedRange<C, Integer>> iterations           = new ArrayList<>();
        int                           valueOffset          = 0;
        long                          uniqueIterationCount = units.findUniqueIterationCount();

        while (startOffset.compareTo(duration) < 0) {
            for (ValuedRange<C, Integer> unit : units.getRanges()) {
                Range<C> range = unit.getRange();
                Integer  value = unit.getValue();

                for (Range<C> subRange : ranges) {
                    Range<C> intersect = Range.of(sum.apply(startOffset, range.getStartInclusive()),
                                                  sum.apply(startOffset, range.getEndExclusive())).intersect(subRange);
                    if (!intersect.isEmpty()) {
                        iterations.add(ValuedRange.of(intersect, valueOffset + value));
                    }
                }
            }
            valueOffset += uniqueIterationCount;
            startOffset = sum.apply(startOffset, unitDuration);
        }

        return Iterations.of(duration, iterations);
    }

    public Iterations<C> toIterations() {
        return Iterations.of(duration,
                             ranges.stream().map(range -> ValuedRange.of(range, 0)).collect(Collectors.toList()));
    }
}
