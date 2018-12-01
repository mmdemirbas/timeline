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
    private final List<Range<C>> subRanges;

    @SafeVarargs
    public static <C extends Comparable<? super C>> Iteration<C> of(C duration, Range<C>... ranges) {
        return of(duration, asList(ranges));
    }

    public static <C extends Comparable<? super C>> Iteration<C> of(C duration, List<Range<C>> subRanges) {
        return new Iteration<C>(duration, subRanges);
    }

    private Iteration(C duration, List<Range<C>> subRanges) {
        this.duration = duration;
        this.subRanges = Range.toDisjointRanges(subRanges);

        C max = this.subRanges.stream().map(it -> it.getEndExclusive()).max(Comparator.naturalOrder()).orElse(null);

        if (duration.compareTo(max) < 0) {
            throw new RuntimeException(String.format(
                    "Sub-ranges exceed the iteration duration. Iteration duration was: %s, sub-ranges ends at: %s",
                    duration,
                    max));
        }
    }

    public Iterations<C> split(Iteration<C> unit, C offset, BinaryOperator<C> sum) {
        return split(unit.toIterations(), offset, sum, 1);
    }

    public Iterations<C> split(Iterations<C> units,
                               C startOffset,
                               BinaryOperator<C> sum,
                               int incrementIterationIndexPerUnit) {
        C                             unitDuration = units.getDuration();
        List<ValuedRange<C, Integer>> iterations   = new ArrayList<>();

        int valueOffset = 0;

        while (startOffset.compareTo(duration) < 0) {
            for (ValuedRange<C, Integer> unit : units.getIterations()) {
                Range<C> range = unit.getRange();
                Integer  value = unit.getValue();

                for (Range<C> subRange : subRanges) {
                    Range<C> intersect = Range.of(sum.apply(startOffset, range.getStartInclusive()),
                                                  sum.apply(startOffset, range.getEndExclusive())).intersect(subRange);
                    if (!intersect.isEmpty()) {
                        iterations.add(ValuedRange.of(intersect, valueOffset + value));
                    }
                }
            }
            valueOffset += incrementIterationIndexPerUnit;
            startOffset = sum.apply(startOffset, unitDuration);
        }

        return Iterations.of(duration, iterations);
    }

    public Iterations<C> toIterations() {
        return Iterations.of(duration,
                             subRanges.stream().map(range -> ValuedRange.of(range, 0)).collect(Collectors.toList()));
    }
}
