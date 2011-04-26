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

package org.zkoss.ganttz.data.resourceload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;

public class LoadPeriod {

    private static final Log LOG = LogFactory.getLog(LoadPeriod.class);

    private final LocalDate start;

    private final LocalDate end;

    private final LoadLevel loadLevel;

    private final int totalResourceWorkHours;

    private final int assignedHours;

    public LoadPeriod(LocalDate start, LocalDate end,
            int totalResourceWorkHours, int assignedHours, LoadLevel loadLevel) {
        Validate.notNull(start);
        Validate.notNull(end);
        Validate.notNull(loadLevel);
        Validate.notNull(totalResourceWorkHours);
        Validate.notNull(assignedHours);
        Validate.isTrue(!start.isAfter(end));
        this.start = start;
        this.end = end;
        this.loadLevel = loadLevel;
        this.totalResourceWorkHours = totalResourceWorkHours;
        this.assignedHours = assignedHours;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public boolean overlaps(LoadPeriod other) {
        return start.isBefore(other.end) && end.isAfter(other.start);
    }

    /**
     * @param notOverlappingPeriods
     * @return
     * @throws IllegalArgumentException
     *             if some of the LoadPeriod overlaps
     */
    public static List<LoadPeriod> sort(
            Collection<? extends LoadPeriod> notOverlappingPeriods)
            throws IllegalArgumentException {
        ArrayList<LoadPeriod> result = new ArrayList<LoadPeriod>(
                notOverlappingPeriods);
        Collections.sort(result, new Comparator<LoadPeriod>() {

            @Override
            public int compare(LoadPeriod o1, LoadPeriod o2) {
                if (o1.overlaps(o2)) {
                    LOG.warn(o1 + " overlaps with " + o2);
                    throw new IllegalArgumentException(o1 + " overlaps with "
                            + o2);
                }
                int comparison = o1.start.compareTo(o2.start);
                if (comparison != 0) {
                    return comparison;
                }
                return o1.end.compareTo(o2.end);
            }
        });
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public LoadLevel getLoadLevel() {
        return loadLevel;
    }

    public int getTotalResourceWorkHours() {
        return totalResourceWorkHours;
    }

    public int getAssignedHours() {
        return assignedHours;
    }
}
