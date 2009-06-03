package org.navalplanner.web.common.converters;


/**
 * Retrieves a Converter given a type <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IConverterFactory {

    <T> Converter<? super T> getConverterFor(Class<T> klass);

}
