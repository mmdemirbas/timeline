package com.github.mmdemirbas.oncalls;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-18 09:54
 */
@ToString
@EqualsAndHashCode
public final class Event<Point extends Comparable<? super Point>, V> {
    @Getter private final Range<Point> range;
    @Getter private final V            value;

    public Event(Range<Point> range, V value) {
        this.range = range;
        this.value = value;
    }
}
