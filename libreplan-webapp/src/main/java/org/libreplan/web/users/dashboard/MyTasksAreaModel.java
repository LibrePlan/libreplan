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

package org.libreplan.web.users.dashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.planner.daos.IResourceAllocationDAO;
import org.libreplan.business.planner.entities.SpecificResourceAllocation;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for for "My tasks" area in the user dashboard window
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MyTasksAreaModel implements IMyTasksAreaModel {

    @Autowired
    private IResourceAllocationDAO resourceAllocationDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasks() {
        User user = UserUtil.getUserFromSession();
        if (!user.isBound()) {
            return new ArrayList<Task>();
        }

        List<SpecificResourceAllocation> resourceAllocations = resourceAllocationDAO
                .findSpecificAllocationsRelatedTo(scenarioManager.getCurrent(),
                        UserDashboardUtil.getBoundResourceAsList(user), null,
                        null);

        List<Task> tasks = new ArrayList<Task>();
        for (SpecificResourceAllocation each : resourceAllocations) {
            Task task = each.getTask();
            forceLoad(task);
            tasks.add(task);
        }

        sortTasksDescendingByStartDate(tasks);

        return tasks;
    }

    private void sortTasksDescendingByStartDate(List<Task> tasks) {
        Collections.sort(tasks, new Comparator<Task>() {

            @Override
            public int compare(Task o1, Task o2) {
                return o2.getIntraDayStartDate().compareTo(
                        o1.getIntraDayStartDate());
            }
        });
    }

    private void forceLoad(Task task) {
        task.getName();
        task.getOrderElement().getOrder().getName();
        DirectAdvanceAssignment advanceAssignment = task.getOrderElement()
                .getReportGlobalAdvanceAssignment();
        if (advanceAssignment != null) {
            AdvanceMeasurement advanceMeasurement = advanceAssignment
                    .getLastAdvanceMeasurement();
            if (advanceMeasurement != null) {
                advanceMeasurement.getValue();
            }
        }
    }

}
