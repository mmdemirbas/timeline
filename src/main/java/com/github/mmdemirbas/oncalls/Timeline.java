package com.github.mmdemirbas.oncalls;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.github.mmdemirbas.oncalls.Utils.nextOrNull;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableNavigableMap;

/**
 * Represents an ordered line of {@link Interval}s which are {@link Range}-{@link V} associations.
 * This is a generalization of timeline of events.
 * <p>
 * This class is immutable if the generic types {@link C} and {@link V} is immutable.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-18 09:55
 */
@ToString
@EqualsAndHashCode
public final class Timeline<C extends Comparable<? super C>, V> {
    @Getter public final transient List<Interval<C, V>>     intervals;
    @Getter public final           NavigableMap<C, List<V>> intervalMap;

    @SafeVarargs
    public static <C extends Comparable<? super C>, V> Timeline<C, V> of(Interval<C, V>... intervals) {
        return new Timeline<>(asList(intervals));
    }

    public static <C extends Comparable<? super C>, V> Timeline<C, V> of(Collection<Interval<C, V>> intervals) {
        return new Timeline<>(intervals);
    }

    private Timeline(Collection<Interval<C, V>> intervals) {
        this.intervals = unmodifiableList(new ArrayList<>(intervals));
        intervalMap = buildIntervalMap(intervals);
    }

    /**
     * Builds an interval map which can be considered as another form of an "interval tree".
     */
    private static <C extends Comparable<? super C>, V> NavigableMap<C, List<V>> buildIntervalMap(Iterable<Interval<C, V>> intervals) {
        NavigableMap<C, List<V>> intervalMap   = new TreeMap<>();
        List<V>                  ongoingEvents = new ArrayList<>();
        buildChangePointsMap(intervals).forEach((point, changes) -> {
            changes.forEach(change -> change.accept(ongoingEvents));
            intervalMap.put(point, unmodifiableList(new ArrayList<>(ongoingEvents)));
        });
        return unmodifiableNavigableMap(intervalMap);
    }

    private static <C extends Comparable<? super C>, V> NavigableMap<C, List<Consumer<List<V>>>> buildChangePointsMap(
            Iterable<Interval<C, V>> intervals) {
        // todo: add ve remove'lar ayrı listelerde tutulabilir, daha basit olur
        NavigableMap<C, List<Consumer<List<V>>>> changePoints = new TreeMap<>();
        intervals.forEach(interval -> {
            Range<C> range = interval.getRange();
            if (!range.isEmpty()) {
                changePoints.computeIfAbsent(range.getStartInclusive(), x -> new ArrayList<>())
                            .add(list -> list.add(interval.getValue()));
                changePoints.computeIfAbsent(range.getEndExclusive(), x -> new ArrayList<>())
                            .add(list -> list.remove(interval.getValue()));
            }
        });
        return changePoints;
    }

    // todo: tests & javadocs

    public Interval<C, List<V>> findCurrentInterval(C point) {
        return getValuesOrNull(intervalMap.floorEntry(point));
    }

    public Interval<C, List<V>> findNextInterval(C point) {
        return getValuesOrNull(intervalMap.higherEntry(point));
    }

    private Interval<C, List<V>> getValuesOrNull(Entry<? extends C, ? extends List<V>> entry) {
        if (entry == null) {
            return null;
        }
        C key = entry.getKey();
        if (key == null) {
            return null;
        }
        C nextKey = intervalMap.higherKey(key);
        if (nextKey == null) {
            return null;
        }
        List<V> value = entry.getValue();
        return Interval.of(key, nextKey, value);
    }

    public <U> Timeline<C, U> mapWith(Function<? super V, ? extends U> mapper) {
        return new Timeline<>(Utils.map(intervals,
                                        interval -> Interval.of(interval.getRange(),
                                                                mapper.apply(interval.getValue()))));
    }

    public Timeline<C, V> limitWith(Range<? extends C> calculationRange) {
        return new Timeline<>(Utils.map(intervals,
                                        interval -> Interval.of(interval.getRange()
                                                                        .intersectedBy(calculationRange),
                                                                interval.getValue())));
    }

    public Timeline<C, V> mergeWith(Timeline<C, ? extends V> other) {
        return merge(other, (values, otherValues) -> {
            List<V> result = new ArrayList<>();
            result.addAll(values);
            result.addAll(otherValues);
            return result;
        });
    }

    public Timeline<C, V> patchWith(Timeline<C, ? extends UnaryOperator<List<V>>> patchTimeline) {
        return merge(patchTimeline,
                     (values, patches) -> Utils.reduce((List<V>) new ArrayList<>(values),
                                                       patches,
                                                       (acc, patch) -> patch.apply(acc)));
    }

    private <P, U> Timeline<C, U> merge(Timeline<C, P> other,
                                        BiFunction<? super List<? extends V>, ? super List<? extends P>, ? extends List<? extends U>> mergeFunction) {
        Collection<C> keyPoints = new TreeSet<>();
        keyPoints.addAll(intervalMap.keySet());
        keyPoints.addAll(other.intervalMap.keySet());

        List<Interval<C, U>> mergedIntervals = new ArrayList<>();
        Iterator<C>          iterator        = keyPoints.iterator();
        C                    current         = nextOrNull(iterator);
        C                    next            = nextOrNull(iterator);
        List<? extends U>    buffer          = new ArrayList<>();
        C                    bufferStart     = null;

        while (next != null) {
            List<V>           vs = valuesAt(current);
            List<P>           ps = other.valuesAt(current);
            List<? extends U> us = mergeFunction.apply(vs, ps);

            if (!buffer.equals(us)) {
                for (U u : buffer) {
                    mergedIntervals.add(Interval.of(bufferStart, current, u));
                }
                buffer = us;
                bufferStart = current;
            }

            current = next;
            next = nextOrNull(iterator);
        }
        for (U u : buffer) {
            mergedIntervals.add(Interval.of(bufferStart, current, u));
        }
        return new Timeline<>(mergedIntervals);
    }

    private List<V> valuesAt(C point) {
        Interval<C, List<V>> current = findCurrentInterval(point);
        return (current == null) ? emptyList() : current.getValue();
    }

    /**
     * Represents an association between a {@link Range} and an arbitrary value.
     * <p>
     * This class is immutable if the generic types {@link C} and {@link V} is immutable.
     *
     * @author Muhammed Demirbaş
     * @since 2018-11-18 09:54
     */
    @ToString
    @EqualsAndHashCode
    public static final class Interval<C extends Comparable<? super C>, V> {
        @Getter private final Range<C> range;
        @Getter private final V        value;

        public static <C extends Comparable<? super C>, V> Interval<C, V> of(C startInclusive,
                                                                             C endExclusive,
                                                                             V value) {
            return of(Range.of(startInclusive, endExclusive), value);
        }

        public static <C extends Comparable<? super C>, V> Interval<C, V> of(Range<C> range, V value) {
            return new Interval<>(range, value);
        }

        private Interval(Range<C> range, V value) {
            this.range = range;
            this.value = value;
        }
    }
}
