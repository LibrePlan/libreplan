package org.navalplanner.web.planner;

import static org.navalplanner.web.I18nHelper._;

import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;

/**
 * A command that opens a window to make the calendar allocation of a task.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CalendarAllocationCommand implements ICalendarAllocationCommand {

    private CalendarAllocationController calendarAllocationController;

    public CalendarAllocationCommand() {
    }

    @Override
    public void doAction(IContextWithPlannerTask<TaskElement> context,
            TaskElement task) {
        if (task instanceof Task) {
            this.calendarAllocationController.showWindow((Task) task, context
                    .getTask());
        }
    }

    @Override
    public String getName() {
        return _("Calendar allocation");
    }

    @Override
    public void setCalendarAllocationController(
            CalendarAllocationController calendarAllocationController) {
        this.calendarAllocationController = calendarAllocationController;
    }


}
