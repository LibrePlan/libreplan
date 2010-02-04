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

package org.zkoss.ganttz.data.resourceload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.util.Interval;

public class LoadTimelinesGroup {

    public static Interval getIntervalFrom(List<LoadTimelinesGroup> timeLines) {
        Validate.notEmpty(timeLines);
        LocalDate start = null;
        LocalDate end = null;
        for (LoadTimelinesGroup loadTimelinesGroup : timeLines) {
            Validate.notNull(loadTimelinesGroup.getStart());
            start = min(start, loadTimelinesGroup.getStart());
            Validate.notNull(loadTimelinesGroup.getEnd());
            end = max(end, loadTimelinesGroup.getEnd());
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

    private final LoadTimeLine principal;

    private final List<LoadTimeLine> children;

    public LoadTimelinesGroup(LoadTimeLine principal,
            List<? extends LoadTimeLine> children) {
        Validate.notNull(principal);
        Validate.notNull(children);
        allChildrenAreNotEmpty(children);
        this.principal = principal;
        this.children = Collections
                .unmodifiableList(new ArrayList<LoadTimeLine>(children));
    }

    private static void allChildrenAreNotEmpty(
            List<? extends LoadTimeLine> lines) {
        for (LoadTimeLine l : lines) {
            if (l.isEmpty()) {
                throw new IllegalArgumentException(l + " is empty");
            }
        }
    }

    public LoadTimeLine getPrincipal() {
        return principal;
    }

    public List<LoadTimeLine> getChildren() {
        return children;
    }

    private List<LoadTimeLine> getAll() {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        result.add(getPrincipal());
        result.addAll(getChildren());
        return result;
    }

    public LocalDate getStart() {
        LocalDate result = null;
        for (LoadTimeLine loadTimeLine : getAll()) {
            LocalDate start = loadTimeLine.getStart();
            result = result == null || result.compareTo(start) > 0 ? start
                    : result;
        }
        return result;
    }

    public LocalDate getEnd() {
        LocalDate result = null;
        for (LoadTimeLine loadTimeLine : getAll()) {
            LocalDate end = loadTimeLine.getEnd();
            result = result == null || result.compareTo(end) < 0 ? end
                    : result;
        }
        return result;
    }

    public boolean isEmpty() {
        return principal.isEmpty();
    }

}

