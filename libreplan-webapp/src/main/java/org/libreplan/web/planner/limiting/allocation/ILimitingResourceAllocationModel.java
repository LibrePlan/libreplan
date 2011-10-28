/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.web.planner.limiting.allocation;

import java.util.List;

import org.libreplan.business.orders.entities.AggregatedHoursGroup;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.web.planner.allocation.INewAllocationsAdder;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;

/**
 * Contract for {@link Task}.
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
public interface ILimitingResourceAllocationModel extends INewAllocationsAdder {

    void confirmSave();

    List<AggregatedHoursGroup> getHoursAggregatedByCriteria();

    Integer getOrderHours();

    List<LimitingAllocationRow> getResourceAllocationRows();

    void init(IContextWithPlannerTask<TaskElement> context, Task task, PlanningState planningState);

    void setLimitingResourceAllocationController(
            LimitingResourceAllocationController limitingResourceAllocationController);

}