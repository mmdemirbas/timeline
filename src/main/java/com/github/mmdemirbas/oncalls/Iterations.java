package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Muhammed Demirbaş
 * @since 2018-12-01 16:04
 */
@Value
final class Iterations<C extends Comparable<? super C>> {
    private final C                             duration;
    private final List<ValuedRange<C, Integer>> ranges;

    @SafeVarargs
    public static <C extends Comparable<? super C>> Iterations<C> of(C duration,
                                                                     ValuedRange<C, Integer>... iterations) {
        return of(duration, asList(iterations));
    }

    public static <C extends Comparable<? super C>> Iterations<C> of(C duration,
                                                                     List<ValuedRange<C, Integer>> iterations) {
        return new Iterations<>(duration, iterations);
    }

    private Iterations(C duration, List<ValuedRange<C, Integer>> ranges) {
        this.duration = duration;
        this.ranges = ValuedRange.toDisjointIntervals(ranges);
        Iteration.ensureDurationNotExceeded(this.ranges, duration, ValuedRange::getRange);
    }

    public long findUniqueIterationCount() {
        return ranges.stream().map(ValuedRange::getValue).distinct().count();
    }
}
