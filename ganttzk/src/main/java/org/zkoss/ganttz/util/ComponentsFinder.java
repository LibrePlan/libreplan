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
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Row;
import org.zkoss.zul.api.Rows;

/**
 * Utility methods to find components
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ComponentsFinder {

    private ComponentsFinder() {
    }

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

    public static Component findById(String id,
            List<? extends Component> children) {
        for (Component child : children) {
            if (child.getId().equals(id)) {
                return child;
            }
        }
        return null;
    }

    public static Row findRowByValue(Grid grid, Object needle) {
        if (grid == null || needle == null) {
            return null;
        }
        Rows rows = grid.getRows();
        for (Object each : rows.getChildren()) {
            if (each instanceof Row) {
                Row row = (Row) each;
                Object value = row.getValue();
                if (needle.equals(value)) {
                    return row;
                }
            }
        }
        return null;
    }

    public static Listitem findItemByValue(Listbox listbox, Object needle) {
        if (listbox == null || needle == null) {
            return null;
        }
        for (Object each : listbox.getItems()) {
            Listitem item = (Listitem) each;
            Object value = item.getValue();
            if (needle.toString().equals(value.toString())) {
                return item;
            }
        }
        return null;
    }

}
