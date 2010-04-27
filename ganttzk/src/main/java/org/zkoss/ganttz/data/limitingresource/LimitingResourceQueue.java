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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.data.resourceload.TimeLineRole;
import org.zkoss.ganttz.util.Interval;

public class LimitingResourceQueue {

    private final String conceptName;
    private final List<QueueTask> loadPeriods;

    private final TimeLineRole<?> timeLineRole;
    private final String type;

    private final List<LimitingResourceQueue> children;

    public LimitingResourceQueue(String conceptName,
            List<QueueTask> loadPeriods,
            TimeLineRole<?> role) {
        Validate.notEmpty(conceptName);
        Validate.notNull(loadPeriods);
        this.loadPeriods = QueueTask.sort(loadPeriods);
        this.conceptName = conceptName;
        this.type = "";
        this.timeLineRole = role;
        this.children = Collections
        .unmodifiableList(new ArrayList<LimitingResourceQueue>());
    }

    public LimitingResourceQueue(String conceptName,
            List<QueueTask> loadPeriods,
            String type, TimeLineRole<?> role) {
        Validate.notEmpty(conceptName);
        Validate.notNull(loadPeriods);
        this.loadPeriods = QueueTask.sort(loadPeriods);
        this.conceptName = conceptName;
        this.timeLineRole = role;
        this.type = type;
        this.children = Collections
                .unmodifiableList(new ArrayList<LimitingResourceQueue>());
    }

    public LimitingResourceQueue(LimitingResourceQueue principal, List<LimitingResourceQueue> children) {
        Validate.notEmpty(principal.getConceptName());
        Validate.notNull(principal.getQueueTasks());
        this.loadPeriods = QueueTask.sort(principal.getQueueTasks());
        this.conceptName = principal.getConceptName();
        this.timeLineRole = principal.getRole();
        this.type = principal.getType();
        Validate.notNull(children);
        allChildrenAreNotEmpty(children);
        this.children = Collections
                .unmodifiableList(new ArrayList<LimitingResourceQueue>(children));

    }

    public List<QueueTask> getQueueTasks() {
        return loadPeriods;
    }

    public String getConceptName() {
        return conceptName;
    }

    public TimeLineRole<?> getRole() {
        return timeLineRole;
    }

    private QueueTask getFirst() {
        return loadPeriods.get(0);
    }

    private QueueTask getLast() {
        return loadPeriods.get(loadPeriods.size() - 1);
    }

    public LocalDate getStartPeriod() {
        if (isEmpty()) {
            return null;
        }
        return getFirst().getStart();
    }

    public boolean isEmpty() {
        return loadPeriods.isEmpty();
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

    private static void allChildrenAreNotEmpty(List<LimitingResourceQueue> lines) {
        for (LimitingResourceQueue l : lines) {
            if (l.isEmpty()) {
                throw new IllegalArgumentException(l + " is empty");
            }
            if (l.hasChildren()) {
                allChildrenAreNotEmpty(l.getChildren());
            }
        }
    }

    public boolean hasChildren() {
        return (!children.isEmpty());
    }

    public List<LimitingResourceQueue> getChildren() {
        return children;
    }

    public List<LimitingResourceQueue> getAllChildren() {
        List<LimitingResourceQueue> result = new ArrayList<LimitingResourceQueue>();
        for (LimitingResourceQueue child : children) {
            result.addAll(child.getAllChildren());
            result.add(child);
        }
        return result;
    }

    public LocalDate getStart() {
        LocalDate result = getStartPeriod();
        for (LimitingResourceQueue loadTimeLine : getChildren()) {
            LocalDate start = loadTimeLine.getStart();
            if (start != null) {
            result = result == null || result.compareTo(start) > 0 ? start
                    : result;
            }
        }
        return result;
    }

    public LocalDate getEnd() {
        LocalDate result = getEndPeriod();
        for (LimitingResourceQueue loadTimeLine : getChildren()) {
            LocalDate end = loadTimeLine.getEnd();
            if (end != null) {
            result = result == null || result.compareTo(end) < 0 ? end : result;
            }
        }
        return result;
    }

}
