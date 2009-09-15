package org.navalplanner.web.planner.allocation;

import java.util.List;
import java.util.Set;

import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Worker;

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
     * Returns the {@link List} of {@link AllocationDTO} in the current state of
     * the conversation.
     * @return a {@link List} of {@link AllocationDTO}
     */
    List<AllocationDTO> getAllocations();

    /**
     * Gets the current {@link Task} object.
     *
     * @return A {@link Task}
     */
    Task getTask();

    /**
     * Removes the {@link ResourceAllocation} from the current {@link Task}.
     *
     * @param resourceAllocation
     *            The object to be removed
     */
    void removeResourceAllocation(ResourceAllocation resourceAllocation);

    /**
     * Removes {@link SpecificResourceAllocation} from current allocations list
     * @param data
     */
    void removeSpecificResourceAllocation(SpecificAllocationDTO data);

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
     */
    void initAllocationsFor(Task task, org.zkoss.ganttz.data.Task ganttTask);

}
