package com.github.mmdemirbas.oncalls;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

final class Utils {
    static <V> List<V> orEmpty(List<V> list) {
        return (list == null) ? emptyList() : list;
    }

    static <V> List<V> unmodifiableCopyOf(List<? extends V> input) {
        return ((input == null) || input.isEmpty()) ? emptyList() : unmodifiableList(new ArrayList<>(input));
    }
}
