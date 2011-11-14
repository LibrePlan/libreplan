/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 CafédeRed Solutions, S.L.
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

package org.libreplan.business.resources.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Predefined location criterions<br />
 *
 * @author Ignacio Díaz Teijido <ignacio.diaz@cafedered.com>
 */
public enum LocationCriteria {
    AFRICA("Africa"), AMERICA("America"), ASIA("Asia"), AUSTRALIA("Australia"), EUROPE(
            "Europe");

    public static List<String> getCriterionNames() {
        ArrayList<String> result = new ArrayList<String>();
        for (LocationCriteria locationCriterions : values()) {
            result.add(locationCriterions.criterionName);
        }
        return result;
    }

    private final String criterionName;

    public Criterion criterion() {
        return Criterion.create(criterionName, CriterionType
                .asCriterionType(PredefinedCriterionTypes.LOCATION));
    }

    private LocationCriteria(String name) {
        this.criterionName = name;
    }
}
