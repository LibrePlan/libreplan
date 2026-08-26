/*
 * This file is part of LibrePlan
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
package org.libreplan.web.planner.tabs;

import org.apache.commons.lang3.Validate;
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.sys.Attributes;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CreatedOnDemandTab implements ITab {

    private final String name;

    private Component parent;

    private final IComponentCreator componentCreator;

    private Component component;

    private final String cssClass;

    public interface IComponentCreator {
        Component create(Component parent);
    }

    public CreatedOnDemandTab(String name, IComponentCreator componentCreator) {
        this(name, null, componentCreator);
    }

    public CreatedOnDemandTab(String name, String cssClass, IComponentCreator componentCreator) {
        Validate.notNull(name);
        Validate.notNull(componentCreator);
        this.componentCreator = componentCreator;
        this.name = name;
        this.cssClass = cssClass;
    }

    @Override
    public void addToParent(Component parent) {
        this.parent = parent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void hide() {
        if (component != null) {
            component.detach();
        }
    }

    @Override
    public void show() {
        beforeShowAction();
        if (component == null) {
            component = componentCreator.create(parent);

            // Native (<n:...>) content anywhere under this tab (e.g. ganttzk's TimeTracker
            // header, chart legend swatches) renders as a deferred "#stub" placeholder needing a
            // separate client-side mount round trip whenever it's (re)sent via a partial AU
            // update - which is exactly what happens every time this tab is hidden (detach())
            // and reshown (setParent() below) after the first time. That mount round trip doesn't
            // survive the cycle, leaving the reshown tab blank with a client-side "Unknown stub"
            // error. Attributes.STUB_NATIVE disables the deferred stub for this component and,
            // per ZK's rendering walk, cascades to every native element nested under it, so it's
            // set once here rather than chasing down each individual native element.
            component.setAttribute(Attributes.STUB_NATIVE, Boolean.FALSE);
        }
        component.setParent(parent);
        afterShowAction();
    }

    protected final Desktop getDesktop() {
        if (component == null) {
            return null;
        }

        return component.getDesktop();
    }

    protected void beforeShowAction() {
    }

    protected void afterShowAction() {
    }

    @Override
    public String getCssClass() {
        return cssClass;
    }

}
