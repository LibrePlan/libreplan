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

package org.libreplan.web.planner.order;

import java.util.List;

import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.web.common.ViewSwitcher;
import org.libreplan.web.planner.advances.AdvanceAssignmentPlanningController;
import org.libreplan.web.planner.calendar.CalendarAllocationController;
import org.libreplan.web.planner.consolidations.AdvanceConsolidationController;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.libreplan.web.planner.taskedition.AdvancedAllocationTaskController;
import org.libreplan.web.planner.taskedition.EditTaskController;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.extensions.ICommand;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IOrderPlanningModel {

    void setConfigurationToPlanner(Planner planner, Order order,
            ViewSwitcher viewSwitcher,
            EditTaskController editTaskController,
            AdvancedAllocationTaskController advancedAllocationTaskController,
            AdvanceAssignmentPlanningController advanceAssignmentPlanningController,
            AdvanceConsolidationController advanceConsolidationController,
            CalendarAllocationController calendarAllocationController,
            List<ICommand<TaskElement>> additional);

    Order getOrder();

    PlanningState getPlanningState();

    void forceLoadLabelsAndCriterionRequirements();

}
