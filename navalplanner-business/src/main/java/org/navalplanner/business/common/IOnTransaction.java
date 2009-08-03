package org.navalplanner.business.common;

/**
 * Represents some work done inside a transaction <br />
 * @author oscar @param <T>
 */
public interface IOnTransaction<T> {
    public T execute();
}