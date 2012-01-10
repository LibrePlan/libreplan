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

package org.libreplan.web.planner.advances;

import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;

/**
 * Contract for {@link Task}.
 * @author Susana Montes Pedreira<smontes@wirelessgalicia.com>
 */
public interface IAdvanceAssignmentPlanningModel {

    public interface IAdvanceAssignmentPlanningContext<T> {
        public T doInsideTransaction();
    }

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
    void initAdvancesFor(TaskElement task,
            IContextWithPlannerTask<TaskElement> context,
            PlanningState planningState);

    public OrderElement getOrderElement();
}