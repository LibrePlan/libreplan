package org.navalplanner.web.planner;

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
