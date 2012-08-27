/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

/**
 * Different values for personal timesheets periodicity.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public enum PersonalTimesheetsPeriodicityEnum {

    MONTHLY(_("Monthly")),
    TWICE_MONTHLY(_("Twice-monthly")),
    WEEKLY(_("Weekly"));

    private String name;

    private PersonalTimesheetsPeriodicityEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
