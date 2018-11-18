package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-18 10:33
 */
public final class EventLineTest {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void getIntervalMap_NoEvents() {
        assertIntervalMap(asList(), asList());
    }

    @Test
    public void getIntervalMap_SingleEvent_Empty() {
        Event<Integer, String> a = Event.of(Range.of(1, 1), "a");
        assertIntervalMap(asList(pair(1, asList())), asList(a));
    }

    @Test
    public void getIntervalMap_SingleEvent_NonEmpty() {
        Event<Integer, String> a = Event.of(Range.of(1, 3), "a");
        assertIntervalMap(asList(pair(1, asList(a)), pair(3, asList())), asList(a));
    }

    @Test
    public void getIntervalMap_DisjointEvents() {
        Event<Integer, String> a = Event.of(Range.of(1, 3), "a");
        Event<Integer, String> b = Event.of(Range.of(5, 7), "b");
        assertIntervalMap(asList(pair(1, asList(a)), pair(3, asList()), pair(5, asList(b)), pair(7, asList())),
                          asList(a, b));
    }

    @Test
    public void getIntervalMap_SuccessiveEvents() {
        Event<Integer, String> a = Event.of(Range.of(1, 3), "a");
        Event<Integer, String> b = Event.of(Range.of(3, 5), "b");
        assertIntervalMap(asList(pair(1, asList(a)), pair(3, asList(b)), pair(5, asList())), asList(a, b));
    }

    @Test
    public void getIntervalMap_IntersectingEvents() {
        Event<Integer, String> a = Event.of(Range.of(1, 5), "a");
        Event<Integer, String> b = Event.of(Range.of(3, 7), "b");
        assertIntervalMap(asList(pair(1, asList(a)), pair(3, asList(a, b)), pair(5, asList(b)), pair(7, asList())),
                          asList(a, b));
    }

    @Test
    public void getIntervalMap_OverlappingEvents() {
        Event<Integer, String> a = Event.of(Range.of(1, 7), "a");
        Event<Integer, String> b = Event.of(Range.of(3, 5), "b");
        assertIntervalMap(asList(pair(1, asList(a)), pair(3, asList(a, b)), pair(5, asList(a)), pair(7, asList())),
                          asList(a, b));
    }

    private static void assertIntervalMap(List<Entry<Integer, List<Event<Integer, String>>>> expected,
                                          Iterable<Event<Integer, String>> events) {
        EventLine<Integer, String> eventLine = EventLine.of(events);
        assertEquals(mapOf(expected), eventLine.getIntervalMap());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static <K, V> Map<K, V> mapOf(List<? extends Entry<K, V>> entries) {
        Map<K, V> map = new LinkedHashMap<>();
        for (Entry<K, V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    private static <K, V> Entry<K, V> pair(K key, V value) {
        return new SimpleImmutableEntry<>(key, value);
    }
}