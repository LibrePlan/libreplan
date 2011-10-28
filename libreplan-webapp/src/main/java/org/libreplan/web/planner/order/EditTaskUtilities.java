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

import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.calendars.entities.CalendarData;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.daos.ITaskSourceDAO;
import org.libreplan.business.planner.entities.SubcontractedTaskData;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Common functions to make needed reattachments for edit a {@link TaskElement}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EditTaskUtilities implements IEditTaskUtilities {

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    @Override
    @Transactional(readOnly = true)
    public void reattach(TaskElement taskElement) {
        if (taskElement.getTaskSource() != null) {
            taskSourceDAO.reattach(taskElement.getTaskSource());
        }

        taskElementDAO.reattach(taskElement);
        if (taskElement instanceof Task) {
            forceLoadHoursGroup((Task) taskElement);
            if (taskElement.isSubcontracted()) {
                forceLoadExternalCompany(((Task) taskElement)
                        .getSubcontractedTaskData());
            }
        }
        reattachAndLoadCalendar(taskElement);
    }

    private void forceLoadHoursGroup(Task task) {
        task.getHoursGroup();
    }

    private void forceLoadExternalCompany(
            SubcontractedTaskData subcontractedTaskData) {
        subcontractedTaskData.getExternalCompany().getName();
    }

    private void reattachAndLoadCalendar(TaskElement taskElement) {
        BaseCalendar calendar = taskElement.getCalendar();
        if (calendar == null) {
            return;
        }
        baseCalendarDAO.reattach(calendar);
        forceLoadCalendar(calendar);
        calendar.getCapacityOn(
                PartialDay.wholeDay(taskElement.getIntraDayStartDate()
                        .getDate()));
    }

    private void forceLoadCalendar(BaseCalendar baseCalendar) {
        for (CalendarData calendarData : baseCalendar.getCalendarDataVersions()) {
            calendarData.getHoursPerDay().size();
            if (calendarData.getParent() != null) {
                forceLoadCalendar(calendarData.getParent());
            }
        }
        baseCalendar.getExceptions().size();
    }

}
