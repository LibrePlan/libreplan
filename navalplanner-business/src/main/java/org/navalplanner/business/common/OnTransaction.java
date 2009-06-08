package org.navalplanner.business.common;

/**
 * Represents some work done inside a transaction <br />
 * @author oscar @param <T>
 */
public interface OnTransaction<T> {
    public T execute();
}