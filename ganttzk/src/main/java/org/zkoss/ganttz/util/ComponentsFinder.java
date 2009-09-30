package org.zkoss.ganttz.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods to find components
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ComponentsFinder {

    public static <T> List<T> findComponentsOfType(Class<T> type,
            List<? extends Object> children) {
        ArrayList<T> result = new ArrayList<T>();
        for (Object child : children) {
            if (type.isInstance(child)) {
                result.add(type.cast(child));
            }
        }
        return result;
    }

}
