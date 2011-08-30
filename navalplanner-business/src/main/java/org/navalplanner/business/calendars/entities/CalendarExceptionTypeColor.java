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
    DEFAULT(_("red (default)"), "#FF3333", "#FF9999"),
    GREEN(_("green"),"#2ee62e", "#8ae68a"),
    BLUE(_("blue"), "#3333FF", "#9999FF"),
    CYAN(_("cyan"), "#33FFFF", "#99FFFF"),
    MAGENTA(_("magenta"), "#FF33FF", "#FF99FF"),
    YELLOW(_("yellow"), "#e6e62e", "#e6e6a1"),
    BLACK(_("black"), "#333333", "#999999"),
    ORANGE(_("orange"), "#ffb733", "#ffdb99"),
    PURPLE(_("purple"), "#801a80", "#b38eb3");

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
