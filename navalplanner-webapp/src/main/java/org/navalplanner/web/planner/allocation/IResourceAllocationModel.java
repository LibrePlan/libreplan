package org.navalplanner.web.planner.allocation;

import java.util.Set;

import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.planner.PlanningState;

/**
 * Contract for {@link Task}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public interface IResourceAllocationModel {

    /**
     * Adds {@link SpecificResourceAllocation} to {@link Task}
     * {@link ResourceAllocation} list
     *
     * If a {@link SpecificResourceAllocation} satisfies {@link Task} criterions
     * one of the {@link GenericResourceAllocation} assigned to
     * {@link ResourceAllocation} is removed (in case any exists)
     *
     */
    void addSpecificResourceAllocation(Worker worker) throws Exception;

    /**
     * Returns {@link Set} of {@link Criterion} of the current {@link Task}
     *
     * @return
     */
    Set<Criterion> getCriterions();

    /**
     * Gets the current {@link Task} object.
     *
     * @return A {@link Task}
     */
    Task getTask();

    /**
     * Cancel operation
     */
    void cancel();

    /**
     * Save task
     */
    void save();

    /**
     * Starts the use case
     * @param task
     * @param ganttTask
     * @param planningState
     */
    ResourceAllocationsBeingEdited initAllocationsFor(Task task,
            org.zkoss.ganttz.data.Task ganttTask,
            PlanningState planningState);

}
