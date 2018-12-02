package com.github.mmdemirbas.oncalls;

import java.util.List;

import static java.util.Collections.emptyList;

final class Utils {
    static <V> List<V> orEmpty(List<V> list) {
        return (list == null) ? emptyList() : list;
    }
}
