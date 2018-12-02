package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

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
        ensureDurationNotExceeded(this.ranges, duration, ValuedRange::getRange);
    }

    public static <C extends Comparable<? super C>, T> void ensureDurationNotExceeded(Collection<T> elements,
                                                                                      C duration,
                                                                                      Function<? super T, Range<C>> getRange) {
        C max = elements.stream().map(getRange).map(Range::getEndExclusive).max(Comparator.naturalOrder()).orElse(null);
        if (max == null) {
            throw new NullPointerException("Iteration has no sub-ranges.");
        }
        if (duration.compareTo(max) < 0) {
            throw new RuntimeException(String.format(
                    "Sub-ranges exceed the iteration end. Iteration ends at: %s, sub-ranges ends at: %s",
                    duration,
                    max));
        }
    }

    public long findUniqueIterationCount() {
        return ranges.stream().map(ValuedRange::getValue).distinct().count();
    }
}
