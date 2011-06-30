/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.common.entrypoints;

import static org.navalplanner.web.I18nHelper._;

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

/**
 * Creates implemnetations of controllers that sends http redirects to the
 * proper page <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class RedirectorSynthetiser implements BeanFactoryPostProcessor {
    private static final Log LOG = LogFactory
            .getLog(RedirectorSynthetiser.class);

    private static final class SynthetizedImplementation implements
            InvocationHandler {

        private final ConfigurableListableBeanFactory beanFactory;

        private final Class<?> pageInterface;

        private EntryPointsHandler<?> urlHandler;

        private SynthetizedImplementation(
                ConfigurableListableBeanFactory beanFactory,
                Class<?> pageInterface) {
            this.beanFactory = beanFactory;
            this.pageInterface = pageInterface;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            EntryPointsHandler<?> redirector = getHandler();
            redirector.doTransition(method.getName(), args);
            return null;
        }

        private EntryPointsHandler<?> getHandler() {
            if (urlHandler != null) {
                return urlHandler;
            }
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
