package com.github.mmdemirbas.oncalls;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

final class Utils {
    static <V> List<V> orEmpty(List<V> list) {
        return (list == null) ? emptyList() : list;
    }

    static <V> List<V> unmodifiableCopyOf(List<? extends V> input) {
        // todo: bu ve bunun gibi yerlerde list olarak kullanılacak şey null ise empty list kullanılabilir NPE atmak yerine..
        return unmodifiableList(new ArrayList<>(Objects.requireNonNull(input, "input")));
    }
}
