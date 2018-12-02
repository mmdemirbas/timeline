package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

@Value
final class Iterations<C extends Comparable<? super C>> {
    private final C                             duration;
    private final List<ValuedRange<C, Integer>> ranges;

    @SafeVarargs
    public static <C extends Comparable<? super C>> Iterations<C> of(C duration, ValuedRange<C, Integer>... ranges) {
        return of(duration, asList(ranges));
    }

    public static <C extends Comparable<? super C>> Iterations<C> of(C duration, List<ValuedRange<C, Integer>> ranges) {
        return new Iterations<>(duration, ranges);
    }

    private Iterations(C duration, Collection<ValuedRange<C, Integer>> ranges) {
        this.duration = requireNonNull(duration, "duration");
        this.ranges = ValuedRange.toDisjointIntervals(ranges);
        ensureDurationNotExceeded(this.ranges, duration, ValuedRange::getRange);
    }

    public static <C extends Comparable<? super C>, T> void ensureDurationNotExceeded(Collection<T> elements,
                                                                                      C allowedEnd,
                                                                                      Function<? super T, Range<C>> getRange) {
        C elementsEnd = elements.stream()
                                .map(getRange)
                                .map(Range::getEndExclusive)
                                .max(Comparator.naturalOrder())
                                .orElse(null);
        requireNonNull(elementsEnd, "Iteration has no sub-ranges.");
        if (allowedEnd.compareTo(elementsEnd) < 0) {
            // todo: improve exception message considering the method used from multiple places.
            throw new RuntimeException(String.format(
                    "Sub-ranges exceed the iteration end. Iteration ends at: %s, sub-ranges ends at: %s",
                    allowedEnd,
                    elementsEnd));
        }
    }

    public long findUniqueIterationCount() {
        return ranges.stream().map(ValuedRange::getValue).distinct().count();
    }
}
