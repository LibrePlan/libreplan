package org.navalplanner.web.planner;

import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.TaskEditFormComposer;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;

/**
 * Command to edit a {@link TaskElement}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EditTaskCommand implements IEditTaskCommand {

    private TaskEditFormComposer taskEditFormComposer;

    @Override
    public void doAction(IContextWithPlannerTask<TaskElement> context,
            TaskElement task) {
        taskEditFormComposer.showEditFormFor(context.getRelativeTo(), context
                .getTask());
    }

    @Override
    public String getName() {
        return "Edit";
    }

    @Override
    public void setTaskEditFormComposer(
            TaskEditFormComposer taskEditFormComposer) {
        this.taskEditFormComposer = taskEditFormComposer;
    }

}
