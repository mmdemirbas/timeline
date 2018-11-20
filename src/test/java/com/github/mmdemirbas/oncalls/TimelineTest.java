package com.github.mmdemirbas.oncalls;

import com.github.mmdemirbas.oncalls.Timeline.Interval;
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
final class TimelineTest {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void getIntervalMap_NoEvents() {
        assertIntervalMap(mapOf(), asList());
    }

    @Test
    void getIntervalMap_SingleEvent_Empty() {
        assertIntervalMap(mapOf(), asList(interval(1, 1, "a")));
    }

    @Test
    void getIntervalMap_SingleEvent_NonEmpty() {
        assertIntervalMap(mapOf(pair(1, asList("a")), pair(3, asList())), asList(interval(1, 3, "a")));
    }

    @Test
    void getIntervalMap_DisjointEvents() {
        assertIntervalMap(mapOf(pair(1, asList("a")), pair(3, asList()), pair(5, asList("b")), pair(7, asList())),
                          asList(interval(1, 3, "a"), interval(5, 7, "b")));
    }

    @Test
    void getIntervalMap_SuccessiveEvents() {
        assertIntervalMap(mapOf(pair(1, asList("a")), pair(3, asList("b")), pair(5, asList())),
                          asList(interval(1, 3, "a"), interval(3, 5, "b")));
    }

    @Test
    void getIntervalMap_IntersectingEvents() {
        assertIntervalMap(mapOf(pair(1, asList("a")),
                                pair(3, asList("a", "b")),
                                pair(5, asList("b")), pair(7, asList())),
                          asList(interval(1, 5, "a"), interval(3, 7, "b")));
    }

    @Test
    void getIntervalMap_OverlappingEvents() {
        assertIntervalMap(mapOf(pair(1, asList("a")),
                                pair(3, asList("a", "b")),
                                pair(5, asList("a")), pair(7, asList())),
                          asList(interval(1, 7, "a"), interval(3, 5, "b")));
    }

    private static void assertIntervalMap(Map<Integer, List<String>> expected,
                                          Collection<Interval<Integer, String>> intervals) {
        Timeline<Integer, String> timeline = Timeline.of(intervals);
        assertEquals(expected, timeline.getIntervalMap());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static <C extends Comparable<? super C>, V> Interval<C, V> interval(C startInclusive,
                                                                                C endExclusive,
                                                                                V value) {
        return Interval.of(Range.of(startInclusive, endExclusive), value);
    }
}