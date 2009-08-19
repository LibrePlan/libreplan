package org.navalplanner.web.planner;

import org.navalplanner.business.planner.daos.ITaskElementDAO;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;

import static org.navalplanner.web.I18nHelper._;

/**
 * Command to edit a {@link TaskElement}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EditTaskCommand implements IEditTaskCommand {

    @Autowired
    private ITaskElementDAO taskElementDAO;

    private EditTaskController editTaskController;

    @Override
    @Transactional(readOnly = true)
    public void doAction(IContextWithPlannerTask<TaskElement> context,
            TaskElement taskElement) {

        taskElementDAO.save(taskElement);
        if (taskElement instanceof Task) {
            forceLoadHoursGroup((Task) taskElement);
        }

        editTaskController.showEditFormFor(context.getRelativeTo(), context
                .getTask(), taskElement);
    }

    private void forceLoadHoursGroup(Task task) {
        task.getHoursGroup();
    }

    @Override
    public String getName() {
        return _("Edit");
    }

    @Override
    public void setEditTaskController(EditTaskController editTaskController) {
        this.editTaskController = editTaskController;
    }

}
