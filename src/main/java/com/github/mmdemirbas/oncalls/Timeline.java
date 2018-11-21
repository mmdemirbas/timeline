package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.github.mmdemirbas.oncalls.Utils.map;
import static com.github.mmdemirbas.oncalls.Utils.reduce;
import static com.github.mmdemirbas.oncalls.Utils.sorted;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableNavigableMap;

/**
 * Represents a timeline of {@link Interval}s which are {@link Range}s associated with arbitrary values of type {@link V}.
 * <p>
 * This class is immutable if the generic types {@link C} and {@link V} is immutable.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-18 09:55
 */
@Value
public final class Timeline<C extends Comparable<? super C>, V> {
    private static final Interval EMPTY_INTERVAL = Interval.of(null, emptyList());
    // todo: document type params

    NavigableMap<C, List<V>> intervalMap;

    @SafeVarargs
    public static <C extends Comparable<? super C>, V> Timeline<C, V> of(Interval<C, V>... intervals) {
        return of(asList(intervals));
    }

    public static <C extends Comparable<? super C>, V> Timeline<C, V> of(Iterable<Interval<C, V>> intervals) {
        return new Timeline<>(buildIntervalMap(intervals));
    }

    private Timeline(NavigableMap<C, List<V>> intervalMap) {
        this.intervalMap = intervalMap;
    }

    /**
     * Builds an interval map which can be considered as another form of an "interval tree".
     */
    private static <C extends Comparable<? super C>, V> NavigableMap<C, List<V>> buildIntervalMap(Iterable<Interval<C, V>> intervals) {
        NavigableMap<C, List<V>> add           = indexBy(intervals, Range::getStartInclusive);
        NavigableMap<C, List<V>> remove        = indexBy(intervals, Range::getEndExclusive);
        NavigableMap<C, List<V>> intervalMap   = new TreeMap<>();
        List<V>                  ongoingEvents = new ArrayList<>();

        sorted(add.keySet(), remove.keySet()).forEach(point -> {
            ongoingEvents.addAll(orEmpty(add.get(point)));
            ongoingEvents.removeAll(orEmpty(remove.get(point)));
            intervalMap.put(point, unmodifiableList(new ArrayList<>(ongoingEvents)));
        });
        return unmodifiableNavigableMap(intervalMap);
    }

    private static <C extends Comparable<? super C>, V> NavigableMap<C, List<V>> indexBy(Iterable<Interval<C, V>> intervals,
                                                                                         Function<? super Range<C>, ? extends C> fn) {
        NavigableMap<C, List<V>> index = new TreeMap<>();
        intervals.forEach(interval -> {
            Range<C> range = interval.getRange();
            if (!range.isEmpty()) {
                C       key    = fn.apply(range);
                List<V> values = index.computeIfAbsent(key, x -> new ArrayList<>());
                values.add(interval.getValue());
            }
        });
        return index;
    }

    private static <V> List<V> orEmpty(List<V> input) {
        return (input == null) ? emptyList() : input;
    }

    public Interval<C, List<V>> findCurrentInterval(C point) {
        // todo: write tests & javadocs
        return getValuesOrEmpty(intervalMap.floorEntry(point));
    }

    public Interval<C, List<V>> findNextInterval(C point) {
        // todo: write tests & javadocs
        return getValuesOrEmpty(intervalMap.higherEntry(point));
    }

    private Interval<C, List<V>> getValuesOrEmpty(Entry<? extends C, ? extends List<V>> entry) {
        if (entry == null) {
            return EMPTY_INTERVAL;
        }
        C key     = entry.getKey();
        C nextKey = intervalMap.higherKey(key);
        if (nextKey == null) {
            return EMPTY_INTERVAL;
        }
        List<V> value = entry.getValue();
        return Interval.of(Range.of(key, nextKey), value);
    }

    public <U> Timeline<C, U> mapWith(Function<? super V, ? extends U> mapper) {
        // todo: write tests & javadocs
        NavigableMap<C, List<U>> map = new TreeMap<>();
        intervalMap.forEach((key, values) -> map.put(key, map(values, mapper)));
        return new Timeline<>(map);
    }

    public Timeline<C, V> limitWith(Range<C> range) {
        // todo: write tests & javadocs
        C                        start = range.getStartInclusive();
        C                        end   = range.getEndExclusive();
        NavigableMap<C, List<V>> map   = new TreeMap<>(intervalMap.subMap(start, end));

        List<V> startValue = valueOrEmpty(intervalMap.floorEntry(start));
        if (!startValue.isEmpty()) {
            map.put(start, startValue);
        }

        List<V> endValue = valueOrEmpty(intervalMap.lowerEntry(end));
        if (!endValue.isEmpty()) {
            map.put(end, emptyList());
        }

        return new Timeline<>(map);
    }

    private static <K, V> List<V> valueOrEmpty(Entry<K, List<V>> entry) {
        return (entry == null) ? emptyList() : entry.getValue();
    }

    public Timeline<C, V> mergeWith(Timeline<C, ? extends V> other) {
        // todo: write tests & javadocs
        return mergeWith(other, (values, otherValues) -> {
            List<V> result = new ArrayList<>();
            result.addAll(values);
            result.addAll(otherValues);
            return result;
        });
    }

    public Timeline<C, V> patchWith(Timeline<C, ? extends UnaryOperator<List<V>>> patchTimeline) {
        // todo: write tests & javadocs
        return mergeWith(patchTimeline,
                         (values, patches) -> reduce((List<V>) new ArrayList<>(values),
                                                     patches,
                                                     (acc, patch) -> patch.apply(acc)));
    }

    private <Y, Z> Timeline<C, Z> mergeWith(Timeline<C, Y> other,
                                            BiFunction<? super List<? extends V>, ? super List<? extends Y>, ? extends List<? extends Z>> mergeFunction) {
        List<Interval<C, Z>> intervals = new ArrayList<>();
        List<? extends Z>    values    = emptyList();
        C                    start     = null;
        C                    end       = null;

        Set<C> keyPointsSorted = sorted(intervalMap.keySet(), other.intervalMap.keySet());
        for (C keyPoint : keyPointsSorted) {
            end = keyPoint;
            Interval<C, List<V>> xInterval    = findCurrentInterval(keyPoint);
            Interval<C, List<Y>> yInterval    = other.findCurrentInterval(keyPoint);
            List<? extends Z>    mergedValues = mergeFunction.apply(xInterval.getValue(), yInterval.getValue());

            if (!values.equals(mergedValues)) {
                addIntervals(intervals, values, start, end);
                values = mergedValues;
                start = end;
            }
        }

        addIntervals(intervals, values, start, end);
        return of(intervals);
    }

    private static <C extends Comparable<? super C>, U> void addIntervals(List<? super Interval<C, U>> output,
                                                                          Collection<? extends U> values,
                                                                          C start,
                                                                          C end) {
        if (!values.isEmpty()) {
            Range<C> range = Range.of(start, end);
            values.forEach(value -> output.add(Interval.of(range, value)));
        }
    }

    /**
     * Represents an association between a {@link Range} and an arbitrary value.
     * <p>
     * This class is immutable if the generic types {@link C} and {@link V} is immutable.
     *
     * @author Muhammed Demirbaş
     * @since 2018-11-18 09:54
     */
    @Value
    public static final class Interval<C extends Comparable<? super C>, V> {
        Range<C> range;
        V        value;

        public static <C extends Comparable<? super C>, V> Interval<C, V> of(Range<C> range, V value) {
            return new Interval<>(range, value);
        }

        private Interval(Range<C> range, V value) {
            this.range = range;
            this.value = value;
        }
    }
}
