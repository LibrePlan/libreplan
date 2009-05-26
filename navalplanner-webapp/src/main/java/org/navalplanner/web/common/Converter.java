package org.navalplanner.web.common;

/**
 * Converts from an object to an string representation, and converts the object
 * back <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface Converter<T> {

    Class<T> getType();

    String asString(T entity);

    T asObject(String stringRepresentation);

    String asStringUngeneric(Object entity);

}
