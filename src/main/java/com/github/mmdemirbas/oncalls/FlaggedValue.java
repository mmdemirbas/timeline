package com.github.mmdemirbas.oncalls;

import lombok.Value;

/**
 * Represents an association between an arbitrary value and a {@code boolean} flag.
 * <p>
 * This class is immutable if the generic type {@link V} is immutable.
 *
 * @author Muhammed Demirbaş
 * @since 2018-11-19 14:47
 */
@Value
public final class FlaggedValue<V> {
    V       value;
    boolean flag;
}
