package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

// todo: remove author header if removed from everywhere

/**
 * @author Muhammed Demirbaş
 * @since 2018-12-01 16:04
 */
@Value
public final class Iteration<C extends Comparable<? super C>> {
    private final C              duration;
    private final List<Range<C>> ranges; // todo: write immutability tests for all classes

    @SafeVarargs
    public static <C extends Comparable<? super C>> Iteration<C> of(C duration, Range<C>... ranges) {
        return of(duration, asList(ranges));
    }

    public static <C extends Comparable<? super C>> Iteration<C> of(C duration, List<Range<C>> subRanges) {
        return new Iteration<>(duration, subRanges);
    }

    private Iteration(C duration, Collection<Range<C>> ranges) {
        this.duration = duration;
        this.ranges = Range.toDisjointRanges(ranges);
        ensureDurationNotExceeded(this.ranges, duration, it -> it);
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
                    Range<C> intersect = range.map(sum, startOffset).intersect(subRange);
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

    public Iteration<C> multiply(int count, BinaryOperator<C> sum) {
        if (count < 1)
            throw new RuntimeException("count must be >= 1, but was: " + count);

        Iteration<C> result = this;
        for (int i = 1; i < count; i++) {
            result = result.concat(this, sum);
        }
        return result;
    }

    public Iteration<C> concat(Iteration<C> other, BinaryOperator<C> sum) {
        C              totalDuration  = sum.apply(duration, other.duration);
        List<Range<C>> appendedRanges = new ArrayList<>(ranges);
        appendedRanges.addAll(other.rangesWithOffset(duration, sum));
        return of(totalDuration, appendedRanges);
    }

    private List<Range<C>> rangesWithOffset(C offset, BinaryOperator<C> sum) {
        return ranges.stream().map(range -> range.map(sum, offset)).collect(Collectors.toList());
    }
}
