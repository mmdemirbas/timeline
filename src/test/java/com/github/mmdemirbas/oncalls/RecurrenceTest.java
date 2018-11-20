package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static com.github.mmdemirbas.oncalls.Utils.mapOf;
import static com.github.mmdemirbas.oncalls.Utils.pair;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-19 14:55
 */
final class RecurrenceTest {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void toDisjointRanges_NoRange() {
        assertDisjointRanges(asList(), asList());
    }

    @Test
    void toDisjointRanges_EmptyRange() {
        assertDisjointRanges(asList(), asList(Range.of(2, 2)));
    }

    @Test
    void toDisjointRanges_EmptyRanges() {
        assertDisjointRanges(asList(), asList(Range.of(1, 1), Range.of(5, 5)));
    }

    @Test
    void toDisjointRanges_SingleRange() {
        assertDisjointRanges(asList(Range.of(2, 4)), asList(Range.of(2, 4)));
    }

    @Test
    void toDisjointRanges_DisjointRanges() {
        assertDisjointRanges(asList(Range.of(2, 4), Range.of(8, 10)), asList(Range.of(2, 4), Range.of(8, 10)));
    }

    @Test
    void toDisjointRanges_SuccessiveRanges() {
        assertDisjointRanges(asList(Range.of(2, 8)), asList(Range.of(2, 4), Range.of(4, 8)));
    }

    @Test
    void toDisjointRanges_IntersectingRanges() {
        assertDisjointRanges(asList(Range.of(2, 8)), asList(Range.of(2, 6), Range.of(4, 8)));
    }

    @Test
    void toDisjointRanges_OverlappingRanges() {
        assertDisjointRanges(asList(Range.of(2, 8)), asList(Range.of(2, 8), Range.of(4, 6)));
    }

    @Test
    void toDisjointRanges_DuplicateRanges() {
        assertDisjointRanges(asList(Range.of(2, 8)), asList(Range.of(2, 8), Range.of(2, 8)));
    }

    private static void assertDisjointRanges(List<Range<Integer>> expected, Collection<Range<Integer>> ranges) {
        assertEquals(expected, Recurrence.toDisjointRanges(ranges));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void toTimeline_SimplestCase() {
        assertTimeline(range(0, 50),
                       iteration(10),
                       asList(),
                       range(0, 50),
                       mapOf(pair(zonedDateTime(0), asList((long) 0)),
                             pair(zonedDateTime(10), asList((long) 1)),
                             pair(zonedDateTime(20), asList((long) 2)),
                             pair(zonedDateTime(30), asList((long) 3)),
                             pair(zonedDateTime(40), asList((long) 4)),
                             pair(zonedDateTime(50), asList())));
    }

    @Test
    void toTimeline_RecurrenceFinishedBeforeIteration() {
        assertTimeline(range(0, 45),
                       iteration(10),
                       asList(),
                       range(0, 45),
                       mapOf(pair(zonedDateTime(0), asList((long) 0)),
                             pair(zonedDateTime(10), asList((long) 1)),
                             pair(zonedDateTime(20), asList((long) 2)),
                             pair(zonedDateTime(30), asList((long) 3)),
                             pair(zonedDateTime(40), asList((long) 4)),
                             pair(zonedDateTime(45), asList())));
    }

    @Test
    void toTimeline_CalculationRangeBeforeRecurrenceRange() {
        assertTimeline(range(20, 40), iteration(10), asList(), range(0, 20), mapOf());
    }

    @Test
    void toTimeline_RecurrenceRangeBeforeCalculationRange() {
        assertTimeline(range(0, 20), iteration(10), asList(), range(20, 40), mapOf());
    }

    @Test
    void toTimeline_CalculationRangeContainsRecurrenceRange() {
        assertTimeline(range(10, 30),
                       iteration(10),
                       asList(),
                       range(0, 50),
                       mapOf(pair(zonedDateTime(10), asList((long) 0)),
                             pair(zonedDateTime(20), asList((long) 1)),
                             pair(zonedDateTime(30), asList())));
    }

    @Test
    void toTimeline_RecurrenceRangeContainsCalculationRange() {
        assertTimeline(range(0, 50),
                       iteration(10),
                       asList(),
                       range(10, 30),
                       mapOf(pair(zonedDateTime(10), asList((long) 1)),
                             pair(zonedDateTime(20), asList((long) 2)),
                             pair(zonedDateTime(30), asList())));
    }

    @Test
    void toTimeline_CalculationRangeContainsIncompleteIterations() {
        assertTimeline(range(0, 50),
                       iteration(10),
                       asList(),
                       range(15, 35),
                       mapOf(pair(zonedDateTime(15), asList((long) 1)),
                             pair(zonedDateTime(20), asList((long) 2)),
                             pair(zonedDateTime(30), asList((long) 3)),
                             pair(zonedDateTime(35), asList())));
    }

    @Test
    void toTimeline_SingleSubRange() {
        assertTimeline(range(0, 30),
                       iteration(10),
                       asList(subrange(3, 7)),
                       range(0, 30),
                       mapOf(pair(zonedDateTime(3), asList((long) 0)),
                             pair(zonedDateTime(7), asList()),
                             pair(zonedDateTime(13), asList((long) 1)),
                             pair(zonedDateTime(17), asList()),
                             pair(zonedDateTime(23), asList((long) 2)),
                             pair(zonedDateTime(27), asList())));
    }

    @Test
    void toTimeline_MultipleSubRanges() {
        assertTimeline(range(0, 30),
                       iteration(10),
                       asList(subrange(3, 5), subrange(7, 9)),
                       range(0, 30),
                       mapOf(pair(zonedDateTime(3), asList((long) 0)),
                             pair(zonedDateTime(5), asList()),
                             pair(zonedDateTime(7), asList((long) 0)),
                             pair(zonedDateTime(9), asList()),
                             pair(zonedDateTime(13), asList((long) 1)),
                             pair(zonedDateTime(15), asList()),
                             pair(zonedDateTime(17), asList((long) 1)),
                             pair(zonedDateTime(19), asList()),
                             pair(zonedDateTime(23), asList((long) 2)),
                             pair(zonedDateTime(25), asList()),
                             pair(zonedDateTime(27), asList((long) 2)),
                             pair(zonedDateTime(29), asList())));
    }

    @Test
    void toTimeline_IncompleteIterationsAndMultipleSubRanges() {
        assertTimeline(range(0, 30),
                       iteration(10),
                       asList(subrange(3, 5), subrange(7, 9)),
                       range(14, 28),
                       mapOf(pair(zonedDateTime(14), asList((long) 1)),
                             pair(zonedDateTime(15), asList()),
                             pair(zonedDateTime(17), asList((long) 1)),
                             pair(zonedDateTime(19), asList()),
                             pair(zonedDateTime(23), asList((long) 2)),
                             pair(zonedDateTime(25), asList()),
                             pair(zonedDateTime(27), asList((long) 2)),
                             pair(zonedDateTime(28), asList())));
    }

    private static void assertTimeline(Range<ZonedDateTime> recurrenceRange,
                                       Duration iterationDuration,
                                       Collection<Range<Instant>> subRanges,
                                       Range<ZonedDateTime> calculationRange,
                                       Map<ZonedDateTime, List<Long>> expected) {
        Recurrence                    recurrence = new Recurrence(recurrenceRange, iterationDuration, subRanges);
        Timeline<ZonedDateTime, Long> timeline   = recurrence.toTimeline(calculationRange);
        assertEquals(expected, timeline.getIntervalMap());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Range<ZonedDateTime> range(int startMillis, int endMillis) {
        return Range.of(zonedDateTime(startMillis), zonedDateTime(endMillis));
    }

    private static Range<Instant> subrange(int startMillis, int endMillis) {
        return Range.of(instant(startMillis), instant(endMillis));
    }

    private static ZonedDateTime zonedDateTime(int millis) {
        return ZonedDateTime.ofInstant(instant(millis), ZoneOffset.UTC);
    }

    private static Instant instant(int millis) {
        return Instant.ofEpochMilli(millis);
    }

    private static Duration iteration(int durationMillis) {
        return Duration.ofMillis(durationMillis);
    }
}