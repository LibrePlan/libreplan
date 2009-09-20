package org.navalplanner.web.planner.allocation;

import static org.navalplanner.web.I18nHelper._;

import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.web.planner.IResourceAllocationCommand;
import org.navalplanner.web.planner.PlanningState;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;

/**
 * A command that opens a window to make the resource allocation of a task.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourceAllocationCommand implements IResourceAllocationCommand {

    private ResourceAllocationController resourceAllocationController;
    private PlanningState planningState;

    public ResourceAllocationCommand() {
    }

    @Override
    public void doAction(IContextWithPlannerTask<TaskElement> context,
            TaskElement task) {
        if (task instanceof Task) {
            this.resourceAllocationController.showWindow((Task) task, context
                    .getTask(), planningState);
        }
    }

    @Override
    public String getName() {
        return _("Resource allocation");
    }

    @Override
    public void initialize(
            ResourceAllocationController resourceAllocationController,
            PlanningState planningState) {
        this.resourceAllocationController = resourceAllocationController;
        this.planningState = planningState;
    }


}
