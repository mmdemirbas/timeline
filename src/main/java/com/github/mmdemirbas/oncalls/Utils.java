package com.github.mmdemirbas.oncalls;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableNavigableMap;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-19 12:20
 */
public final class Utils {
    public static <T extends Comparable<? super T>> T maxOf(T a, T b) {
        return (a.compareTo(b) > 0) ? a : b;
    }

    public static <T extends Comparable<? super T>> T minOf(T a, T b) {
        return (a.compareTo(b) < 0) ? a : b;
    }

    @SafeVarargs
    public static <K, V> Map<K, V> mapOf(Entry<K, V>... entries) {
        Map<K, V> map = new LinkedHashMap<>();
        for (Entry<K, V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static <K, V> Entry<K, V> pair(K key, V value) {
        return new SimpleImmutableEntry<>(key, value);
    }

    public static <T> T todo() {
        throw new RuntimeException("not implemented!");
    }

    /**
     * Builds an interval map which can be considered as another form of an "interval tree".
     */
    static <Point extends Comparable<? super Point>, T> NavigableMap<Point, List<T>> buildIntervalMap(Iterable<? extends T> items,
                                                                                                      Function<? super T, Range<Point>> getRange) {
        // firstly, detect change points and which changes occur at each point
        NavigableMap<Point, List<FlaggedValue<T>>> changePoints = buildChangePointsMap(items, getRange);

        // then, build the interval map using a sweep line algorithm on the detected change points
        NavigableMap<Point, List<T>> intervalMap   = new TreeMap<>();
        List<T>                      ongoingEvents = new ArrayList<>();
        changePoints.forEach((point, changes) -> {
            changes.forEach(change -> {
                if (change.isFlag()) {
                    ongoingEvents.add(change.getValue());
                } else {
                    ongoingEvents.remove(change.getValue());
                }
            });
            intervalMap.put(point, unmodifiableList(new ArrayList<>(ongoingEvents)));
        });
        return unmodifiableNavigableMap(intervalMap);
    }

    private static <Point extends Comparable<? super Point>, T> NavigableMap<Point, List<FlaggedValue<T>>> buildChangePointsMap(
            Iterable<? extends T> items,
            Function<? super T, Range<Point>> getRange) {
        NavigableMap<Point, List<FlaggedValue<T>>> changePoints = new TreeMap<>();
        items.forEach(item -> {
            Range<Point> range = getRange.apply(item);
            if (!range.isEmpty()) {
                changePoints.computeIfAbsent(range.getStartInclusive(), x -> new ArrayList<>())
                            .add(new FlaggedValue<>(item, true));
                changePoints.computeIfAbsent(range.getEndExclusive(), x -> new ArrayList<>())
                            .add(new FlaggedValue<>(item, false));
            }
        });
        return changePoints;
    }

    public static <V> V getModuloIndex(List<V> list, long index) {
        return list.get((int) (index % list.size()));
    }

    public static <K, V> List<V> getValueOrNull(Entry<K, ? extends List<V>> entry) {
        return (entry == null) ? emptyList() : entry.getValue();
    }

    public static long nanosOf(Instant instant) {
        return TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
    }

    public static <T> T nextOrNull(Iterator<? extends T> iterator) {
        return iterator.hasNext() ? iterator.next() : null;
    }

    @SafeVarargs
    public static <T> List<T> merge(List<T>... lists) {
        List<T> result = new ArrayList<>();
        Stream.of(lists)
              .forEachOrdered(result::addAll);
        return result;
    }

    static ZonedDateTime instantToZonedDateTime(Instant instant) {
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    static Instant min(int minute) {
        return Instant.ofEpochMilli(TimeUnit.MINUTES.toMillis(minute));
    }

    static Instant hour(int hour) {
        return Instant.ofEpochMilli(TimeUnit.HOURS.toMillis(hour));
    }

    static String format(Instant instant) {
        OffsetDateTime dateTime   = utc(instant);
        int            day        = dateTime.getDayOfYear() - 1;
        String         hourMinute = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        return (day == 0) ? hourMinute : String.format("d%d:%s", day, hourMinute);
    }

    private static OffsetDateTime utc(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }

    static <T> String joinLines(Collection<? extends T> items, Function<? super T, String> toString) {
        return String.join("\n", map(items, toString));
    }

    private static <T, R> List<R> map(Collection<? extends T> items, Function<? super T, ? extends R> toString) {
        return items.stream()
                    .map(toString)
                    .collect(Collectors.toList());
    }
}
