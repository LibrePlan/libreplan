/*
 * This file is part of NavalPlan
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
package org.navalplanner.web.planner.allocation.streches;

import java.util.HashMap;

import org.navalplanner.business.planner.entities.AssignmentFunction;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.StretchesFunction;
import org.navalplanner.business.planner.entities.StretchesFunctionTypeEnum;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.planner.allocation.IAssignmentFunctionConfiguration;
import org.navalplanner.web.planner.allocation.streches.StretchesFunctionController.IGraphicGenerator;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.api.Window;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public abstract class StrechesFunctionConfiguration implements
        IAssignmentFunctionConfiguration {

    @Override
    public void goToConfigure() {
        StretchesFunctionController stretchesFunctionController = new StretchesFunctionController(
                getGraphicsGenerators());
        stretchesFunctionController.setTitle(getTitle());
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("stretchesFunctionController", stretchesFunctionController);
        Window window = (Window) Executions.createComponents(
                "/planner/stretches_function.zul",
                getParentOnWhichOpenWindow(), args);
        Util.createBindingsFor(window);
        ResourceAllocation<?> allocation = getAllocation();
        stretchesFunctionController
                .setResourceAllocation(allocation, getType());

        int exitStatus = stretchesFunctionController.showWindow();
        if (exitStatus == Messagebox.OK) {
            getAllocation().setAssignmentFunction(
                    stretchesFunctionController.getAssignmentFunction());
            assignmentFunctionChanged();
        }

    }

    private IGraphicGenerator getGraphicsGenerators() {
        return GraphicForStreches.forType(getType());
    }

    protected abstract StretchesFunctionTypeEnum getType();

    protected abstract boolean getChartsEnabled();

    protected abstract String getTitle();

    protected abstract ResourceAllocation<?> getAllocation();

    protected abstract void assignmentFunctionChanged();

    protected abstract Component getParentOnWhichOpenWindow();

    public abstract String getName();

    @Override
    public boolean isTargetedTo(AssignmentFunction function) {
        if (!(function instanceof StretchesFunction)) {
            return false;
        }
        StretchesFunction s = (StretchesFunction) function;
        return s.getType() == getType();
    }

    @Override
    public void applyOn(ResourceAllocation<?> resourceAllocation) {
        StretchesFunction stretchesFunction = StretchesFunctionModel
                .createDefaultStretchesFunction(resourceAllocation.getTask()
                        .getEndDate());
        resourceAllocation.setAssignmentFunction(stretchesFunction);
    }

}
