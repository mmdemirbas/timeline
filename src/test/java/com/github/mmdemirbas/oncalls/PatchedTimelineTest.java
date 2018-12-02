package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class PatchedTimelineTest {
    @Test
    void immutability() {
        Range<Integer>                                    range    = Range.of(10, 20);
        ValuedRange<Integer, String>                      base     = ValuedRange.of(range, "base");
        ValuedRange<Integer, UnaryOperator<List<String>>> patch    = ValuedRange.of(range, x -> asList("patch"));
        ValuedRange<Integer, List<String>>                expected = ValuedRange.of(range, asList("patch"));

        List<Timeline<Integer, UnaryOperator<List<String>>>> input = new ArrayList<>(asList(StaticTimeline.ofIntervals(
                asList(patch))));

        PatchedTimeline<Integer, String> timeline = PatchedTimeline.of(StaticTimeline.ofIntervals(asList(base)), input);

        assertEquals(expected, timeline.toSegment(range).findCurrentInterval(10));

        input.clear();

        assertEquals(expected, timeline.toSegment(range).findCurrentInterval(10));
    }
}