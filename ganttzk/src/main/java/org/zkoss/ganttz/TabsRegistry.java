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

import static org.zkoss.ganttz.i18n.I18nHelper._;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.ganttz.util.IMenuItemsRegister;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

public class TabsRegistry {

    private List<ITab> tabs = new ArrayList<ITab>();

    private final Component parent;

    private Map<ITab, Object> fromTabToMenuKey = new HashMap<ITab, Object>();

    private IMenuItemsRegister menu;

    public TabsRegistry(Component parent) {
        this.parent = parent;
    }

    public void add(ITab tab) {
        tab.addToParent(parent);
        tabs.add(tab);
    }

    public interface IBeforeShowAction {
        public void doAction();
    }

    private static final IBeforeShowAction DO_NOTHING = new IBeforeShowAction() {
        @Override
        public void doAction() {
        }
    };

    public void show(ITab tab) {
        show(tab, DO_NOTHING);
    }

    public void show(ITab tab, IBeforeShowAction beforeShowAction) {
        hideAllExcept(tab);
        beforeShowAction.doAction();
        tab.show();
        parent.invalidate();
        activateMenuIfRegistered(tab);
    }

    public void loadNewName(ITab tab) {
        if (fromTabToMenuKey.containsKey(tab)) {
            Object key = fromTabToMenuKey.get(tab);
            menu.renameMenuItem(key, tab.getName(), tab.getCssClass());
        }
    }

    public void toggleVisibilityTo(ITab tab, boolean visible) {
        if (fromTabToMenuKey.containsKey(tab)) {
            menu.toggleVisibilityTo(fromTabToMenuKey.get(tab), visible);
        }
    }

    private void activateMenuIfRegistered(ITab tab) {
        if (fromTabToMenuKey.containsKey(tab)) {
            menu.activateMenuItem(fromTabToMenuKey.get(tab));
        }
    }

    private void hideAllExcept(ITab tab) {
        for (ITab t : tabs) {
            if (t.equals(tab)) {
                continue;
            }
            t.hide();
        }
    }

    public void showFirst() {
        if (!tabs.isEmpty()) {
            show(tabs.get(0));
        }
    }

    public void registerAtMenu(IMenuItemsRegister menu) {
        this.menu = menu;
        for (final ITab t : tabs) {
            Object key = menu.addMenuItem(t.getName(), t.getCssClass(),
                    new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    if (_("Limiting resources").equals(t.getName())) {
                        Executions.sendRedirect("/planner/index.zul;limiting_resources");
                    } else {
                        show(t);
                    }
                }
            });
            fromTabToMenuKey.put(t, key);
        }

    }

}
