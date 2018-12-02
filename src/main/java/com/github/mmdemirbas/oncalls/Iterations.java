package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Muhammed Demirbaş
 * @since 2018-12-01 16:04
 */
@Value
final class Iterations<C extends Comparable<? super C>> {
    private final C                             duration;
    private final List<ValuedRange<C, Integer>> ranges; // todo: ensure immutability

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
        this.ranges = ranges;

        // todo: make ranges disjoint

        C max = ranges.stream().map(it -> it.getRange().getEndExclusive()).max(Comparator.naturalOrder()).orElse(null);
        if (max == null) {
            throw new NullPointerException("Iteration has no sub-ranges.");
        }
        if (duration.compareTo(max) < 0) {
            throw new RuntimeException("Iteration duration couldn't be smaller than sub-ranges.");
        }
    }

    public long findUniqueIterationCount() {
        return ranges.stream().map(it -> it.getValue()).distinct().count();
    }
}
