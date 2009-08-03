package org.navalplanner.web.common.converters;

/**
 * Converts from an object to an string representation, and converts the object
 * back <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IConverter<T> {

    Class<T> getType();

    String asString(T entity);

    T asObject(String stringRepresentation);

    String asStringUngeneric(Object entity);

}
