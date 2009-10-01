/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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
import org.zkoss.zul.Comboitem;

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

    public Comboitem getSelectedItem() {
        return combo.getSelectedItem();
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
