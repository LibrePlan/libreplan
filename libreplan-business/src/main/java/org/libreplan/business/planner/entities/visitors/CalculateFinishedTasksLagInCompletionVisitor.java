/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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

package org.libreplan.business.planner.entities.visitors;

/**
 * This visitor calculates lastWorkReportDate/EndDate deviation
 * for all finished tasks with work report lines attached.
 * @author Nacho Barrientos <nacho@igalia.com>
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.util.TaskElementVisitor;
import org.libreplan.business.workreports.entities.WorkReportLine;

public class CalculateFinishedTasksLagInCompletionVisitor extends TaskElementVisitor {

    private List<Double> deviations;

    public CalculateFinishedTasksLagInCompletionVisitor() {
        this.deviations = new ArrayList<Double>();
    }

    public List<Double> getDeviations() {
        return this.deviations;
    }

    public void visit(Task task) {
        if (task.isFinished()) {
            List<WorkReportLine> workReportLines = task.
                    getOrderElement().getWorkReportLines(true);
            if (workReportLines.size() > 0) {
                WorkReportLine last = getLastWorkReportLineWithEffortDurationNoZero(workReportLines);
                if (last != null) {
                    LocalDate lastRLDate = LocalDate.fromDateFields(last
                            .getDate());
                    LocalDate endDate = task.getEndAsLocalDate();
                    deviations.add((double) Days.daysBetween(endDate,
                            lastRLDate).getDays());
                }
            }
        }
    }

    private WorkReportLine getLastWorkReportLineWithEffortDurationNoZero(
            List<WorkReportLine> workReportLines) {
        Collections.reverse(workReportLines);
        for (WorkReportLine each : workReportLines) {
            if (!each.getEffort().isZero()) {
                return each;
            }
        }
        return null;
    }

    public void visit(TaskGroup taskGroup) {
        for (TaskElement each: taskGroup.getChildren()) {
            each.acceptVisitor(this);
        }
    }

}
