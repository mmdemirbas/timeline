package com.github.mmdemirbas.oncalls;

import lombok.Getter;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.mmdemirbas.oncalls.Utils.sorted;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableNavigableMap;

/**
 * A {@link Timeline} implementation which statically associates {@link Range}s with values of type {@link V}.
 * <p>
 * This class is immutable if the generic types {@link C} and {@link V} are immutable.
 */
public final class StaticTimeline<C extends Comparable<? super C>, V> implements Timeline<C, V> {
    private static final Interval EMPTY_INTERVAL = new Interval(null, emptyList());

    @Getter private final NavigableMap<C, List<V>> intervalMap;

    public static <C extends Comparable<? super C>, V> StaticTimeline<C, V> of(Iterable<Interval<C, V>> intervals) {
        return new StaticTimeline<>(Interval.buildIntervalMap(intervals));
    }

    private StaticTimeline(NavigableMap<C, List<V>> intervalMap) {
        this.intervalMap = intervalMap;
    }

    @Override
    public StaticTimeline<C, V> toStaticTimeline(Range<? extends C> calculationRange) {
        C                        start = calculationRange.getStartInclusive();
        C                        end   = calculationRange.getEndExclusive();
        NavigableMap<C, List<V>> map   = new TreeMap<>(intervalMap.subMap(start, end));

        Entry<C, List<V>> startEntry = intervalMap.floorEntry(start);
        List<V>           startValue = (startEntry == null) ? emptyList() : startEntry.getValue();
        if (!startValue.isEmpty()) {
            map.put(start, startValue);
        }

        Entry<C, List<V>> endEntry = intervalMap.lowerEntry(end);
        List<V>           endValue = (endEntry == null) ? emptyList() : endEntry.getValue();
        if (!endValue.isEmpty()) {
            map.put(end, emptyList());
        }

        return new StaticTimeline<>(map);
    }

    /**
     * Creates a new {@link StaticTimeline} combining this timeline with the given timeline using
     * the provided {@code mergeFunction}.
     *
     * @param other         other timeline to combine
     * @param mergeFunction function to use to decide final values for an interval
     * @param <A>           type of the values of the other timeline
     * @param <U>           type of the values of the returning timeline
     */
    public <A, U> StaticTimeline<C, U> combine(StaticTimeline<C, A> other,
                                               BiFunction<List<V>, List<A>, List<U>> mergeFunction) {
        List<Interval<C, U>> intervals = new ArrayList<>();
        List<U>              values    = emptyList();
        C                    start     = null;
        C                    end       = null;

        for (C point : sorted(intervalMap.keySet(), other.intervalMap.keySet())) {
            end = point;
            List<U> mergedValues = mergeFunction.apply(findCurrentValues(point), other.findCurrentValues(point));
            if (!values.equals(mergedValues)) {
                if (!values.isEmpty()) {
                    Range<C> range = Range.of(start, end);
                    values.forEach(value -> intervals.add(new Interval<>(range, value)));
                }
                values = mergedValues;
                start = end;
            }
        }

        if (!values.isEmpty()) {
            Range<C> range = Range.of(start, end);
            values.forEach(value -> intervals.add(new Interval<>(range, value)));
        }
        return of(intervals);
    }

    /**
     * Returns values of the interval containing the specified {@code point}.
     */
    public List<V> findCurrentValues(C point) {
        return findCurrentInterval(point).getValue();
    }

    /**
     * Returns the interval containing the specified {@code point}.
     */
    public Interval<C, List<V>> findCurrentInterval(C point) {
        return getValuesOrEmpty(intervalMap.floorEntry(point));
    }

    /**
     * Returns the interval coming just after the interval containing the specified {@code point}.
     */
    public Interval<C, List<V>> findNextInterval(C point) {
        return getValuesOrEmpty(intervalMap.higherEntry(point));
    }

    private Interval<C, List<V>> getValuesOrEmpty(Entry<C, List<V>> entry) {
        if (entry != null) {
            C key     = entry.getKey();
            C nextKey = intervalMap.higherKey(key);
            if (nextKey != null) {
                return new Interval<>(Range.of(key, nextKey), entry.getValue());
            }
        }
        return EMPTY_INTERVAL;
    }

    /**
     * Represents an association between a {@link Range} and an arbitrary value.
     * <p>
     * This class is immutable if the generic types {@link C} and {@link V} is immutable.
     *
     * @param <C> type of the {@link Comparable} values used as time points
     * @param <V> type of the value associated by the time range
     */
    @Value
    public static final class Interval<C extends Comparable<? super C>, V> {
        private final Range<C> range;
        private final V        value;

        /**
         * Builds an interval map which can be considered as another form of an "interval tree".
         */
        public static <C extends Comparable<? super C>, V> NavigableMap<C, List<V>> buildIntervalMap(Iterable<Interval<C, V>> intervals) {
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
                                                                                             Function<Range<C>, C> fn) {
            NavigableMap<C, List<V>> index = new TreeMap<>();
            intervals.forEach(interval -> {
                Range<C> range = interval.range;
                if (!range.isEmpty()) {
                    C       key    = fn.apply(range);
                    List<V> values = index.computeIfAbsent(key, x -> new ArrayList<>());
                    values.add(interval.value);
                }
            });
            return index;
        }

        private static <V> List<V> orEmpty(List<V> input) {
            return (input == null) ? emptyList() : input;
        }
    }
}
