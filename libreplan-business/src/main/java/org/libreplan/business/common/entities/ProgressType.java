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

package org.libreplan.business.common.entities;

import static org.libreplan.business.i18n.I18nHelper._;

import java.util.Arrays;
import java.util.List;

/**
*
* @author Diego Pino García<dpino@igalia.com>
*
*/
public enum ProgressType {

    SPREAD_PROGRESS(_("Spreading progress")),
    ALL_NUMHOURS(_("Progress with all tasks by hours")),
    CRITICAL_PATH_NUMHOURS(_("Progress with critical path tasks by hours")),
    CRITICAL_PATH_DURATION(_("Progress with critical path tasks by duration"));

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
