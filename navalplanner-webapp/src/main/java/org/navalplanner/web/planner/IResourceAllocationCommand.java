package org.navalplanner.web.planner;

import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.web.planner.allocation.ResourceAllocationCommand;
import org.navalplanner.web.planner.allocation.ResourceAllocationController;
import org.zkoss.ganttz.extensions.ICommandOnTask;

/**
 * Contract for {@link ResourceAllocationCommand}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IResourceAllocationCommand extends ICommandOnTask<TaskElement> {

    void setResourceAllocationController(
            ResourceAllocationController resourceAllocationController);

}
