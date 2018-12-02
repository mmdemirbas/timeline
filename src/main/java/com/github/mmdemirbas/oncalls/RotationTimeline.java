package com.github.mmdemirbas.oncalls;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A skeletal {@link Timeline} implementation which represents a recurring period in a finite interval.
 * <p>
 * This class is immutable, if the generic types {@link C}, {@link U} and {@link V} are immutable.
 *
 * @param <U> type of time points of {@link Iterations}
 */
public abstract class RotationTimeline<C extends Comparable<? super C>, U extends Comparable<? super U>, V> implements
                                                                                                            Timeline<C, V> {
    private final Range<C>      rotationRange;
    private final Iterations<U> iterations;

    public RotationTimeline(Range<C> rotationRange, Iterations<U> iterations) {
        this.rotationRange = requireNonNull(rotationRange, "rotationRange");
        this.iterations = requireNonNull(iterations, "iterations");
    }

    @Override
    public final TimelineSegment<C, V> toSegment(Range<C> calculationRange) {
        // todo: test all timelines with null calculationRange
        boolean  noLimit              = calculationRange == null;
        Range<C> effectiveRange       = noLimit ? rotationRange : rotationRange.intersect(calculationRange);
        long     startIndex           = indexAtPoint(effectiveRange.getStartInclusive());
        long     endIndex             = indexAtPoint(effectiveRange.getEndExclusive());
        C        rangeOffset          = pointAtIndex(startIndex);
        long     uniqueIterationCount = iterations.findUniqueIterationCount();
        long     indexOffset          = uniqueIterationCount * startIndex;

        // todo: rename other valuedRanges to intervals
        List<ValuedRange<C, V>> intervals = new ArrayList<>();
        for (long index = startIndex; index <= endIndex; index++) {
            for (ValuedRange<U, Integer> valuedRange : iterations.getRanges()) {
                Range<U> range            = valuedRange.getRange();
                long     recipientIndex   = indexOffset + valuedRange.getValue();
                V        recipient        = recipientAtIndex(recipientIndex);
                C        finalRangeOffset = rangeOffset;
                intervals.add(ValuedRange.of(range.map(value -> add(finalRangeOffset, value)).intersect(effectiveRange),
                                             recipient));
            }
            rangeOffset = add(rangeOffset, iterations.getDuration());
            indexOffset += uniqueIterationCount;
        }
        return StaticTimeline.ofIntervals(intervals);
    }

    private C add(C offset, U unitDuration) {
        C point = pointAtIndex(offset, unitDuration, 1);
        return requireNonNull(point, "point");
    }

    private C pointAtIndex(long iterationIndex) {
        U unitDuration = iterations.getDuration();
        C offset       = rotationRange.getStartInclusive();
        C point        = pointAtIndex(offset, unitDuration, iterationIndex);
        return requireNonNull(point, "point");
    }

    private long indexAtPoint(C point) {
        U    unitDuration = iterations.getDuration();
        C    offset       = rotationRange.getStartInclusive();
        long index        = indexAtPoint(offset, unitDuration, point);
        return index;
    }

    protected abstract long indexAtPoint(C offset, U unitDuration, C point);

    protected abstract C pointAtIndex(C offset, U unitDuration, long iterationIndex);

    protected abstract V recipientAtIndex(long recipientIndex);
}
