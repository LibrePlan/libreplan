package org.navalplanner.web.planner;

import java.util.Set;

import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Worker;

/**
 * Contract for {@link Task}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IResourceAllocationModel {

    /**
     * Gets the current {@link Task} object.
     *
     * @return A {@link Task}
     */
    Task getTask();

    /**
     * Adds a new {@link ResourceAllocation} to the current {@link Task}.
     */
    void addResourceAllocation();

    /**
     * Removes the {@link ResourceAllocation} from the current {@link Task}.
     *
     * @param resourceAllocation
     *            The object to be removed
     */
    void removeResourceAllocation(ResourceAllocation resourceAllocation);

    /**
     * Tries to find a {@link Worker} with the specified NIF.
     *
     * @param nif
     *            The NIF to search the {@link Worker}
     * @return The {@link Worker} with this NIF or <code>null</code> if it's not
     *         found
     */
    Worker findWorkerByNif(String nif);

    /**
     * Relates a {@link Worker} and a {@link Task} through a
     * {@link SpecificResourceAllocation}.
     *
     * @param resourceAllocation
     *            A {@link SpecificResourceAllocation} to set the {@link Worker}
     * @param worker
     *            A {@link Worker} for the {@link SpecificResourceAllocation}
     */
    void setWorker(SpecificResourceAllocation resourceAllocation, Worker worker);

    /**
     * Sets the current {@link Task}, where the user is allocating resources.
     *
     * @param task
     *            A {@link Task}
     */
    void setTask(Task task);

    /**
     * Gets the {@link Set} of {@link Criterion} of the current task.
     *
     * @return A {@link Set} of {@link Criterion}
     */
    Set<Criterion> getCriterions();

    /**
     * Gets the {@link Set} of {@link ResourceAllocation} of the current task.
     *
     * @return A {@link Set} of {@link ResourceAllocation}
     */
    Set<ResourceAllocation> getResourceAllocations();

    /**
     * Sets the current {@link ResourceAllocation} to be rendered.
     *
     * @param resourceAllocation
     *            The current {@link ResourceAllocation}
     */
    void setResourceAllocation(ResourceAllocation resourceAllocation);

    /**
     * Gets the {@link Worker} of the current {@link ResourceAllocation}.
     *
     * @return A {@link Worker}
     */
    Worker getWorker();

    /**
     * Checks if the {@link Worker} of the current {@link ResourceAllocation}
     * satisfies the {@link Criterion} of the current {@link Task}.
     *
     * @return True if the {@link Worker} satisfies the {@link Criterion}
     *         required. Or if the current {@link Worker} is <code>null</code>.
     *         Or if the {@link Criterion} list is empty.
     */
    boolean workerSatisfiesCriterions();

    /**
     * Sets the current Gantt {@link org.zkoss.ganttz.data.Task ganttTask},
     * where the user is allocating resources.
     *
     * @param ganttTask
     */
    void setGanttTask(org.zkoss.ganttz.data.Task ganttTask);

    /**
     * Update the duration of the current Gantt
     * {@link org.zkoss.ganttz.data.Task ganttTask}, depending on the resources
     * assigned and the dedication.
     *
     * @param ganttTask
     */
    void updateGanttTaskDuration();

    /**
     * Adds {@link SpecificResourceAllocation} to {@link Task}
     *
     * @param worker
     */
    void addSpecificResourceAllocation(Worker worker);

}
