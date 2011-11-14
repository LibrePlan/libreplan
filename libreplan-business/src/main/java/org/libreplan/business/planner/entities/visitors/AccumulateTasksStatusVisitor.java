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
 * Visits a task graph computing statuses and writing them
 * down in a Map.
 *
 * @author Nacho Barrientos <nacho@igalia.com>
 */
import java.util.EnumMap;
import java.util.Map;

import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.planner.entities.TaskStatusEnum;
import org.libreplan.business.util.TaskElementVisitor;

public class AccumulateTasksStatusVisitor extends TaskElementVisitor {

    private Map<TaskStatusEnum, Integer> taskStatusData;

    public AccumulateTasksStatusVisitor() {
        this.taskStatusData = new EnumMap<TaskStatusEnum, Integer>(TaskStatusEnum.class);
        for (TaskStatusEnum status: TaskStatusEnum.values()) {
            this.taskStatusData.put(status, new Integer(0));
        }
    }

    public Map<TaskStatusEnum, Integer> getTaskStatusData() {
        return taskStatusData;
    }

    public void visit(Task task) {
        TaskStatusEnum status = task.getTaskStatus();
        Integer currentValue = getTaskStatusData().get(status);
        taskStatusData.put(status, currentValue++);
    }

    public void visit(TaskGroup taskGroup) {
        for (TaskElement each: taskGroup.getAllChildren()) {
            each.acceptVisitor(this);
        }
    }

}
