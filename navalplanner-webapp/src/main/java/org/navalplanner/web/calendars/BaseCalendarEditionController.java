package org.navalplanner.web.calendars;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.CalendarData;
import org.navalplanner.business.calendars.entities.BaseCalendar.DayType;
import org.navalplanner.business.calendars.entities.CalendarData.Days;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.CalendarHighlightedDays;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Tab;
import org.zkoss.zul.api.Datebox;
import org.zkoss.zul.api.Window;

/**
 * Controller for edit and create one {@link BaseCalendar}. It's separated of
 * {@link BaseCalendarCRUDController} to be used from other parts of the
 * application.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class BaseCalendarEditionController extends
        GenericForwardComposer {

    private IBaseCalendarModel baseCalendarModel;

    private Window window;

    private Window createNewVersionWindow;

    private HoursPerDayRenderer hoursPerDayRenderer = new HoursPerDayRenderer();

    private HistoryVersionsRenderer historyVersionsRenderer = new HistoryVersionsRenderer();

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private boolean creatingNewVersion = false;

    public BaseCalendarEditionController(IBaseCalendarModel baseCalendarModel,
            Window window, Window createNewVersionWindow) {
        this.baseCalendarModel = baseCalendarModel;
        this.window = window;
        this.createNewVersionWindow = createNewVersionWindow;
    }

    public abstract void goToList();

    public abstract void save();

    public abstract void cancel();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        if (baseCalendarModel.isDerived()) {
            prepareParentCombo();
        }
    }

    private void prepareParentCombo() {
        Combobox parentCalendars = (Combobox) window
                    .getFellow("parentCalendars");

        fillParentComboAndMarkSelectedItem(parentCalendars);
        addListenerParentCombo(parentCalendars);
    }

    private void fillParentComboAndMarkSelectedItem(Combobox parentCalendars) {
        parentCalendars.getChildren().clear();
        BaseCalendar parent = baseCalendarModel.getParent();

        List<BaseCalendar> possibleParentCalendars = getParentCalendars();
        for (BaseCalendar baseCalendar : possibleParentCalendars) {
            Comboitem item = new Comboitem(baseCalendar.getName());
            item.setValue(baseCalendar);
            parentCalendars.appendChild(item);
            if (baseCalendar.getId().equals(parent.getId())) {
                parentCalendars.setSelectedItem(item);
            }
        }
    }

    private void addListenerParentCombo(final Combobox parentCalendars) {
        parentCalendars.addEventListener(Events.ON_SELECT, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                BaseCalendar selected = (BaseCalendar) parentCalendars
                        .getSelectedItem().getValue();
                baseCalendarModel.setParent(selected);
                reloadCurrentWindow();
            }

        });
    }

    public boolean isEditing() {
        return baseCalendarModel.isEditing();
    }

    public BaseCalendar getBaseCalendar() {
        return baseCalendarModel.getBaseCalendar();
    }

    public String getCalendarType() {
        if (baseCalendarModel.isDerived()) {
            return _("Derived");
        }
        return _("Normal");
    }

    public boolean isDerived() {
        return baseCalendarModel.isDerived();
    }

    public boolean isDateValidFromPast() {
        if (!isEditing()) {
            return false;
        }

        Date dateValidFrom = baseCalendarModel.getDateValidFrom();
        if (dateValidFrom != null) {
            return isPast(dateValidFrom);
        }

        return true;
    }

    private boolean isPast(Date date) {
        LocalDate localDate = new LocalDate(date);
        LocalDate currentLocalDate = new LocalDate();
        return localDate.compareTo(currentLocalDate) <= 0;
    }

    public List<Days> getHoursPerDay() {
        return Arrays.asList(Days.values());
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
            final Intbox intBox = new Intbox();
            Intbox hoursIntbox = Util.bind(intBox, new Util.Getter<Integer>() {

                @Override
                public Integer get() {
                    return baseCalendarModel.getHours(day);
                }
            }, new Util.Setter<Integer>() {

                @Override
                public void set(Integer value) {
                    try {
                        baseCalendarModel.setHours(day, value);
                    } catch (IllegalArgumentException e) {
                        throw new WrongValueException(intBox, e.getMessage());
                    }
                }
            });

            hoursIntbox.addEventListener(Events.ON_CHANGE, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    reloadDayInformation();
                }

            });

            if (isDateValidFromPast()) {
                hoursIntbox.setDisabled(true);
            } else if (baseCalendarModel.isDerived()
                    && baseCalendarModel.isDefault(day)) {
                hoursIntbox.setDisabled(true);
            }

            hoursListcell.appendChild(hoursIntbox);
            item.appendChild(hoursListcell);

            if (baseCalendarModel.isDerived()) {
                Listcell defaultListcell = new Listcell();
                Checkbox defaultCheckbox = Util.bind(new Checkbox(),
                        new Util.Getter<Boolean>() {

                            @Override
                            public Boolean get() {
                                return baseCalendarModel.isDefault(day);
                            }
                        }, new Util.Setter<Boolean>() {

                            @Override
                            public void set(Boolean value) {
                                if (value) {
                                    baseCalendarModel.setDefault(day);
                                } else {
                                    baseCalendarModel.unsetDefault(day);
                                }
                            }
                        });
                defaultCheckbox.addEventListener(Events.ON_CHECK,
                        new EventListener() {

                            @Override
                            public void onEvent(Event event) throws Exception {
                                reloadCurrentWindow();
                            }

                        });

                if (isDateValidFromPast()) {
                    defaultCheckbox.setDisabled(true);
                }

                defaultListcell.appendChild(defaultCheckbox);
                item.appendChild(defaultListcell);
            }
        }

    }

    private void reloadCurrentWindow() {
        Util.reloadBindings(window);
        highlightDaysOnCalendar();
    }

    private void reloadDayInformation() {
        Util.reloadBindings(window.getFellow("dayInformation"));
        highlightDaysOnCalendar();
    }

    private void highlightDaysOnCalendar() {
        ((CalendarHighlightedDays) window.getFellow("calendarWidget"))
                .highlightDays();
    }

    public Date getSelectedDay() {
        Date selectedDay = baseCalendarModel.getSelectedDay();
        if (selectedDay == null) {
            return new Date();
        }
        return selectedDay;
    }

    public void setSelectedDay(Date date) {
        baseCalendarModel.setSelectedDay(date);

        reloadDayInformation();
    }

    private Map<DayType, String> getDaysCurrentMonthByType() {
        LocalDate currentDate = new LocalDate(baseCalendarModel
                .getSelectedDay());

        LocalDate minDate = currentDate.dayOfMonth().withMinimumValue();
        LocalDate maxDate = currentDate.dayOfMonth().withMaximumValue();

        List<Integer> ancestorExceptionsDays = new ArrayList<Integer>();
        List<Integer> ownExceptionDays = new ArrayList<Integer>();
        List<Integer> zeroHoursDays = new ArrayList<Integer>();
        List<Integer> normalDays = new ArrayList<Integer>();

        for (LocalDate date = minDate; date.compareTo(maxDate) <= 0; date = date
                .plusDays(1)) {
            DayType typeOfDay = baseCalendarModel.getTypeOfDay(date);
            if (typeOfDay != null) {
                switch (typeOfDay) {
                case ANCESTOR_EXCEPTION:
                    ancestorExceptionsDays.add(date.getDayOfMonth());
                    break;
                case OWN_EXCEPTION:
                    ownExceptionDays.add(date.getDayOfMonth());
                    break;
                case ZERO_HOURS:
                    zeroHoursDays.add(date.getDayOfMonth());
                    break;
                case NORMAL:
                default:
                    normalDays.add(date.getDayOfMonth());
                    break;
                }
            }
        }

        Map<DayType, String> result = new HashMap<DayType, String>();

        result.put(DayType.ANCESTOR_EXCEPTION, StringUtils.join(
                ancestorExceptionsDays, ","));
        result.put(DayType.OWN_EXCEPTION, StringUtils.join(ownExceptionDays,
                ","));
        result.put(DayType.ZERO_HOURS, StringUtils.join(zeroHoursDays, ","));
        result.put(DayType.NORMAL, StringUtils.join(normalDays, ","));

        return result;
    }

    public String getAncestorExceptionDays() {
        return getDaysCurrentMonthByType().get(DayType.ANCESTOR_EXCEPTION);
    }

    public String getOwnExceptionDays() {
        return getDaysCurrentMonthByType().get(DayType.OWN_EXCEPTION);
    }

    public String getZeroHoursDays() {
        return getDaysCurrentMonthByType().get(DayType.ZERO_HOURS);
    }

    public String getTypeOfDay() {
        DayType typeOfDay = baseCalendarModel.getTypeOfDay();
        if (typeOfDay == null) {
            return "";
        }

        switch (typeOfDay) {
        case ANCESTOR_EXCEPTION:
            return _("Derived excpetion");
        case OWN_EXCEPTION:
            return _("Exception");
        case ZERO_HOURS:
            return _("Not working day");
        case NORMAL:
        default:
            return _("Normal");
        }
    }

    public Integer getHoursOfDay() {
        return baseCalendarModel.getHoursOfDay();
    }

    public boolean isSelectedDateFromPast() {
        Date selectedDay = baseCalendarModel.getSelectedDay();
        if (selectedDay != null) {
            return isPast(selectedDay);
        }

        return true;
    }

    public boolean isNotSelectedDateFromPast() {
        return !isSelectedDateFromPast();
    }

    public void createException() {
        Component exceptionHoursIntbox = window.getFellow("exceptionHours");

        Integer hours = ((Intbox) exceptionHoursIntbox).getValue();

        if (hours < 0) {
            throw new WrongValueException(
                    exceptionHoursIntbox,
                    _("Hours for an exception day should be greater or equal than zero"));
        } else {
            Clients.closeErrorBox(exceptionHoursIntbox);
            baseCalendarModel.createException(hours);
            reloadDayInformation();
        }
    }

    public boolean isNotExceptional() {
        return !baseCalendarModel.isExceptional();
    }

    public void removeException() {
        baseCalendarModel.removeException();

        reloadDayInformation();
    }

    public Date getDateValidFrom() {
        return baseCalendarModel.getDateValidFrom();
    }

    public void setDateValidFrom(Date date) {
        Component component = window.getFellow("dateValidFrom");

        try {
            baseCalendarModel.setDateValidFrom(date);
        } catch (IllegalArgumentException e) {
            throw new WrongValueException(component, e.getMessage());
        } catch (UnsupportedOperationException e) {
            throw new WrongValueException(component, e.getMessage());
        }
        Clients.closeErrorBox(component);
        baseCalendarModel.setSelectedDay(date);
        reloadCurrentWindow();
    }

    public Date getExpiringDate() {
        return baseCalendarModel.getExpiringDate();
    }

    public void setExpiringDate(Date date) {
        Component component = window.getFellow("expiringDate");

        try {
            baseCalendarModel.setExpiringDate((new LocalDate(date)).plusDays(1)
                    .toDateTimeAtStartOfDay().toDate());
        } catch (IllegalArgumentException e) {
            throw new WrongValueException(component, e.getMessage());
        } catch (UnsupportedOperationException e) {
            throw new WrongValueException(component, e.getMessage());
        }
        Clients.closeErrorBox(component);
        baseCalendarModel.setSelectedDay(date);
        reloadCurrentWindow();
    }

    public List<CalendarData> getHistoryVersions() {
        return baseCalendarModel.getHistoryVersions();
    }

    public HistoryVersionsRenderer getHistoryVersionsRenderer() {
        return historyVersionsRenderer;
    }

    public class HistoryVersionsRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            CalendarData calendarData = (CalendarData) data;

            Listcell nameListcell = new Listcell();
            nameListcell.appendChild(new Label(baseCalendarModel.getName()));
            item.appendChild(nameListcell);

            Listcell parentListcell = new Listcell();
            Label parentLabel = new Label();
            BaseCalendar parent = calendarData.getParent();
            if (parent != null) {
                parentLabel.setValue(parent.getName());
            }
            parentListcell.appendChild(parentLabel);
            item.appendChild(parentListcell);

            Listcell validFromListcell = new Listcell();
            Label validFromLabel = new Label();
            final LocalDate dateValidFrom = baseCalendarModel
                    .getValidFrom(calendarData);
            if (dateValidFrom != null) {
                validFromLabel.setValue(dateValidFrom.toString());
            }
            validFromListcell.appendChild(validFromLabel);
            item.appendChild(validFromListcell);

            Listcell expiringDateListcell = new Listcell();
            final LocalDate expiringDate = calendarData.getExpiringDate();
            Label expiringDateLabel = new Label();
            if (expiringDate != null) {
                LocalDate date = new LocalDate(expiringDate).minusDays(1);
                expiringDateLabel.setValue(date.toString());
            }
            expiringDateListcell.appendChild(expiringDateLabel);
            item.appendChild(expiringDateListcell);

            Listcell summaryListcell = new Listcell();
            List<String> summary = new ArrayList<String>();
            for (Days day : Days.values()) {
                Integer hours = calendarData.getHours(day);
                if (hours == null) {
                    summary.add("D");
                } else {
                    summary.add(hours.toString());
                }
            }
            summaryListcell.appendChild(new Label(StringUtils.join(summary,
                    " - ")));
            item.appendChild(summaryListcell);

            Listcell buttonListcell = new Listcell();
            Button button = new Button(_("Go to this calendar"));
            button.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    if (dateValidFrom != null) {
                        goToDate(dateValidFrom.toDateTimeAtStartOfDay()
                                .toDate());
                    } else if (expiringDate != null) {
                        goToDate(expiringDate.minusDays(1)
                                .toDateTimeAtStartOfDay().toDate());
                    } else {
                        goToDate(new Date());
                    }
                }
            });
            buttonListcell.appendChild(button);
            item.appendChild(buttonListcell);
        }

    }

    public boolean isLastVersion() {
        return baseCalendarModel.isLastVersion();
    }

    public void goToDate(Date date) {
        setSelectedDay(date);

        ((Tab) window.getFellow("dataTab")).setSelected(true);
        reloadCurrentWindow();
    }

    public boolean isCreatingNewVersion() {
        return creatingNewVersion;
    }

    public void createNewVersion() {
        creatingNewVersion = true;
        try {
            Util.reloadBindings(createNewVersionWindow);
            createNewVersionWindow.doModal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void acceptCreateNewVersion() {
        Component component = createNewVersionWindow
                .getFellow("dateValidFromNewVersion");
        Date date = ((Datebox) component).getValue();

        try {
            baseCalendarModel.createNewVersion(date);
        } catch (IllegalArgumentException e) {
            throw new WrongValueException(component, e.getMessage());
        }

        Clients.closeErrorBox(component);
        creatingNewVersion = false;
        Util.reloadBindings(createNewVersionWindow);
        setSelectedDay(date);
        reloadCurrentWindow();
    }

    public void cancelNewVersion() {
        creatingNewVersion = false;
        Util.reloadBindings(createNewVersionWindow);
    }

    public Date getDateValidFromNewVersion() {
        return (new LocalDate()).plusDays(1).toDateTimeAtStartOfDay().toDate();
    }

    public void setDateValidFromNewVersion(Date date) {
        // Just for ZK binding not needed
    }

    public List<BaseCalendar> getParentCalendars() {
        return baseCalendarModel.getPossibleParentCalendars();
    }

}
