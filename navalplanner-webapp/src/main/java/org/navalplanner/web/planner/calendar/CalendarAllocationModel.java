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

package org.navalplanner.web.planner.calendar;

import java.util.List;

import org.navalplanner.business.calendars.daos.IBaseCalendarDAO;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.planner.entities.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to calendar allocation popup.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CalendarAllocationModel implements ICalendarAllocationModel {

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    private Task task;

    @Override
    @Transactional(readOnly = true)
    public List<BaseCalendar> getBaseCalendars() {
        return baseCalendarDAO.getBaseCalendars();
    }

    @Override
    public void setTask(Task task) {
        this.task = task;
    }

    @Override
    public void confirmAssignCalendar(BaseCalendar calendar) {
        task.setCalendar(calendar);
    }

    @Override
    public BaseCalendar getAssignedCalendar() {
        return task.getCalendar();
    }

    @Override
    public void cancel() {
        task = null;
    }

}
