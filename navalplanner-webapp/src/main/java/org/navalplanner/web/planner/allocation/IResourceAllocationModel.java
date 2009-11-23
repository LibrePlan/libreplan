/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.planner.allocation;

import java.util.List;
import java.util.Set;

import org.navalplanner.business.orders.entities.AggregatedHoursGroup;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.planner.order.PlanningState;

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
    void accept();

    /**
     * Starts the use case
     * @param task
     * @param ganttTask
     * @param planningState
     */
    ResourceAllocationsBeingEdited initAllocationsFor(Task task,
            org.zkoss.ganttz.data.Task ganttTask,
            PlanningState planningState);

    void accept(AllocationResult modifiedAllocationResult);

    List<AggregatedHoursGroup> getHoursAggregatedByCriterions();

    Integer getOrderHours();

}
