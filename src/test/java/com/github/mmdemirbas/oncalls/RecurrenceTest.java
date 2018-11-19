package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static com.github.mmdemirbas.oncalls.Utils.mapOf;
import static com.github.mmdemirbas.oncalls.Utils.pair;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-19 14:55
 */
final class RecurrenceTest {

    @Test
    void toTimeline_SimplestCase() {
        assertTimeline(range(0, 50),
                       iteration(10),
                       Ranges.of(),
                       range(0, 50),
                       mapOf(pair(time(0), asList(mapping(0, range(0, 10)))),
                             pair(time(10), asList(mapping(1, range(10, 20)))),
                             pair(time(20), asList(mapping(2, range(20, 30)))),
                             pair(time(30), asList(mapping(3, range(30, 40)))),
                             pair(time(40), asList(mapping(4, range(40, 50)))),
                             pair(time(50), asList())));
    }

    @Test
    void toTimeline_RecurrenceFinishedBeforeIteration() {
        assertTimeline(range(0, 45),
                       iteration(10),
                       Ranges.of(),
                       range(0, 45),
                       mapOf(pair(time(0), asList(mapping(0, range(0, 10)))),
                             pair(time(10), asList(mapping(1, range(10, 20)))),
                             pair(time(20), asList(mapping(2, range(20, 30)))),
                             pair(time(30), asList(mapping(3, range(30, 40)))),
                             pair(time(40), asList(mapping(4, range(40, 45)))),
                             pair(time(45), asList())));
    }

    @Test
    void toTimeline_CalculationRangeBeforeRecurrenceRange() {
        assertTimeline(range(20, 40), iteration(10), Ranges.of(), range(0, 20), mapOf());
    }

    @Test
    void toTimeline_RecurrenceRangeBeforeCalculationRange() {
        assertTimeline(range(0, 20), iteration(10), Ranges.of(), range(20, 40), mapOf());
    }

    @Test
    void toTimeline_CalculationRangeContainsRecurrenceRange() {
        assertTimeline(range(10, 30),
                       iteration(10),
                       Ranges.of(),
                       range(0, 50),
                       mapOf(pair(time(10), asList(mapping(0, range(10, 20)))),
                             pair(time(20), asList(mapping(1, range(20, 30)))),
                             pair(time(30), asList())));
    }

    @Test
    void toTimeline_RecurrenceRangeContainsCalculationRange() {
        assertTimeline(range(0, 50),
                       iteration(10),
                       Ranges.of(),
                       range(10, 30),
                       mapOf(pair(time(10), asList(mapping(1, range(10, 20)))),
                             pair(time(20), asList(mapping(2, range(20, 30)))),
                             pair(time(30), asList())));
    }

    @Test
    void toTimeline_CalculationRangeContainsIncompleteIterations() {
        assertTimeline(range(0, 50),
                       iteration(10),
                       Ranges.of(),
                       range(15, 35),
                       mapOf(pair(time(15), asList(mapping(1, range(15, 20)))),
                             pair(time(20), asList(mapping(2, range(20, 30)))),
                             pair(time(30), asList(mapping(3, range(30, 35)))),
                             pair(time(35), asList())));
    }

    @Test
    void toTimeline_SingleSubRange() {
        assertTimeline(range(0, 30),
                       iteration(10),
                       Ranges.of(subrange(3, 7)),
                       range(0, 30),
                       mapOf(pair(time(3), asList(mapping(0, range(3, 7)))),
                             pair(time(7), asList()),
                             pair(time(13), asList(mapping(1, range(13, 17)))),
                             pair(time(17), asList()),
                             pair(time(23), asList(mapping(2, range(23, 27)))),
                             pair(time(27), asList())));
    }

    @Test
    void toTimeline_MultipleSubRanges() {
        assertTimeline(range(0, 30),
                       iteration(10),
                       Ranges.of(subrange(3, 5), subrange(7, 9)),
                       range(0, 30),
                       mapOf(pair(time(3), asList(mapping(0, range(3, 5)))),
                             pair(time(5), asList()),
                             pair(time(7), asList(mapping(0, range(7, 9)))),
                             pair(time(9), asList()),
                             pair(time(13), asList(mapping(1, range(13, 15)))),
                             pair(time(15), asList()),
                             pair(time(17), asList(mapping(1, range(17, 19)))),
                             pair(time(19), asList()),
                             pair(time(23), asList(mapping(2, range(23, 25)))),
                             pair(time(25), asList()),
                             pair(time(27), asList(mapping(2, range(27, 29)))),
                             pair(time(29), asList())));
    }

    @Test
    void toTimeline_IncompleteIterationsAndMultipleSubRanges() {
        assertTimeline(range(0, 30),
                       iteration(10),
                       Ranges.of(subrange(3, 5), subrange(7, 9)),
                       range(14, 28),
                       mapOf(pair(time(14), asList(mapping(1, range(14, 15)))),
                             pair(time(15), asList()),
                             pair(time(17), asList(mapping(1, range(17, 19)))),
                             pair(time(19), asList()),
                             pair(time(23), asList(mapping(2, range(23, 25)))),
                             pair(time(25), asList()),
                             pair(time(27), asList(mapping(2, range(27, 28)))),
                             pair(time(28), asList())));
    }

    private static void assertTimeline(Range<ZonedDateTime> recurrenceRange,
                                       Duration iterationDuration,
                                       Ranges<Instant> subRanges,
                                       Range<ZonedDateTime> calculationRange,
                                       Map<ZonedDateTime, List<?>> expected) {
        Recurrence                    recurrence = new Recurrence(recurrenceRange, iterationDuration, subRanges);
        Timeline<ZonedDateTime, Long> timeline   = recurrence.toTimeline(calculationRange, iterationNo -> iterationNo);
        assertEquals(expected, timeline.getIntervalMap());
    }

    private static Event<Long, ZonedDateTime> mapping(int iterationIndex, Range<ZonedDateTime> range) {
        return new Event<>(((long) iterationIndex), range);
    }

    private static Range<ZonedDateTime> range(int startMillis, int endMillis) {
        return Range.of(time(startMillis), time(endMillis));
    }

    private static Range<Instant> subrange(int startMillis, int endMillis) {
        return Range.of(instant(startMillis), instant(endMillis));
    }

    private static ZonedDateTime time(int millis) {
        return ZonedDateTime.ofInstant(instant(millis), ZoneOffset.UTC);
    }

    private static Instant instant(int millis) {
        return Instant.ofEpochMilli(millis);
    }

    private static Duration iteration(int durationMillis) {
        return Duration.ofMillis(durationMillis);
    }
}