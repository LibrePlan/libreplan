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

package org.zkoss.ganttz.data.resourceload;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.Validate;
import org.zkoss.ganttz.data.GanttDate;
import org.zkoss.ganttz.util.Interval;

public class LoadTimeLine {

    @SuppressWarnings("unchecked")
    private static final Comparator<GanttDate> nullSafeComparator = ComparatorUtils
            .nullLowComparator(ComparatorUtils.naturalComparator());

    public static Comparator<LoadTimeLine> byStartAndEndDate() {
        return new Comparator<LoadTimeLine>() {

            @Override
            public int compare(LoadTimeLine o1, LoadTimeLine o2) {
                int result = nullSafeComparator.compare(o1.getStartPeriod(),
                        o2.getStartPeriod());
                if (result == 0) {
                    return nullSafeComparator.compare(o1.getEndPeriod(),
                            o2.getEndPeriod());
                }
                return result;

            }
        };
    }

    private final String conceptName;
    private final List<LoadPeriod> loadPeriods;

    private final TimeLineRole<?> timeLineRole;
    private final String type;

    private final List<LoadTimeLine> children;

    public LoadTimeLine(String conceptName, List<LoadPeriod> loadPeriods,
            TimeLineRole<?> role) {
        Validate.notEmpty(conceptName);
        Validate.notNull(loadPeriods);
        this.loadPeriods = LoadPeriod.sort(loadPeriods);
        this.conceptName = conceptName;
        this.type = "";
        this.timeLineRole = role;
        this.children = Collections
        .unmodifiableList(new ArrayList<LoadTimeLine>());
    }

    public LoadTimeLine(String conceptName, List<LoadPeriod> loadPeriods,
            String type, TimeLineRole<?> role) {
        Validate.notEmpty(conceptName);
        Validate.notNull(loadPeriods);
        this.loadPeriods = LoadPeriod.sort(loadPeriods);
        this.conceptName = conceptName;
        this.timeLineRole = role;
        this.type = type;
        this.children = Collections
                .unmodifiableList(new ArrayList<LoadTimeLine>());
    }

    public LoadTimeLine(LoadTimeLine main, List<LoadTimeLine> children) {
        Validate.notEmpty(main.getConceptName());
        Validate.notNull(main.getLoadPeriods());
        this.loadPeriods = LoadPeriod.sort(main.getLoadPeriods());
        this.conceptName = main.getConceptName();
        this.timeLineRole = main.getRole();
        this.type = main.getType();
        Validate.notNull(children);
        this.children = Collections
                .unmodifiableList(new ArrayList<LoadTimeLine>(children));

    }

    public List<LoadPeriod> getLoadPeriods() {
        return loadPeriods;
    }

    public String getConceptName() {
        return conceptName;
    }

    public TimeLineRole<?> getRole() {
        return timeLineRole;
    }

    private LoadPeriod getFirst() {
        return loadPeriods.get(0);
    }

    private LoadPeriod getLast() {
        return loadPeriods.get(loadPeriods.size() - 1);
    }

    public GanttDate getStartPeriod() {
        if (isEmpty()) {
            return null;
        }
        return getFirst().getStart();
    }

    public boolean isEmpty() {
        return loadPeriods.isEmpty();
    }

    public GanttDate getEndPeriod() {
        if (isEmpty()) {
            return null;
        }
        return getLast().getEnd();
    }

    public String getType() {
        return this.type;
    }

    public static Interval getIntervalFrom(List<LoadTimeLine> timeLines) {
        GanttDate start = null;
        GanttDate end = null;
        for (LoadTimeLine loadTimeLine : timeLines) {
            if(!loadTimeLine.isEmpty()) {
                Validate.notNull(loadTimeLine.getStart());
                start = min(start, loadTimeLine.getStart());
                Validate.notNull(loadTimeLine.getEnd());
                end = max(end, loadTimeLine.getEnd());
            }
        }
        if (timeLines.isEmpty() || start == null || end == null) {
            return new Interval(new Date(), plusFiveYears(new Date()));
        }
        return new Interval(start.toLocalDate(), end.asExclusiveEnd());
    }

    private static Date plusFiveYears(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, 5);
        return calendar.getTime();
    }

    private static GanttDate max(GanttDate one, GanttDate other) {
        if (one == null) {
            return other;
        }
        if (other == null) {
            return one;
        }
        return one.compareTo(other) > 0 ? one : other;
    }

    private static GanttDate min(GanttDate one, GanttDate other) {
        if (one == null) {
            return other;
        }
        if (other == null) {
            return one;
        }
        return one.compareTo(other) < 0 ? one : other;
    }

    public boolean hasChildren() {
        return (!children.isEmpty());
    }

    public List<LoadTimeLine> getChildren() {
        return children;
    }

    public List<LoadTimeLine> getAllChildren() {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        for (LoadTimeLine child : children) {
            result.add(child);
            result.addAll(child.getAllChildren());
        }
        return result;
    }

    public GanttDate getStart() {
        GanttDate result = getStartPeriod();
        for (LoadTimeLine loadTimeLine : getChildren()) {
            GanttDate start = loadTimeLine.getStart();
            if (start != null) {
                result = result == null || result.compareTo(start) > 0 ? start
                        : result;
            }
        }
        return result;
    }

    public GanttDate getEnd() {
        GanttDate result = getEndPeriod();
        for (LoadTimeLine loadTimeLine : getChildren()) {
            GanttDate end = loadTimeLine.getEnd();
            if (end != null) {
                result = result == null || result.compareTo(end) < 0 ? end
                        : result;
            }
        }
        return result;
    }

}
