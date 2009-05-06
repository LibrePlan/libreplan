package org.navalplanner.web.common;

import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.DataBinder;

/**
 * Utilities class. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Util {

    public static void reloadBindings(Component reload) {
        DataBinder binder = Util.getBinder(reload);
        if (binder != null) {
            binder.loadComponent(reload);
        }
    }

    public static DataBinder getBinder(Component component) {
        return (DataBinder) component.getVariable("binder", false);
    }

}
