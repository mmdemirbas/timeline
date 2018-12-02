package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.mmdemirbas.oncalls.TestUtils.map;
import static com.github.mmdemirbas.oncalls.TestUtils.pair;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class TimelineTest {
    private static final OnCall[] NO_ONCALL   = new OnCall[0];
    private static final Duration HOURLY      = Duration.ofHours(1);
    private static final Duration DAILY       = Duration.ofDays(1);
    private static final Duration THIRTY_DAYS = Duration.ofDays(30);
    private static final Instant  ZERO        = Instant.EPOCH;
    private static final Instant  INF         = Instant.MAX;

    @Test
    void infiniteRotation_FullTest() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateForever(HOURLY, "A", "B")),
                         asList(),
                         expectAt(min(0), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(1), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(2), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(3), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(4), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(5), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(58), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(59), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(60), onCallUntil(min(120), "B"), nextOnCallFrom(min(120), "A")),
                         expectAt(min(61), onCallUntil(min(120), "B"), nextOnCallFrom(min(120), "A")),
                         expectAt(min(62), onCallUntil(min(120), "B"), nextOnCallFrom(min(120), "A")),
                         expectAt(min(119), onCallUntil(min(120), "B"), nextOnCallFrom(min(120), "A")),
                         expectAt(min(120), onCallUntil(min(180), "A"), nextOnCallFrom(min(180), "B")),
                         expectAt(min(121), onCallUntil(min(180), "A"), nextOnCallFrom(min(180), "B")));
    }

    @Test
    void onCallAtRotationEndTime() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateUntil(min(3), HOURLY, "A", "B")),
                         asList(),
                         expectAt(min(3), NO_ONCALL));
    }

    @Test
    void onCallAtOverrideEndTimeWithoutRotation() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(),
                         asList(overrideBetween(min(1), min(3), "A")),
                         expectAt(min(3), NO_ONCALL));
    }

    @Test
    void rotationWithEndDateSmallerThan30Minutes_FullTest() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateUntil(min(3), HOURLY, "A", "B", "C")),
                         asList(),
                         expectAt(min(0), onCallUntil(min(3), "A")),
                         expectAt(min(1), onCallUntil(min(3), "A")),
                         expectAt(min(2), onCallUntil(min(3), "A")),
                         expectAt(min(3), NO_ONCALL),
                         expectAt(min(4), NO_ONCALL),
                         expectAt(min(5), NO_ONCALL));
    }

    @Test
    void rotationWithEndDateSmallerThan60Minutes_FullTest() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateUntil(min(45), HOURLY, "A", "B", "C")),
                         asList(),
                         expectAt(min(0), onCallUntil(min(45), "A")),
                         expectAt(min(1), onCallUntil(min(45), "A")),
                         expectAt(min(2), onCallUntil(min(45), "A")),
                         expectAt(min(3), onCallUntil(min(45), "A")),
                         expectAt(min(44), onCallUntil(min(45), "A")),
                         expectAt(min(45), NO_ONCALL),
                         expectAt(min(46), NO_ONCALL),
                         expectAt(min(59), NO_ONCALL),
                         expectAt(min(60), NO_ONCALL),
                         expectAt(min(61), NO_ONCALL));
    }

    @Test
    void rotationWithEndDateSmallerThan120Minutes_FullTest() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateUntil(min(90), HOURLY, "A", "B", "C")),
                         asList(),
                         expectAt(min(0), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(1), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(2), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(30), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(59), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(60), onCallUntil(min(90), "B")),
                         expectAt(min(61), onCallUntil(min(90), "B")),
                         expectAt(min(89), onCallUntil(min(90), "B")),
                         expectAt(min(90), NO_ONCALL),
                         expectAt(min(91), NO_ONCALL));
    }

    @Test
    void overrideWithoutRotation_FullTest() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(),
                         asList(overrideBetween(min(2), min(4), "D")),
                         expectAt(min(0), nextOnCallFrom(min(2), "D")),
                         expectAt(min(1), nextOnCallFrom(min(2), "D")),
                         expectAt(min(2), onCallUntil(min(4), "D")),
                         expectAt(min(3), onCallUntil(min(4), "D")),
                         expectAt(min(4), NO_ONCALL),
                         expectAt(min(5), NO_ONCALL));
    }

    @Test
    void overrideInMinutesLevelOnInfiniteRotation_FullTest() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateForever(HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(2), min(4), "D")),
                         expectAt(min(0), onCallUntil(min(2), "A"), nextOnCallFrom(min(2), "D")),
                         expectAt(min(1), onCallUntil(min(2), "A"), nextOnCallFrom(min(2), "D")),
                         expectAt(min(2), onCallUntil(min(4), "D"), nextOnCallFrom(min(4), "A")),
                         expectAt(min(3), onCallUntil(min(4), "D"), nextOnCallFrom(min(4), "A")),
                         expectAt(min(4), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(5), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")));
    }

    @Test
    void overrideInMinutesLevelOnInfiniteRotation() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateForever(HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(2), min(4), "D")),
                         expectAt(min(2), onCallUntil(min(4), "D"), nextOnCallFrom(min(4), "A")));
    }

    @Test
    void preOverrideOnCallEndTime() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateUntil(min(30), HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(10), min(20), "D")),
                         expectAt(min(5), onCallUntil(min(10), "A"), nextOnCallFrom(min(10), "D")));
    }

    @Test
    void preOverrideOnCallWithoutEndTime() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateForever(HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(10), min(20), "D")),
                         expectAt(min(5), onCallUntil(min(10), "A"), nextOnCallFrom(min(10), "D")));
    }

    @Test
    void overrideInMinutesLevelOnRotationWithEndTime_FullTest() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateUntil(min(3), HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(2), min(4), "D")),
                         expectAt(min(0), onCallUntil(min(2), "A"), nextOnCallFrom(min(2), "D")),
                         expectAt(min(1), onCallUntil(min(2), "A"), nextOnCallFrom(min(2), "D")),
                         expectAt(min(2), onCallUntil(min(4), "D")),
                         expectAt(min(3), onCallUntil(min(4), "D")),
                         expectAt(min(4), NO_ONCALL),
                         expectAt(min(5), NO_ONCALL));
    }

    @Test
    void overrideExceedsRotationEndTime() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateUntil(min(20), HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(10), min(30), "D")),
                         expectAt(min(25), onCallUntil(min(30), "D")));
    }

    @Test
    void singleUserRotationWithEndTime() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateUntil(hour(12), DAILY, "A")),
                         asList(),
                         expectAt(hour(0), onCallUntil(hour(12), "A")));
    }

    @Test
    void singleUserRotationWithEndTime_FullTest() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateUntil(hour(30), DAILY, "A")),
                         asList(),
                         expectAt(hour(0), onCallUntil(hour(30), "A")),
                         expectAt(hour(1), onCallUntil(hour(30), "A")),
                         expectAt(hour(2), onCallUntil(hour(30), "A")),
                         expectAt(hour(3), onCallUntil(hour(30), "A")),
                         expectAt(hour(4), onCallUntil(hour(30), "A")),
                         expectAt(hour(5), onCallUntil(hour(30), "A")),
                         expectAt(hour(6), onCallUntil(hour(30), "A")),
                         expectAt(hour(7), onCallUntil(hour(30), "A")),
                         expectAt(hour(8), onCallUntil(hour(30), "A")),
                         expectAt(hour(9), onCallUntil(hour(30), "A")),
                         expectAt(hour(10), onCallUntil(hour(30), "A")),
                         expectAt(hour(11), onCallUntil(hour(30), "A")),
                         expectAt(hour(12), onCallUntil(hour(30), "A")),
                         expectAt(hour(13), onCallUntil(hour(30), "A")),
                         expectAt(hour(14), onCallUntil(hour(30), "A")),
                         expectAt(hour(15), onCallUntil(hour(30), "A")),
                         expectAt(hour(16), onCallUntil(hour(30), "A")),
                         expectAt(hour(17), onCallUntil(hour(30), "A")),
                         expectAt(hour(18), onCallUntil(hour(30), "A")),
                         expectAt(hour(19), onCallUntil(hour(30), "A")),
                         expectAt(hour(20), onCallUntil(hour(30), "A")),
                         expectAt(hour(21), onCallUntil(hour(30), "A")),
                         expectAt(hour(22), onCallUntil(hour(30), "A")),
                         expectAt(hour(23), onCallUntil(hour(30), "A")),
                         expectAt(hour(24), onCallUntil(hour(30), "A")),
                         expectAt(hour(25), onCallUntil(hour(30), "A")),
                         expectAt(hour(26), onCallUntil(hour(30), "A")),
                         expectAt(hour(27), onCallUntil(hour(30), "A")),
                         expectAt(hour(28), onCallUntil(hour(30), "A")),
                         expectAt(hour(29), onCallUntil(hour(30), "A")),
                         expectAt(hour(30), NO_ONCALL),
                         expectAt(hour(31), NO_ONCALL),
                         expectAt(hour(32), NO_ONCALL),
                         expectAt(hour(33), NO_ONCALL));
    }

    @Test
    void singleUserInifiniteRotation_at0() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateForever(DAILY, "A")),
                         asList(),
                         expectAt(hour(0), onCallUntil(INF, "A")));
    }

    @Test
    void singleUserInifiniteRotation_at24() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateForever(DAILY, "A")),
                         asList(),
                         expectAt(hour(24), onCallUntil(INF, "A")));
    }

    @Test
    void singleUserInifiniteRotation_FullTest() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateForever(DAILY, "A")),
                         asList(),
                         expectAt(hour(0), onCallUntil(INF, "A")),
                         expectAt(hour(1), onCallUntil(INF, "A")),
                         expectAt(hour(2), onCallUntil(INF, "A")),
                         expectAt(hour(3), onCallUntil(INF, "A")),
                         expectAt(hour(4), onCallUntil(INF, "A")),
                         expectAt(hour(5), onCallUntil(INF, "A")),
                         expectAt(hour(6), onCallUntil(INF, "A")),
                         expectAt(hour(7), onCallUntil(INF, "A")),
                         expectAt(hour(8), onCallUntil(INF, "A")),
                         expectAt(hour(9), onCallUntil(INF, "A")),
                         expectAt(hour(10), onCallUntil(INF, "A")),
                         expectAt(hour(11), onCallUntil(INF, "A")),
                         expectAt(hour(12), onCallUntil(INF, "A")),
                         expectAt(hour(13), onCallUntil(INF, "A")),
                         expectAt(hour(14), onCallUntil(INF, "A")),
                         expectAt(hour(15), onCallUntil(INF, "A")),
                         expectAt(hour(16), onCallUntil(INF, "A")),
                         expectAt(hour(17), onCallUntil(INF, "A")),
                         expectAt(hour(18), onCallUntil(INF, "A")),
                         expectAt(hour(19), onCallUntil(INF, "A")),
                         expectAt(hour(20), onCallUntil(INF, "A")),
                         expectAt(hour(21), onCallUntil(INF, "A")),
                         expectAt(hour(22), onCallUntil(INF, "A")),
                         expectAt(hour(23), onCallUntil(INF, "A")),
                         expectAt(hour(24), onCallUntil(INF, "A")),
                         expectAt(hour(25), onCallUntil(INF, "A")),
                         expectAt(hour(26), onCallUntil(INF, "A")),
                         expectAt(hour(27), onCallUntil(INF, "A")),
                         expectAt(hour(28), onCallUntil(INF, "A")),
                         expectAt(hour(29), onCallUntil(INF, "A")),
                         expectAt(hour(30), onCallUntil(INF, "A")));
    }

    @Test
    void singleUserRotationWithTimeRestriction() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateForever(DAILY, asList(restrictTo(hour(8), hour(18))), "A")),
                         asList(),
                         expectAt(hour(8), onCallUntil(hour(18), "A")));
    }

    @Test
    void singleUserRotationWithTimeRestriction_FullTest() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateForever(DAILY, asList(restrictTo(hour(8), hour(18))), "A")),
                         asList(),
                         expectAt(hour(0), nextOnCallFrom(hour(8), "A")),
                         expectAt(hour(1), nextOnCallFrom(hour(8), "A")),
                         expectAt(hour(2), nextOnCallFrom(hour(8), "A")),
                         expectAt(hour(3), nextOnCallFrom(hour(8), "A")),
                         expectAt(hour(4), nextOnCallFrom(hour(8), "A")),
                         expectAt(hour(5), nextOnCallFrom(hour(8), "A")),
                         expectAt(hour(6), nextOnCallFrom(hour(8), "A")),
                         expectAt(hour(7), nextOnCallFrom(hour(8), "A")),
                         expectAt(hour(8), onCallUntil(hour(18), "A")),
                         expectAt(hour(9), onCallUntil(hour(18), "A")),
                         expectAt(hour(10), onCallUntil(hour(18), "A")),
                         expectAt(hour(11), onCallUntil(hour(18), "A")),
                         expectAt(hour(12), onCallUntil(hour(18), "A")),
                         expectAt(hour(13), onCallUntil(hour(18), "A")),
                         expectAt(hour(14), onCallUntil(hour(18), "A")),
                         expectAt(hour(15), onCallUntil(hour(18), "A")),
                         expectAt(hour(16), onCallUntil(hour(18), "A")),
                         expectAt(hour(17), onCallUntil(hour(18), "A")),
                         expectAt(hour(18), nextOnCallFrom(hour(32), "A")),
                         expectAt(hour(19), nextOnCallFrom(hour(32), "A")),
                         expectAt(hour(20), nextOnCallFrom(hour(32), "A")),
                         expectAt(hour(21), nextOnCallFrom(hour(32), "A")),
                         expectAt(hour(22), nextOnCallFrom(hour(32), "A")),
                         expectAt(hour(23), nextOnCallFrom(hour(32), "A")),
                         expectAt(hour(24), nextOnCallFrom(hour(32), "A")),
                         expectAt(hour(25), nextOnCallFrom(hour(32), "A")),
                         expectAt(hour(26), nextOnCallFrom(hour(32), "A")),
                         expectAt(hour(27), nextOnCallFrom(hour(32), "A")),
                         expectAt(hour(28), nextOnCallFrom(hour(32), "A")),
                         expectAt(hour(29), nextOnCallFrom(hour(32), "A")),
                         expectAt(hour(30), nextOnCallFrom(hour(32), "A")),
                         expectAt(hour(31), nextOnCallFrom(hour(32), "A")),
                         expectAt(hour(32), onCallUntil(hour(42), "A")),
                         expectAt(hour(33), onCallUntil(hour(42), "A")),
                         expectAt(hour(34), onCallUntil(hour(42), "A")),
                         expectAt(hour(35), onCallUntil(hour(42), "A")),
                         expectAt(hour(36), onCallUntil(hour(42), "A")),
                         expectAt(hour(37), onCallUntil(hour(42), "A")),
                         expectAt(hour(38), onCallUntil(hour(42), "A")),
                         expectAt(hour(39), onCallUntil(hour(42), "A")),
                         expectAt(hour(40), onCallUntil(hour(42), "A")),
                         expectAt(hour(41), onCallUntil(hour(42), "A")),
                         expectAt(hour(42), nextOnCallFrom(hour(56), "A")),
                         expectAt(hour(43), nextOnCallFrom(hour(56), "A")),
                         expectAt(hour(44), nextOnCallFrom(hour(56), "A")),
                         expectAt(hour(45), nextOnCallFrom(hour(56), "A")),
                         expectAt(hour(46), nextOnCallFrom(hour(56), "A")),
                         expectAt(hour(47), nextOnCallFrom(hour(56), "A")),
                         expectAt(hour(48), nextOnCallFrom(hour(56), "A")));
    }

    @Test
    void successiveOverridesOfSameUser() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateForever(HOURLY, "A", "B")),
                         asList(overrideBetween(min(10), min(15), "D"), overrideBetween(min(15), min(20), "D")),
                         expectAt(min(10), onCallUntil(min(20), "D"), nextOnCallFrom(min(20), "A")));
    }

    @Test
    void successiveOverridesOfSameUser_FullTest() {
        assertRecipients(TimelineTest::getRecipientsWithInterval,
                         asList(rotateForever(HOURLY, "A", "B")),
                         asList(overrideBetween(min(10), min(15), "D"), overrideBetween(min(15), min(20), "D")),
                         expectAt(min(0), onCallUntil(min(10), "A"), nextOnCallFrom(min(10), "D")),
                         expectAt(min(1), onCallUntil(min(10), "A"), nextOnCallFrom(min(10), "D")),
                         expectAt(min(2), onCallUntil(min(10), "A"), nextOnCallFrom(min(10), "D")),
                         expectAt(min(3), onCallUntil(min(10), "A"), nextOnCallFrom(min(10), "D")),
                         expectAt(min(4), onCallUntil(min(10), "A"), nextOnCallFrom(min(10), "D")),
                         expectAt(min(5), onCallUntil(min(10), "A"), nextOnCallFrom(min(10), "D")),
                         expectAt(min(6), onCallUntil(min(10), "A"), nextOnCallFrom(min(10), "D")),
                         expectAt(min(7), onCallUntil(min(10), "A"), nextOnCallFrom(min(10), "D")),
                         expectAt(min(8), onCallUntil(min(10), "A"), nextOnCallFrom(min(10), "D")),
                         expectAt(min(9), onCallUntil(min(10), "A"), nextOnCallFrom(min(10), "D")),
                         expectAt(min(10), onCallUntil(min(20), "D"), nextOnCallFrom(min(20), "A")),
                         expectAt(min(11), onCallUntil(min(20), "D"), nextOnCallFrom(min(20), "A")),
                         expectAt(min(12), onCallUntil(min(20), "D"), nextOnCallFrom(min(20), "A")),
                         expectAt(min(13), onCallUntil(min(20), "D"), nextOnCallFrom(min(20), "A")),
                         expectAt(min(14), onCallUntil(min(20), "D"), nextOnCallFrom(min(20), "A")),
                         expectAt(min(15), onCallUntil(min(20), "D"), nextOnCallFrom(min(20), "A")),
                         expectAt(min(16), onCallUntil(min(20), "D"), nextOnCallFrom(min(20), "A")),
                         expectAt(min(17), onCallUntil(min(20), "D"), nextOnCallFrom(min(20), "A")),
                         expectAt(min(18), onCallUntil(min(20), "D"), nextOnCallFrom(min(20), "A")),
                         expectAt(min(19), onCallUntil(min(20), "D"), nextOnCallFrom(min(20), "A")),
                         expectAt(min(20), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")));
    }

    /// test dsl ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OnCall {
        String  name;
        Instant startTime;
        Instant endTime;
        boolean next;

        OnCall(String name, Instant startTime, Instant endTime, boolean next) {
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
            this.next = next;
        }
    }

    private static void assertRecipients(BiFunction<Timeline<ZonedDateTime, String>, Long, List<OnCall>> getRecipients,
                                         List<Timeline<ZonedDateTime, String>> rotations,
                                         List<ValuedRange<Instant, String>> overrides,
                                         Entry<Instant, List<OnCall>>... expecteds) {
        Timeline<ZonedDateTime, String> snapshot = PatchedTimeline.of(UnionTimeline.of(rotations),
                                                                      asList(StaticTimeline.ofIntervals(map(overrides,
                                                                                                               TimelineTest::asPatch))));

        // execute the method
        List<Entry<Instant, List<OnCall>>> actuals = new ArrayList<>();
        for (Entry<Instant, List<OnCall>> expected : expecteds) {
            Instant      time   = expected.getKey();
            long         millis = time.toEpochMilli();
            List<OnCall> actual = getRecipients.apply(snapshot, millis);
            actuals.add(pair(time, actual));
        }

        // prepare for comparison
        List<Entry<Instant, List<OnCall>>> expectedsModified = Stream.of(expecteds).map(expected -> {
            Instant calculationOffset = expected.getKey();
            return pair(calculationOffset,
                        expected.getValue()
                                .stream()
                                .map(value -> new OnCall(value.name,
                                                         normalize(value.startTime, calculationOffset),
                                                         normalize(value.endTime, calculationOffset),
                                                         value.next))
                                .collect(Collectors.toList()));
        }).collect(Collectors.toList());

        // compare results
        System.out.println(format(actuals));
        assertEquals(format(expectedsModified), format(actuals));
    }

    private static Instant normalize(Instant instant, Instant calculationOffset) {
        return instant.equals(INF) ? calculationOffset.plus(THIRTY_DAYS) : instant;
    }

    private static ValuedRange<ZonedDateTime, UnaryOperator<List<String>>> asPatch(ValuedRange<Instant, String> override) {
        Range<Instant> range = override.getRange();
        return ValuedRange.of(dateRange(range.getStartInclusive(), range.getEndExclusive()),
                              ignored -> asList(override.getValue()));
    }

    private static List<OnCall> getRecipientsWithInterval(Timeline<ZonedDateTime, String> rotations, long millis) {
        ZonedDateTime                          dateTime         = instantToZonedDateTime(Instant.ofEpochMilli(millis));
        ZonedDateTime                          oneMonthBefore   = dateTime.minus(THIRTY_DAYS);
        ZonedDateTime                          oneMonthAfter    = dateTime.plus(THIRTY_DAYS);
        Range<ZonedDateTime>                   calculationRange = Range.of(oneMonthBefore, oneMonthAfter);
        TimelineSegment<ZonedDateTime, String> timeline         = rotations.toSegment(calculationRange);
        List<OnCall>                           oncalls          = new ArrayList<>();
        addIfNotNull(oncalls, false, timeline.findCurrentInterval(dateTime));
        addIfNotNull(oncalls, true, timeline.findNextInterval(dateTime));
        return oncalls;
    }

    private static void addIfNotNull(List<OnCall> oncalls,
                                     boolean nextOnCall,
                                     ValuedRange<ZonedDateTime, List<String>> interval) {
        if (interval != null) {
            interval.getValue().forEach(value -> {
                Range<ZonedDateTime> range          = interval.getRange();
                ZonedDateTime        startInclusive = range.getStartInclusive();
                ZonedDateTime        endExclusive   = range.getEndExclusive();
                oncalls.add(new OnCall(value,
                                       !nextOnCall ? ZERO : startInclusive.toInstant(),
                                       nextOnCall ? ZERO : endExclusive.toInstant(),
                                       nextOnCall));
            });
        }
    }

    private static Entry<Instant, List<OnCall>> expectAt(Instant atTime, OnCall... expecteds) {
        return pair(atTime, asList(expecteds));
    }

    private static Timeline<ZonedDateTime, String> rotateForever(Duration rotationPeriod, String... recipients) {
        return rotateForever(rotationPeriod,
                             asList(restrictTo(min(0), min((int) rotationPeriod.toMinutes()))),
                             recipients);
    }

    private static Timeline<ZonedDateTime, String> rotateForever(Duration rotationPeriod,
                                                                 List<Range<Instant>> restrictions,
                                                                 String... recipients) {
        return rotateUntil(Instant.ofEpochMilli(Long.MAX_VALUE), rotationPeriod, restrictions, recipients);
    }

    private static Timeline<ZonedDateTime, String> rotateUntil(Instant endTime,
                                                               Duration rotationPeriod,
                                                               String... recipients) {
        return rotateUntil(endTime,
                           rotationPeriod,
                           asList(restrictTo(min(0), min((int) rotationPeriod.toMinutes()))),
                           recipients);
    }

    private static Timeline<ZonedDateTime, String> rotateUntil(Instant endTime,
                                                               Duration rotationPeriod,
                                                               List<Range<Instant>> restrictions,
                                                               String... recipients) {
        return new ZonedRotationTimeline<>(dateRange(ZERO, endTime),
                                           Iteration.of(min((int) rotationPeriod.toMinutes()), restrictions)
                                                    .toIterations(),
                                           asList(recipients));
    }

    private static ValuedRange<Instant, String> overrideBetween(Instant startTime, Instant endTime, String name) {
        return ValuedRange.of(Range.of(startTime, endTime), name);
    }

    private static Range<Instant> restrictTo(Instant startTime, Instant endTime) {
        return Range.of(startTime, endTime);
    }

    private static OnCall onCallUntil(Instant endTime, String name) {
        return new OnCall(name, ZERO, endTime, false);
    }

    private static OnCall nextOnCallFrom(Instant startTime, String name) {
        return new OnCall(name, startTime, ZERO, true);
    }

    private static String format(Collection<Entry<Instant, List<OnCall>>> items) {
        return joinLines(items, TimelineTest::format);
    }

    private static String format(Entry<Instant, List<OnCall>> onCalls) {
        return String.format("at %s:\n%s",
                             format(onCalls.getKey()),
                             joinLines(onCalls.getValue(), TimelineTest::format));
    }

    private static String format(OnCall onCall) {
        return String.format("  %s on-call is %s  - from %s to %s",
                             onCall.next ? "   next" : "current",
                             onCall.name,
                             format(onCall.startTime),
                             format(onCall.endTime));
    }

    private static String format(Instant instant) {
        OffsetDateTime dateTime   = instant.atOffset(ZoneOffset.UTC);
        int            day        = dateTime.getDayOfYear() - 1;
        String         hourMinute = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        return (day == 0) ? hourMinute : String.format("d%d:%s", day, hourMinute);
    }

    private static <T> String joinLines(Collection<? extends T> items, Function<? super T, String> toString) {
        return String.join("\n", map(items, toString));
    }

    private static Instant min(int minute) {
        return instantOf(minute, TimeUnit.MINUTES);
    }

    private static Instant hour(int hour) {
        return instantOf(hour, TimeUnit.HOURS);
    }

    private static Instant instantOf(int amount, TimeUnit unit) {
        return Instant.ofEpochMilli(unit.toMillis(amount));
    }

    private static Range<ZonedDateTime> dateRange(Instant startTime, Instant endTime) {
        return Range.of(instantToZonedDateTime(startTime), instantToZonedDateTime(endTime));
    }

    private static ZonedDateTime instantToZonedDateTime(Instant instant) {
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
