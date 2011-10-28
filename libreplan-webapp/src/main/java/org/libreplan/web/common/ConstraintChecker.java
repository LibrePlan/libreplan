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
import org.zkoss.zul.impl.InputElement;

/**
 * Class for checking if a component is completely valid (checks all constraints
 * within a component)
 *
 * @author Diego Pino García <dpino@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class ConstraintChecker {

    @SuppressWarnings("unchecked")
    public static void isValid(Component component) {
        checkIsValid(component);
        checkIsValid(component.getChildren());
    }

    private static void checkIsValid(List<Component> components) {
        for (Component component: components) {
            isValid(component);
        }
    }

    private static void checkIsValid(Component child) {
        if (child instanceof InputElement) {
            inputIsValid((InputElement) child);
        }
    }

    private static void inputIsValid(InputElement input) {
        if (!input.isValid()) {
            input.getText();
        }
    }

}
