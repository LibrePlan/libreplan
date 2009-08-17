package org.navalplanner.business.planner.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderLine;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskGroup extends TaskElement {

    public static TaskGroup create() {
        TaskGroup taskGroup = new TaskGroup();
        taskGroup.setNewObject(true);
        return taskGroup;
    }

    private List<TaskElement> taskElements = new ArrayList<TaskElement>();

    /**
     * Constructor for hibernate. Do not use!
     */
    public TaskGroup() {

    }

    public void addTaskElement(TaskElement task) {
        Validate.notNull(task);
        task.setParent(this);
        taskElements.add(task);
    }

    @Override
    public List<TaskElement> getChildren() {
        return Collections.unmodifiableList(taskElements);
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public Integer defaultWorkHours() {
        return getOrderElement().getWorkHours();
    }

    public void remove(TaskElement taskElement) {
        taskElements.remove(taskElement);
    }

    public boolean canBeMerged() {
        return isAssociatedWithAnOrderLine() && !taskElements.isEmpty()
                && allSubTaskGroupsCanBeMerged()
                && allChildrenHaveTheSameHoursGroup()
                && sumOfHoursIsEqualToWorkingHours();
    }

    private boolean allSubTaskGroupsCanBeMerged() {
        for (TaskElement t : taskElements) {
            if (t instanceof TaskGroup) {
                TaskGroup group = (TaskGroup) t;
                if (!group.canBeMerged())
                    return false;
            }
        }
        return true;
    }

    private boolean sumOfHoursIsEqualToWorkingHours() {
        int sum = 0;
        for (TaskElement taskElement : taskElements) {
            sum += taskElement.getWorkHours();
        }
        return sum == getWorkHours();
    }

    private boolean allChildrenHaveTheSameHoursGroup() {
        HoursGroup hoursGroup = null;
        for (TaskElement taskElement : taskElements) {
            HoursGroup current = getHoursGroupFor(taskElement);
            if (current == null)
                return false;
            if (hoursGroup == null)
                hoursGroup = current;
            if (!current.equals(hoursGroup)) {
                return false;
            }
        }
        return true;
    }

    private HoursGroup getHoursGroupFor(TaskElement taskElement) {
        if (taskElement instanceof Task) {
            Task t = (Task) taskElement;
            return t.getHoursGroup();
        }
        return ((TaskGroup) taskElement).inferHoursGroupFromChildren();
    }

    private boolean isAssociatedWithAnOrderLine() {
        return getOrderElement() instanceof OrderLine;
    }

    public Task merge() {
        if (!canBeMerged())
            throw new IllegalStateException(
                    "merge must not be called on a TaskGroup such canBeMerged returns false");
        HoursGroup hoursGroup = inferHoursGroupFromChildren();
        Task result = Task.createTask(hoursGroup);
        result.copyPropertiesFrom(this);
        result.shareOfHours = this.shareOfHours;
        copyDependenciesTo(result);
        copyParenTo(result);
        return result;
    }

    private HoursGroup inferHoursGroupFromChildren() {
        TaskElement taskElement = getChildren().get(0);
        if (taskElement instanceof Task) {
            Task t = (Task) taskElement;
            return t.getHoursGroup();
        } else {
            TaskGroup group = (TaskGroup) taskElement;
            return group.inferHoursGroupFromChildren();
        }
    }

}
