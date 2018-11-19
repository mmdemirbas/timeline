package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.github.mmdemirbas.oncalls.Utils.mapOf;
import static com.github.mmdemirbas.oncalls.Utils.pair;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-18 10:33
 */
public final class TimelineTest {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void getIntervalMap_NoEvents() {
        assertIntervalMap(mapOf(), asList());
    }

    @Test
    public void getIntervalMap_SingleEvent_Empty() {
        Event<String, Integer> a = new Event<>("a", Range.of(1, 1));
        assertIntervalMap(mapOf(), asList(a));
    }

    @Test
    public void getIntervalMap_SingleEvent_NonEmpty() {
        Event<String, Integer> a = new Event<>("a", Range.of(1, 3));
        assertIntervalMap(mapOf(pair(1, asList(a)), pair(3, asList())), asList(a));
    }

    @Test
    public void getIntervalMap_DisjointEvents() {
        Event<String, Integer> a = new Event<>("a", Range.of(1, 3));
        Event<String, Integer> b = new Event<>("b", Range.of(5, 7));
        assertIntervalMap(mapOf(pair(1, asList(a)), pair(3, asList()), pair(5, asList(b)), pair(7, asList())),
                          asList(a, b));
    }

    @Test
    public void getIntervalMap_SuccessiveEvents() {
        Event<String, Integer> a = new Event<>("a", Range.of(1, 3));
        Event<String, Integer> b = new Event<>("b", Range.of(3, 5));
        assertIntervalMap(mapOf(pair(1, asList(a)), pair(3, asList(b)), pair(5, asList())), asList(a, b));
    }

    @Test
    public void getIntervalMap_IntersectingEvents() {
        Event<String, Integer> a = new Event<>("a", Range.of(1, 5));
        Event<String, Integer> b = new Event<>("b", Range.of(3, 7));
        assertIntervalMap(mapOf(pair(1, asList(a)), pair(3, asList(a, b)), pair(5, asList(b)), pair(7, asList())),
                          asList(a, b));
    }

    @Test
    public void getIntervalMap_OverlappingEvents() {
        Event<String, Integer> a = new Event<>("a", Range.of(1, 7));
        Event<String, Integer> b = new Event<>("b", Range.of(3, 5));
        assertIntervalMap(mapOf(pair(1, asList(a)), pair(3, asList(a, b)), pair(5, asList(a)), pair(7, asList())),
                          asList(a, b));
    }

    private static void assertIntervalMap(Map<Integer, List<Event<String, Integer>>> expected,
                                          Collection<Event<String, Integer>> events) {
        Timeline<Integer, String> timeline = Timeline.of(events);
        assertEquals(expected, timeline.getIntervalMap());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}