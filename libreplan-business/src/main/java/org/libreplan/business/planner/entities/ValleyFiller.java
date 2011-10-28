/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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
package org.libreplan.business.planner.entities;

/**
 * It fills valleys in the discrete function represented by hours. A valley is
 * the portion between two maximum points. Besides the last value must be the
 * maximum for all the function
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ValleyFiller {

    private ValleyFiller() {
    }

    public static int[] fillValley(int... hours) {
        int[] result = new int[hours.length];
        int lastMaximum = Integer.MIN_VALUE;
        int globalMaximum = hours[hours.length - 1];
        for (int i = 0; i < hours.length; i++) {
            if (hours[i] > lastMaximum) {
                lastMaximum = hours[i];
            }
            result[i] = Math.min(lastMaximum, globalMaximum);
        }
        return result;
    }

}
