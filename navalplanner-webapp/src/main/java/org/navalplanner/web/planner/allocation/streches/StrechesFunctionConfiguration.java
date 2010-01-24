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
package org.navalplanner.web.planner.allocation.streches;

import java.util.HashMap;

import org.navalplanner.business.planner.entities.AssignmentFunction;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.StretchesFunction;
import org.navalplanner.business.planner.entities.StretchesFunction.Type;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.planner.allocation.IAssignmentFunctionConfiguration;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.api.Window;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public abstract class StrechesFunctionConfiguration implements
        IAssignmentFunctionConfiguration {

    @Override
    public void goToConfigure() {
        StretchesFunctionController stretchesFunctionController = new StretchesFunctionController();
        stretchesFunctionController.setTitle(getTitle());
        stretchesFunctionController.setChartsEnabled(getChartsEnabled());
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("stretchesFunctionController", stretchesFunctionController);
        Window window = (Window) Executions.createComponents(
                "/planner/stretches_function.zul",
                getParentOnWhichOpenWindow(), args);
        Util.createBindingsFor(window);
        stretchesFunctionController.setResourceAllocation(getAllocation());
        stretchesFunctionController.showWindow();
        getAllocation().setAssignmentFunction(
                stretchesFunctionController.getAssignmentFunction());
        assignmentFunctionChanged();
    }

    protected abstract Type getType();

    protected abstract boolean getChartsEnabled();

    protected abstract String getTitle();

    protected abstract ResourceAllocation<?> getAllocation();

    protected abstract void assignmentFunctionChanged();

    protected abstract Component getParentOnWhichOpenWindow();

    public abstract String getName();

    @Override
    public boolean isTargetedTo(AssignmentFunction function) {
        return function instanceof StretchesFunction;
    }

    @Override
    public void applyDefaultFunction(ResourceAllocation<?> resourceAllocation) {
        StretchesFunction stretchesFunction = StretchesFunctionModel
                .createDefaultStretchesFunction(resourceAllocation.getTask()
                        .getEndDate(), getType());
        resourceAllocation.setAssignmentFunction(stretchesFunction);
    }

}
