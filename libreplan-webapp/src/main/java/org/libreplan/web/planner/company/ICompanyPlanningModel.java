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

package org.libreplan.web.planner.company;

import java.util.Collection;

import org.joda.time.LocalDate;
import org.libreplan.business.common.entities.ProgressType;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.templates.entities.OrderTemplate;
import org.libreplan.web.planner.tabs.MultipleTabsPlannerController;
import org.zkoss.ganttz.IPredicate;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.extensions.ICommandOnTask;

/**
 * Contract for {@link CompanyPlanningModel}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface ICompanyPlanningModel {

    public void setConfigurationToPlanner(Planner planner,
            Collection<ICommandOnTask<TaskElement>> additional,
            ICommandOnTask<TaskElement> doubleClickCommand, IPredicate predicate);

    public void setTabsController(MultipleTabsPlannerController tabsController);

    LocalDate getFilterStartDate();

    LocalDate getFilterFinishDate();

    ProgressType getProgressTypeFromConfiguration();

    public IPredicate getDefaultPredicate(Boolean includeOrderElements);

}
