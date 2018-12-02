package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class UnionTimelineTest {
    @Test
    void immutability() {
        Range<Integer>                     range     = Range.of(10, 20);
        ValuedRange<Integer, String>       input     = ValuedRange.of(range, "abc");
        ValuedRange<Integer, List<String>> expected  = ValuedRange.of(range, asList("abc"));
        List<Timeline<Integer, String>>    timelines = new ArrayList<>(asList(StaticTimeline.ofIntervals(asList(input))));
        UnionTimeline<Integer, String>     timeline  = UnionTimeline.of(timelines);

        assertEquals(expected, timeline.toSegment(range).findCurrentInterval(10));

        timelines.clear();

        assertEquals(expected, timeline.toSegment(range).findCurrentInterval(10));
    }
}