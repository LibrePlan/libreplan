package org.zkoss.ganttz.util;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;

public class MenuItemsRegisterLocator {

    private static final String MENU_ATTRIBUTE = MenuItemsRegisterLocator.class
            .getSimpleName()
            + "_menu";

    private MenuItemsRegisterLocator() {
    }

    public static void store(IMenuItemsRegister register) {
        getDesktop().setAttribute(MENU_ATTRIBUTE, register);
    }

    private static Desktop getDesktop() {
        return Executions.getCurrent().getDesktop();
    }

    public static IMenuItemsRegister retrieve()
            throws IllegalStateException {
        Object result = getDesktop().getAttribute(MENU_ATTRIBUTE);
        if (result == null)
            throw new IllegalStateException("no "
                    + IMenuItemsRegister.class.getSimpleName() + " registered");
        return (IMenuItemsRegister) result;
    }

}
