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
package org.libreplan.business.costcategories.entities;

import java.math.BigDecimal;

/**
 * Defines the default {@link TypeOfWorkHours}.
 *
 * @author Ignacio Díaz Teijido <ignacio.diaz@cafedered.com>
 */
public enum PredefinedTypeOfWorkHours {

    DEFAULT("Default", 30), OVERTIME("Overtime", 50);

    private TypeOfWorkHours typeOfWorkHours;


    private PredefinedTypeOfWorkHours(String name, double price) {
        typeOfWorkHours = TypeOfWorkHours.create();
        typeOfWorkHours.setName(name);
        typeOfWorkHours.setDefaultPrice(new BigDecimal(price).setScale(2));
    }

    public TypeOfWorkHours getTypeOfWorkHours() {
        return typeOfWorkHours;
    }

    public static boolean contains(TypeOfWorkHours typeOfWorkHours) {
        PredefinedTypeOfWorkHours[] types = PredefinedTypeOfWorkHours.values();
        for (PredefinedTypeOfWorkHours each : types) {
            if (each.getTypeOfWorkHours().getName()
                    .equals(typeOfWorkHours.getName())) {
                return true;
            }
        }
        return false;
    }

}