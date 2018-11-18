package com.github.mmdemirbas.oncalls;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents an association between a {@link Range} and an arbitrary value.
 * <p>
 * This class is immutable if the generic types {@link Point} and {@link Value} is immutable.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-18 09:54
 */
@ToString
@EqualsAndHashCode
public final class Event<Point extends Comparable<? super Point>, Value> {
    @Getter private final Range<Point> range;
    @Getter private final Value        value;

    public static <Point extends Comparable<? super Point>, Value> Event<Point, Value> of(Range<Point> range,
                                                                                          Value value) {
        return new Event<>(range, value);
    }

    private Event(Range<Point> range, Value value) {
        this.range = range;
        this.value = value;
    }
}
