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

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
package org.libreplan.business.workreports.entities;

import static org.libreplan.business.i18n.I18nHelper._;

public enum HoursManagementEnum {

    NUMBER_OF_HOURS(_("Number of assigned hours")),
    HOURS_CALCULATED_BY_CLOCK(_("Number of hours calculated by clock")),
    NUMBER_OF_HOURS_AND_CLOCK(_("Number of assigned hours and the time"));

    private String description;

    private HoursManagementEnum(String description) {
        this.description = description;
    }

    public String toString() {
        return this.description;
    }

    public static HoursManagementEnum getDefault() {
        return NUMBER_OF_HOURS;
    }
}
