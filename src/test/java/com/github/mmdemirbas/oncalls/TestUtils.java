package com.github.mmdemirbas.oncalls;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-22 16:43
 */
final class TestUtils {
    @SafeVarargs
    static <K, V> Map<K, V> mapOf(Entry<K, V>... entries) {
        Map<K, V> map = new LinkedHashMap<>();
        asList(entries).forEach(entry -> map.put(entry.getKey(), entry.getValue()));
        return map;
    }

    static <K, V> Entry<K, V> pair(K key, V value) {
        return new SimpleImmutableEntry<>(key, value);
    }

    static <T, R> List<R> map(Collection<? extends T> items, Function<? super T, ? extends R> mapper) {
        return items.stream().map(mapper).collect(Collectors.toList());
    }
}
