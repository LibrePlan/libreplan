/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalicia, S.L.
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
package org.libreplan.business.filmingprogress.entities;

import static org.libreplan.business.i18n.I18nHelper._;
/**
 * Represents the progress granularity in a filming.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public enum ProgressGranularityType {

    DAY(_("day")), WEEK(_("week")), MONTH(_("month"));

    private String description;

    ProgressGranularityType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
