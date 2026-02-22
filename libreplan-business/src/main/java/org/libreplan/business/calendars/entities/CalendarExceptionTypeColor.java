/*
 * This file is part of LibrePlan
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

package org.libreplan.business.calendars.entities;

import static org.libreplan.business.i18n.I18nHelper._t;

/**
 * Enum representing the possible colors to choose for a {@link CalendarExceptionType}.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public enum CalendarExceptionTypeColor {
    DEFAULT(_t("red (default)"), "#FF3333", "#FF9999"),
    GREEN(_t("green"),"#2ee62e", "#8ae68a"),
    BLUE(_t("blue"), "#3333FF", "#9999FF"),
    CYAN(_t("cyan"), "#33FFFF", "#99FFFF"),
    MAGENTA(_t("magenta"), "#FF33FF", "#FF99FF"),
    YELLOW(_t("yellow"), "#e6e62e", "#e6e6a1"),
    BLACK(_t("black"), "#333333", "#999999"),
    ORANGE(_t("orange"), "#ffb733", "#ffdb99"),
    PURPLE(_t("purple"), "#801a80", "#b38eb3");

    private final String name;

    private final String colorOwnException;

    private final String colorDerivedException;

    CalendarExceptionTypeColor(String name, String colorOwnException, String colorDerivedException) {
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
