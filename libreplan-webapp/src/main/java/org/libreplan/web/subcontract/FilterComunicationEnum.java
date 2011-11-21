/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 WirelessGalicia, S.L.
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

package org.libreplan.web.subcontract;

/**
 * Enum to filter the {@link CustomerComunication} list.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public enum FilterComunicationEnum {
    ALL(_("All")), NOT_REVIEWED(_("Not Reviewed"));

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    private String displayName;

    private FilterComunicationEnum(String displayName) {
        this.displayName = displayName;
    }

    public static FilterComunicationEnum getDefault() {
        return ALL;
    }

    @Override
    public String toString() {
        return displayName;
    }
}