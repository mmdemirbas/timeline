package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class StaticTimelineTest {

    // todo: related tests may be grouped using junit5 all over the code

    @Test
    void immutability_input() {
        ValuedRange<Integer, String> a = ValuedRange.of(Range.of(0, 1), "a");
        ValuedRange<Integer, String> b = ValuedRange.of(Range.of(1, 2), "b");
        ValuedRange<Integer, String> c = ValuedRange.of(Range.of(2, 3), "c");

        ValuedRange<Integer, List<String>> as = ValuedRange.of(Range.of(0, 1), asList("a"));
        ValuedRange<Integer, List<String>> bs = ValuedRange.of(Range.of(1, 2), asList("b"));
        ValuedRange<Integer, List<String>> cs = ValuedRange.of(Range.of(2, 3), asList("c"));

        List<ValuedRange<Integer, String>> intervals = new ArrayList<>(asList(a, b, c));
        StaticTimeline<Integer, String>    timeline  = StaticTimeline.ofIntervals(intervals);

        assertEquals(as, timeline.findCurrentInterval(0));
        assertEquals(bs, timeline.findCurrentInterval(1));
        assertEquals(cs, timeline.findCurrentInterval(2));

        intervals.clear();

        assertEquals(as, timeline.findCurrentInterval(0));
        assertEquals(bs, timeline.findCurrentInterval(1));
        assertEquals(cs, timeline.findCurrentInterval(2));
    }

    @Test
    void immutability_findCurrentInterval() {
        assertUnmodifiable(buildTimeline().findCurrentInterval(0).getValue(), "0");
    }

    @Test
    void immutability_findNextInterval() {
        assertUnmodifiable(buildTimeline().findNextInterval(0).getValue(), "0");
    }

    @Test
    void immutability_findCurrentValues() {
        assertUnmodifiable(buildTimeline().findCurrentValues(0), "0");
    }

    @Test
    void immutability_getKeyPoints() {
        assertUnmodifiable(buildTimeline().getKeyPoints(), 123);
    }

    @Test
    void immutability_toIntervalMap() {
        assertUnmodifiable(buildTimeline().toIntervalMap());
    }

    private static StaticTimeline<Integer, String> buildTimeline() {
        return StaticTimeline.ofIntervals(asList(ValuedRange.of(Range.of(0, 1), "a"),
                                                 ValuedRange.of(Range.of(1, 2), "b"),
                                                 ValuedRange.of(Range.of(2, 3), "c")));
    }

    public static <T> void assertUnmodifiable(List<? super T> list, T element) {
        assertThrows(RuntimeException.class, list::clear);
        assertThrows(RuntimeException.class, () -> list.set(0, element));
        assertThrows(RuntimeException.class, () -> list.add(element));
        assertThrows(RuntimeException.class, () -> list.remove(element));
    }

    public static <T> void assertUnmodifiable(Set<? super T> set, T element) {
        assertThrows(RuntimeException.class, set::clear);
        assertThrows(RuntimeException.class, () -> set.add(element));
        assertThrows(RuntimeException.class, () -> set.remove(element));
    }

    public static void assertUnmodifiable(NavigableMap<? super Integer, List<String>> map) {
        assertThrows(RuntimeException.class, map::clear);
        assertThrows(RuntimeException.class, () -> map.put(1, emptyList()));
        assertThrows(RuntimeException.class, () -> map.remove(1));
        assertUnmodifiable(map.get(0), "0");
    }
}