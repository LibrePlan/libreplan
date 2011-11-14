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
 * Visits task graphs computing deadline violation statuses
 * filling in a Map summarizing the status of all tasks.
 *
 * @author Nacho Barrientos <nacho@igalia.com>
 */
import java.util.HashMap;
import java.util.Map;

import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskDeadlineViolationStatusEnum;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.util.TaskElementVisitor;

public class AccumulateTasksDeadlineStatusVisitor extends TaskElementVisitor {

    private Map<TaskDeadlineViolationStatusEnum, Integer> taskDeadlineViolationStatusData;

    public AccumulateTasksDeadlineStatusVisitor() {
        this.taskDeadlineViolationStatusData = new HashMap<TaskDeadlineViolationStatusEnum, Integer>();
    }

    public Map<TaskDeadlineViolationStatusEnum, Integer> getTaskDeadlineViolationStatusData() {
        return taskDeadlineViolationStatusData;
    }

    public void visit(Task task) {
        TaskDeadlineViolationStatusEnum status = task.getDeadlineViolationStatus();
        Integer currentValue = taskDeadlineViolationStatusData.get(status);
        taskDeadlineViolationStatusData.put(status, Integer.valueOf(currentValue.intValue() + 1));
    }

    public void visit(TaskGroup taskGroup) {
        for (TaskElement each: taskGroup.getChildren()) {
            each.acceptVisitor(this);
        }
    }

}
