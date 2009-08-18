package org.zkoss.ganttz.util;

import org.zkoss.zk.ui.event.EventListener;

public interface IMenuItemsRegister {

    public void addMenuItem(String name, EventListener eventListener);

}
