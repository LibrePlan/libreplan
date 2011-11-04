package org.libreplan.business.planner.entities.visitors;

import java.util.HashMap;
import java.util.Map;

import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.planner.entities.TaskStatusEnum;
import org.libreplan.business.util.Visitor;

public class AccumulateTasksStatusVisitor extends Visitor {

    private Map<TaskStatusEnum, Integer> taskStatusData;

    public AccumulateTasksStatusVisitor() {
        this.taskStatusData = new HashMap<TaskStatusEnum, Integer>();
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
        for(TaskElement each: taskGroup.getAllChildren()) {
            each.acceptVisitor(this);
        }
    }

}
