/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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
package org.libreplan.web.planner.adaptplanning;

import static org.libreplan.web.I18nHelper._;

import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.extensions.IContext;

/**
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AdaptPlanningCommand implements IAdaptPlanningCommand {

    private PlanningState planningState;

    @Override
    public String getName() {
        return _("Adapt planning acording to timesheets");
    }

    @Override
    public void doAction(IContext<TaskElement> context) {
        // TODO Auto-generated method stub
    }

    @Override
    public String getImage() {
        return "/common/img/ico_adapt_planning.png";
    }

    @Override
    public boolean isDisabled() {
        return false;
    }

    @Override
    public void setState(PlanningState planningState) {
        this.planningState = planningState;
    }

    @Override
    public boolean isPlannerCommand() {
        return true;
    }

}