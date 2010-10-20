/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.navalplanner.business.BootstrapOrder;
import org.navalplanner.business.IDataBootstrap;
import org.navalplanner.business.hibernate.notification.PredefinedDatabaseSnapshots;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class BootstrapListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        WebApplicationContext webApplicationContext = WebApplicationContextUtils
                .getWebApplicationContext(servletContextEvent
                        .getServletContext());
        doBootstrap(webApplicationContext);
        // some snapshots could depend on the bootstrap being done, so they are
        // launched after
        launchSnapshots(webApplicationContext);
    }

    private void doBootstrap(WebApplicationContext webApplicationContext) {
        List<IDataBootstrap> bootstrapsFound = findBootstraps(webApplicationContext);
        Collections.sort(bootstrapsFound, byBootstrapOrder());
        for (IDataBootstrap each : bootstrapsFound) {
            each.loadRequiredData();
        }
    }

    private List<IDataBootstrap> findBootstraps(
            WebApplicationContext webApplicationContext) {
        String[] beanNames = BeanFactoryUtils
                .beanNamesForTypeIncludingAncestors(webApplicationContext,
                        IDataBootstrap.class);
        List<IDataBootstrap> result = new ArrayList<IDataBootstrap>();
        for (String name : beanNames) {
            IDataBootstrap bootstrap = (IDataBootstrap) webApplicationContext
                    .getBean(name);
            result.add(bootstrap);
        }
        return result;
    }

    private Comparator<IDataBootstrap> byBootstrapOrder() {
        return new Comparator<IDataBootstrap>() {

            @Override
            public int compare(IDataBootstrap o1, IDataBootstrap o2) {
                return getOrderingValue(o1) - getOrderingValue(o2);
            }

            private int getOrderingValue(IDataBootstrap dataBootstrap) {
                BootstrapOrder annotation = searchAnnotationFor(dataBootstrap);
                return annotation == null ? 0 : annotation.value();
            }

            private BootstrapOrder searchAnnotationFor(
                    IDataBootstrap dataBootstrap) {
                Class<? extends IDataBootstrap> klass = dataBootstrap
                        .getClass();
                BootstrapOrder result = klass
                        .getAnnotation(BootstrapOrder.class);
                if (result == null && dataBootstrap instanceof Advised) {
                    return searchInProxiedClass((Advised) dataBootstrap);
                }
                return result;
            }

            private BootstrapOrder searchInProxiedClass(Advised advised) {
                Class<?> proxiedClass = advised.getTargetSource()
                        .getTargetClass();
                return proxiedClass.getAnnotation(BootstrapOrder.class);
            }
        };
    }

    private void launchSnapshots(WebApplicationContext webApplicationContext) {
        PredefinedDatabaseSnapshots snapshots = getPredefinedDatabaseSnapshots(webApplicationContext);
        snapshots.registerSnapshots();
    }

    private PredefinedDatabaseSnapshots getPredefinedDatabaseSnapshots(
            WebApplicationContext webApplicationContext) {
        return (PredefinedDatabaseSnapshots) webApplicationContext
                .getBean(lowercaseFirst(PredefinedDatabaseSnapshots.class
                        .getSimpleName()));
    }

    private String lowercaseFirst(String simpleName) {
        return simpleName.substring(0, 1).toLowerCase()
                + simpleName.substring(1);
    }

}
