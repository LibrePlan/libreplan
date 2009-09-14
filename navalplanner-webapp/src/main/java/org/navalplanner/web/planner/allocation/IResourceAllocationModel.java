package org.navalplanner.web.planner.allocation;

import java.math.BigDecimal;
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
     * Adds a new {@link GenericResourceAllocation} to the current {@link Task}
     * if no {@link ResourceAllocation} exist.
     */
    void addGenericResourceAllocationIfNoAllocationExists();

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
     * Returns {@link Set} of {@link GenericResourceAllocation} of current
     *
     * @return
     */
    Set<GenericResourceAllocation> getGenericResourceAllocations();

    /**
     * Returns number of {@link ResourceAllocation} which are candidate to add
     * as {@link GenericResourceAllocation}
     *
     * @return
     */
    int getNumberUnassignedResources();

    /**
     * Returns the {@link Set} of {@link ResourceAllocation} of the current
     * {@link Task}.
     *
     * @return A {@link Set} of {@link ResourceAllocation}
     */
    Set<ResourceAllocation> getResourceAllocations();

    /**
     * Return sum of percentages for current {@link Task}
     * {@link ResourceAllocation}
     *
     * @return
     */
    BigDecimal getSumPercentageResourceAllocations();

    /**
     * Return sum of percentages for current {@link Task}
     * {@link SpecificResourceAllocation}
     *
     * @return
     */
    BigDecimal getSumPercentageSpecificResourceAllocations();

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
     * Removes {@link SpecificResourceAllocation} from current {@link Task}
     * {@link ResourceAllocation} list
     *
     * @param resourceAllocation
     */
    void removeSpecificResourceAllocation(
            SpecificResourceAllocation resourceAllocation);

    /**
     * Sets the current Gantt {@link org.zkoss.ganttz.data.Task ganttTask},
     * where the user is allocating resources.
     *
     * @param ganttTask
     */
    void setGanttTask(org.zkoss.ganttz.data.Task ganttTask);

    /**
     * Sets the current {@link Task}, where the user is allocating resources.
     *
     * @param task
     *            A {@link Task}
     */
    void setTask(Task task);

    /**
     * Update the duration of the current Gantt
     * {@link org.zkoss.ganttz.data.Task ganttTask}, depending on the resources
     * assigned and the dedication.
     *
     * @param ganttTask
     */
    void updateGanttTaskDuration();

    /**
     * Updates {@link GenericResourceAllocation} percentages of current
     * {@link Task}
     *
     * @param totalPercentage
     */
    void updateGenericPercentages(BigDecimal totalPercentage);

    /**
     * Cancel operation
     */
    void cancel();

    /**
     * Save task
     */
    void save();

}
