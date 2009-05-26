package org.navalplanner.web.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Default implementation for {@link IConverterFactory} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ConverterFactory implements IConverterFactory {

    private Map<Class<?>, Converter<?>> convertersByType = new HashMap<Class<?>, Converter<?>>();

    @Autowired
    public ConverterFactory(List<Converter<?>> converters) {
        for (Converter<?> converter : converters) {
            convertersByType.put(converter.getType(), converter);
        }
    }

    @Override
    public <T> Converter<? super T> getConverterFor(Class<T> klass) {
        if (convertersByType.containsKey(klass))
            return (Converter<? super T>) convertersByType.get(klass);
        for (Class<?> registeredKlass : convertersByType.keySet()) {
            if (registeredKlass.isAssignableFrom(klass)) {
                Converter<?> result = convertersByType.get(registeredKlass);
                convertersByType.put(klass, result);
                return (Converter<? super T>) result;
            }
        }
        throw new RuntimeException("not found converter for " + klass);
    }
}
