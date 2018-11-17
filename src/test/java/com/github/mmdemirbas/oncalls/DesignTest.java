package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-16 18:31
 */
public final class DesignTest {

    // todo: implement Rotation.toFixedTimeline() for a FixedRange
    // todo: implement applyForwarding()
    // todo: implement applyOverrides()

    @Value
    public static final class Schedule<V> {
        Collection<Rotation<V>> rotations;
        FixedPatch<V>           globalPatches;
    }

    @Value
    public static final class Rotation<V> {
        Recurrence        recurrence;
        List<V>           recurringRecipients;
        RecurringPatch<V> recurringPatches;
        FixedPatch<V>     fixedPatches;
    }

    @Value
    public static final class Recurrence {
        FixedRange      range;
        Duration        period;
        RecurringRanges subRanges;
    }

    @Value
    public static final class FixedPatch<V> {
        FixedForwardings<V> forwardings;
        FixedEvents<V>      overrides;
    }

    @Value
    public static final class RecurringPatch<V> {
        RecurringForwardings<V> forwardings;
        RecurringEvents<V>      overrides;
    }

    @Value
    public static final class FixedForwardings<V> {
        Set<FixedForwarding<V>> forwardings;
    }

    @Value
    public static final class RecurringForwardings<V> {
        Set<RecurringForwarding<V>> forwardings;
    }

    @Value
    public static final class FixedForwarding<V> {
        FixedRanges   when;
        Forwarding<V> forwarding;
    }

    @Value
    public static final class RecurringForwarding<V> {
        RecurringRanges when;
        Forwarding<V>   forwarding;
    }

    @Value
    public static final class Forwarding<V> {
        V forwarder;
        V forwardedTo;
    }

    @Value
    public static final class FixedEvents<V> {
        NavigableMap<ZonedDateTime, List<FixedEvent<V>>> index;
    }

    @Value
    public static final class RecurringEvents<V> {
        NavigableMap<Instant, List<RecurringEvent<V>>> index;
    }

    @Value
    public static final class FixedEvent<V> {
        FixedRange when;
        V          what;
    }

    @Value
    public static final class RecurringEvent<V> {
        RecurringRange when;
        V              what;
    }

    @Value
    public static final class FixedRanges {
        NavigableSet<FixedRange> ranges;
    }

    @Value
    public static final class RecurringRanges {
        NavigableSet<RecurringRange> ranges;
    }

    @Value
    public static final class FixedRange {
        Range<ZonedDateTime> ranges;
    }

    @Value
    public static final class RecurringRange {
        Range<Instant> ranges;
    }
}
