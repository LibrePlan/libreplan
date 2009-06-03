package org.navalplanner.web.common.entrypoints;

import java.util.HashMap;
import java.util.Map;

import org.navalplanner.web.common.converters.IConverterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Registry of {@link URLHandler} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class URLHandlerRegistry implements IURLHandlerRegistry {

    @Autowired
    private ExecutorRetriever executorRetriever;

    @Autowired
    private IConverterFactory converterFactory;

    private Map<Class<?>, URLHandler<?>> cached = new HashMap<Class<?>, URLHandler<?>>();;

    @SuppressWarnings("unchecked")
    public <T> URLHandler<T> getRedirectorFor(Class<T> klassWithLinkableMetadata) {
        if (cached.containsKey(klassWithLinkableMetadata))
            return (URLHandler<T>) cached.get(klassWithLinkableMetadata);
        URLHandler<T> result = new URLHandler<T>(converterFactory,
                executorRetriever, klassWithLinkableMetadata);
        cached.put(klassWithLinkableMetadata, result);
        return result;
    }
}
