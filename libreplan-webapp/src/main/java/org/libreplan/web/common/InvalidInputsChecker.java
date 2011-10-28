/*
 * This file is part of LibrePlan
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

package org.libreplan.web.common;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.impl.api.InputElement;

/**
 * Class for checking the inexistence of invalid values
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class InvalidInputsChecker {

    private InvalidInputsChecker() {
    }

    public static boolean thereAreInvalidInputsOn(Component component) {
        if (isInvalid(component)) {
            return true;
        }
        List<Component> children = component.getChildren();
        for (Component child : children) {
            if (thereAreInvalidInputsOn(child)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInvalid(Component child) {
        if (child instanceof InputElement) {
            InputElement input = (InputElement) child;
            if (!input.isValid()) {
                return true;
            }
        }
        return false;
    }

}
