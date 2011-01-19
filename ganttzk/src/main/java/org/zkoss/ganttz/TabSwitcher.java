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

package org.zkoss.ganttz;

import org.zkoss.ganttz.adapters.TabsConfiguration;
import org.zkoss.ganttz.util.IMenuItemsRegister;
import org.zkoss.ganttz.util.OnZKDesktopRegistry;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;

public class TabSwitcher extends HtmlMacroComponent {

    private TabsConfiguration configuration;

    private TabsRegistry tabsRegistry;
    private Component container = null;

    public TabSwitcher() {
    }

    public TabSwitcher(TabsConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setConfiguration(TabsConfiguration configuration) {
        this.configuration = configuration;
        addTabsFromComfiguration(configuration);
    }

    private void addTabsFromComfiguration(TabsConfiguration configuration) {
        container.getChildren().clear();
        IMenuItemsRegister menu = getMenuItemsRegisterLocator().retrieve();
        configuration.applyTo(tabsRegistry, menu);
    }

    @Override
    public void afterCompose() {
        super.afterCompose();
        container = getFellow("container");
        tabsRegistry = new TabsRegistry(container);
        if (configuration != null) {
            addTabsFromComfiguration(configuration);
        }
    }

    private OnZKDesktopRegistry<IMenuItemsRegister> getMenuItemsRegisterLocator() {
        return OnZKDesktopRegistry.getLocatorFor(IMenuItemsRegister.class);
    }

    public TabsRegistry getTabsRegistry() {
        return tabsRegistry;
    }

}
