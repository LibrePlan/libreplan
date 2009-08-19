package org.navalplanner.web.planner;

import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;

import static org.navalplanner.web.I18nHelper._;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MergeTaskCommand implements IMergeTaskCommand {

    private PlanningState planningState;

    @Override
    public void setState(PlanningState planningState) {
        this.planningState = planningState;
    }

    @Override
    public void doAction(IContextWithPlannerTask<TaskElement> context,
            TaskElement task) {
        if (!(task instanceof TaskGroup)) {
            return;
        }
        TaskGroup old = (TaskGroup) task;
        if (!old.canBeMerged())
            return;
        Task result = old.merge();
        context.replace(old, result);
        planningState.removed(old);
        planningState.added(result);
    }

    @Override
    public String getName() {
        return _("Merge");
    }

}
