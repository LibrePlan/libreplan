package org.navalplanner.web.bootstrap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.navalplanner.business.IDataBootstrap;
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
        String[] beanNames = BeanFactoryUtils
                .beanNamesForTypeIncludingAncestors(webApplicationContext,
                        IDataBootstrap.class);
        for (String name : beanNames) {
            IDataBootstrap bootstrap = (IDataBootstrap) webApplicationContext
                    .getBean(name);
            bootstrap.loadRequiredData();
        }
    }

}
