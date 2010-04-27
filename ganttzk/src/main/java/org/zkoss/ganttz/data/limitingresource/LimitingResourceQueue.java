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

package org.zkoss.ganttz.data.limitingresource;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.data.resourceload.TimeLineRole;
import org.zkoss.ganttz.util.Interval;

public class LimitingResourceQueue {

    private final String resourcetName;
    private final List<QueueTask> queueTask;

    private final TimeLineRole<?> timeLineRole;
    private final String type;

    public LimitingResourceQueue(String conceptName,
            List<QueueTask> loadPeriods,
            TimeLineRole<?> role) {
        Validate.notEmpty(conceptName);
        Validate.notNull(loadPeriods);
        this.queueTask = QueueTask.sort(loadPeriods);
        this.resourcetName = conceptName;
        this.type = "";
        this.timeLineRole = role;
    }

    public LimitingResourceQueue(String conceptName,
            List<QueueTask> loadPeriods,
            String type, TimeLineRole<?> role) {
        Validate.notEmpty(conceptName);
        Validate.notNull(loadPeriods);
        this.queueTask = QueueTask.sort(loadPeriods);
        this.resourcetName = conceptName;
        this.timeLineRole = role;
        this.type = type;
    }

    public LimitingResourceQueue(LimitingResourceQueue principal) {
        Validate.notEmpty(principal.getResourceName());
        Validate.notNull(principal.getQueueTasks());
        this.queueTask = QueueTask.sort(principal.getQueueTasks());
        this.resourcetName = principal.getResourceName();
        this.timeLineRole = principal.getRole();
        this.type = principal.getType();
    }

    public List<QueueTask> getQueueTasks() {
        return queueTask;
    }

    public String getResourceName() {
        return resourcetName;
    }

    public TimeLineRole<?> getRole() {
        return timeLineRole;
    }

    private QueueTask getFirst() {
        return queueTask.get(0);
    }

    private QueueTask getLast() {
        return queueTask.get(queueTask.size() - 1);
    }

    public LocalDate getStartPeriod() {
        if (isEmpty()) {
            return null;
        }
        return getFirst().getStart();
    }

    public boolean isEmpty() {
        return queueTask.isEmpty();
    }

    public LocalDate getEndPeriod() {
        if (isEmpty()) {
            return null;
        }
        return getLast().getEnd();
    }

    public String getType() {
        return this.type;
    }

    public static Interval getIntervalFrom(List<LimitingResourceQueue> timeLines) {
        Validate.notEmpty(timeLines);
        LocalDate start = null;
        LocalDate end = null;
        for (LimitingResourceQueue loadTimeLine : timeLines) {
            Validate.notNull(loadTimeLine.getStart());
            start = min(start, loadTimeLine.getStart());
            Validate.notNull(loadTimeLine.getEnd());
            end = max(end, loadTimeLine.getEnd());
        }
        return new Interval(toDate(start), toDate(end));
    }

    private static Date toDate(LocalDate localDate) {
        return localDate.toDateTimeAtStartOfDay().toDate();
    }

    private static LocalDate max(LocalDate one, LocalDate other) {
        if (one == null) {
            return other;
        }
        if (other == null) {
            return one;
        }
        return one.compareTo(other) > 0 ? one : other;
    }

    private static LocalDate min(LocalDate one, LocalDate other) {
        if (one == null) {
            return other;
        }
        if (other == null) {
            return one;
        }
        return one.compareTo(other) < 0 ? one : other;
    }

    public LocalDate getStart() {
        return getStartPeriod();
    }

    public LocalDate getEnd() {
        return getEndPeriod();
    }

}
