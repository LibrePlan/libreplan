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
package org.libreplan.web.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.libreplan.web.planner.allocation.AdvancedAllocationController;
import org.libreplan.web.planner.allocation.AllocationResult;
import org.libreplan.web.planner.allocation.AdvancedAllocationController.AllocationInput;
import org.libreplan.web.planner.allocation.AdvancedAllocationController.IAdvanceAllocationResultReceiver;
import org.libreplan.web.planner.allocation.AdvancedAllocationController.IBack;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.util.Composer;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ViewSwitcher implements Composer {

    private org.zkoss.zk.ui.Component parent;

    private IChildrenSnapshot planningOrder;

    private boolean isInPlanningOrder = false;

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) {
        this.parent = comp;
        isInPlanningOrder = true;
    }

    public void goToAdvancedAllocation(AllocationResult allocationResult,
            IAdvanceAllocationResultReceiver resultReceiver) {
        planningOrder = ComponentsReplacer.replaceAllChildren(parent,
                "advance_allocation.zul", createArgsForAdvancedAllocation(
                        allocationResult, resultReceiver));
        isInPlanningOrder = false;
    }

    private Map<String, Object> createArgsForAdvancedAllocation(
            AllocationResult allocationResult,
            IAdvanceAllocationResultReceiver resultReceiver) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("advancedAllocationController",
                new AdvancedAllocationController(createBack(),
                        asAllocationInput(allocationResult, resultReceiver)));
        return result;
    }

    private IBack createBack() {
        return new IBack() {
            @Override
            public void goBack() {
                goToPlanningOrderView();
            }

            @Override
            public boolean isAdvanceAssignmentOfSingleTask() {
                return true;
            }

        };
    }

    private List<AllocationInput> asAllocationInput(
            AllocationResult allocationResult,
            IAdvanceAllocationResultReceiver resultReceiver) {
        return Collections.singletonList(new AllocationInput(allocationResult
                .getAggregate(), allocationResult.getTask(), resultReceiver));
    }

    public void goToPlanningOrderView() {
        if (isInPlanningOrder) {
            return;
        }
        planningOrder.restore();
        isInPlanningOrder = true;
    }
}
