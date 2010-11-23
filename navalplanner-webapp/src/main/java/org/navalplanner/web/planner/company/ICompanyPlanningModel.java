/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.planner.company;

import java.util.Collection;

import org.joda.time.LocalDate;
import org.navalplanner.business.common.entities.ProgressType;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.templates.entities.OrderTemplate;
import org.navalplanner.web.planner.tabs.MultipleTabsPlannerController;
import org.zkoss.ganttz.IPredicate;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.extensions.ICommandOnTask;

/**
 * Contract for {@link CompanyPlanningModel}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface ICompanyPlanningModel {

    void setConfigurationToPlanner(final Planner planner,
            Collection<ICommandOnTask<TaskElement>> additional);

    public void setConfigurationToPlanner(Planner planner,
            Collection<ICommandOnTask<TaskElement>> additional,
            ICommandOnTask<TaskElement> doubleClickCommand);

    public void setConfigurationToPlanner(Planner planner,
            Collection<ICommandOnTask<TaskElement>> additional,
            ICommandOnTask<TaskElement> doubleClickCommand, IPredicate predicate);

    public void setTabsController(MultipleTabsPlannerController tabsController);

    LocalDate getFilterStartDate();

    LocalDate getFilterFinishDate();

    void goToCreateOtherOrderFromTemplate(OrderTemplate template);

    ProgressType getProgressTypeFromConfiguration();

}
