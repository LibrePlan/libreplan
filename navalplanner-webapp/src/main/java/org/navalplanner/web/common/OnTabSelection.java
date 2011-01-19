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
package org.navalplanner.web.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Tabbox;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class OnTabSelection {

    public interface IOnSelectingTab {
        public void tabSelected();
    }

    private Map<Component, IOnSelectingTab> tabs = new HashMap<Component, IOnSelectingTab>();

    public static OnTabSelection createFor(Tabbox tabBox) {
        return new OnTabSelection(tabBox);
    }

    public OnTabSelection(Tabbox tabBox) {
        Validate.notNull(tabBox);
        tabBox.addEventListener(Events.ON_SELECT, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                SelectEvent e = (SelectEvent) event;
                for (Object each : e.getSelectedItems()) {
                    if (tabs.get(each) != null) {
                        tabs.get(each).tabSelected();
                    }
                }
            }
        });
    }

    public OnTabSelection onSelectingTab(Component tab,
            IOnSelectingTab onSelectingTab) {
        Validate.notNull(tab);
        Validate.notNull(onSelectingTab);
        tabs.put(tab, onSelectingTab);
        return this;
    }

}
