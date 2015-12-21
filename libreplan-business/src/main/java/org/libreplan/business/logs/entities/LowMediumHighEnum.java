/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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
package org.libreplan.business.logs.entities;

import static org.libreplan.business.i18n.I18nHelper._;


/**
 * Defines the low, medium and high enums to be used as data type in
 * {@link IssueLog} and {@link RiskLog}
 * 
 * @author Misha Gozhda <misha@libreplan-enterprise.com>
 */
public enum LowMediumHighEnum {

    LOW(_("Low")), MEDIUM(_("Medium")), HIGH(_("High"));

    private final String lowMediumHighEnum;

    LowMediumHighEnum(String lowMediumHighEnum) {
        this.lowMediumHighEnum = lowMediumHighEnum;
    }

    public String getDisplayName() {
        return lowMediumHighEnum;
    }

    public static LowMediumHighEnum getDefault() {
        return LowMediumHighEnum.LOW;
    }
}
