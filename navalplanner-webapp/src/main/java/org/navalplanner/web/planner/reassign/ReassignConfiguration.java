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
package org.navalplanner.web.planner.reassign;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.web.planner.TaskElementAdapter;
import org.zkoss.ganttz.data.Task;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class ReassignConfiguration {

    public static ReassignConfiguration create(Type type,
            LocalDate date) {
        return new ReassignConfiguration(type, date);
    }

    private Type type;

    private LocalDate date;

    private ReassignConfiguration(Type type, LocalDate date) {
        this.type = type;
        Validate.isTrue(!type.needsAssociatedDate() || date != null);
        this.date = date == null ? new LocalDate() : date;
    }

    public List<Task> filterForReassignment(List<Task> list) {
        List<Task> result = new ArrayList<Task>();
        for (Task each : list) {
            if (each.isLeaf() && isChoosenForReassignation(each)) {
                result.add(each);
            }
        }
        return result;
    }

    private boolean isChoosenForReassignation(Task each) {
        return type == Type.ALL || isAfterDate(each);
    }

    private boolean isAfterDate(Task each) {
        LocalDate start = TaskElementAdapter.toLocalDate(each.getBeginDate());
        LocalDate end = TaskElementAdapter.toLocalDate(each.getEndDate());
        return start.compareTo(date) > 0 || end.compareTo(date) > 0;
    }

}

