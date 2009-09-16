package org.navalplanner.web.common;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.impl.api.InputElement;

/**
 * Class for checking the inexistence of invalid values
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class InvalidInputsChecker {

    public static boolean thereAreInvalidInputsOn(Component component) {
        if (isInvalid(component)) {
            return true;
        }
        List<Component> children = component.getChildren();
        for (Component child : children) {
            if (isInvalid(child)) {
                return true;
            }
            if (thereAreInvalidInputsOn(child)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInvalid(Component child) {
        if (child instanceof InputElement) {
            InputElement input = (InputElement) child;
            if (!input.isValid()) {
                return true;
            }
        }
        return false;
    }

}
