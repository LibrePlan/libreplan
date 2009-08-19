package org.navalplanner.web.calendars;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.BaseCalendar.DayType;
import org.navalplanner.business.calendars.entities.BaseCalendar.Days;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions over a {@link BaseCalendar}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class BaseCalendarCRUDController extends GenericForwardComposer {

    private IBaseCalendarModel baseCalendarModel;

    private Window listWindow;

    private Window createWindow;

    private Window editWindow;

    private Window confirmRemove;

    private boolean confirmingRemove = false;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private HoursPerDayRenderer hoursPerDayRenderer = new HoursPerDayRenderer();

    public List<BaseCalendar> getBaseCalendars() {
        return baseCalendarModel.getBaseCalendars();
    }

    public BaseCalendar getBaseCalendar() {
        return baseCalendarModel.getBaseCalendar();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setVariable("controller", this, true);
        getVisibility().showOnly(listWindow);
    }

    public void cancel() {
        baseCalendarModel.cancel();
        goToList();
    }

    public void goToList() {
        Util.reloadBindings(listWindow);
        getVisibility().showOnly(listWindow);
    }

    public void goToEditForm(BaseCalendar baseCalendar) {
        baseCalendarModel.initEdit(baseCalendar);
        selectDay(new Date());
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    public void save() {
        try {
            baseCalendarModel.confirmSave();
            messagesForUser.showMessage(Level.INFO, "base calendar saved");
            goToList();
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
    }

    public void confirmRemove(BaseCalendar baseCalendar) {
        baseCalendarModel.initRemove(baseCalendar);
        showConfirmingWindow();
    }

    public void cancelRemove() {
        confirmingRemove = false;
        baseCalendarModel.cancel();
        confirmRemove.setVisible(false);
        Util.reloadBindings(confirmRemove);
    }

    public boolean isConfirmingRemove() {
        return confirmingRemove;
    }

    private void hideConfirmingWindow() {
        confirmingRemove = false;
        Util.reloadBindings(confirmRemove);
    }

    private void showConfirmingWindow() {
        confirmingRemove = true;
        try {
            Util.reloadBindings(confirmRemove);
            confirmRemove.doModal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void remove() {
        String name = baseCalendarModel.getBaseCalendar().getName();
        baseCalendarModel.confirmRemove();
        hideConfirmingWindow();
        Util.reloadBindings(listWindow);
        messagesForUser.showMessage(Level.INFO, "removed " + name);
    }

    public void goToCreateForm() {
        baseCalendarModel.initCreate();
        selectDay(new Date());
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    private OnlyOneVisible getVisibility() {
        if (visibility == null) {
            visibility = new OnlyOneVisible(listWindow, createWindow,
                    editWindow);
        }
        return visibility;
    }

    public void selectDay(Date date) {
        baseCalendarModel.selectDay(date);

        reloadCurrentWindow();
    }

    public String getTypeOfDay() {
        DayType typeOfDay = baseCalendarModel.getTypeOfDay();
        if (typeOfDay == null) {
            return "";
        }

        switch (typeOfDay) {
        case ANCESTOR_EXCEPTION:
            return "Derived excpetion";
        case OWN_EXCEPTION:
            return "Exception";
        case ZERO_HOURS:
            return "Not working day";
        case NORMAL:
        default:
            return "Normal";
        }
    }

    public Integer getHoursOfDay() {
        return baseCalendarModel.getHoursOfDay();
    }

    public void createException(Integer hours) {
        // TODO check hours parameter is >= 0
        baseCalendarModel.createException(hours);

        reloadCurrentWindow();
    }

    public List<Days> getHoursPerDay() {
        return Arrays.asList(Days.values());
    }

    public boolean isNotExceptional() {
        return !baseCalendarModel.isExceptional();
    }

    public void removeException() {
        baseCalendarModel.removeException();

        reloadCurrentWindow();
    }

    public HoursPerDayRenderer getHoursPerDayRenderer() {
        return hoursPerDayRenderer;
    }

    public class HoursPerDayRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            final Days day = (Days) data;

            Listcell labelListcell = new Listcell();
            labelListcell.appendChild(new Label(day.toString()));
            item.appendChild(labelListcell);

            Listcell hoursListcell = new Listcell();
            Intbox hoursIntbox = Util.bind(new Intbox(),
                    new Util.Getter<Integer>() {

                        @Override
                        public Integer get() {
                            return baseCalendarModel.getHours(day);
                        }
                    }, new Util.Setter<Integer>() {

                        @Override
                        public void set(Integer value) {
                            baseCalendarModel.setHours(day,
                                    value);
                        }
                    });
            hoursIntbox.addEventListener(Events.ON_CHANGE, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    reloadCurrentWindow();
                }

            });
            hoursListcell.appendChild(hoursIntbox);
            item.appendChild(hoursListcell);

            if (baseCalendarModel.isDerived()) {
                Listcell defaultListcell = new Listcell();
                defaultListcell.appendChild(Util.bind(new Checkbox(),
                        new Util.Getter<Boolean>() {

                            @Override
                            public Boolean get() {
                                return baseCalendarModel.isDefault(day);
                            }
                        }, new Util.Setter<Boolean>() {

                            @Override
                            public void set(Boolean value) {
                                baseCalendarModel.setDefault(day);
                            }
                        }));
                item.appendChild(defaultListcell);
            }
        }

    }

    private void reloadCurrentWindow() {
        if (baseCalendarModel.isEditing()) {
            Util.reloadBindings(editWindow);
        } else {
            Util.reloadBindings(createWindow);
        }
    }

}
