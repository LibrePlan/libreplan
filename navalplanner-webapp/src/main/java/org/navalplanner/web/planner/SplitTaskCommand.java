package org.navalplanner.web.planner;

import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SplitTaskCommand implements ISplitTaskCommand {

    private PlanningState planningState;

    @Override
    public void setState(PlanningState planningState) {
        this.planningState = planningState;
    }

    @Override
    public void doAction(IContextWithPlannerTask<TaskElement> context,
            TaskElement taskElement) {
        if (!taskElement.isLeaf()) {
            // TODO show some message if this action is not aplyable
            return;
        }
        Task task = (Task) taskElement;
        TaskGroup newGroup = task.split(createTwoEqualShares(taskElement));
        context.replace(task, newGroup);
        planningState.removed(taskElement);
        planningState.added(newGroup);
    }

    private int[] createTwoEqualShares(TaskElement taskElement) {
        Integer workHours = taskElement.getWorkHours();
        int half = workHours / 2;
        return new int[] { half, half + workHours % 2 };
    }

    @Override
    public String getName() {
        return "Split task";
    }

}
