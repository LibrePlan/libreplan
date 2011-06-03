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
package org.navalplanner.business.resources.entities;

import java.util.Comparator;

import org.joda.time.LocalDate;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueElement;

/**
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 *
 */
public class LimitingResourceQueueElementComparator implements
    Comparator<LimitingResourceQueueElement> {

    @Override
    public int compare(LimitingResourceQueueElement arg0,
            LimitingResourceQueueElement arg1) {
        int compareDates = compare(arg0.getStartDate(), arg1.getStartDate());
        return (compareDates != 0) ? compareDates : compare(
                arg0.getStartHour(), arg1.getStartHour());
    }

    private int compare(LocalDate arg0, LocalDate arg1) {
        if (arg0  == null) {
            return -1;
        }
        if (arg1 == null) {
            return 1;
        }
        return arg0.compareTo(arg1);
    }

    private int compare(int arg0, int arg1) {
        final int deltaHour = arg0 - arg1;
        return (deltaHour != 0) ? deltaHour / Math.abs(deltaHour) : 0;
    }

}
