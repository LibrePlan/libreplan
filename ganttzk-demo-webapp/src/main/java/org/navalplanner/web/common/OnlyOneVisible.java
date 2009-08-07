package org.navalplanner.web.common;

import java.util.Arrays;
import java.util.List;

import org.zkoss.zk.ui.Component;

/**
 * Utility for enforcing that only one of the supplied component is visible. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class OnlyOneVisible {

    private List<Component> components;

    public OnlyOneVisible(Component... components) {
        this.components = Arrays.asList(components);
        showOnly(null);
    }

    public void showOnly(Component component) {
        for (Component c : components) {
            if (c != null) {
                c.setVisible(component != null && c.equals(component));
            }
        }
    }

}
