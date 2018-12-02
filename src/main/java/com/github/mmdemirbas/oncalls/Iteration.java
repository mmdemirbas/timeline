package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.Collection;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

@Value
public final class Iteration<C extends Comparable<? super C>> {
    private final C              duration;
    private final List<Range<C>> ranges;

    @SafeVarargs
    public static <C extends Comparable<? super C>> Iteration<C> of(C duration, Range<C>... ranges) {
        return of(duration, asList(ranges));
    }

    public static <C extends Comparable<? super C>> Iteration<C> of(C duration, List<Range<C>> subRanges) {
        return new Iteration<>(duration, subRanges);
    }

    private Iteration(C duration, Collection<Range<C>> ranges) {
        this.duration = requireNonNull(duration, "duration");
        this.ranges = Range.toDisjointRanges(ranges);
        Iterations.ensureDurationNotExceeded(this.ranges, duration, it -> it);
    }

    public Iterations<C> toIterations() {
        return Iterations.of(duration,
                             ranges.stream().map(range -> ValuedRange.of(range, 0)).collect(Collectors.toList()));
    }

    public IterationBuilder<C> newBuilder(BinaryOperator<C> sum) {
        return IterationBuilder.of(this, sum);
    }
}
