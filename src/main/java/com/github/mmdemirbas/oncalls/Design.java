package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-16 18:31
 */
public final class Design {

    // todo: implement Rotation.toFixedTimeline() for a Range<ZonedDateTime>
    // todo: implement applyForwarding()
    // todo: implement applyOverrides()

    @Value
    public static final class Schedule<V> {
        Collection<Rotation<V>>   rotations;
        Patches<ZonedDateTime, V> globalPatches;
    }

    @Value
    public static final class Rotation<V> {
        Recurrence                recurrence;
        List<V>                   recurringRecipients;
        Patches<Instant, V>       recurringPatches;
        Patches<ZonedDateTime, V> fixedPatches;
    }

    @Value
    public static final class Recurrence {
        Range<ZonedDateTime> range;
        Duration             period;
        Ranges<Instant>      subRanges;
    }

    @Value
    public static final class Patches<Time extends Comparable<? super Time>, V> {
        Forwardings<Time, V> forwardings;
        Events<Time, V>      overrides;
    }

    @Value
    public static final class Forwardings<Time extends Comparable<? super Time>, V> {
        Set<Forwarding<Time, V>> forwardings;
    }

    @Value
    public static final class Events<Time extends Comparable<? super Time>, V> {
        NavigableMap<Time, List<Event<Time, V>>> index;
    }

    @Value
    public static final class Forwarding<Time extends Comparable<? super Time>, V> {
        Range<Time> when;
        V            forwarder;
        V            forwardedTo;
    }

    @Value
    public static final class Event<Time extends Comparable<? super Time>, V> {
        Range<Time> when;
        V           what;
    }
}
