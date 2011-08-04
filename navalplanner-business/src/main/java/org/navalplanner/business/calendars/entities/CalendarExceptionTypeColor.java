/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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

package org.navalplanner.business.calendars.entities;

import static org.navalplanner.business.i18n.I18nHelper._;

/**
 * Enum representing the possible colors to choose for a
 * {@link CalendarExceptionType}
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public enum CalendarExceptionTypeColor {
    DEFAULT(_("red (default)"), "red", "lightcoral"),
    GREEN(_("green"), "green", "lightgreen"),
    BLUE(_("blue"), "blue", "lightblue");

    private final String name;
    private final String colorOwnException;
    private final String colorDerivedException;

    private CalendarExceptionTypeColor(String name, String colorOwnException,
            String colorDerivedException) {
        this.name = name;
        this.colorOwnException = colorOwnException;
        this.colorDerivedException = colorDerivedException;
    }

    public String getName() {
        return name;
    }

    public String getColorOwnException() {
        return colorOwnException;
    }

    public String getColorDerivedException() {
        return colorDerivedException;
    }

}
