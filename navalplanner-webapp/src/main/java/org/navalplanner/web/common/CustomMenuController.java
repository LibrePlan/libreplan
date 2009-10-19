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

package org.navalplanner.web.common;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zkoss.ganttz.util.IMenuItemsRegister;
import org.zkoss.ganttz.util.OnZKDesktopRegistry;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;

/**
 * Controller for customMenu <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class CustomMenuController extends Div implements IMenuItemsRegister {

    private List<CustomMenuItem> firstLevel;

    public static class CustomMenuItem {

        private final String name;
        private final String encodedURL;
        private final String unencodedURL;
        private final List<CustomMenuItem> children;
        private boolean activeParent;
        private boolean disabled;

        public String getName() {
            return name;
        }

        public String getUrl() {
            return encodedURL;
        }

        public List<CustomMenuItem> getChildren() {
            return children;
        }

        public CustomMenuItem(String name, String url) {
            this(name, url, new ArrayList<CustomMenuItem>());
        }

        public CustomMenuItem(String name, String url, boolean disabled) {
            this(name, url, new ArrayList<CustomMenuItem>());
            this.disabled = disabled;
        }

        public CustomMenuItem(String name, String url,
                List<CustomMenuItem> children) {
            this.name = name;
            this.encodedURL = Executions.getCurrent().encodeURL(url);
            this.unencodedURL = url;
            this.children = children;
            this.disabled = false;
        }

        public void appendChildren(CustomMenuItem newChildren) {
            this.children.add(newChildren);
        }

        public boolean isActiveParent() {
            return activeParent;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public boolean contains(String requestPath) {
            for (CustomMenuItem item : thisAndChildren()) {
                if (requestContains(requestPath, item.unencodedURL))
                    return true;
            }
            return false;
        }

        private List<CustomMenuItem> thisAndChildren() {
            List<CustomMenuItem> items = new ArrayList<CustomMenuItem>();
            items.add(this);
            items.addAll(children);
            return items;
        }

        private static boolean requestContains(String requestPath, String url) {
            return requestPath.startsWith(url);
        }

        public void setActive(boolean activeParent) {
            this.activeParent = activeParent;
        }

    }

    public CustomMenuController() {
        this.firstLevel = new ArrayList<CustomMenuItem>();
        initializeMenu();
        activateCurrentOne();
        getLocator().store(this);
    }

    private void activateCurrentOne() {
        String requestPath = Executions.getCurrent().getDesktop()
                .getRequestPath();
        for (CustomMenuItem ci : this.firstLevel) {
            if (ci.contains(requestPath)) {
                ci.setActive(true);
                break;
            }
        }
    }

    private OnZKDesktopRegistry<IMenuItemsRegister> getLocator() {
        return OnZKDesktopRegistry.getLocatorFor(IMenuItemsRegister.class);
    }

    private CustomMenuController topItem(String name, String url,
            CustomMenuItem... items) {
        return topItem(name, url, false, items);
    }

    private CustomMenuController topItem(String name, String url,
            boolean disabled, CustomMenuItem... items) {
        CustomMenuItem parent = new CustomMenuItem(name, url, disabled);
        this.firstLevel.add(parent);
        for (CustomMenuItem child : items) {
            parent.appendChildren(child);
        }
        return this;
    }

    private CustomMenuItem subItem(String name, String url, boolean disabled) {
        return new CustomMenuItem(name, url, disabled);
    }

    private CustomMenuItem subItem(String name, String url) {
        return new CustomMenuItem(name, url);
    }

    public void initializeMenu() {
        topItem(_("Planification"), "/planner/index.zul");

        topItem(_("Resources"), "/resources/worker/worker.zul",
                subItem(_("Workers List"),
                    "/resources/worker/worker.zul#list"),
                subItem(_("Machines List"),
                    "/resources/machine/machines.zul#list"));

        topItem(_("Work reports"), "/workreports/workReportTypes.zul", subItem(
                _("Work report types"), "/workreports/workReportTypes.zul"),
                subItem(_("Work report list"),
                        "/workreports/workReport.zul#list"));

        topItem(_("Administration"),
                "/advance/advanceTypes.zul",
                subItem(_("Manage advances types"),
                        "/advance/advanceTypes.zul"),
                subItem(_("Manage criterions"),
                        "/resources/criterions/criterions-V2.zul"),
                subItem(_("Calendars"), "/calendars/calendars.zul"),
                subItem(_("Label types"), "/labels/labelTypes.zul"));

        topItem(_("Quality management"), "/", true);
    }

    private Hbox getRegisteredItemsInsertionPoint() {
        return (Hbox) getFellow("registeredItemsInsertionPoint");
    }

    public List<CustomMenuItem> getCustomMenuItems() {
        return this.firstLevel;
    }

    public List<CustomMenuItem> getCustomMenuSecondaryItems() {
        for (CustomMenuItem ci : this.firstLevel) {
            if (ci.isActiveParent())
                return ci.getChildren();
        }
        return Collections.<CustomMenuItem> emptyList();
    }

    private Button currentOne = null;

    @Override
    public Object addMenuItem(String name,
            org.zkoss.zk.ui.event.EventListener eventListener) {
        Hbox insertionPoint = getRegisteredItemsInsertionPoint();
        Button button = new Button();
        button.setLabel(_(name));
        setDeselectedClass(button);
        button.addEventListener(Events.ON_CLICK, doNotCallTwice(button,
                eventListener));
        insertionPoint.appendChild(button);
        insertionPoint.appendChild(separator());
        return button;
    }

    @Override
    public void activateMenuItem(Object key) {
        switchCurrentButtonTo((Button) key);
    }

    @Override
    public void renameMenuItem(Object key, String name) {
        Button button = (Button) key;
        button.setLabel(name);
    }

    private void setSelectClass(final Button button) {
        button.setSclass("sub_menu_active");
    }

    private void setDeselectedClass(Button button) {
        button.setSclass("sub_menu");
    }

    private EventListener doNotCallTwice(final Button button,
            final org.zkoss.zk.ui.event.EventListener originalListener) {
        return new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                if (currentOne == button) {
                    return;
                }
                switchCurrentButtonTo(button);
                originalListener.onEvent(event);
            }
        };
    }

    private Component separator() {
        Div div = new Div();
        div.setSclass("vertical_separator");
        return div;
    }

    public String getContextPath() {
        return Executions.getCurrent().getContextPath();
    }

    private void switchCurrentButtonTo(final Button button) {
        if (currentOne == button) {
            return;
        }
        if (currentOne != null) {
            currentOne.setSclass("sub_menu");
            setDeselectedClass(currentOne);
        }
        setSelectClass(button);
        currentOne = button;
    }

}
