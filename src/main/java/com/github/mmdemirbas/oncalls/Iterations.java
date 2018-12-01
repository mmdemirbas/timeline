package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.ArrayList;
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
    private final List<ValuedRange<C, Integer>> iterations;

    @SafeVarargs
    public static <C extends Comparable<? super C>> Iterations<C> of(C duration,
                                                                     ValuedRange<C, Integer>... iterations) {
        return of(duration, asList(iterations));
    }

    public static <C extends Comparable<? super C>> Iterations<C> of(C duration,
                                                                     List<ValuedRange<C, Integer>> iterations) {
        return new Iterations<>(duration, iterations);
    }

    private Iterations(C duration, List<ValuedRange<C, Integer>> iterations) {
        this.duration = duration;
        this.iterations = iterations;

        C maxSubrangeEnd = iterations.stream()
                                     .map(it -> it.getRange().getEndExclusive())
                                     .max(Comparator.naturalOrder())
                                     .orElse(null);
        if (maxSubrangeEnd == null) {
            throw new NullPointerException("Iteration has no sub-ranges.");
        } else if (duration.compareTo(maxSubrangeEnd) < 0) {
            throw new RuntimeException("Iteration duration couldn't be smaller than sub-ranges.");
        }
    }

    public Iterations<C> union(Iterations<C> other) {
        // todo: assumed that durations are same
        // todo: assumed that ranges doesn't intersect. Result should be a list of iteration indices instead of a single index.
        List<ValuedRange<C, Integer>> union = new ArrayList<>();
        union.addAll(iterations);
        union.addAll(other.iterations);
        return of(duration, union);
    }
}
