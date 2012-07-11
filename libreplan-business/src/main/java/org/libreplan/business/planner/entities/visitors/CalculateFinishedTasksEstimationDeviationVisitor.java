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
 * FIXME
 * This visitor calculates allocated/spent hours deviation
 * for finished tasks.
 * @author Nacho Barrientos <nacho@igalia.com>
 */
import java.util.ArrayList;
import java.util.List;

import org.libreplan.business.orders.entities.SumChargedEffort;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.util.TaskElementVisitor;
import org.libreplan.business.workingday.EffortDuration;

public class CalculateFinishedTasksEstimationDeviationVisitor extends TaskElementVisitor {

    private List<Double> deviations;

    public CalculateFinishedTasksEstimationDeviationVisitor() {
        this.deviations = new ArrayList<Double>();
    }

    public List<Double> getDeviations() {
        return this.deviations;
    }

    public int getNumberOfConsideredTasks(){
        return deviations.size();
    }

    public void visit(Task task) {
        if (task.isFinished()) {
            EffortDuration effort = task.getAssignedEffort();
            if (effort.isZero()) {
                effort = EffortDuration.hours(task.getOrderElement()
                        .getWorkHours());
            }
            if (!effort.isZero()) {
                SumChargedEffort sumChargedEffort = task.getOrderElement()
                        .getSumChargedEffort();
                EffortDuration spentEffort = sumChargedEffort == null ? EffortDuration
                        .zero() : sumChargedEffort.getTotalChargedEffort();
                if (!spentEffort.isZero()) {
                    deviations.add(spentEffort.minus(effort)
                            .dividedByAndResultAsBigDecimal(effort)
                            .doubleValue() * 100);
                }
            }
        }
    }

    public void visit(TaskGroup taskGroup) {
        for (TaskElement each: taskGroup.getChildren()) {
            each.acceptVisitor(this);
        }
    }

}
