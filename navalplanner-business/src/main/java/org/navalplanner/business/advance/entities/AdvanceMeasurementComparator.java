/*
 * This file is part of NavalPlan
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.navalplanner.business.advance.entities;

import java.util.Comparator;

/**
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class AdvanceMeasurementComparator implements Comparator<AdvanceMeasurement> {

    public AdvanceMeasurementComparator(){
    }

    @Override
    public int compare(AdvanceMeasurement arg0, AdvanceMeasurement arg1) {
        if (arg0.getDate() == arg1.getDate()) {
            return 0;
        }
        if (arg1.getDate() == null) {
            return -1;
        }
        if (arg0.getDate() == null) {
            return 1;
        }
        return arg0.getDate().compareTo(arg1.getDate());
    }
}
