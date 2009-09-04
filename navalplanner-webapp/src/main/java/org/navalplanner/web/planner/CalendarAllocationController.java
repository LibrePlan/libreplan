package org.navalplanner.web.planner;

import java.util.List;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.web.common.Util;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Window;

/**
 * Controller for allocate one calendar to a task view.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@org.springframework.stereotype.Component("calendarAllocationController")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CalendarAllocationController extends GenericForwardComposer {

    private ICalendarAllocationModel calendarAllocationModel;

    private Window window;

    private Combobox calendarCombo;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        window = (Window) comp;
    }

    public void showWindow(Task task, org.zkoss.ganttz.data.Task task2) {
        calendarAllocationModel.setTask(task);

        calendarCombo = (Combobox) window.getFellow("calendarCombo");
        fillCalendarComboAndMarkSelected();

        try {
            Util.reloadBindings(window);
            window.doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillCalendarComboAndMarkSelected() {
        calendarCombo.getChildren().clear();
        BaseCalendar assignedCalendar = calendarAllocationModel
                .getAssignedCalendar();

        List<BaseCalendar> calendars = calendarAllocationModel
                .getBaseCalendars();
        for (BaseCalendar calendar : calendars) {
            Comboitem item = new org.zkoss.zul.Comboitem(calendar.getName());
            item.setValue(calendar);
            calendarCombo.appendChild(item);
            if ((assignedCalendar != null)
                    && calendar.getId().equals(assignedCalendar.getId())) {
                calendarCombo.setSelectedItem(item);
            }
        }
    }

    public void assign(Comboitem comboitem) {
        BaseCalendar calendar = (BaseCalendar) comboitem.getValue();
        calendarAllocationModel.confirmAssignCalendar(calendar);
        window.setVisible(false);
    }

    public void cancel() {
        calendarAllocationModel.cancel();
        window.setVisible(false);
    }

    public BaseCalendar getAssignedCalendar() {
        return calendarAllocationModel.getAssignedCalendar();
    }

}