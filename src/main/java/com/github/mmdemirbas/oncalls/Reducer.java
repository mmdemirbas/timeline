package com.github.mmdemirbas.oncalls;

import java.util.List;

@FunctionalInterface
public interface Reducer<V, A> {
    List<V> merge(List<V> acc, List<A> current);
}
