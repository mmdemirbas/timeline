package com.github.mmdemirbas.oncalls;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;

/**
 * @author Muhammed Demirbaş
 * @since 2018-11-18 10:33
 */
public final class TimelineTest {
    @Test
    public void name() {
        Timeline<Integer, String> timeline = new Timeline<>(asList(event(Range.of(1, 3), "A"),
                                                                   event(Range.of(1, 3), "A")));


    }

    private static Event<Integer, String> event(Range<Integer> range, String value) {
        return new Event<>(range, value);
    }
}