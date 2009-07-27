package org.navalplanner.web.planner;

import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.extensions.IContext;

/**
 * A command that opens a window to make the resource allocation of a task.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourceAllocationCommand implements IResourceAllocationCommand {

    private ResourceAllocationController resourceAllocationController;

    public ResourceAllocationCommand() {
    }

    @Override
    public void doAction(IContext<TaskElement> context, TaskElement task) {
        if (task instanceof Task) {
            this.resourceAllocationController.showWindow((Task) task);
        }
    }

    @Override
    public String getName() {
        return "Resource allocation";
    }

    @Override
    public void setResourceAllocationController(
            ResourceAllocationController resourceAllocationController) {
        this.resourceAllocationController = resourceAllocationController;
    }

}
