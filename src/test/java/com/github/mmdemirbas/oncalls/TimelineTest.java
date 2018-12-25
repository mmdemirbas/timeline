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
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.mmdemirbas.oncalls.TestUtils.map;
import static com.github.mmdemirbas.oncalls.TestUtils.pair;
import static com.github.mmdemirbas.oncalls.TimelineTest.DAY.FRIDAY;
import static com.github.mmdemirbas.oncalls.TimelineTest.DAY.MONDAY;
import static com.github.mmdemirbas.oncalls.TimelineTest.DAY.SATURDAY;
import static com.github.mmdemirbas.oncalls.TimelineTest.DAY.THURSDAY;
import static com.github.mmdemirbas.oncalls.TimelineTest.DAY.TUESDAY;
import static com.github.mmdemirbas.oncalls.TimelineTest.DAY.WEDNESDAY;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

final class TimelineTest {
    private static final long     MONDAY_OFFSET = TimeUnit.DAYS.toMillis(4); // 1970-01-05 Monday
    private static final Oncall[] NO_ONCALL     = new Oncall[0];
    private static final Duration HOURLY        = Duration.ofHours(1);
    private static final Duration DAILY         = Duration.ofDays(1);
    private static final Duration WEEKLY        = Duration.ofDays(7);
    private static final Duration THIRTY_DAYS   = Duration.ofDays(30);
    private static final Instant  ZERO          = min(0);
    private static final Instant  INF           = Instant.MAX;

    @Test
    void infiniteRotation_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(HOURLY, "A", "B")),
                         asList(), expectAt(ZERO, onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
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
    void onCallAtRotationEndTime() throws Exception {
        assertRecipients(asList(rotateUntil(min(3), HOURLY, "A", "B")), asList(), expectAt(min(3), NO_ONCALL));
    }

    @Test
    void onCallAtOverrideEndTimeWithoutRotation() throws Exception {
        assertRecipients(asList(), asList(overrideBetween(min(1), min(3), "A")), expectAt(min(3), NO_ONCALL));
    }

    @Test
    void rotationWithEndDateSmallerThan30Minutes() throws Exception {
        assertRecipients(asList(rotateUntil(min(3), HOURLY, "A", "B", "C")),
                         asList(), expectAt(ZERO, onCallUntil(min(3), "A")));
    }

    @Test
    void rotationWithEndDateSmallerThan30Minutes_FullTest() throws Exception {
        assertRecipients(asList(rotateUntil(min(3), HOURLY, "A", "B", "C")),
                         asList(), expectAt(ZERO, onCallUntil(min(3), "A")),
                         expectAt(min(1), onCallUntil(min(3), "A")),
                         expectAt(min(2), onCallUntil(min(3), "A")),
                         expectAt(min(3), NO_ONCALL),
                         expectAt(min(4), NO_ONCALL),
                         expectAt(min(5), NO_ONCALL));
    }

    @Test
    void rotationWithEndDateSmallerThan60Minutes_FullTest() throws Exception {
        assertRecipients(asList(rotateUntil(min(45), HOURLY, "A", "B", "C")),
                         asList(), expectAt(ZERO, onCallUntil(min(45), "A")),
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
    void rotationWithEndDateSmallerThan120Minutes_FullTest() throws Exception {
        assertRecipients(asList(rotateUntil(min(90), HOURLY, "A", "B", "C")),
                         asList(), expectAt(ZERO, onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
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
    void overrideWithoutRotation_FullTest() throws Exception {
        assertRecipients(asList(),
                         asList(overrideBetween(min(2), min(4), "D")), expectAt(ZERO, nextOnCallFrom(min(2), "D")),
                         expectAt(min(1), nextOnCallFrom(min(2), "D")),
                         expectAt(min(2), onCallUntil(min(4), "D")),
                         expectAt(min(3), onCallUntil(min(4), "D")),
                         expectAt(min(4), NO_ONCALL),
                         expectAt(min(5), NO_ONCALL));
    }

    @Test
    void overrideInMinutesLevelOnInfiniteRotation_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(2), min(4), "D")),
                         expectAt(ZERO, onCallUntil(min(2), "A"), nextOnCallFrom(min(2), "D")),
                         expectAt(min(1), onCallUntil(min(2), "A"), nextOnCallFrom(min(2), "D")),
                         expectAt(min(2), onCallUntil(min(4), "D"), nextOnCallFrom(min(4), "A")),
                         expectAt(min(3), onCallUntil(min(4), "D"), nextOnCallFrom(min(4), "A")),
                         expectAt(min(4), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(5), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")));
    }

    @Test
    void overrideInMinutesLevelOnInfiniteRotation() throws Exception {
        assertRecipients(asList(rotateForever(HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(2), min(4), "D")),
                         expectAt(min(2), onCallUntil(min(4), "D"), nextOnCallFrom(min(4), "A")));
    }

    @Test
    void preOverrideOnCallEndTime() throws Exception {
        assertRecipients(asList(rotateUntil(min(30), HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(10), min(20), "D")),
                         expectAt(min(5), onCallUntil(min(10), "A"), nextOnCallFrom(min(10), "D")));
    }

    @Test
    void preOverrideOnCallWithoutEndTime() throws Exception {
        assertRecipients(asList(rotateForever(HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(10), min(20), "D")),
                         expectAt(min(5), onCallUntil(min(10), "A"), nextOnCallFrom(min(10), "D")));
    }

    @Test
    void overrideInMinutesLevelOnRotationWithEndTime_FullTest() throws Exception {
        assertRecipients(asList(rotateUntil(min(3), HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(2), min(4), "D")),
                         expectAt(ZERO, onCallUntil(min(2), "A"), nextOnCallFrom(min(2), "D")),
                         expectAt(min(1), onCallUntil(min(2), "A"), nextOnCallFrom(min(2), "D")),
                         expectAt(min(2), onCallUntil(min(4), "D")),
                         expectAt(min(3), onCallUntil(min(4), "D")),
                         expectAt(min(4), NO_ONCALL),
                         expectAt(min(5), NO_ONCALL));
    }

    @Test
    void overrideExceedsRotationEndTime() throws Exception {
        assertRecipients(asList(rotateUntil(min(20), HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(10), min(30), "D")),
                         expectAt(min(25), onCallUntil(min(30), "D")));
    }

    @Test
    void singleUserRotationWithEndTime() throws Exception {
        assertRecipients(asList(rotateUntil(hour(12), DAILY, "A")),
                         asList(),
                         expectAt(hour(0), onCallUntil(hour(12), "A")));
    }

    @Test
    void singleUserRotationWithEndTime_FullTest() throws Exception {
        assertRecipients(asList(rotateUntil(hour(30), DAILY, "A")),
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
    void singleUserInifiniteRotation() throws Exception {
        assertRecipients(asList(rotateForever(DAILY, "A")), asList(), expectAt(hour(0), onCallUntil(hour(0), "A")));
    }

    @Test
    void singleUserInifiniteRotation_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(DAILY, "A")),
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
    void singleUserRotationWithTimeRestriction() throws Exception {
        assertRecipients(asList(rotateForever(DAILY, asList(restrictTo(hour(8), hour(18))), "A")),
                         asList(),
                         expectAt(hour(8), onCallUntil(hour(18), "A")));
    }

    @Test
    void singleUserRotationWithTimeRestriction_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(DAILY, asList(restrictTo(hour(8), hour(18))), "A")),
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
    void bugfix_TARDIS_2340_a() throws Exception {
        // Small (<30min) periods can't be seen on mobile whoIsOnCall screen
        assertRecipients(asList(rotateForever(HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(20), min(25), "Z")),
                         expectAt(ZERO, onCallUntil(min(20), "A"), nextOnCallFrom(min(20), "Z")),
                         expectAt(min(19), onCallUntil(min(20), "A"), nextOnCallFrom(min(20), "Z")),
                         expectAt(min(20), onCallUntil(min(25), "Z"), nextOnCallFrom(min(25), "A")),
                         expectAt(min(21), onCallUntil(min(25), "Z"), nextOnCallFrom(min(25), "A")),
                         expectAt(min(22), onCallUntil(min(25), "Z"), nextOnCallFrom(min(25), "A")),
                         expectAt(min(23), onCallUntil(min(25), "Z"), nextOnCallFrom(min(25), "A")),
                         expectAt(min(24), onCallUntil(min(25), "Z"), nextOnCallFrom(min(25), "A")),
                         expectAt(min(25), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(26), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(27), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(28), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(29), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(30), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(31), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")));
    }

    @Test
    void bugfix_TARDIS_2340_b() throws Exception {
        // Also 1 hour overrides can't be seen if there is no rotation.
        assertRecipients(asList(),
                         asList(overrideBetween(hour(2), hour(3), "A")),
                         expectAt(min(45), nextOnCallFrom(min(120), "A")),
                         expectAt(min(59), nextOnCallFrom(min(120), "A")),
                         expectAt(min(60), nextOnCallFrom(min(120), "A")),
                         expectAt(min(61), nextOnCallFrom(min(120), "A")),
                         expectAt(min(119), nextOnCallFrom(min(120), "A")),
                         expectAt(min(120), onCallUntil(min(180), "A")),
                         expectAt(min(121), onCallUntil(min(180), "A")),
                         expectAt(min(179), onCallUntil(min(180), "A")),
                         expectAt(min(180), NO_ONCALL));
    }

    @Test
    void bugfix_TARDIS_2340_c() throws Exception {
        // And if there is a 1 hour override and a 8 hours rotation (endDate is startDate +8h) there is also next oncall calculation problem.
        assertRecipients(asList(rotateUntil(hour(8), HOURLY, "A", "B", "C", "D", "E", "F", "G", "H")),
                         asList(overrideBetween(hour(2), hour(3), "Z")),
                         expectAt(hour(0), onCallUntil(hour(1), "A"), nextOnCallFrom(hour(1), "B")),
                         expectAt(hour(1), onCallUntil(hour(2), "B"), nextOnCallFrom(hour(2), "Z")),
                         expectAt(hour(2), onCallUntil(hour(3), "Z"), nextOnCallFrom(hour(3), "D")),
                         expectAt(hour(3), onCallUntil(hour(4), "D"), nextOnCallFrom(hour(4), "E")),
                         expectAt(hour(4), onCallUntil(hour(5), "E"), nextOnCallFrom(hour(5), "F")),
                         expectAt(hour(5), onCallUntil(hour(6), "F"), nextOnCallFrom(hour(6), "G")),
                         expectAt(hour(6), onCallUntil(hour(7), "G"), nextOnCallFrom(hour(7), "H")),
                         expectAt(hour(7), onCallUntil(hour(8), "H")),
                         expectAt(hour(8), NO_ONCALL));
    }

    @Test
    void overrideExceedingTimeRestriction_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(DAILY, asList(restrictTo(hour(8), hour(18))), "A", "B", "C")),
                         asList(overrideBetween(hour(16), hour(20), "Z")),
                         expectAt(hour(15), onCallUntil(hour(16), "A"), nextOnCallFrom(hour(16), "Z")),
                         expectAt(hour(16), onCallUntil(hour(20), "Z"), nextOnCallFrom(hour(32), "B")),
                         expectAt(hour(17), onCallUntil(hour(20), "Z"), nextOnCallFrom(hour(32), "B")),
                         expectAt(hour(18), onCallUntil(hour(20), "Z"), nextOnCallFrom(hour(32), "B")),
                         expectAt(hour(19), onCallUntil(hour(20), "Z"), nextOnCallFrom(hour(32), "B")),
                         expectAt(hour(20), nextOnCallFrom(hour(32), "B")),
                         expectAt(hour(21), nextOnCallFrom(hour(32), "B")));
    }

    @Test
    void intersectingOverrides() throws Exception {
        assertRecipients(asList(rotateForever(DAILY, "A", "B", "C")),
                         asList(overrideBetween(hour(2), hour(6), "D", min(1)),
                                overrideBetween(hour(4), hour(8), "E", min(3)),
                                overrideBetween(hour(6), hour(8), "F", min(2))),
                         expectAt(hour(2), onCallUntil(hour(4), "D"), nextOnCallFrom(hour(4), "E")));
    }

    @Test
    void intersectingOverrides_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(DAILY, "A", "B", "C")),
                         asList(overrideBetween(hour(2), hour(6), "D", min(1)),
                                overrideBetween(hour(4), hour(8), "E", min(3)),
                                overrideBetween(hour(6), hour(8), "F", min(2))),
                         expectAt(hour(1), onCallUntil(hour(2), "A"), nextOnCallFrom(hour(2), "D")),
                         expectAt(hour(2), onCallUntil(hour(4), "D"), nextOnCallFrom(hour(4), "E")),
                         expectAt(hour(3), onCallUntil(hour(4), "D"), nextOnCallFrom(hour(4), "E")),
                         expectAt(hour(4), onCallUntil(hour(8), "E"), nextOnCallFrom(hour(8), "A")),
                         expectAt(hour(5), onCallUntil(hour(8), "E"), nextOnCallFrom(hour(8), "A")),
                         expectAt(hour(6), onCallUntil(hour(8), "E"), nextOnCallFrom(hour(8), "A")),
                         expectAt(hour(7), onCallUntil(hour(8), "E"), nextOnCallFrom(hour(8), "A")),
                         expectAt(hour(8), onCallUntil(hour(24), "A"), nextOnCallFrom(hour(24), "B")),
                         expectAt(hour(9), onCallUntil(hour(24), "A"), nextOnCallFrom(hour(24), "B")));
    }

    @Test
    void successiveOverridesOfSameUser_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(DAILY, "A", "B", "C")),
                         asList(overrideBetween(hour(2), hour(4), "D", min(1)),
                                overrideBetween(hour(4), hour(6), "D", min(2))),
                         expectAt(hour(1), onCallUntil(hour(2), "A"), nextOnCallFrom(hour(2), "D")),
                         expectAt(hour(2), onCallUntil(hour(6), "D"), nextOnCallFrom(hour(6), "A")),
                         expectAt(hour(3), onCallUntil(hour(6), "D"), nextOnCallFrom(hour(6), "A")),
                         expectAt(hour(4), onCallUntil(hour(6), "D"), nextOnCallFrom(hour(6), "A")),
                         expectAt(hour(5), onCallUntil(hour(6), "D"), nextOnCallFrom(hour(6), "A")),
                         expectAt(hour(6), onCallUntil(hour(24), "A"), nextOnCallFrom(hour(24), "B")));
    }

    @Test
    void nonSuccessiveOverridesOfSameUser_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(DAILY, "A", "B", "C")),
                         asList(overrideBetween(hour(2), hour(4), "D", min(1)),
                                overrideBetween(hour(6), hour(8), "D", min(2))),
                         expectAt(hour(1), onCallUntil(hour(2), "A"), nextOnCallFrom(hour(2), "D")),
                         expectAt(hour(2), onCallUntil(hour(4), "D"), nextOnCallFrom(hour(4), "A")),
                         expectAt(hour(3), onCallUntil(hour(4), "D"), nextOnCallFrom(hour(4), "A")),
                         expectAt(hour(4), onCallUntil(hour(6), "A"), nextOnCallFrom(hour(6), "D")),
                         expectAt(hour(5), onCallUntil(hour(6), "A"), nextOnCallFrom(hour(6), "D")),
                         expectAt(hour(6), onCallUntil(hour(8), "D"), nextOnCallFrom(hour(8), "A")),
                         expectAt(hour(7), onCallUntil(hour(8), "D"), nextOnCallFrom(hour(8), "A")),
                         expectAt(hour(8), onCallUntil(hour(24), "A"), nextOnCallFrom(hour(24), "B")),
                         expectAt(hour(9), onCallUntil(hour(24), "A"), nextOnCallFrom(hour(24), "B")));
    }

    @Test
    void globalOverride_FullTest() throws Exception {
        assertRecipients(asList(),
                         asList(overrideBetween(hour(1), hour(2), "D")),
                         expectAt(hour(0), nextOnCallFrom(hour(1), "D")),
                         expectAt(hour(1), onCallUntil(hour(2), "D")),
                         expectAt(hour(2), NO_ONCALL));
    }


    @Test
    void overrideOfSameUserBeforeRotation_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(30), min(60), "B")),
                         expectAt(min(15), onCallUntil(min(30), "A"), nextOnCallFrom(min(30), "B")),
                         expectAt(min(29), onCallUntil(min(30), "A"), nextOnCallFrom(min(30), "B")),
                         expectAt(min(30), onCallUntil(min(120), "B"), nextOnCallFrom(min(120), "C")),
                         expectAt(min(31), onCallUntil(min(120), "B"), nextOnCallFrom(min(120), "C")),
                         expectAt(min(59), onCallUntil(min(120), "B"), nextOnCallFrom(min(120), "C")),
                         expectAt(min(60), onCallUntil(min(120), "B"), nextOnCallFrom(min(120), "C")),
                         expectAt(min(61), onCallUntil(min(120), "B"), nextOnCallFrom(min(120), "C")),
                         expectAt(min(119), onCallUntil(min(120), "B"), nextOnCallFrom(min(120), "C")),
                         expectAt(min(120), onCallUntil(min(180), "C"), nextOnCallFrom(min(180), "A")));
    }

    @Test
    void overrideOfSameUserAfterRotation_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(60), min(90), "A")),
                         expectAt(min(59), onCallUntil(min(90), "A"), nextOnCallFrom(min(90), "B")),
                         expectAt(min(60), onCallUntil(min(90), "A"), nextOnCallFrom(min(90), "B")),
                         expectAt(min(61), onCallUntil(min(90), "A"), nextOnCallFrom(min(90), "B")),
                         expectAt(min(89), onCallUntil(min(90), "A"), nextOnCallFrom(min(90), "B")),
                         expectAt(min(90), onCallUntil(min(120), "B"), nextOnCallFrom(min(120), "C")),
                         expectAt(min(91), onCallUntil(min(120), "B"), nextOnCallFrom(min(120), "C")),
                         expectAt(min(119), onCallUntil(min(120), "B"), nextOnCallFrom(min(120), "C")),
                         expectAt(min(120), onCallUntil(min(180), "C"), nextOnCallFrom(min(180), "A")),
                         expectAt(min(121), onCallUntil(min(180), "C"), nextOnCallFrom(min(180), "A")));
    }

    @Test
    void overrideOfSameUserDuringRotation_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(30), min(45), "A")),
                         expectAt(min(29), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(30), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(31), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(44), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(45), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(46), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(59), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(60), onCallUntil(min(120), "B"), nextOnCallFrom(min(120), "C")),
                         expectAt(min(61), onCallUntil(min(120), "B"), nextOnCallFrom(min(120), "C")));
    }

    @Test
    void singleUserInfiniteRotationSelfOverride_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(HOURLY, "A")),
                         asList(overrideBetween(min(45), min(60), "A")),
                         expectAt(min(44), onCallUntil(ZERO, "A")),
                         expectAt(min(45), onCallUntil(ZERO, "A")),
                         expectAt(min(46), onCallUntil(ZERO, "A")),
                         expectAt(min(59), onCallUntil(ZERO, "A")),
                         expectAt(min(60), onCallUntil(ZERO, "A")),
                         expectAt(min(61), onCallUntil(ZERO, "A")));
    }

    @Test
    void singleUserInfiniteRotationAnotherOneOverrides_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(HOURLY, "A")),
                         asList(overrideBetween(min(45), min(60), "B")),
                         expectAt(min(44), onCallUntil(min(45), "A"), nextOnCallFrom(min(45), "B")),
                         expectAt(min(45), onCallUntil(min(60), "B"), nextOnCallFrom(min(60), "A")),
                         expectAt(min(46), onCallUntil(min(60), "B"), nextOnCallFrom(min(60), "A")),
                         expectAt(min(59), onCallUntil(min(60), "B"), nextOnCallFrom(min(60), "A")),
                         expectAt(min(60), onCallUntil(ZERO, "A")),
                         expectAt(min(61), onCallUntil(ZERO, "A")));
    }

    @Test
    void singleUserTimeRestrictedRotation_FullTest() throws Exception {
        Instant next = min((24 * 60) + 30);
        assertRecipients(asList(rotateForever(DAILY, asList(restrictTo(min(30), min(60))), "A")),
                         asList(),
                         expectAt(min(29), nextOnCallFrom(min(30), "A")),
                         expectAt(min(30), onCallUntil(min(60), "A")),
                         expectAt(min(31), onCallUntil(min(60), "A")),
                         expectAt(min(59), onCallUntil(min(60), "A")),
                         expectAt(min(60), nextOnCallFrom(next, "A")),
                         expectAt(min(61), nextOnCallFrom(next, "A")));
    }

    @Test
    void multiUserTimeRestrictedRotation_FullTest() throws Exception {
        Instant next = min((24 * 60) + 30);
        assertRecipients(asList(rotateForever(DAILY, asList(restrictTo(min(30), min(60))), "A", "B")),
                         asList(),
                         expectAt(min(29), nextOnCallFrom(min(30), "A")),
                         expectAt(min(30), onCallUntil(min(60), "A"), nextOnCallFrom(next, "B")),
                         expectAt(min(31), onCallUntil(min(60), "A"), nextOnCallFrom(next, "B")),
                         expectAt(min(59), onCallUntil(min(60), "A"), nextOnCallFrom(next, "B")),
                         expectAt(min(60), nextOnCallFrom(next, "B")),
                         expectAt(min(61), nextOnCallFrom(next, "B")));
    }

    @Test
    void singleUserTimeRestrictedRotationWithDisjointOverride_FullTest() throws Exception {
        Instant next = min((24 * 60) + 30);
        assertRecipients(asList(rotateForever(DAILY, asList(restrictTo(min(30), min(60))), "A")),
                         asList(overrideBetween(min(90), min(120), "A")),
                         expectAt(min(29), nextOnCallFrom(min(30), "A")),
                         expectAt(min(30), onCallUntil(min(60), "A"), nextOnCallFrom(min(90), "A")),
                         expectAt(min(31), onCallUntil(min(60), "A"), nextOnCallFrom(min(90), "A")),
                         expectAt(min(59), onCallUntil(min(60), "A"), nextOnCallFrom(min(90), "A")),
                         expectAt(min(60), nextOnCallFrom(min(90), "A")),
                         expectAt(min(61), nextOnCallFrom(min(90), "A")),
                         expectAt(min(89), nextOnCallFrom(min(90), "A")),
                         expectAt(min(90), onCallUntil(min(120), "A")),
                         expectAt(min(91), onCallUntil(min(120), "A")),
                         expectAt(min(119), onCallUntil(min(120), "A")),
                         expectAt(min(120), nextOnCallFrom(next, "A")),
                         expectAt(min(121), nextOnCallFrom(next, "A")));
    }

    @Test
    void globalSelfOverride_FullTest() throws Exception {
        assertRecipients(asList(),
                         asList(overrideBetween(min(30), min(50), "A", min(1)),
                                overrideBetween(min(40), min(60), "A", min(2)),
                                overrideBetween(min(60), min(90), "B", min(3))),
                         expectAt(min(29), nextOnCallFrom(min(30), "A")),
                         expectAt(min(30), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(31), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(39), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(40), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(41), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(59), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(60), onCallUntil(min(90), "B")),
                         expectAt(min(61), onCallUntil(min(90), "B")),
                         expectAt(min(89), onCallUntil(min(90), "B")),
                         expectAt(min(90)),
                         expectAt(min(91)));
    }

    @Test
    void selfOverride() throws Exception {
        assertRecipients(asList(rotateForever(HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(45), min(60), "A")),
                         expectAt(ZERO, onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")));
    }

    @Test
    void selfOverride_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(HOURLY, "A", "B", "C")),
                         asList(overrideBetween(min(45), min(60), "A")),
                         expectAt(ZERO, onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(44), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(45), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(46), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(59), onCallUntil(min(60), "A"), nextOnCallFrom(min(60), "B")),
                         expectAt(min(60), onCallUntil(min(120), "B"), nextOnCallFrom(min(120), "C")));
    }

    @Test
    void tardisScrumMaster_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(WEEKLY,
                                              asList(restrictTo(MONDAY, dayHour(0, 9), dayHour(0, 9.5))),
                                              "A",
                                              "B",
                                              "C")),
                         asList(),
                         expectAt(dayHour(0, 0), nextOnCallFrom(dayHour(0, 9), "A")),
                         expectAt(dayHour(0, 8), nextOnCallFrom(dayHour(0, 9), "A")),
                         expectAt(dayHour(0, 9), onCallUntil(dayHour(0, 9.5), "A"), nextOnCallFrom(dayHour(7, 9), "B")),
                         expectAt(dayHour(0, 9.5), nextOnCallFrom(dayHour(7, 9), "B")),
                         expectAt(dayHour(0, 10), nextOnCallFrom(dayHour(7, 9), "B")));
    }

    @Test
    void tardisDaily_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(DAILY,
                                              asList(restrictTo(MONDAY, hour(9), hour(23)),
                                                     restrictTo(TUESDAY, hour(9), hour(23)),
                                                     restrictTo(WEDNESDAY, hour(9), hour(23)),
                                                     restrictTo(THURSDAY, hour(9), hour(23)),
                                                     restrictTo(FRIDAY, hour(9), hour(23))),
                                              "A",
                                              "B",
                                              "C",
                                              "D",
                                              "E",
                                              "F",
                                              "G")),
                         asList(),

                         expectAt(dayHour(0, 8), nextOnCallFrom(dayHour(0, 9), "A")),
                         expectAt(dayHour(0, 9), onCallUntil(dayHour(0, 23), "A"), nextOnCallFrom(dayHour(1, 9), "B")),
                         expectAt(dayHour(0, 10), onCallUntil(dayHour(0, 23), "A"), nextOnCallFrom(dayHour(1, 9), "B")),
                         expectAt(dayHour(0, 22), onCallUntil(dayHour(0, 23), "A"), nextOnCallFrom(dayHour(1, 9), "B")),
                         expectAt(dayHour(0, 23), nextOnCallFrom(dayHour(1, 9), "B")),
                         expectAt(dayHour(0, 24), nextOnCallFrom(dayHour(1, 9), "B")),

                         expectAt(dayHour(1, 8), nextOnCallFrom(dayHour(1, 9), "B")),
                         expectAt(dayHour(1, 9), onCallUntil(dayHour(1, 23), "B"), nextOnCallFrom(dayHour(2, 9), "C")),
                         expectAt(dayHour(1, 10), onCallUntil(dayHour(1, 23), "B"), nextOnCallFrom(dayHour(2, 9), "C")),
                         expectAt(dayHour(1, 22), onCallUntil(dayHour(1, 23), "B"), nextOnCallFrom(dayHour(2, 9), "C")),
                         expectAt(dayHour(1, 23), nextOnCallFrom(dayHour(2, 9), "C")),
                         expectAt(dayHour(1, 24), nextOnCallFrom(dayHour(2, 9), "C")),

                         expectAt(dayHour(2, 8), nextOnCallFrom(dayHour(2, 9), "C")),

                         expectAt(dayHour(3, 8), nextOnCallFrom(dayHour(3, 9), "D")),

                         expectAt(dayHour(4, 8), nextOnCallFrom(dayHour(4, 9), "E")),
                         expectAt(dayHour(4, 9), onCallUntil(dayHour(4, 23), "E"), nextOnCallFrom(dayHour(7, 9), "F")),
                         expectAt(dayHour(4, 10), onCallUntil(dayHour(4, 23), "E"), nextOnCallFrom(dayHour(7, 9), "F")),
                         expectAt(dayHour(4, 22), onCallUntil(dayHour(4, 23), "E"), nextOnCallFrom(dayHour(7, 9), "F")),
                         expectAt(dayHour(4, 23), nextOnCallFrom(dayHour(7, 9), "F")),
                         expectAt(dayHour(4, 24), nextOnCallFrom(dayHour(7, 9), "F")));
    }

    @Test
    void platformShadow_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(WEEKLY,
                                              asList(restrictTo(MONDAY, hour(8.5), hour(18.5)),
                                                     restrictTo(TUESDAY, hour(8.5), hour(18.5)),
                                                     restrictTo(WEDNESDAY, hour(8.5), hour(18.5)),
                                                     restrictTo(THURSDAY, hour(8.5), hour(18.5)),
                                                     restrictTo(FRIDAY, hour(8.5), hour(18.5))),
                                              "A",
                                              "B",
                                              "C",
                                              "D",
                                              "E",
                                              "F",
                                              "G")),
                         asList(),

                         expectAt(dayHour(0, 8), nextOnCallFrom(dayHour(0, 8.5), "A")),
                         expectAt(dayHour(0, 8.5),
                                  onCallUntil(dayHour(0, 18.5), "A"),
                                  nextOnCallFrom(dayHour(7, 8.5), "B")),
                         expectAt(dayHour(0, 9),
                                  onCallUntil(dayHour(0, 18.5), "A"),
                                  nextOnCallFrom(dayHour(7, 8.5), "B")),
                         expectAt(dayHour(0, 18),
                                  onCallUntil(dayHour(0, 18.5), "A"),
                                  nextOnCallFrom(dayHour(7, 8.5), "B")),
                         expectAt(dayHour(0, 18.5), nextOnCallFrom(dayHour(1, 8.5), "A")),
                         expectAt(dayHour(0, 24), nextOnCallFrom(dayHour(1, 8.5), "A")),

                         expectAt(dayHour(1, 8), nextOnCallFrom(dayHour(1, 8.5), "A")),
                         expectAt(dayHour(1, 8.5),
                                  onCallUntil(dayHour(1, 18.5), "A"),
                                  nextOnCallFrom(dayHour(7, 8.5), "B")),
                         expectAt(dayHour(1, 9),
                                  onCallUntil(dayHour(1, 18.5), "A"),
                                  nextOnCallFrom(dayHour(7, 8.5), "B")),
                         expectAt(dayHour(1, 18),
                                  onCallUntil(dayHour(1, 18.5), "A"),
                                  nextOnCallFrom(dayHour(7, 8.5), "B")),
                         expectAt(dayHour(1, 18.5), nextOnCallFrom(dayHour(2, 8.5), "A")),
                         expectAt(dayHour(1, 24), nextOnCallFrom(dayHour(2, 8.5), "A")),

                         expectAt(dayHour(4, 8), nextOnCallFrom(dayHour(4, 8.5), "A")),
                         expectAt(dayHour(4, 8.5),
                                  onCallUntil(dayHour(4, 18.5), "A"),
                                  nextOnCallFrom(dayHour(7, 8.5), "B")),
                         expectAt(dayHour(4, 9),
                                  onCallUntil(dayHour(4, 18.5), "A"),
                                  nextOnCallFrom(dayHour(7, 8.5), "B")),
                         expectAt(dayHour(4, 18),
                                  onCallUntil(dayHour(4, 18.5), "A"),
                                  nextOnCallFrom(dayHour(7, 8.5), "B")),
                         expectAt(dayHour(4, 18.5), nextOnCallFrom(dayHour(7, 8.5), "B")),
                         expectAt(dayHour(4, 24), nextOnCallFrom(dayHour(7, 8.5), "B")));
    }

    @Test
    void platformResponder_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(DAILY,
                                              asList(restrictTo(MONDAY, hour(8.5), hour(18.5)),
                                                     restrictTo(TUESDAY, hour(8.5), hour(18.5)),
                                                     restrictTo(WEDNESDAY, hour(8.5), hour(18.5)),
                                                     restrictTo(THURSDAY, hour(8.5), hour(18.5)),
                                                     restrictTo(FRIDAY, hour(8.5), hour(18.5))),
                                              "A",
                                              "B",
                                              "C",
                                              "D",
                                              "E",
                                              "F",
                                              "G")),
                         asList(),

                         expectAt(dayHour(0, 8), nextOnCallFrom(dayHour(0, 8.5), "A")),
                         expectAt(dayHour(0, 8.5),
                                  onCallUntil(dayHour(0, 18.5), "A"),
                                  nextOnCallFrom(dayHour(1, 8.5), "B")),
                         expectAt(dayHour(0, 9),
                                  onCallUntil(dayHour(0, 18.5), "A"),
                                  nextOnCallFrom(dayHour(1, 8.5), "B")),
                         expectAt(dayHour(0, 18),
                                  onCallUntil(dayHour(0, 18.5), "A"),
                                  nextOnCallFrom(dayHour(1, 8.5), "B")),
                         expectAt(dayHour(0, 18.5), nextOnCallFrom(dayHour(1, 8.5), "B")),
                         expectAt(dayHour(0, 24), nextOnCallFrom(dayHour(1, 8.5), "B")),

                         expectAt(dayHour(1, 8), nextOnCallFrom(dayHour(1, 8.5), "B")),
                         expectAt(dayHour(1, 8.5),
                                  onCallUntil(dayHour(1, 18.5), "B"),
                                  nextOnCallFrom(dayHour(2, 8.5), "C")),
                         expectAt(dayHour(1, 9),
                                  onCallUntil(dayHour(1, 18.5), "B"),
                                  nextOnCallFrom(dayHour(2, 8.5), "C")),
                         expectAt(dayHour(1, 18),
                                  onCallUntil(dayHour(1, 18.5), "B"),
                                  nextOnCallFrom(dayHour(2, 8.5), "C")),
                         expectAt(dayHour(1, 18.5), nextOnCallFrom(dayHour(2, 8.5), "C")),
                         expectAt(dayHour(1, 24), nextOnCallFrom(dayHour(2, 8.5), "C")),

                         expectAt(dayHour(2, 8), nextOnCallFrom(dayHour(2, 8.5), "C")),

                         expectAt(dayHour(3, 8), nextOnCallFrom(dayHour(3, 8.5), "D")),

                         expectAt(dayHour(4, 8), nextOnCallFrom(dayHour(4, 8.5), "E")),
                         expectAt(dayHour(4, 8.5),
                                  onCallUntil(dayHour(4, 18.5), "E"),
                                  nextOnCallFrom(dayHour(7, 8.5), "F")),
                         expectAt(dayHour(4, 9),
                                  onCallUntil(dayHour(4, 18.5), "E"),
                                  nextOnCallFrom(dayHour(7, 8.5), "F")),
                         expectAt(dayHour(4, 18),
                                  onCallUntil(dayHour(4, 18.5), "E"),
                                  nextOnCallFrom(dayHour(7, 8.5), "F")),
                         expectAt(dayHour(4, 18.5), nextOnCallFrom(dayHour(7, 8.5), "F")),
                         expectAt(dayHour(4, 24), nextOnCallFrom(dayHour(7, 8.5), "F")));
    }

    @Test
    void platformResponderNightWatch_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(dayHour(0, 8.5),
                                              WEEKLY,
                                              asList(restrictTo(MONDAY, hour(18.5), TUESDAY, hour(8.5)),
                                                     restrictTo(TUESDAY, hour(18.5), WEDNESDAY, hour(8.5)),
                                                     restrictTo(WEDNESDAY, hour(18.5), THURSDAY, hour(8.5)),
                                                     restrictTo(THURSDAY, hour(18.5), FRIDAY, hour(8.5)),
                                                     restrictTo(FRIDAY, hour(18.5), MONDAY, hour(8.5))),
                                              "A",
                                              "B",
                                              "C",
                                              "D",
                                              "E",
                                              "F",
                                              "G")),
                         asList(),

                         expectAt(dayHour(0, 8), nextOnCallFrom(dayHour(0, 18.5), "A")),
                         expectAt(dayHour(0, 8.5), nextOnCallFrom(dayHour(0, 18.5), "A")),
                         expectAt(dayHour(0, 18), nextOnCallFrom(dayHour(0, 18.5), "A")),
                         expectAt(dayHour(0, 18.5),
                                  onCallUntil(dayHour(1, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(0, 23),
                                  onCallUntil(dayHour(1, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(0, 24),
                                  onCallUntil(dayHour(1, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),

                         expectAt(dayHour(1, 8),
                                  onCallUntil(dayHour(1, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(1, 8.5), nextOnCallFrom(dayHour(1, 18.5), "A")),
                         expectAt(dayHour(1, 9), nextOnCallFrom(dayHour(1, 18.5), "A")),
                         expectAt(dayHour(1, 18), nextOnCallFrom(dayHour(1, 18.5), "A")),
                         expectAt(dayHour(1, 18.5),
                                  onCallUntil(dayHour(2, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(1, 24),
                                  onCallUntil(dayHour(2, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),

                         expectAt(dayHour(4, 8),
                                  onCallUntil(dayHour(4, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(4, 8.5), nextOnCallFrom(dayHour(4, 18.5), "A")),
                         expectAt(dayHour(4, 9), nextOnCallFrom(dayHour(4, 18.5), "A")),
                         expectAt(dayHour(4, 18), nextOnCallFrom(dayHour(4, 18.5), "A")),
                         expectAt(dayHour(4, 18.5),
                                  onCallUntil(dayHour(7, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(4, 24),
                                  onCallUntil(dayHour(7, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),

                         expectAt(dayHour(5, 8),
                                  onCallUntil(dayHour(7, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(5, 8.5),
                                  onCallUntil(dayHour(7, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(5, 9),
                                  onCallUntil(dayHour(7, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(5, 18),
                                  onCallUntil(dayHour(7, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(5, 18.5),
                                  onCallUntil(dayHour(7, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(5, 24),
                                  onCallUntil(dayHour(7, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),

                         expectAt(dayHour(6, 8),
                                  onCallUntil(dayHour(7, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(6, 8.5),
                                  onCallUntil(dayHour(7, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(6, 9),
                                  onCallUntil(dayHour(7, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(6, 18),
                                  onCallUntil(dayHour(7, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(6, 18.5),
                                  onCallUntil(dayHour(7, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(6, 24),
                                  onCallUntil(dayHour(7, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),

                         expectAt(dayHour(7, 8),
                                  onCallUntil(dayHour(7, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(7, 8.5), nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(7, 9), nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(7, 18), nextOnCallFrom(dayHour(7, 18.5), "B")),
                         expectAt(dayHour(7, 18.5),
                                  onCallUntil(dayHour(8, 8.5), "B"),
                                  nextOnCallFrom(dayHour(14, 18.5), "C")),
                         expectAt(dayHour(7, 24),
                                  onCallUntil(dayHour(8, 8.5), "B"),
                                  nextOnCallFrom(dayHour(14, 18.5), "C")));
    }

    @Test
    void platformResponderNightWatch() throws Exception {
        assertRecipients(asList(rotateForever(dayHour(0, 8.5),
                                              WEEKLY,
                                              asList(restrictTo(MONDAY, hour(18.5), TUESDAY, hour(8.5)),
                                                     restrictTo(TUESDAY, hour(18.5), WEDNESDAY, hour(8.5)),
                                                     restrictTo(WEDNESDAY, hour(18.5), THURSDAY, hour(8.5)),
                                                     restrictTo(THURSDAY, hour(18.5), FRIDAY, hour(8.5)),
                                                     restrictTo(FRIDAY, hour(18.5), MONDAY, hour(8.5))),
                                              "A",
                                              "B",
                                              "C",
                                              "D",
                                              "E",
                                              "F",
                                              "G")),
                         asList(),
                         expectAt(dayHour(0, 18.5),
                                  onCallUntil(dayHour(1, 8.5), "A"),
                                  nextOnCallFrom(dayHour(7, 18.5), "B")));
    }

    @Test
    void timeRestrictionExceedingCurrentDay_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(DAILY,
                                              asList(restrictTo(hour(18), hour(8))),
                                              "A",
                                              "B",
                                              "C",
                                              "D",
                                              "E",
                                              "F",
                                              "G")),
                         asList(),
                         expectAt(dayHour(0, 17), nextOnCallFrom(dayHour(0, 18), "A")),
                         expectAt(dayHour(0, 18), onCallUntil(dayHour(1, 0), "A"), nextOnCallFrom(dayHour(1, 0), "B")),
                         expectAt(dayHour(0, 23), onCallUntil(dayHour(1, 0), "A"), nextOnCallFrom(dayHour(1, 0), "B")),
                         expectAt(dayHour(1, 0), onCallUntil(dayHour(1, 8), "B"), nextOnCallFrom(dayHour(2, 0), "C")),
                         expectAt(dayHour(1, 1), onCallUntil(dayHour(1, 8), "B"), nextOnCallFrom(dayHour(2, 0), "C")),
                         expectAt(dayHour(1, 7), onCallUntil(dayHour(1, 8), "B"), nextOnCallFrom(dayHour(2, 0), "C")),
                         expectAt(dayHour(1, 8), nextOnCallFrom(dayHour(1, 18), "B")),
                         expectAt(dayHour(1, 9), nextOnCallFrom(dayHour(1, 18), "B")),
                         expectAt(dayHour(1, 17), nextOnCallFrom(dayHour(1, 18), "B")),
                         expectAt(dayHour(1, 18), onCallUntil(dayHour(2, 0), "B"), nextOnCallFrom(dayHour(2, 0), "C")));
    }

    @Test
    void timeRestrictionExceedingCurrentDay() throws Exception {
        assertRecipients(asList(rotateForever(DAILY,
                                              asList(restrictTo(hour(18), hour(8))),
                                              "A",
                                              "B",
                                              "C",
                                              "D",
                                              "E",
                                              "F",
                                              "G")),
                         asList(),
                         expectAt(dayHour(1, 1), onCallUntil(dayHour(1, 8), "B"), nextOnCallFrom(dayHour(2, 0), "C")));
    }

    @Test
    void timeRestrictionCoveringWholeDayWithSingleRestrictionInDailyRotation_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(dayHour(0, 0),
                                              DAILY,
                                              asList(restrictTo(hour(18), hour(18))),
                                              "A",
                                              "B",
                                              "C",
                                              "D",
                                              "E",
                                              "F",
                                              "G")),
                         asList(),
                         expectAt(dayHour(0, 0), onCallUntil(dayHour(1, 0), "A"), nextOnCallFrom(dayHour(1, 0), "B")),
                         expectAt(dayHour(0, 18), onCallUntil(dayHour(1, 0), "A"), nextOnCallFrom(dayHour(1, 0), "B")),
                         expectAt(dayHour(0, 19), onCallUntil(dayHour(1, 0), "A"), nextOnCallFrom(dayHour(1, 0), "B")),
                         expectAt(dayHour(0, 23), onCallUntil(dayHour(1, 0), "A"), nextOnCallFrom(dayHour(1, 0), "B")),
                         expectAt(dayHour(1, 0), onCallUntil(dayHour(2, 0), "B"), nextOnCallFrom(dayHour(2, 0), "C")),
                         expectAt(dayHour(1, 1), onCallUntil(dayHour(2, 0), "B"), nextOnCallFrom(dayHour(2, 0), "C")),
                         expectAt(dayHour(1, 8), onCallUntil(dayHour(2, 0), "B"), nextOnCallFrom(dayHour(2, 0), "C")),
                         expectAt(dayHour(1, 18), onCallUntil(dayHour(2, 0), "B"), nextOnCallFrom(dayHour(2, 0), "C")));
    }

    @Test
    void timeRestrictionCoveringWholeDayWithMultipleRestrictionsInDailyRotation_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(dayHour(0, 0),
                                              DAILY,
                                              asList(restrictTo(hour(18), hour(8)), restrictTo(hour(8), hour(18))),
                                              "A",
                                              "B",
                                              "C",
                                              "D",
                                              "E",
                                              "F",
                                              "G")),
                         asList(),
                         expectAt(dayHour(0, 0), onCallUntil(dayHour(1, 0), "A"), nextOnCallFrom(dayHour(1, 0), "B")),
                         expectAt(dayHour(0, 18), onCallUntil(dayHour(1, 0), "A"), nextOnCallFrom(dayHour(1, 0), "B")),
                         expectAt(dayHour(0, 19), onCallUntil(dayHour(1, 0), "A"), nextOnCallFrom(dayHour(1, 0), "B")),
                         expectAt(dayHour(0, 23), onCallUntil(dayHour(1, 0), "A"), nextOnCallFrom(dayHour(1, 0), "B")),
                         expectAt(dayHour(1, 0), onCallUntil(dayHour(2, 0), "B"), nextOnCallFrom(dayHour(2, 0), "C")),
                         expectAt(dayHour(1, 1), onCallUntil(dayHour(2, 0), "B"), nextOnCallFrom(dayHour(2, 0), "C")),
                         expectAt(dayHour(1, 8), onCallUntil(dayHour(2, 0), "B"), nextOnCallFrom(dayHour(2, 0), "C")),
                         expectAt(dayHour(1, 18), onCallUntil(dayHour(2, 0), "B"), nextOnCallFrom(dayHour(2, 0), "C")));
    }

    @Test
    void timeRestrictionCoveringWholeDayInWeeklyRotation_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(WEEKLY,
                                              asList(restrictTo(MONDAY, hour(8), TUESDAY, hour(8)),
                                                     restrictTo(TUESDAY, hour(8), WEDNESDAY, hour(8)),
                                                     restrictTo(WEDNESDAY, hour(8), THURSDAY, hour(8)),
                                                     restrictTo(THURSDAY, hour(8), FRIDAY, hour(8)),
                                                     restrictTo(FRIDAY, hour(8), SATURDAY, hour(8))),
                                              "A",
                                              "B",
                                              "C",
                                              "D",
                                              "E",
                                              "F",
                                              "G")),
                         asList(),
                         expectAt(dayHour(0, 0), nextOnCallFrom(dayHour(0, 8), "A")),
                         expectAt(dayHour(0, 1), nextOnCallFrom(dayHour(0, 8), "A")),
                         expectAt(dayHour(0, 7), nextOnCallFrom(dayHour(0, 8), "A")),
                         expectAt(dayHour(0, 8), onCallUntil(dayHour(5, 8), "A"), nextOnCallFrom(dayHour(7, 8), "B")),
                         expectAt(dayHour(0, 9), onCallUntil(dayHour(5, 8), "A"), nextOnCallFrom(dayHour(7, 8), "B")),
                         expectAt(dayHour(0, 17), onCallUntil(dayHour(5, 8), "A"), nextOnCallFrom(dayHour(7, 8), "B")),
                         expectAt(dayHour(0, 18), onCallUntil(dayHour(5, 8), "A"), nextOnCallFrom(dayHour(7, 8), "B")),
                         expectAt(dayHour(0, 19), onCallUntil(dayHour(5, 8), "A"), nextOnCallFrom(dayHour(7, 8), "B")),
                         expectAt(dayHour(0, 23), onCallUntil(dayHour(5, 8), "A"), nextOnCallFrom(dayHour(7, 8), "B")),
                         expectAt(dayHour(1, 0), onCallUntil(dayHour(5, 8), "A"), nextOnCallFrom(dayHour(7, 8), "B")),
                         expectAt(dayHour(5, 7), onCallUntil(dayHour(5, 8), "A"), nextOnCallFrom(dayHour(7, 8), "B")),
                         expectAt(dayHour(5, 8), nextOnCallFrom(dayHour(7, 8), "B")),
                         expectAt(dayHour(5, 9), nextOnCallFrom(dayHour(7, 8), "B")),
                         expectAt(dayHour(6, 0), nextOnCallFrom(dayHour(7, 8), "B")),
                         expectAt(dayHour(7, 0), nextOnCallFrom(dayHour(7, 8), "B")),
                         expectAt(dayHour(7, 1), nextOnCallFrom(dayHour(7, 8), "B")),
                         expectAt(dayHour(7, 7), nextOnCallFrom(dayHour(7, 8), "B")),
                         expectAt(dayHour(7, 8),
                                  onCallUntil(dayHour(12, 8), "B"),
                                  nextOnCallFrom(dayHour(14, 8), "C")));
    }

    @Test
    void timeRestrictionExceedingCurrentWeek_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(dayHour(0, 0),
                                              WEEKLY,
                                              asList(restrictTo(MONDAY, hour(18), MONDAY, hour(8))),
                                              "A",
                                              "B",
                                              "C",
                                              "D",
                                              "E",
                                              "F",
                                              "G")),
                         asList(),
                         expectAt(dayHour(0, 17), nextOnCallFrom(dayHour(0, 18), "A")),
                         expectAt(dayHour(0, 18), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(0, 19), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(0, 23), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(1, 0), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(1, 1), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(1, 7), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(1, 8), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(1, 9), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(6, 7), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(6, 8), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(6, 9), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(6, 17), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(6, 18), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(6, 19), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(6, 23), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(7, 0), onCallUntil(dayHour(7, 8), "B"), nextOnCallFrom(dayHour(14, 0), "C")),
                         expectAt(dayHour(7, 1), onCallUntil(dayHour(7, 8), "B"), nextOnCallFrom(dayHour(14, 0), "C")),
                         expectAt(dayHour(7, 7), onCallUntil(dayHour(7, 8), "B"), nextOnCallFrom(dayHour(14, 0), "C")),
                         expectAt(dayHour(7, 8), nextOnCallFrom(dayHour(7, 18), "B")),
                         expectAt(dayHour(7, 9), nextOnCallFrom(dayHour(7, 18), "B")),
                         expectAt(dayHour(7, 17), nextOnCallFrom(dayHour(7, 18), "B")),
                         expectAt(dayHour(7, 18),
                                  onCallUntil(dayHour(14, 0), "B"),
                                  nextOnCallFrom(dayHour(14, 0), "C")));
    }

    @Test
    void timeRestrictionCoveringWholeWeekWithSingleRestriction_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(dayHour(0, 0),
                                              WEEKLY,
                                              asList(restrictTo(MONDAY, hour(0), MONDAY, hour(0))),
                                              "A",
                                              "B",
                                              "C",
                                              "D",
                                              "E",
                                              "F",
                                              "G")),
                         asList(),
                         expectAt(dayHour(0, 0), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(1, 0), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(7, 0),
                                  onCallUntil(dayHour(14, 0), "B"),
                                  nextOnCallFrom(dayHour(14, 0), "C")));
    }

    @Test
    void timeRestrictionCoveringWholeWeekWithMultipleRestrictions_FullTest() throws Exception {
        assertRecipients(asList(rotateForever(dayHour(0, 0),
                                              WEEKLY,
                                              asList(restrictTo(MONDAY, hour(0), FRIDAY, hour(0)),
                                                     restrictTo(FRIDAY, hour(0), MONDAY, hour(0))),
                                              "A",
                                              "B",
                                              "C",
                                              "D",
                                              "E",
                                              "F",
                                              "G")),
                         asList(),
                         expectAt(dayHour(0, 0), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(1, 0), onCallUntil(dayHour(7, 0), "A"), nextOnCallFrom(dayHour(7, 0), "B")),
                         expectAt(dayHour(7, 0),
                                  onCallUntil(dayHour(14, 0), "B"),
                                  nextOnCallFrom(dayHour(14, 0), "C")));
    }

    @Test
    void issue2614() throws Exception {
        String y                  = "Y";
        String g                  = "G";
        String p                  = "P";
        String d                  = "D";
        String overriddenRotation = "Day Rotation";
        int    zeroBasedWeekNo    = 8;
        int    day                = (zeroBasedWeekNo * 7) + 4;
        assertRecipients(asList(rotateForever(dayHour(3, 18),
                                              WEEKLY,
                                              asList(restrictTo(MONDAY, hour(18), TUESDAY, hour(9)),
                                                     restrictTo(TUESDAY, hour(18), WEDNESDAY, hour(9)),
                                                     restrictTo(WEDNESDAY, hour(18), THURSDAY, hour(9)),
                                                     restrictTo(THURSDAY, hour(18), FRIDAY, hour(9)),
                                                     restrictTo(FRIDAY, hour(18), MONDAY, hour(9))),
                                              y,
                                              g,
                                              p,
                                              d),
                                rotateForever(overriddenRotation,
                                              dayHour(3, 9),
                                              WEEKLY,
                                              asList(restrictTo(MONDAY, hour(9), MONDAY, hour(18)),
                                                     restrictTo(TUESDAY, hour(9), TUESDAY, hour(18)),
                                                     restrictTo(WEDNESDAY, hour(9), WEDNESDAY, hour(18)),
                                                     restrictTo(THURSDAY, hour(9), THURSDAY, hour(18)),
                                                     restrictTo(FRIDAY, hour(9), FRIDAY, hour(18))),
                                              p,
                                              d,
                                              g)),
                         asList(overrideBetween(dayHour(day, 12), dayHour(day, 18), asList(overriddenRotation), d)),
                         expectAt(dayHour(day, 0),
                                  onCallUntil(dayHour(day, 9), y),
                                  nextOnCallFrom(dayHour(day + 6, 18), g),
                                  nextOnCallFrom(dayHour(day, 9), g)),
                         expectAt(dayHour(day, 1),
                                  onCallUntil(dayHour(day, 9), y),
                                  nextOnCallFrom(dayHour(day + 6, 18), g),
                                  nextOnCallFrom(dayHour(day, 9), g)),
                         expectAt(dayHour(day, 2),
                                  onCallUntil(dayHour(day, 9), y),
                                  nextOnCallFrom(dayHour(day + 6, 18), g),
                                  nextOnCallFrom(dayHour(day, 9), g)),
                         expectAt(dayHour(day, 3),
                                  onCallUntil(dayHour(day, 9), y),
                                  nextOnCallFrom(dayHour(day + 6, 18), g),
                                  nextOnCallFrom(dayHour(day, 9), g)),
                         expectAt(dayHour(day, 4),
                                  onCallUntil(dayHour(day, 9), y),
                                  nextOnCallFrom(dayHour(day + 6, 18), g),
                                  nextOnCallFrom(dayHour(day, 9), g)),
                         expectAt(dayHour(day, 5),
                                  onCallUntil(dayHour(day, 9), y),
                                  nextOnCallFrom(dayHour(day + 6, 18), g),
                                  nextOnCallFrom(dayHour(day, 9), g)),
                         expectAt(dayHour(day, 6),
                                  onCallUntil(dayHour(day, 9), y),
                                  nextOnCallFrom(dayHour(day + 6, 18), g),
                                  nextOnCallFrom(dayHour(day, 9), g)),
                         expectAt(dayHour(day, 7),
                                  onCallUntil(dayHour(day, 9), y),
                                  nextOnCallFrom(dayHour(day + 6, 18), g),
                                  nextOnCallFrom(dayHour(day, 9), g)),
                         expectAt(dayHour(day, 8),
                                  onCallUntil(dayHour(day, 9), y),
                                  nextOnCallFrom(dayHour(day + 6, 18), g),
                                  nextOnCallFrom(dayHour(day, 9), g)),
                         expectAt(dayHour(day, 9),
                                  onCallUntil(dayHour(day, 12), g),
                                  nextOnCallFrom(dayHour(day, 18), y),
                                  nextOnCallFrom(dayHour(day, 12), d)),
                         expectAt(dayHour(day, 10),
                                  onCallUntil(dayHour(day, 12), g),
                                  nextOnCallFrom(dayHour(day, 18), y),
                                  nextOnCallFrom(dayHour(day, 12), d)),
                         expectAt(dayHour(day, 11),
                                  onCallUntil(dayHour(day, 12), g),
                                  nextOnCallFrom(dayHour(day, 18), y),
                                  nextOnCallFrom(dayHour(day, 12), d)),
                         expectAt(dayHour(day, 12),
                                  onCallUntil(dayHour(day, 18), d),
                                  nextOnCallFrom(dayHour(day, 18), y),
                                  nextOnCallFrom(dayHour(day + 3, 9), g)),
                         expectAt(dayHour(day, 13),
                                  onCallUntil(dayHour(day, 18), d),
                                  nextOnCallFrom(dayHour(day, 18), y),
                                  nextOnCallFrom(dayHour(day + 3, 9), g)),
                         expectAt(dayHour(day, 14),
                                  onCallUntil(dayHour(day, 18), d),
                                  nextOnCallFrom(dayHour(day, 18), y),
                                  nextOnCallFrom(dayHour(day + 3, 9), g)),
                         expectAt(dayHour(day, 15),
                                  onCallUntil(dayHour(day, 18), d),
                                  nextOnCallFrom(dayHour(day, 18), y),
                                  nextOnCallFrom(dayHour(day + 3, 9), g)),
                         expectAt(dayHour(day, 16),
                                  onCallUntil(dayHour(day, 18), d),
                                  nextOnCallFrom(dayHour(day, 18), y),
                                  nextOnCallFrom(dayHour(day + 3, 9), g)),
                         expectAt(dayHour(day, 17),
                                  onCallUntil(dayHour(day, 18), d),
                                  nextOnCallFrom(dayHour(day, 18), y),
                                  nextOnCallFrom(dayHour(day + 3, 9), g)),
                         expectAt(dayHour(day, 18),
                                  onCallUntil(dayHour(day + 3, 9), y),
                                  nextOnCallFrom(dayHour(day + 6, 18), g),
                                  nextOnCallFrom(dayHour(day + 3, 9), g)),
                         expectAt(dayHour(day, 19),
                                  onCallUntil(dayHour(day + 3, 9), y),
                                  nextOnCallFrom(dayHour(day + 6, 18), g),
                                  nextOnCallFrom(dayHour(day + 3, 9), g)),
                         expectAt(dayHour(day, 20),
                                  onCallUntil(dayHour(day + 3, 9), y),
                                  nextOnCallFrom(dayHour(day + 6, 18), g),
                                  nextOnCallFrom(dayHour(day + 3, 9), g)),
                         expectAt(dayHour(day, 21),
                                  onCallUntil(dayHour(day + 3, 9), y),
                                  nextOnCallFrom(dayHour(day + 6, 18), g),
                                  nextOnCallFrom(dayHour(day + 3, 9), g)),
                         expectAt(dayHour(day, 22),
                                  onCallUntil(dayHour(day + 3, 9), y),
                                  nextOnCallFrom(dayHour(day + 6, 18), g),
                                  nextOnCallFrom(dayHour(day + 3, 9), g)),
                         expectAt(dayHour(day, 23),
                                  onCallUntil(dayHour(day + 3, 9), y),
                                  nextOnCallFrom(dayHour(day + 6, 18), g),
                                  nextOnCallFrom(dayHour(day + 3, 9), g)));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///                                                                                                                                            ///
    ///                                                         T E S T   D S L                                                                    ///
    ///                                                                                                                                            ///
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void assertRecipients(List<Timeline<ZonedDateTime, String>> rotations,
                                         List<ValuedRange<Instant, String>> overrides,
                                         Entry<Instant, List<Oncall>>... expecteds) {
        List<ValuedRange<ZonedDateTime, UnaryOperator<List<String>>>> intervals = map(overrides, TimelineTest::asPatch);
        StaticTimeline<ZonedDateTime, UnaryOperator<List<String>>>    patches   = StaticTimeline.ofIntervals(intervals);
        UnionTimeline<ZonedDateTime, String>                          base      = UnionTimeline.of(rotations);
        Timeline<ZonedDateTime, String>                               snapshot  = PatchedTimeline.of(base,
                                                                                                     asList(patches));

        // execute the method
        List<Entry<Instant, List<Oncall>>> actuals = new ArrayList<>();
        for (Entry<Instant, List<Oncall>> expected : expecteds) {
            Instant                                time             = expected.getKey();
            long                                   millis           = time.toEpochMilli();
            ZonedDateTime                          dateTime         = instantToZonedDateTime(Instant.ofEpochMilli(millis));
            ZonedDateTime                          oneMonthBefore   = dateTime.minus(THIRTY_DAYS);
            ZonedDateTime                          oneMonthAfter    = dateTime.plus(THIRTY_DAYS);
            Range<ZonedDateTime>                   calculationRange = Range.of(oneMonthBefore, oneMonthAfter);
            TimelineSegment<ZonedDateTime, String> timeline         = snapshot.toSegment(calculationRange);
            List<Oncall>                           oncalls          = new ArrayList<>();
            addIfNotNull(oncalls, false, timeline.findCurrentInterval(dateTime));
            addIfNotNull(oncalls, true, timeline.findNextNonEmptyInterval(dateTime));
            actuals.add(pair(time, oncalls));
        }

        // prepare for comparison
        List<Entry<Instant, List<Oncall>>> expectedsModified = Stream.of(expecteds).map(expected -> {
            Instant calculationOffset = expected.getKey();
            return pair(calculationOffset,
                        expected.getValue()
                                .stream().map(value -> new Oncall(value.names,
                                                                  normalize(value.startTime, calculationOffset),
                                                                  normalize(value.endTime, calculationOffset),
                                                                  value.next))
                                .collect(Collectors.toList()));
        }).collect(Collectors.toList());

        // compare results
        assertEquals(format(expectedsModified), format(actuals));
    }

    private static Instant normalize(Instant instant, Instant calculationOffset) {
        return instant.isBefore(calculationOffset.plus(THIRTY_DAYS)) ? instant : ZERO;
    }

    private static ValuedRange<ZonedDateTime, UnaryOperator<List<String>>> asPatch(ValuedRange<Instant, String> override) {
        Range<Instant> range = override.getRange();
        return ValuedRange.of(dateRange(range.getStartInclusive(), range.getEndExclusive()),
                              ignored -> asList(override.getValue()));
    }

    private static void addIfNotNull(List<Oncall> oncalls,
                                     boolean nextOnCall,
                                     ValuedRange<ZonedDateTime, List<String>> interval) {
        if (interval != null) {
            interval.getValue().forEach(value -> {
                Range<ZonedDateTime> range          = interval.getRange();
                ZonedDateTime        startInclusive = range.getStartInclusive();
                ZonedDateTime        endExclusive   = range.getEndExclusive();
                oncalls.add(new Oncall(asList(value),
                                       !nextOnCall ? ZERO : startInclusive.toInstant(),
                                       nextOnCall ? ZERO : endExclusive.toInstant(),
                                       nextOnCall));
            });
        }
    }

    private static Entry<Instant, List<Oncall>> expectAt(Instant atTime, Oncall... expecteds) {
        return pair(atTime, asList(expecteds));
    }

    private static Timeline<ZonedDateTime, String> rotateForever(Duration rotationPeriod, String... recipients) {
        return rotate("ruleId", ZERO, INF, rotationPeriod, asList(), recipients);
    }

    private static Timeline<ZonedDateTime, String> rotateForever(Duration rotationPeriod,
                                                                 List<Range<Instant>> restrictions,
                                                                 String... recipients) {
        return rotate("ruleId", ZERO, INF, rotationPeriod, restrictions, recipients);
    }

    private static Timeline<ZonedDateTime, String> rotateForever(Instant startTime,
                                                                 Duration rotationPeriod,
                                                                 List<Range<Instant>> restrictions,
                                                                 String... recipients) {
        return rotate("ruleId", startTime, INF, rotationPeriod, restrictions, recipients);
    }

    private static Timeline<ZonedDateTime, String> rotateForever(String id,
                                                                 Instant startTime,
                                                                 Duration rotationPeriod,
                                                                 List<Range<Instant>> restrictions,
                                                                 String... recipients) {
        return rotate(id, startTime, INF, rotationPeriod, restrictions, recipients);
    }

    private static Timeline<ZonedDateTime, String> rotateUntil(Instant endTime,
                                                               Duration rotationPeriod,
                                                               String... recipients) {
        return rotate("ruleId", ZERO, endTime, rotationPeriod, asList(), recipients);
    }

    private static Timeline<ZonedDateTime, String> rotateUntil(Instant endTime,
                                                               Duration rotationPeriod,
                                                               List<Range<Instant>> restrictions,
                                                               String... recipients) {
        return rotate("ruleId", ZERO, endTime, rotationPeriod, restrictions, recipients);
    }

    private static Timeline<ZonedDateTime, String> rotate(String rotationId,
                                                          Instant startTime,
                                                          Instant endTime,
                                                          Duration rotationPeriod,
                                                          List<Range<Instant>> restrictions,
                                                          String... recipients) {
        return new ZonedRotationTimeline<>(dateRange(startTime, endTime),
                                           Iteration.of(min((int) rotationPeriod.toMinutes()),
                                                        restrictions.isEmpty()
                                                        ? asList(restrictTo(ZERO,
                                                                            min((int) rotationPeriod.toMinutes())))
                                                        : restrictions).toIterations(),
                                           asList(recipients));
    }


    private static ValuedRange<Instant, String> overrideBetween(Instant startTime,
                                                                Instant endTime,
                                                                List<String> rotationIds,
                                                                String recipient) {
        return overrideBetween(startTime, endTime, rotationIds, recipient, ZERO);
    }

    private static ValuedRange<Instant, String> overrideBetween(Instant startTime, Instant endTime, String recipient) {
        return overrideBetween(startTime, endTime, asList(), recipient, ZERO);
    }

    private static ValuedRange<Instant, String> overrideBetween(Instant startTime,
                                                                Instant endTime,
                                                                String recipient,
                                                                Instant insertedAt) {
        return overrideBetween(startTime, endTime, asList(), recipient, insertedAt);
    }

    private static ValuedRange<Instant, String> overrideBetween(Instant startTime,
                                                                Instant endTime,
                                                                List<String> rotationIds,
                                                                String recipient,
                                                                Instant insertedAt) {
        return ValuedRange.of(Range.of(startTime, endTime), recipient);
    }

    private static Range<Instant> restrictTo(Instant startTime, Instant endTime) {
        if (startTime.isAfter(endTime))
            fail("Reversed time ranges not supported yet!");
        if (startTime.equals(endTime))
            fail("Full time ranges not supported yet!");
        return Range.of(startTime, endTime);
    }

    private static Range<Instant> restrictTo(DAY day, Instant startTime, Instant endTime) {
        return restrictTo(day, startTime, day, endTime);
    }

    private static Range<Instant> restrictTo(DAY startDay, Instant startTime, DAY endDay, Instant endTime) {
        fail("Weekday-based restrictions not supported yet!");
        return Range.of(startTime, endTime);
    }

    private static Oncall onCallUntil(Instant endTime, String... names) {
        return new Oncall(asList(names), ZERO, endTime, false);
    }

    private static Oncall nextOnCallFrom(Instant startTime, String... names) {
        return new Oncall(asList(names), startTime, ZERO, true);
    }

    private static Instant dayHour(int day, double hour) {
        return hour((day * 24) + hour);
    }

    private static Instant hour(double hour) {
        return min((int) (hour * 60));
    }

    private static Instant min(int minute) {
        return instantPlusOffset(TimeUnit.MINUTES.toMillis(minute));
    }

    private static Instant instantPlusOffset(long millis) {
        return instant(MONDAY_OFFSET + millis);
    }

    private static Instant instantMinusOffset(long millis) {
        return instant((millis == 0L) ? 0L : (millis - MONDAY_OFFSET));
    }

    private static Instant instant(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli);
    }

    private static Range<ZonedDateTime> dateRange(Instant startTime, Instant endTime) {
        return Range.of(instantToZonedDateTime(startTime), instantToZonedDateTime(endTime));
    }

    private static ZonedDateTime instantToZonedDateTime(Instant instant) {
        return (instant == INF)
               ? ZonedDateTime.of(3000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
               : ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static String format(Collection<Entry<Instant, List<Oncall>>> items) {
        return joinLines(items, TimelineTest::format);
    }

    private static String format(Entry<Instant, List<Oncall>> onCalls) {
        return String.format("at %s:\n%s",
                             format(onCalls.getKey()),
                             joinLines(onCalls.getValue(), TimelineTest::format));
    }

    private static String format(Oncall onCall) {
        String names = String.join(", ", onCall.names);
        if (names.isEmpty()) {
            names = "<absent>";
        }
        return onCall.next
               ? String.format("     next on-call: %s  - from  %s", names, format(onCall.startTime))
               : String.format("  current on-call: %s  - until %s", names, format(onCall.endTime));
    }

    private static String format(Instant instant) {
        long           millis     = instant.toEpochMilli();
        OffsetDateTime dateTime   = utc(instantMinusOffset(millis));
        int            day        = dateTime.getDayOfYear() - 1;
        String         hourMinute = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        return (day == 0) ? hourMinute : String.format("d%d:%s", day, hourMinute);
    }

    private static OffsetDateTime utc(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }

    private static <T> String joinLines(Collection<? extends T> items, Function<? super T, String> toString) {
        return String.join("\n", map(items, toString));
    }

    private static final class Oncall {
        List<String> names;
        boolean      next;
        Instant      startTime;
        Instant      endTime;

        Oncall(List<String> names, Instant startTime, Instant endTime, boolean next) {
            this.names = names;
            this.startTime = startTime;
            this.endTime = endTime;
            this.next = next;
        }
    }

    public enum DAY {
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY
    }
}
