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

package org.zkoss.ganttz.adapters;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.ganttz.TabsRegistry;
import org.zkoss.ganttz.adapters.State.IValueChangeListener;
import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.ganttz.util.IMenuItemsRegister;

public class TabsConfiguration {

    public static ChangeableTab configure(ITab tab) {
        return new ChangeableTab(tab);
    }

    public static class ChangeableTab {
        private final ITab tab;

        private State<Void> reloadNameState;

        private State<Boolean> visibility;

        ChangeableTab(ITab tab) {
            this.tab = tab;
        }

        public ChangeableTab reloadNameOn(State<Void> reloadName) {
            this.reloadNameState = reloadName;
            return this;
        }

        public ChangeableTab visibleOn(State<Boolean> visibility) {
            this.visibility = visibility;
            return this;
        }
    }

    public static TabsConfiguration create() {
        return new TabsConfiguration();
    }

    private List<ChangeableTab> tabs = new ArrayList<ChangeableTab>();

    private TabsConfiguration() {
    }

    public TabsConfiguration add(ITab tab) {
        tabs.add(new ChangeableTab(tab));
        return this;
    }

    public TabsConfiguration add(ChangeableTab changeableTab) {
        tabs.add(changeableTab);
        return this;
    }

    public void applyTo(TabsRegistry tabsRegistry, IMenuItemsRegister menu) {
        for (ChangeableTab tab : tabs) {
            tabsRegistry.add(tab.tab);
        }
        tabsRegistry.registerAtMenu(menu);
        for (ChangeableTab tab : tabs) {
            reloadNameIfNeeded(tabsRegistry, tab);
            changeVisibilityWhenNeeded(tabsRegistry, tab);
        }
    }

    private void reloadNameIfNeeded(final TabsRegistry tabsRegistry,
            final ChangeableTab tab) {
        if (tab.reloadNameState == null) {
            return;
        }
        tab.reloadNameState.addListener(new IValueChangeListener<Void>() {

            @Override
            public void hasChanged(State<Void> condition) {
                tabsRegistry.loadNewName(tab.tab);
            }
        });
    }

    private void changeVisibilityWhenNeeded(final TabsRegistry tabsRegistry,
            final ChangeableTab tab) {
        if (tab.visibility == null) {
            return;
        }
        tabsRegistry.toggleVisibilityTo(tab.tab, tab.visibility.getValue());
        tab.visibility.addListener(new IValueChangeListener<Boolean>() {

            @Override
            public void hasChanged(State<Boolean> state) {
                tabsRegistry.toggleVisibilityTo(tab.tab, state.getValue());
            }
        });
    }

}
