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

package org.libreplan.business.externalcompanies.entities;

import static org.libreplan.business.i18n.I18nHelper._;

/**
 * Enum for specified the type of {@link CustomerCommunication}
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public enum CommunicationType {

    NEW_PROJECT(_("New project")), PROGRESS_UPDATE(_("Progress Update")), UPDATE_DELIVERING_DATE(
            _("Update Delivering Date"));

    private String description;

    private CommunicationType(String description) {
        this.description = description;
    }

    public String toString() {
        return this.description;
    }

    public static CommunicationType getDefault() {
        return NEW_PROJECT;
    }
}
