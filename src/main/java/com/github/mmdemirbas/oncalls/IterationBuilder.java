package com.github.mmdemirbas.oncalls;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Muhammed Demirbaş
 * @since 2018-12-02 21:41
 */
public final class IterationBuilder<C extends Comparable<? super C>> {
    private final BinaryOperator<C> sum;
    private       Iteration<C>      iteration;

    public static <C extends Comparable<? super C>> IterationBuilder<C> of(Iteration<C> iteration,
                                                                           BinaryOperator<C> sum) {
        return new IterationBuilder<>(iteration, sum);
    }

    private IterationBuilder(Iteration<C> iteration, BinaryOperator<C> sum) {
        // todo: add null-checks for other classes also
        this.iteration = Objects.requireNonNull(iteration, "iteration");
        this.sum = Objects.requireNonNull(sum, "sum");
    }

    public Iteration<C> build() {
        return iteration;
    }

    public Iterations<C> split(Iteration<C> unit, C startOffset, Consumer<IterationBuilder<C>> modify) {
        return split(modify(unit, modify), startOffset);
    }

    public Iterations<C> split(Iteration<C> unit, C startOffset) {
        return split(unit.toIterations(), startOffset);
    }

    public Iterations<C> split(Iterations<C> units, C startOffset) {
        C                             unitDuration         = units.getDuration();
        List<ValuedRange<C, Integer>> iterations           = new ArrayList<>();
        int                           valueOffset          = 0;
        long                          uniqueIterationCount = units.findUniqueIterationCount();

        while (startOffset.compareTo(iteration.getDuration()) < 0) {
            for (ValuedRange<C, Integer> unit : units.getRanges()) {
                Range<C> range = unit.getRange();
                Integer  value = unit.getValue();

                for (Range<C> subRange : iteration.getRanges()) {
                    Range<C> intersect = range.map(sum, startOffset).intersect(subRange);
                    if (!intersect.isEmpty()) {
                        iterations.add(ValuedRange.of(intersect, valueOffset + value));
                    }
                }
            }
            valueOffset += uniqueIterationCount;
            startOffset = sum.apply(startOffset, unitDuration);
        }

        return Iterations.of(iteration.getDuration(), iterations);
    }

    public IterationBuilder<C> repeat(int count) {
        if (count < 1)
            throw new RuntimeException("count must be >= 1, but was: " + count);

        Iteration<C> unit = iteration;
        for (int i = 1; i < count; i++) {
            concat(unit);
        }
        return this;
    }

    public IterationBuilder<C> concat(Iteration<C> other, Consumer<IterationBuilder<C>> modify) {
        return concat(modify(other, modify));
    }

    public IterationBuilder<C> concat(Iteration<C> other) {
        C              totalDuration  = sum.apply(iteration.getDuration(), other.getDuration());
        List<Range<C>> appendedRanges = new ArrayList<>(iteration.getRanges());
        appendedRanges.addAll(rangesWithOffset(other, iteration.getDuration()));
        iteration = Iteration.of(totalDuration, appendedRanges);
        return this;
    }

    private List<Range<C>> rangesWithOffset(Iteration<C> other, C offset) {
        return other.getRanges().stream().map(range -> range.map(sum, offset)).collect(Collectors.toList());
    }

    private Iteration<C> modify(Iteration<C> other, Consumer<IterationBuilder<C>> modify) {
        IterationBuilder<C> builder = other.newBuilder(sum);
        modify.accept(builder);
        return builder.iteration;
    }
}
