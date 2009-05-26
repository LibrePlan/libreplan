package org.navalplanner.web.common;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Registry of {@link Redirector} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class RedirectorRegistry implements IRedirectorRegistry {

    @Autowired
    private ExecutorRetriever executorRetriever;

    @Autowired
    private IConverterFactory converterFactory;

    private Map<Class<?>, Redirector> cached = new HashMap<Class<?>, Redirector>();;

    public <T> Redirector<T> getRedirectorFor(Class<T> klassWithLinkableMetadata) {
        if (cached.containsKey(klassWithLinkableMetadata))
            return cached.get(klassWithLinkableMetadata);
        Redirector<T> result = new Redirector<T>(converterFactory,
                executorRetriever, klassWithLinkableMetadata);
        cached.put(klassWithLinkableMetadata, result);
        return result;
    }
}
