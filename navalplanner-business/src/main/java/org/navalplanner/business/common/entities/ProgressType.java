/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.common.entities;

import static org.navalplanner.business.i18n.I18nHelper.*;

import java.util.Arrays;
import java.util.List;

/**
*
* @author Diego Pino García<dpino@igalia.com>
*
*/
public enum ProgressType {

    SPREAD_PROGRESS(_("Spread progress")),
    CRITICAL_PATH_DURATION(_("Critical path by duration")),
    CRITICAL_PATH_NUMHOURS(_("Critical path by number of hours"));

    private String value;

    private ProgressType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return value;
    }

    public static List<ProgressType> getAll() {
        return Arrays.asList(ProgressType.values());
    }

    public static ProgressType asEnum(String value) {
        for (ProgressType each: getAll()) {
            if (each.getValue().equals(value)) {
                return each;
            }
        }
        return null;
    }

}
