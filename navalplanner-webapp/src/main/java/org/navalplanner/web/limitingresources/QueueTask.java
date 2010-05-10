/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.limitingresources;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.navalplanner.business.planner.entities.LimitingResourceQueueElement;
import org.zkoss.zul.Div;

public class QueueTask extends Div {

    private static final Log LOG = LogFactory.getLog(QueueTask.class);

    private final LocalDate start;

    private final LocalDate end;

    private final int totalResourceWorkHours;

    private final int assignedHours;

    public QueueTask(LocalDate start, LocalDate end,
            int totalResourceWorkHours, int assignedHours) {
        Validate.notNull(start);
        Validate.notNull(end);
        Validate.notNull(totalResourceWorkHours);
        Validate.notNull(assignedHours);
        Validate.isTrue(!start.isAfter(end));
        this.start = start;
        this.end = end;
        this.totalResourceWorkHours = totalResourceWorkHours;
        this.assignedHours = assignedHours;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    /**
     * @param loadPeriods
     * @return
     * @throws IllegalArgumentException
     *             if some of the QueueTask overlaps
     */
    public static List<QueueTask> sort(List<QueueTask> loadPeriods)
            throws IllegalArgumentException {
        ArrayList<QueueTask> result = new ArrayList<QueueTask>(loadPeriods);
        // Collections.sort(result, new Comparator<QueueTask>() {

        // @Override
        // public int compare(QueueTask o1, QueueTask o2) {
        // if (o1.overlaps(o2)) {
        // LOG.warn(o1 + " overlaps with " + o2);
        // throw new IllegalArgumentException(o1 + " overlaps with "
        // + o2);
        // }
        // int comparison = compareLocalDates(o1.start, o2.start);
        // if (comparison != 0) {
        // return comparison;
        // }
        // return compareLocalDates(o1.end, o2.end);
        // }
        // });
        return result;
    }

    private static int compareLocalDates(LocalDate l1, LocalDate l2) {
        if (l1.isBefore(l2)) {
            return -1;
        }
        if (l1.isAfter(l2)) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public int getTotalResourceWorkHours() {
        return totalResourceWorkHours;
    }

    public int getAssignedHours() {
        return assignedHours;
    }

    public LimitingResourceQueueElement getQueueElement() {
        // TODO Auto-generated method stub
        return null;
    }
}
