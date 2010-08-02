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

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
package org.navalplanner.business.workreports.entities;

import org.navalplanner.business.i18n.I18nHelper;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public enum PositionInWorkReportEnum {

    HEADING(_("heading")), LINE(_("line"));

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    private String displayName;

    private PositionInWorkReportEnum(String displayName) {
        this.displayName = displayName;
    }

    public static PositionInWorkReportEnum getDefault() {
        return LINE;
    }

    public String toString() {
        return I18nHelper._(displayName);
    }

}
