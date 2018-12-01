package com.github.mmdemirbas.oncalls;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

/**
 * A {@link Timeline} implementation which represents a recurring period in a finite interval.
 * <p>
 * This class is immutable, if the generic types {@link C} and {@link V} are immutable.
 */
public final class RotationTimeline<C extends Comparable<? super C>, U extends Comparable<? super U>, V> implements
                                                                                                         Timeline<C, V> {
    // todo: try to split. 4 props are too much!
    private final Range<C>                            rotationRange;
    private final Iterations<U>                       iterations;
    private final PointToIndexConverter<C, ? super U> pointToIndexConverter;
    private final IndexToPoinConverter<C, ? super U>  indexToPoinConverter;
    private final LongFunction<? extends V>           recipientSupplier;

    public RotationTimeline(Range<C> rotationRange,
                            Iterations<U> iterations,
                            PointToIndexConverter<C, ? super U> pointToIndexConverter,
                            IndexToPoinConverter<C, ? super U> indexToPoinConverter,
                            LongFunction<? extends V> recipientSupplier) {
        this.rotationRange = rotationRange;
        this.iterations = iterations;
        this.pointToIndexConverter = pointToIndexConverter;
        this.indexToPoinConverter = indexToPoinConverter;
        this.recipientSupplier = recipientSupplier;
    }

    @Override
    public TimelineSegment<C, V> toSegment(Range<C> calculationRange) {
        List<ValuedRange<C, V>> valuedRanges         = new ArrayList<>();
        Range<C>                effectiveRange       = rotationRange.intersect(calculationRange);
        long                    startIndex           = indexAtPoint(effectiveRange.getStartInclusive());
        long                    endIndex             = indexAtPoint(effectiveRange.getEndExclusive());
        C                       rangeOffset          = pointAtIndex(startIndex);
        long                    uniqueIterationCount = iterations.findUniqueIterationCount();
        long                    indexOffset          = uniqueIterationCount * startIndex;

        for (long index = startIndex; index <= endIndex; index++) {
            for (ValuedRange<U, Integer> valuedRange : iterations.getRanges()) {
                Range<U> range          = valuedRange.getRange();
                long     recipientIndex = indexOffset + valuedRange.getValue();
                V        recipient      = recipientSupplier.apply(recipientIndex);
                valuedRanges.add(ValuedRange.of(Range.of(indexToPoinConverter.pointAtIndex(rangeOffset,
                                                                                           range.getStartInclusive(),
                                                                                           1),
                                                         indexToPoinConverter.pointAtIndex(rangeOffset,
                                                                                           range.getEndExclusive(),
                                                                                           1))
                                                     .intersect(effectiveRange), recipient));
            }
            rangeOffset = indexToPoinConverter.pointAtIndex(rangeOffset, iterations.getDuration(), 1);
            indexOffset += uniqueIterationCount;
        }
        return StaticTimeline.ofIntervals(valuedRanges);
    }

    private C pointAtIndex(long index) {
        U unitDuration = iterations.getDuration();
        C offset       = rotationRange.getStartInclusive();
        return indexToPoinConverter.pointAtIndex(offset, unitDuration, index);
    }

    private long indexAtPoint(C point) {
        U unitDuration = iterations.getDuration();
        C offset       = rotationRange.getStartInclusive();
        return pointToIndexConverter.indexAtPoint(offset, unitDuration, point);
    }

    public interface PointToIndexConverter<C extends Comparable<? super C>, U extends Comparable<? super U>> {
        long indexAtPoint(C offset, U unitDuration, C point);
    }

    public interface IndexToPoinConverter<C extends Comparable<? super C>, U extends Comparable<? super U>> {
        C pointAtIndex(C offset, U unitDuration, long index);
    }
}
