package org.zkoss.ganttz.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
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
        final String name;

        final ItemAction<T> action;

        Item(String name, ItemAction<T> action) {
            this.name = name;
            this.action = action;
        }

        Menuitem createMenuItem() {
            Menuitem result = new Menuitem();
            result.setLabel(name);
            return result;
        }

    }

    private final List<T> elements;

    private final List<Item> items = new ArrayList<Item>();

    private Component root;

    private MenuBuilder(Page page, Collection<? extends T> elements) {
        this.elements = new ArrayList<T>(elements);
        this.root = page.getLastRoot();
    }

    public MenuBuilder<T> item(String name, ItemAction<T> itemAction) {
        if (name == null)
            throw new IllegalArgumentException("name cannot be null");
        if (itemAction == null)
            throw new IllegalArgumentException("itemAction cannot be null");
        items.add(new Item(name, itemAction));
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
            public void onEvent(Event event) throws Exception {
                OpenEvent openEvent = (OpenEvent) event;
                referenced = (T) openEvent.getReference();
            }
        });
        for (final Item item : items) {
            Menuitem menuItem = item.createMenuItem();
            menuItem.addEventListener("onClick", new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    ItemAction<T> action = item.action;
                    action.onEvent(referenced, event);
                }
            });
            result.appendChild(menuItem);
        }
        root.appendChild(result);
        if (setContext) {
            for (T element : elements) {
                element.setContext(result);
            }
        }
        return result;
    }
}
