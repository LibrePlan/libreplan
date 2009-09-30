package org.zkoss.ganttz;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.ganttz.extensions.ITab;
import org.zkoss.ganttz.util.IMenuItemsRegister;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

public class TabsRegistry {

    private List<ITab> tabs = new ArrayList<ITab>();

    private final Component parent;

    public TabsRegistry(Component parent) {
        this.parent = parent;
    }

    public void add(ITab tab) {
        tab.addToParent(parent);
        tabs.add(tab);
    }

    public void show(ITab tab) {
        hideAllExcept(tab);
        tab.show();
        parent.invalidate();
    }

    private void hideAllExcept(ITab tab) {
        for (ITab t : tabs) {
            if (t.equals(tab))
                continue;
            t.hide();
        }
    }

    public void showFirst() {
        if (!tabs.isEmpty()) {
            show(tabs.get(0));
        }
    }

    public void registerAtMenu(IMenuItemsRegister menu) {
        for (final ITab t : tabs) {
            menu.addMenuItem(t.getName(), new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    show(t);
                }
            });
        }
    }
}
