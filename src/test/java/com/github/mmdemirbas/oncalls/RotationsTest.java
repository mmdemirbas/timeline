package com.github.mmdemirbas.oncalls;

import com.github.mmdemirbas.oncalls.Timeline.Interval;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.github.mmdemirbas.oncalls.Utils.map;
import static com.github.mmdemirbas.oncalls.Utils.pair;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-13 12:24
 */
final class RotationsTest {
    private static final OnCall[] NO_ONCALL = new OnCall[0];
    private static final Duration HOURLY    = Duration.ofHours(1);
    private static final Duration DAILY     = Duration.ofDays(1);

    @Test
    void infiniteRotation_FullTest() {
        assertRecipients(RotationsTest::getRecipientsWithInterval,
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
        assertRecipients(RotationsTest::getRecipientsWithInterval,
                         asList(rotateUntil(min(3), HOURLY, "A", "B")),
                         asList(),
                         expectAt(min(3), NO_ONCALL));
    }

    @Test
    void onCallAtOverrideEndTimeWithoutRotation() {
        assertRecipients(RotationsTest::getRecipientsWithInterval,
                         asList(),
                         asList(overrideBetween(min(1), min(3), "A")),
                         expectAt(min(3), NO_ONCALL));
    }

    @Test
    void rotationWithEndDateSmallerThan30Minutes_FullTest() {
        assertRecipients(RotationsTest::getRecipientsWithInterval,
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
        assertRecipients(RotationsTest::getRecipientsWithInterval,
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
        assertRecipients(RotationsTest::getRecipientsWithInterval,
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
        assertRecipients(RotationsTest::getRecipientsWithInterval,
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
        assertRecipients(RotationsTest::getRecipientsWithInterval,
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
        assertRecipients(RotationsTest::getRecipientsWithInterval,
                         asList(rotateForever(HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(2), min(4), "D")),
                         expectAt(min(2), onCallUntil(min(4), "D"), nextOnCallFrom(min(4), "A")));
    }

    @Test
    void preOverrideOnCallEndTime() {
        assertRecipients(RotationsTest::getRecipientsWithInterval,
                         asList(rotateUntil(min(30), HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(10), min(20), "D")),
                         expectAt(min(5), onCallUntil(min(10), "A"), nextOnCallFrom(min(10), "D")));
    }

    @Test
    void preOverrideOnCallWithoutEndTime() {
        assertRecipients(RotationsTest::getRecipientsWithInterval,
                         asList(rotateForever(HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(10), min(20), "D")),
                         expectAt(min(5), onCallUntil(min(10), "A"), nextOnCallFrom(min(10), "D")));
    }

    @Test
    void overrideInMinutesLevelOnRotationWithEndTime_FullTest() {
        assertRecipients(RotationsTest::getRecipientsWithInterval,
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
        assertRecipients(RotationsTest::getRecipientsWithInterval,
                         asList(rotateUntil(min(20), HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(10), min(30), "D")),
                         expectAt(min(25), onCallUntil(min(30), "D")));
    }

    @Test
    void singleUserRotationWithEndTime() {
        assertRecipients(RotationsTest::getRecipientsWithInterval,
                         asList(rotateUntil(hour(12), DAILY, "A")),
                         asList(),
                         expectAt(hour(0), onCallUntil(hour(12), "A")));
    }

    @Test
    void singleUserRotationWithEndTime_FullTest() {
        assertRecipients(RotationsTest::getRecipientsWithInterval,
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
    void singleUserInifiniteRotation() {
        assertRecipients(RotationsTest::getRecipientsWithInterval,
                         asList(rotateForever(DAILY, "A")),
                         asList(),
                         expectAt(hour(0), onCallUntil(hour(0), "A")));
    }

    @Test
    void singleUserInifiniteRotation_FullTest() {
        assertRecipients(RotationsTest::getRecipientsWithInterval,
                         asList(rotateForever(DAILY, "A")),
                         asList(),
                         expectAt(hour(0), onCallUntil(hour(0), "A")),
                         expectAt(hour(1), onCallUntil(hour(0), "A")),
                         expectAt(hour(2), onCallUntil(hour(0), "A")),
                         expectAt(hour(3), onCallUntil(hour(0), "A")),
                         expectAt(hour(4), onCallUntil(hour(0), "A")),
                         expectAt(hour(5), onCallUntil(hour(0), "A")),
                         expectAt(hour(6), onCallUntil(hour(0), "A")),
                         expectAt(hour(7), onCallUntil(hour(0), "A")),
                         expectAt(hour(8), onCallUntil(hour(0), "A")),
                         expectAt(hour(9), onCallUntil(hour(0), "A")),
                         expectAt(hour(10), onCallUntil(hour(0), "A")),
                         expectAt(hour(11), onCallUntil(hour(0), "A")),
                         expectAt(hour(12), onCallUntil(hour(0), "A")),
                         expectAt(hour(13), onCallUntil(hour(0), "A")),
                         expectAt(hour(14), onCallUntil(hour(0), "A")),
                         expectAt(hour(15), onCallUntil(hour(0), "A")),
                         expectAt(hour(16), onCallUntil(hour(0), "A")),
                         expectAt(hour(17), onCallUntil(hour(0), "A")),
                         expectAt(hour(18), onCallUntil(hour(0), "A")),
                         expectAt(hour(19), onCallUntil(hour(0), "A")),
                         expectAt(hour(20), onCallUntil(hour(0), "A")),
                         expectAt(hour(21), onCallUntil(hour(0), "A")),
                         expectAt(hour(22), onCallUntil(hour(0), "A")),
                         expectAt(hour(23), onCallUntil(hour(0), "A")),
                         expectAt(hour(24), onCallUntil(hour(0), "A")),
                         expectAt(hour(25), onCallUntil(hour(0), "A")),
                         expectAt(hour(26), onCallUntil(hour(0), "A")),
                         expectAt(hour(27), onCallUntil(hour(0), "A")),
                         expectAt(hour(28), onCallUntil(hour(0), "A")),
                         expectAt(hour(29), onCallUntil(hour(0), "A")),
                         expectAt(hour(30), onCallUntil(hour(0), "A")));
    }

    @Test
    void singleUserRotationWithTimeRestriction() {
        assertRecipients(RotationsTest::getRecipientsWithInterval,
                         asList(rotateForever(DAILY, asList(restrictTo(hour(8), hour(18))), "A")),
                         asList(),
                         expectAt(hour(8), onCallUntil(hour(18), "A")));
    }

    @Test
    void singleUserRotationWithTimeRestriction_FullTest() {
        assertRecipients(RotationsTest::getRecipientsWithInterval,
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
        assertRecipients(RotationsTest::getRecipientsWithInterval,
                         asList(rotateForever(HOURLY, "A", "B")),
                         asList(overrideBetween(min(10), min(15), "D"), overrideBetween(min(15), min(20), "D")),
                         expectAt(min(10), onCallUntil(min(20), "D"), nextOnCallFrom(min(20), "A")));
    }

    @Test
    void successiveOverridesOfSameUser_FullTest() {
        assertRecipients(RotationsTest::getRecipientsWithInterval,
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

        public OnCall(String name, Instant startTime, Instant endTime, boolean next) {
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
            this.next = next;
        }
    }

    private static void assertRecipients(BiFunction<Rotations<String>, Long, List<OnCall>> getRecipients,
                                         List<Rotation<String>> rotations,
                                         List<Interval<Instant, String>> overrides,
                                         Entry<Instant, List<OnCall>>... expecteds) {
        List<Interval<ZonedDateTime, UnaryOperator<List<String>>>> intervals = map(overrides, override -> {
            Range<Instant> range = override.getRange();
            return Interval.of(range(range.getStartInclusive(), range.getEndExclusive()),
                               ignored -> asList(override.getValue()));
        });
        Rotations<String> snapshot = rotations.isEmpty()
                                     ? new Rotations<>(rotations, asList(Timeline.of(intervals)))
                                     : new Rotations<>(map(rotations,
                                                           rotation -> new Rotation<>(rotation.getRecurrence(),
                                                                                      rotation.getRecipients(),
                                                                                      asList(Timeline.of(intervals)))),
                                                       asList());

        // execute the method
        List<Entry<Instant, List<OnCall>>> actuals = new ArrayList<>();
        for (Entry<Instant, List<OnCall>> expected : expecteds) {
            Instant      time   = expected.getKey();
            long         millis = time.toEpochMilli();
            List<OnCall> actual = getRecipients.apply(snapshot, millis);
            actuals.add(pair(time, actual));
        }

        // compare results
        System.out.println(format(actuals));
        assertEquals(format(asList(expecteds)), format(actuals));
    }

    private static List<OnCall> getRecipientsWithInterval(Rotations<String> rotations, long millis) {
        ZonedDateTime                   dateTime         = instantToZonedDateTime(Instant.ofEpochMilli(millis));
        ZonedDateTime                   oneMonthBefore   = dateTime.minus(1, ChronoUnit.MONTHS);
        ZonedDateTime                   oneMonthAfter    = dateTime.plus(1, ChronoUnit.MONTHS);
        Range<ZonedDateTime>            calculationRange = Range.of(oneMonthBefore, oneMonthAfter);
        Timeline<ZonedDateTime, String> timeline         = rotations.toTimeline(calculationRange);
        List<OnCall>                    oncalls          = new ArrayList<>();
        if (timeline != null) {
            addIfNotNull(oncalls, false, timeline.findCurrentInterval(dateTime));
            addIfNotNull(oncalls, true, timeline.findNextInterval(dateTime));
        }
        return oncalls;
    }

    private static void addIfNotNull(List<OnCall> oncalls,
                                     boolean nextOnCall,
                                     Interval<ZonedDateTime, List<String>> interval) {
        interval.getValue()
                .forEach(value -> {
                    Range<ZonedDateTime> range          = interval.getRange();
                    ZonedDateTime        startInclusive = range.getStartInclusive();
                    ZonedDateTime        endExclusive   = range.getEndExclusive();
                    oncalls.add(new OnCall(value,
                                           !nextOnCall ? Instant.EPOCH : startInclusive.toInstant(),
                                           nextOnCall ? Instant.EPOCH : endExclusive.toInstant(),
                                           nextOnCall));
                });
    }

    private static Entry<Instant, List<OnCall>> expectAt(Instant atTime, OnCall... expecteds) {
        return pair(atTime, asList(expecteds));
    }

    private static Rotation<String> rotateForever(Duration rotationPeriod, String... recipients) {
        return rotateForever(rotationPeriod, asList(), recipients);
    }

    private static Rotation<String> rotateForever(Duration rotationPeriod,
                                                  List<Range<Instant>> restrictions,
                                                  String... recipients) {
        return rotateUntil(Instant.ofEpochMilli(Long.MAX_VALUE), rotationPeriod, restrictions, recipients);
    }

    private static Rotation<String> rotateUntil(Instant endTime, Duration rotationPeriod, String... recipients) {
        return rotateUntil(endTime, rotationPeriod, asList(), recipients);
    }

    private static Rotation<String> rotateUntil(Instant endTime,
                                                Duration rotationPeriod,
                                                List<Range<Instant>> restrictions,
                                                String... recipients) {
        return new Rotation<>(new Recurrence(range(Instant.EPOCH, endTime), rotationPeriod, restrictions),
                              asList(recipients),
                              asList());
    }

    private static Interval<Instant, String> overrideBetween(Instant startTime, Instant endTime, String name) {
        return Interval.of(Range.of(startTime, endTime), name);
    }

    private static Range<Instant> restrictTo(Instant startTime, Instant endTime) {
        return Range.of(startTime, endTime);
    }

    private static OnCall onCallUntil(Instant endTime, String name) {
        return new OnCall(name, Instant.EPOCH, endTime, false);
    }

    private static OnCall nextOnCallFrom(Instant startTime, String name) {
        return new OnCall(name, startTime, Instant.EPOCH, true);
    }

    private static String format(Collection<Entry<Instant, List<OnCall>>> items) {
        return joinLines(items, RotationsTest::format);
    }

    private static String format(Entry<Instant, List<OnCall>> onCalls) {
        return String.format("at %s:\n%s",
                             format(onCalls.getKey()),
                             joinLines(onCalls.getValue(), RotationsTest::format));
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
        return Instant.ofEpochMilli(TimeUnit.MINUTES.toMillis(minute));
    }

    private static Instant hour(int hour) {
        return Instant.ofEpochMilli(TimeUnit.HOURS.toMillis(hour));
    }

    private static Range<ZonedDateTime> range(Instant startTime, Instant endTime) {
        return Range.of(instantToZonedDateTime(startTime), instantToZonedDateTime(endTime));
    }

    private static ZonedDateTime instantToZonedDateTime(Instant instant) {
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
