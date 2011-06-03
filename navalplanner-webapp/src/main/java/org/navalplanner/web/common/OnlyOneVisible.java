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

package org.navalplanner.web.common;

import java.util.ArrayList;
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
        this.components = new ArrayList<Component>(Arrays.asList(components));
        showOnly(null);
    }

    public void showOnly(Component component) {
        if (!components.contains(component)) {
            components.add(component);
        }
        for (Component c : components) {
            if (c != null) {
                c.setVisible(component != null && c.equals(component));
            }
        }
    }

}
