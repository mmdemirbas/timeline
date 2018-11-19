package com.github.mmdemirbas.oncalls;

import lombok.Value;

import java.util.function.Function;

/**
 * Represents an association between an arbitrary value and a {@link Range} object.
 * <p>
 * This class is immutable if the generic types {@link Point} and {@link V} is immutable.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-18 09:54
 */
@Value
public final class Event<V, Point extends Comparable<? super Point>> {
    V            value;
    Range<Point> range;

    public Event<V, Point> mapRange(Function<? super Range<Point>, ? extends Range<Point>> transform) {
        return new Event<>(value, transform.apply(range));
    }
}
