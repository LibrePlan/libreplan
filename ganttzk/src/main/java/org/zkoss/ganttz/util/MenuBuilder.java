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

package org.zkoss.ganttz.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlNativeComponent;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Menuseparator;
import org.zkoss.zul.impl.api.XulElement;

public class MenuBuilder<T extends XulElement> {

    public static <T extends XulElement> MenuBuilder<T> on(Page page,
            Collection<T> elements) {
        return new MenuBuilder<T>(page, elements);
    }

    public static <T extends XulElement> MenuBuilder<T> on(Page page,
            T... elements) {
        return on(page, Arrays.asList(elements));
    }

    public static interface ItemAction<T> {

        void onEvent(T choosen, Event event);
    }

    private class Item {
        private final String name;
        private final String icon;

        private final ItemAction<T> action;

        Item(String name, String icon, ItemAction<T> action) {
            this.name = name;
            this.icon = icon;
            this.action = action;
        }

        Menuitem createMenuItem() {
            Menuitem result = new Menuitem();
            result.setLabel(name);
            if (icon != null) {
                result.setImage(icon);
            }
            return result;
        }

    }

    private final List<T> elements;

    private final List<Item> items = new ArrayList<Item>();

    private Component root;

    private MenuBuilder(Page page, Collection<? extends T> elements) {
        this.elements = new ArrayList<T>(elements);
        this.root = findVisibleOn(getRoots(page));
    }

    private static List<Component> getRoots(Page page) {
        List<Component> result = new ArrayList<Component>();
        Component current = page.getFirstRoot();
        while (current != null) {
            result.add(current);
            current = current.getNextSibling();
        }
        return result;
    }

    private static Component findVisibleOn(
            Collection<? extends Component> candidates) {
        for (Component each : candidates) {
            if (each.isVisible()) {
                return each;
            }
        }
        throw new RuntimeException(
                "not found visible component on which to attach the menu");
    }

    public MenuBuilder<T> item(String name, String icon,
            ItemAction<T> itemAction) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        if (itemAction == null) {
            throw new IllegalArgumentException("itemAction cannot be null");
        }
        items.add(new Item(name, icon, itemAction));
        return this;
    }

    private T referenced;

    public Menupopup createWithoutSettingContext() {
        return create(false);
    }

    public Menupopup create() {
        return create(true);
    }

    private Menupopup create(boolean setContext) {
        Menupopup result = new Menupopup();
        result.addEventListener("onOpen", new EventListener() {

            @Override
            public void onEvent(Event event) {
                OpenEvent openEvent = (OpenEvent) event;
                referenced = (T) openEvent.getReference();
            }
        });
        for (final Item item : items) {
            if (!item.name.equals("separator")) {
                Menuitem menuItem = item.createMenuItem();
                menuItem.addEventListener("onClick", new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        ItemAction<T> action = item.action;
                        action.onEvent(referenced, event);
                    }
                });
                result.appendChild(menuItem);
            } else {
                Menuseparator separator = new Menuseparator();
                result.appendChild(separator);
            }
        }
        insertInRootComponent(result);
        if (setContext) {
            for (T element : elements) {
                element.setContext(result);
            }
        }
        return result;
    }

    private void insertInRootComponent(Menupopup result) {
        ArrayList<Component> children = new ArrayList<Component>(root
                .getChildren());
        Collections.reverse(children);
        // the Menupopup cannot be inserted after a HtmlNativeComponent, so we
        // try to avoid it
        if (children.isEmpty()) {
            root.appendChild(result);
        }
        for (Component child : children) {
            if (!(child instanceof HtmlNativeComponent)) {
                root.insertBefore(result, child);
                return;
            }
        }
        throw new RuntimeException("all children of " + root
                + " are html native");
    }
}
