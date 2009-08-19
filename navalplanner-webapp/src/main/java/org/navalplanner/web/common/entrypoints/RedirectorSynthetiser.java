package org.navalplanner.web.common.entrypoints;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.ClassUtils;

import static org.navalplanner.web.I18nHelper._;

/**
 * Creates implemnetations of controllers that sends http redirects to the
 * proper page <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class RedirectorSynthetiser implements BeanFactoryPostProcessor {
    private static final Log LOG = LogFactory
            .getLog(RedirectorSynthetiser.class);

    private final class SynthetizedImplementation implements InvocationHandler {
        private final ConfigurableListableBeanFactory beanFactory;

        private final Class<?> pageInterface;

        private URLHandler<?> urlHandler;

        private SynthetizedImplementation(
                ConfigurableListableBeanFactory beanFactory,
                Class<?> pageInterface) {
            this.beanFactory = beanFactory;
            this.pageInterface = pageInterface;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            URLHandler<?> redirector = getHandler();
            redirector.doTransition(method.getName(), args);
            return null;
        }

        private URLHandler<?> getHandler() {
            if (urlHandler != null)
                return urlHandler;
            URLHandlerRegistry registry = (URLHandlerRegistry) BeanFactoryUtils
                    .beanOfType(beanFactory, URLHandlerRegistry.class);
            urlHandler = registry.getRedirectorFor(pageInterface);
            return urlHandler;
        }
    }

    public void postProcessBeanFactory(
            ConfigurableListableBeanFactory beanFactory) throws BeansException {
        long elapsedTime = System.currentTimeMillis();
        for (Class<?> pageInterface : findInterfacesMarkedEntryPoints()) {
            beanFactory.registerSingleton(getBeanName(pageInterface),
                    createRedirectorImplementationFor(beanFactory,
                            pageInterface));
        }
        elapsedTime = System.currentTimeMillis() - elapsedTime;
        LOG.debug("Took " + elapsedTime
                + " ms to search for interfaces annotated with "
                + EntryPoints.class.getSimpleName());
    }

    private List<Class<?>> findInterfacesMarkedEntryPoints() {
        List<Class<?>> result = new ArrayList<Class<?>>();
        PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(
                resourceResolver);
        for (Resource resource : findResourcesCouldMatch(resourceResolver)) {
            addIfSuitable(result, metadataReaderFactory, resource);
        }
        return result;
    }

    private Resource[] findResourcesCouldMatch(
            PathMatchingResourcePatternResolver resourceResolver) {
        try {
            return resourceResolver
                    .getResources("classpath*:"
                            + ClassUtils
                                    .convertClassNameToResourcePath("org.navalplanner.web")
                            + "/" + "**/*.class");
        } catch (IOException e) {
            throw new RuntimeException(_("Could not load any resource"), e);
        }
    }

    private void addIfSuitable(List<Class<?>> accumulatedResult,
            CachingMetadataReaderFactory metadataReaderFactory,
            Resource resource) {
        try {
            if (resource.isReadable()) {
                MetadataReader metadataReader = metadataReaderFactory
                        .getMetadataReader(resource);
                AnnotationMetadata annotationMetadata = metadataReader
                        .getAnnotationMetadata();
                ClassMetadata classMetadata = metadataReader.getClassMetadata();
                if (classMetadata.isInterface()
                        && annotationMetadata.getAnnotationTypes().contains(
                                EntryPoints.class.getName())) {
                    Class<?> klass = Class
                            .forName(classMetadata.getClassName());
                    if (klass.isInterface()) {
                        accumulatedResult.add(klass);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("exception processing " + resource, e);
        }
    }

    private Object createRedirectorImplementationFor(
            final ConfigurableListableBeanFactory beanFactory,
            final Class<?> pageInterface) {

        return Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[] { pageInterface }, new SynthetizedImplementation(
                        beanFactory, pageInterface));
    }

    private static String getBeanName(Class<?> pageInterface) {
        EntryPoints annotation = pageInterface.getAnnotation(EntryPoints.class);
        return annotation.registerAs();
    }
}
