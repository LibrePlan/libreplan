package org.navalplanner.business.util;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Pair<T, S> {

    public static <T, S> Pair<T, S> create(T first, S second) {
        return new Pair<T, S>(first, second);
    }

    private final T first;

    private final S second;

    private Pair(T first, S second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair<?, ?>) {
            Pair<?, ?> other = (Pair<?, ?>) obj;
            return new EqualsBuilder().append(getFirst(), other.getFirst())
                    .append(getSecond(), other.getSecond()).isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getFirst()).append(getSecond())
                .toHashCode();
    }

}