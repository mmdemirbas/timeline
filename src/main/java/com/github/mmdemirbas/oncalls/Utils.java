package com.github.mmdemirbas.oncalls;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-19 12:20
 */
public final class Utils {
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

    public static <T, R> List<R> map(Collection<? extends T> items, Function<? super T, ? extends R> mapper) {
        return items.stream()
                    .map(mapper)
                    .collect(Collectors.toList());
    }

    public static <T, R> R reduce(R seed,
                                  Iterable<? extends T> items,
                                  BiFunction<? super R, ? super T, ? extends R> reduce) {
        R result = seed;
        if (items != null) {
            for (T item : items) {
                result = reduce.apply(result, item);
            }
        }
        return result;
    }

    @SafeVarargs
    public static <C extends Comparable<? super C>> Set<C> sorted(Collection<? extends C>... collections) {
        return sortedBy(it -> it, collections);
    }

    @SafeVarargs
    public static <T, C extends Comparable<? super C>> Set<T> sortedBy(Function<? super T, ? extends C> getter,
                                                                       Collection<? extends T>... collections) {
        Set<T> copy = new TreeSet<>(Comparator.comparing(getter));
        Stream.of(collections)
              .forEach(copy::addAll);
        return copy;
    }

    public static <C extends Comparable<? super C>> C maxOf(C x, C y) {
        return (x.compareTo(y) > 0) ? x : y;
    }

    public static <C extends Comparable<? super C>> C minOf(C x, C y) {
        return (x.compareTo(y) < 0) ? x : y;
    }
}
