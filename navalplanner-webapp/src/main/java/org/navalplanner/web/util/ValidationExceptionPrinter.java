/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 Igalia S.L
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

package org.navalplanner.web.util;

import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.zkoss.ganttz.util.ComponentsFinder;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Row;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         Helper class for printing a {@link ValidationException} at a
 *         component.
 *
 */
public class ValidationExceptionPrinter {

    public static void showAt(Component comp, ValidationException e) {
        InvalidValue invalidValue = e.getInvalidValue();
        if (comp instanceof Grid) {
            showAt((Grid) comp, invalidValue);
        } else {
            showAt(comp, invalidValue);
        }
    }

    private static void showAt(Component comp, InvalidValue invalidValue) {
        throw new WrongValueException(comp, invalidValue.getMessage());
    }

    private static void showAt(Grid comp, InvalidValue invalidValue) {
        Row row = ComponentsFinder.findRowByValue(comp, invalidValue.getValue());
        if (row != null) {
            throw new WrongValueException(row, invalidValue.getMessage());
        }
    }

}