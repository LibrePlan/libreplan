package org.navalplanner.web.common.converters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.navalplanner.web.I18nHelper._;

/**
 * Default implementation for {@link IConverterFactory} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ConverterFactory implements IConverterFactory {

    private Map<Class<?>, IConverter<?>> convertersByType = new HashMap<Class<?>, IConverter<?>>();

    @Autowired
    public ConverterFactory(List<IConverter<?>> converters) {
        for (IConverter<?> converter : converters) {
            convertersByType.put(converter.getType(), converter);
        }
    }

    @Override
    public <T> IConverter<? super T> getConverterFor(Class<T> klass) {
        if (convertersByType.containsKey(klass))
            return (IConverter<? super T>) convertersByType.get(klass);
        for (Class<?> registeredKlass : convertersByType.keySet()) {
            if (registeredKlass.isAssignableFrom(klass)) {
                IConverter<?> result = convertersByType.get(registeredKlass);
                convertersByType.put(klass, result);
                return (IConverter<? super T>) result;
            }
        }
        throw new RuntimeException(_("Not found converter for {0}",  klass));
    }
}
