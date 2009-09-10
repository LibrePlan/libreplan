package org.navalplanner.web.common.components;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.web.common.components.finders.IFinder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zul.Combobox;

/**
 * Autocomplete component
 *
 * Extends a {@link Combobox} component providing extra functionality for
 * filling the list of elements with entries, thanks to a class implementing
 * {@link IFinder}
 *
 * FIXME: Typing <shift> in a combobox causes the text to be automatically
 * autocompleted, even when autocomplete is set to false. This implies that's
 * not possible to type major letters inside combobox.
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@SuppressWarnings("serial")
public class Autocomplete extends HtmlMacroComponent {

    private Combobox combo;

    private IFinder finder;

    public void afterCompose() {
        super.afterCompose();
        combo = (Combobox) getFellowIfAny("combo");
        combo.setModel(finder.getModel());
        combo.setItemRenderer(finder.getItemRenderer());
        combo.setParent(this);
    }

    public String getFinder() {
        return finder.getClass().toString();
    }

    public void setFinder(String classname) {
        finder = (IFinder) getBean(StringUtils.uncapitalize(classname));
    }

    private Object getBean(String classname) {
        HttpServletRequest servletRequest = (HttpServletRequest) Executions
                .getCurrent().getNativeRequest();
        ServletContext servletContext = servletRequest.getSession()
                .getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils
                .getWebApplicationContext(servletContext);
        return webApplicationContext.getBean(classname);
    }
}
