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

package org.navalplanner.web.calendars;

import static org.navalplanner.web.I18nHelper._;
import static org.navalplanner.web.common.Util.findOrCreate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.BaseCalendar.DayType;
import org.navalplanner.business.calendars.entities.CalendarAvailability;
import org.navalplanner.business.calendars.entities.CalendarData;
import org.navalplanner.business.calendars.entities.CalendarData.Days;
import org.navalplanner.business.calendars.entities.CalendarException;
import org.navalplanner.business.calendars.entities.CalendarExceptionType;
import org.navalplanner.business.calendars.entities.Capacity;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.EffortDuration.Granularity;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.Util.Getter;
import org.navalplanner.web.common.Util.ICreation;
import org.navalplanner.web.common.Util.Setter;
import org.navalplanner.web.common.components.CalendarHighlightedDays;
import org.navalplanner.web.common.components.CapacityPicker;
import org.navalplanner.web.common.components.EffortDurationPicker;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;
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

    private static String asString(Capacity capacity) {
        String extraEffortString = capacity.isOverAssignableWithoutLimit() ? _("unl")
                : asString(capacity.getAllowedExtraEffort());

        return asString(capacity.getStandardEffort()) + " ["
                + extraEffortString
                + "]";
    }

    private static String asString(EffortDuration duration) {
        if (duration == null) {
            return "";
        }
        EnumMap<Granularity, Integer> decomposed = duration.decompose();

        String result = decomposed.get(Granularity.HOURS) + ":";
        result += decomposed.get(Granularity.MINUTES);

        if (decomposed.get(Granularity.SECONDS) > 0) {
            result += " " + decomposed.get(Granularity.SECONDS) + "s";
        }
        return result;
    }

    private IBaseCalendarModel baseCalendarModel;

    private Window window;

    private Window createNewVersionWindow;

    private HoursPerDayRenderer hoursPerDayRenderer = new HoursPerDayRenderer();

    private HistoryVersionsRenderer historyVersionsRenderer = new HistoryVersionsRenderer();

    private CalendarExceptionRenderer calendarExceptionRenderer = new CalendarExceptionRenderer();

    private CalendarAvailabilityRenderer calendarAvailabilityRenderer = new CalendarAvailabilityRenderer();

    private boolean creatingNewVersion = false;

    private CapacityPicker capacityPicker;

    private IMessagesForUser messagesForUser;

    public BaseCalendarEditionController(IBaseCalendarModel baseCalendarModel,
            Window window, Window createNewVersionWindow,
            IMessagesForUser messagesForUser) {
        this.baseCalendarModel = baseCalendarModel;
        this.window = window;
        this.createNewVersionWindow = createNewVersionWindow;
        this.messagesForUser = messagesForUser;
    }

    public abstract void goToList();

    public abstract void save();

    public abstract void saveAndContinue();

    public abstract void cancel();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        if (baseCalendarModel.isDerived()) {
            prepareParentCombo();
        }
        prepareExceptionTypeCombo();
        capacityPicker = addEffortDurationPickerAtWorkableTimeRow(comp);
        updateWithCapacityFrom(getSelectedExceptionType());
    }

    private CapacityPicker addEffortDurationPickerAtWorkableTimeRow(
            Component comp) {
        Component normalEffortRow = comp
                .getFellow("exceptionDayNormalEffortRow");
        Component extraEffortRow = comp.getFellow("exceptionDayExtraEffortBox");

        EffortDurationPicker normalDuration = findOrCreateDurationPicker(normalEffortRow);
        EffortDurationPicker extraDuration = findOrCreateDurationPicker(extraEffortRow);
        Checkbox checkbox = findOrCreateUnlimitedCheckbox(extraEffortRow);
        return CapacityPicker.workWith(checkbox, normalDuration, extraDuration,
                Capacity.create(EffortDuration.zero()));
    }

    private EffortDurationPicker findOrCreateDurationPicker(Component parent) {
        return findOrCreate(parent, EffortDurationPicker.class,
                new ICreation<EffortDurationPicker>() {

                    @Override
                    public EffortDurationPicker createAt(Component parent) {
                        EffortDurationPicker normalDuration = new EffortDurationPicker();
                        parent.appendChild(normalDuration);
                        return normalDuration;
                    }
                });
    }

    private Checkbox findOrCreateUnlimitedCheckbox(Component parent) {
        return findOrCreate(parent, Checkbox.class, new ICreation<Checkbox>() {

            @Override
            public Checkbox createAt(Component parent) {
                Checkbox result = createUnlimitedCheckbox();
                parent.appendChild(result);
                return result;
            }

        });
    }

    private Checkbox createUnlimitedCheckbox() {
        Checkbox unlimited = new Checkbox();
        unlimited.setLabel(_("Unlimited"));
        unlimited.setTooltiptext(_("Infinitely Over Assignable"));
        return unlimited;
    }

    private void updateWithCapacityFrom(CalendarExceptionType exceptionType) {
        capacityPicker.setValue(exceptionType != null ? exceptionType
                .getCapacity() : Capacity.create(EffortDuration.zero()));
    }

    private CalendarExceptionType getSelectedExceptionType() {
        Comboitem selectedItem = exceptionTypes.getSelectedItem();
        if (selectedItem != null) {
            return (CalendarExceptionType) selectedItem.getValue();
        }
        return null;
    }

    private Combobox exceptionTypes;

    private void prepareExceptionTypeCombo() {
        exceptionTypes = (Combobox) window.getFellow("exceptionTypes");
        fillExceptionTypesComboAndMarkSelectedItem(exceptionTypes);
        addSelectListener(exceptionTypes);
    }

    private void addSelectListener(final Combobox exceptionTypes) {
        exceptionTypes.addEventListener(Events.ON_SELECT, new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                Comboitem selectedItem = getSelectedItem((SelectEvent) event);
                if (selectedItem != null) {
                    updateWithCapacityFrom(getValue(selectedItem));
                }
            }

            private Comboitem getSelectedItem(SelectEvent event) {
                return (Comboitem) event.getSelectedItems().iterator().next();
            }

            private CalendarExceptionType getValue(Comboitem item) {
                return (CalendarExceptionType) item.getValue();
            }
        });
    }

    private void fillExceptionTypesComboAndMarkSelectedItem(
            Combobox exceptionTypes) {
        exceptionTypes.getChildren().clear();
        CalendarExceptionType type = baseCalendarModel
                .getCalendarExceptionType();

        Comboitem defaultItem = new Comboitem("NO_EXCEPTION");
        exceptionTypes.appendChild(defaultItem);
        if (type == null) {
            exceptionTypes.setSelectedItem(defaultItem);
        }

        for (CalendarExceptionType calendarExceptionType : baseCalendarModel
                .getCalendarExceptionTypes()) {
            Comboitem item = new Comboitem(calendarExceptionType.getName());
            item.setValue(calendarExceptionType);
            exceptionTypes.appendChild(item);
            if ((type != null)
                    && (type.getName().equals(calendarExceptionType.getName()))) {
                exceptionTypes.setSelectedItem(item);
            }
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

            addLabelCell(item, day);

            EffortDurationPicker normalDurationPicker = new EffortDurationPicker();
            EffortDurationPicker extraDurationPicker = new EffortDurationPicker();
            Checkbox unlimitedCheckbox = createUnlimitedCheckbox();

            addNormalDurationCell(item, normalDurationPicker);
            addExtraEffortCell(item, extraDurationPicker, unlimitedCheckbox);

            CapacityPicker capacityPicker = CapacityPicker.workWith(unlimitedCheckbox,
                    normalDurationPicker,
                    extraDurationPicker, new Getter<Capacity>() {

                        @Override
                        public Capacity get() {
                            return baseCalendarModel.getCapacityAt(day);
                        }
                    }, new Setter<Capacity>() {

                        @Override
                        public void set(Capacity value) {
                            baseCalendarModel.setCapacityAt(day, value);
                            reloadDayInformation();
                        }
                    });
            capacityPicker.setDisabled(baseCalendarModel.isDerived()
                    && baseCalendarModel.isDefault(day));

            Listcell inheritedListcell = new Listcell();
            if (baseCalendarModel.isDerived()) {
                Checkbox inheritedCheckbox = Util.bind(new Checkbox(),
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
                inheritedCheckbox.addEventListener(Events.ON_CHECK,
                        new EventListener() {

                            @Override
                            public void onEvent(Event event) throws Exception {
                                reloadCurrentWindow();
                            }

                        });

                inheritedListcell.appendChild(inheritedCheckbox);
            }
            item.appendChild(inheritedListcell);
        }

        private void addLabelCell(Listitem item, final Days day) {
            Listcell labelListcell = new Listcell();
            labelListcell.appendChild(new Label(_(day.getName())));
            item.appendChild(labelListcell);
        }

        private void addNormalDurationCell(Listitem item,
                EffortDurationPicker normalDurationPicker) {
            Listcell normalEffortCell = new Listcell();
            normalEffortCell.appendChild(normalDurationPicker);
            item.appendChild(normalEffortCell);
        }

        private void addExtraEffortCell(Listitem item,
                EffortDurationPicker extraDurationPicker, Checkbox checkbox) {
            Listcell extraEffortCell = new Listcell();
            Hbox hbox = new Hbox();
            hbox.setSclass("extra effort cell");
            hbox.appendChild(extraDurationPicker);
            hbox.appendChild(checkbox);
            extraEffortCell.appendChild(hbox);
            item.appendChild(extraEffortCell);
        }

    }

    private void reloadCurrentWindow() {
        Util.reloadBindings(window);
        highlightDaysOnCalendar();
    }

    private void reloadDayInformation() {
        Util.reloadBindings(window.getFellow("dayInformation"));
        Util.reloadBindings(window.getFellow("exceptionInformation"));
        Util.reloadBindings(window.getFellow("historyInformation"));
        reloadTypeDatesAndDuration();
        reloadParentCombo();
        highlightDaysOnCalendar();
    }

    private void reloadParentCombo() {
        if (baseCalendarModel.isDerived()) {
            BaseCalendar parent = baseCalendarModel.getParent();
            Combobox parentCalendars = (Combobox) window
                    .getFellow("parentCalendars");
            @SuppressWarnings("unchecked")
            List<Comboitem> items = parentCalendars.getItems();
            for (Comboitem item : items) {
                BaseCalendar baseCalendar = (BaseCalendar) item.getValue();
                if (baseCalendar.getId().equals(parent.getId())) {
                    parentCalendars.setSelectedItem(item);
                    break;
                }
            }
        }
    }

    private void reloadTypeDatesAndDuration() {
        LocalDate selectedDay = baseCalendarModel.getSelectedDay();

        CalendarExceptionType type = baseCalendarModel
                .getCalendarExceptionType(new LocalDate(selectedDay));
        Combobox exceptionTypes = (Combobox) window.getFellow("exceptionTypes");
        @SuppressWarnings("unchecked")
        List<Comboitem> items = exceptionTypes.getItems();
        for (Comboitem item : items) {
            CalendarExceptionType value = (CalendarExceptionType) item
                    .getValue();
            if ((value == null) && (type == null)) {
                exceptionTypes.setSelectedItem(item);
                break;
            }
            if ((value != null) && (type != null)
                    && (value.getName().equals(type.getName()))) {
                exceptionTypes.setSelectedItem(item);
                break;
            }
        }

        Datebox dateboxStartDate = (Datebox) window
                .getFellow("exceptionStartDate");
        dateboxStartDate.setValue(toDate(selectedDay));
        Datebox dateboxEndDate = (Datebox) window.getFellow("exceptionEndDate");
        dateboxEndDate.setValue(toDate(selectedDay));
        capacityPicker.setValue(baseCalendarModel.getWorkableCapacity());
    }

    private void highlightDaysOnCalendar() {
        ((CalendarHighlightedDays) window.getFellow("calendarWidget"))
                .highlightDays();
    }

    public Date getSelectedDay() {
        Date selectedDay = toDate(baseCalendarModel.getSelectedDay());
        if (selectedDay == null) {
            return new Date();
        }
        return selectedDay;
    }

    private static Date toDate(LocalDate date){
        if (date == null) {
            return null;
        }
        return date.toDateTimeAtStartOfDay().toDate();
    }

    private static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDate.fromDateFields(date);
    }

    public void setSelectedDay(LocalDate date) {
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
            return _("Derived exception");
        case OWN_EXCEPTION:
            return _("Exception");
        case ZERO_HOURS:
            return _("Not working day");
        case NORMAL:
        default:
            return _("Normal");
        }
    }

    public String getWorkableTime() {
        return asString(baseCalendarModel.getWorkableTime());
    }

    public void createException() {
        Combobox exceptionTypes = (Combobox) window.getFellow("exceptionTypes");
        CalendarExceptionType type = (CalendarExceptionType) exceptionTypes
                .getSelectedItem().getValue();
        if (type == null) {
            throw new WrongValueException(exceptionTypes,
                    _("You should select the type of exception"));
        } else {
            Clients.closeErrorBox(exceptionTypes);
        }

        Datebox dateboxStartDate = (Datebox) window
                .getFellow("exceptionStartDate");
        Date startDate = dateboxStartDate.getValue();
        if (startDate == null) {
            throw new WrongValueException(dateboxStartDate,
                    _("You should select a start date for the exception"));
        } else {
            Clients.closeErrorBox(dateboxStartDate);
        }
        Datebox dateboxEndDate = (Datebox) window.getFellow("exceptionEndDate");
        Date endDate = dateboxEndDate.getValue();
        if (endDate == null) {
            throw new WrongValueException(dateboxEndDate,
                    _("You should select a end date for the exception"));
        } else {
            Clients.closeErrorBox(dateboxEndDate);
        }
        if (new LocalDate(startDate).compareTo(new LocalDate(endDate)) > 0) {
            throw new WrongValueException(
                    dateboxEndDate,
                    _("Exception end date should be greater or equals than start date"));
        } else {
            Clients.closeErrorBox(dateboxEndDate);
        }

        Capacity capacity = capacityPicker.getValue();
        baseCalendarModel.createException(type,
                LocalDate.fromDateFields(startDate),
                LocalDate.fromDateFields(endDate), capacity);
        reloadDayInformation();
    }

    public boolean isNotExceptional() {
        return !baseCalendarModel.isExceptional();
    }

    public void removeException() {
        baseCalendarModel.removeException();

        reloadDayInformation();
    }

    public Date getDateValidFrom() {
        return toDate(baseCalendarModel.getDateValidFrom());
    }

    public void setDateValidFrom(Date date) {
        Component component = window.getFellow("dateValidFrom");

        try {
            baseCalendarModel.setDateValidFrom(toLocalDate(date));
        } catch (IllegalArgumentException e) {
            throw new WrongValueException(component, e.getMessage());
        } catch (UnsupportedOperationException e) {
            throw new WrongValueException(component, e.getMessage());
        }
        Clients.closeErrorBox(component);
        baseCalendarModel.setSelectedDay(toLocalDate(date));
        reloadCurrentWindow();
    }

    public Date getExpiringDate() {
        return baseCalendarModel.getExpiringDate();
    }

    public void setExpiringDate(Date date) {
        Component component = window.getFellow("expiringDate");

        try {
            baseCalendarModel.setExpiringDate(toLocalDate(date).plusDays(1));
        } catch (IllegalArgumentException e) {
            throw new WrongValueException(component, e.getMessage());
        } catch (UnsupportedOperationException e) {
            throw new WrongValueException(component, e.getMessage());
        }
        Clients.closeErrorBox(component);
        baseCalendarModel.setSelectedDay(toLocalDate(date));
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
            item.setValue(calendarData);

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
                if (calendarData.isDefault(day)) {
                    if (parent == null) {
                        summary.add("0");
                    } else {
                        summary.add(_("D"));
                    }
                } else {
                    summary.add(asString(calendarData.getCapacityOn(day)));
                }
            }
            summaryListcell.appendChild(new Label(StringUtils.join(summary,
                    " - ")));
            item.appendChild(summaryListcell);

            appendOperationsListcell(item, calendarData);
            markAsSelected(item, calendarData);
            addEventListener(item);
        }

        private void markAsSelected(Listitem item,
                CalendarData calendarData) {
            CalendarData selected = baseCalendarModel.getCalendarData();
            if ((selected != null) && (calendarData.equals(selected))) {
                item.setSelected(true);
            }
        }

        private void appendOperationsListcell(Listitem item,
                CalendarData calendarData) {
            Listcell listcell = new Listcell();
            listcell.appendChild(createRemoveButton(calendarData));
            item.appendChild(listcell);
        }

        private Button createRemoveButton(
                final CalendarData calendarData) {
            Button result = createButton("/common/img/ico_borrar1.png",
                    _("Delete"), "/common/img/ico_borrar.png", "icono",
                    new EventListener() {
                        @Override
                        public void onEvent(Event event) throws Exception {
                            baseCalendarModel.removeCalendarData(calendarData);
                            reloadCurrentWindow();
                        }
                    });
            LocalDate validFrom = baseCalendarModel.getValidFrom(calendarData);
            if ((validFrom == null)
                    || (!baseCalendarModel.getLastCalendarData().equals(
                            calendarData))) {
                result.setDisabled(true);
            }
            return result;
        }

        private Button createButton(String image, String tooltip,
                String hoverImage, String styleClass,
                EventListener eventListener) {
            Button result = new Button("", image);
            result.setHoverImage(hoverImage);
            result.setSclass(styleClass);
            result.setTooltiptext(tooltip);
            result.addEventListener(Events.ON_CLICK, eventListener);
            return result;
        }

        private void addEventListener(Listitem item) {
            item.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    Listitem item = (Listitem) event.getTarget();
                    CalendarData calendarData = (CalendarData) item.getValue();

                    LocalDate dateValidFrom = baseCalendarModel
                            .getValidFrom(calendarData);
                    LocalDate expiringDate = calendarData.getExpiringDate();

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
        }

    }

    public boolean isLastVersion() {
        return baseCalendarModel.isLastVersion();
    }

    public boolean isFirstVersion() {
        return baseCalendarModel.isFirstVersion();
    }

    public void goToDate(Date date) {
        setSelectedDay(toLocalDate(date));

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
        LocalDate date = getLocalDateFrom((Datebox) component);
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

    private static LocalDate getLocalDateFrom(Datebox datebox) {
        Date value = datebox.getValue();
        if (value == null) {
            return null;
        }
        return LocalDate.fromDateFields(value);
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

    public List<CalendarException> getCalendarExceptions() {
        List<CalendarException> calendarExceptions = new ArrayList<CalendarException>(baseCalendarModel.getCalendarExceptions());
        Collections.sort(calendarExceptions,
                new Comparator<CalendarException>() {
                    @Override
                    public int compare(CalendarException o1,
                            CalendarException o2) {
                        return o1.getDate().compareTo(o2.getDate());
                    }
                });
        return calendarExceptions;
    }

    public CalendarExceptionRenderer getCalendarExceptionRenderer() {
        return calendarExceptionRenderer;
    }

    public class CalendarExceptionRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            CalendarException calendarException = (CalendarException) data;
            item.setValue(calendarException);

            appendDayListcell(item, calendarException);
            appendExceptionTypeListcell(item, calendarException);
            appendStandardEffortListcell(item, calendarException.getCapacity());
            appendExtraEffortListcell(item, calendarException.getCapacity());
            appendCodeListcell(item, calendarException);
            appendOriginListcell(item, calendarException);
            appendOperationsListcell(item, calendarException);

            markAsSelected(item, calendarException);

        }

        private void markAsSelected(Listitem item,
                CalendarException calendarException) {
            LocalDate selectedDay = baseCalendarModel.getSelectedDay();
            if (selectedDay != null) {
                if (calendarException.getDate().equals(selectedDay)) {
                    item.setSelected(true);
                }
            }
        }

        private void appendDayListcell(Listitem item,
                CalendarException calendarException) {
            Listcell listcell = new Listcell();
            listcell.appendChild(new Label(calendarException.getDate()
                    .toString()));
            item.appendChild(listcell);
        }

        private void appendStandardEffortListcell(Listitem item,
                Capacity capacity) {
            Listcell listcell = new Listcell();
            listcell.appendChild(new Label(
                    _(capacity.getStandardEffortString())));
            item.appendChild(listcell);
        }

        private void appendExtraEffortListcell(Listitem item, Capacity capacity) {
            Listcell listcell = new Listcell();
            listcell.appendChild(new Label(_(capacity.getExtraEffortString())));
            item.appendChild(listcell);
        }

        private void appendExceptionTypeListcell(Listitem item,
                CalendarException calendarException) {
            Listcell listcell = new Listcell();
            String type = "";
            if (calendarException.getType() != null) {
                type = calendarException.getType().getName();
            }
            listcell.appendChild(new Label(type));
            item.appendChild(listcell);
        }

        private void appendCodeListcell(final Listitem item,
                final CalendarException calendarException) {
            item.setValue(calendarException);
            Listcell listcell = new Listcell();
            final Textbox code = new Textbox();

            if (getBaseCalendar() != null) {
                code.setDisabled(getBaseCalendar().isCodeAutogenerated());
            }

            Util.bind(code, new Util.Getter<String>() {

                @Override
                public String get() {
                    return calendarException.getCode();
                }
            }, new Util.Setter<String>() {

                @Override
                public void set(String value) {
                    try {
                        calendarException.setCode(value);
                    } catch (IllegalArgumentException e) {
                        throw new WrongValueException(code, e.getMessage());
                    }
                }
            });

            code.setConstraint("no empty:" + _("cannot be null or empty"));

            listcell.appendChild(code);
            item.appendChild(listcell);
        }

        private void appendOriginListcell(Listitem item,
                CalendarException calendarException) {
            Listcell listcell = new Listcell();
            String origin = _("Inherited");
            if (baseCalendarModel.isOwnException(calendarException)) {
                origin = _("Direct");
            }
            listcell.appendChild(new Label(origin));
            item.appendChild(listcell);
        }

        private void appendOperationsListcell(Listitem item, CalendarException calendarException) {
            Listcell listcell = new Listcell();
            listcell.appendChild(createRemoveButton(calendarException));
            item.appendChild(listcell);
        }

        private Button createRemoveButton(
                final CalendarException calendarException) {
            Button result = createButton("/common/img/ico_borrar1.png",
                    _("Delete"), "/common/img/ico_borrar.png", "icono",
                    new EventListener() {
                        @Override
                        public void onEvent(Event event) throws Exception {
                            baseCalendarModel
                                    .removeException(calendarException
                                    .getDate());
                            reloadDayInformation();
                        }
                    });
            if (!baseCalendarModel.isOwnException(calendarException)) {
                result.setDisabled(true);
                result
                        .setTooltiptext(_("derived exception can not be removed"));
            }
            return result;
        }

        private Button createButton(String image, String tooltip,
                String hoverImage, String styleClass,
                EventListener eventListener) {
            Button result = new Button("", image);
            result.setHoverImage(hoverImage);
            result.setSclass(styleClass);
            result.setTooltiptext(tooltip);
            result.addEventListener(Events.ON_CLICK, eventListener);
            return result;
        }

    }

    public boolean isOwnExceptionDay() {
        DayType typeOfDay = baseCalendarModel.getTypeOfDay();
        if ((typeOfDay != null) && (typeOfDay.equals(DayType.OWN_EXCEPTION))) {
            return true;
        }
        return false;
    }

    public boolean isNotOwnExceptionDay() {
        return !isOwnExceptionDay();
    }

    public void updateException() {
        Combobox exceptionTypes = (Combobox) window.getFellow("exceptionTypes");
        CalendarExceptionType type = (CalendarExceptionType) exceptionTypes
                .getSelectedItem().getValue();

        Datebox dateboxStartDate = (Datebox) window
                .getFellow("exceptionStartDate");
        Date startDate = dateboxStartDate.getValue();
        if (startDate == null) {
            throw new WrongValueException(dateboxStartDate,
                    _("You should select a start date for the exception"));
        } else {
            Clients.closeErrorBox(dateboxStartDate);
        }
        Datebox dateboxEndDate = (Datebox) window.getFellow("exceptionEndDate");
        Date endDate = dateboxEndDate.getValue();
        if (endDate == null) {
            throw new WrongValueException(dateboxEndDate,
                    _("You should select a end date for the exception"));
        } else {
            Clients.closeErrorBox(dateboxEndDate);
        }
        if (startDate.compareTo(endDate) > 0) {
            throw new WrongValueException(
                    dateboxEndDate,
                    _("Exception end date should be greater or equals than start date"));
        } else {
            Clients.closeErrorBox(dateboxEndDate);
        }

        Capacity capacity = capacityPicker.getValue();
        baseCalendarModel.updateException(type,
                LocalDate.fromDateFields(startDate),
                LocalDate.fromDateFields(endDate), capacity);
        reloadDayInformation();
    }

    public String getNameParentCalendar() {
        BaseCalendar parent = baseCalendarModel.getParent();
        if (parent != null) {
            return parent.getName();
        }
        return "";
    }

    public boolean isResourceCalendar() {
        return baseCalendarModel.isResourceCalendar();
    }

    public List<CalendarAvailability> getCalendarAvailabilities() {
        return baseCalendarModel.getCalendarAvailabilities();
    }

    public CalendarAvailabilityRenderer getCalendarAvailabilityRenderer() {
        return calendarAvailabilityRenderer;
    }

    public class CalendarAvailabilityRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            CalendarAvailability calendarAvailability = (CalendarAvailability) data;
            item.setValue(calendarAvailability);

            appendValidFromListcell(item, calendarAvailability);
            appendExpirationDateListcell(item, calendarAvailability);
            appendAvailabilityCodeListcell(item, calendarAvailability);
            appendOperationsListcell(item, calendarAvailability);
        }

        private void appendValidFromListcell(Listitem item,
                final CalendarAvailability calendarAvailability) {
            Listcell listcell = new Listcell();

            final Datebox datebox = new Datebox();
            Datebox dateboxValidFrom = Util.bind(datebox,
                    new Util.Getter<Date>() {

                        @Override
                        public Date get() {
                            LocalDate startDate = calendarAvailability
                                    .getStartDate();
                            if (startDate != null) {
                                return startDate.toDateTimeAtStartOfDay()
                                        .toDate();
                            }
                            return null;
                        }

                    }, new Util.Setter<Date>() {

                        @Override
                        public void set(Date value) {
                            LocalDate startDate = new LocalDate(value);
                            try {
                                baseCalendarModel.setStartDate(
                                        calendarAvailability, startDate);
                            } catch (IllegalArgumentException e) {
                                throw new WrongValueException(datebox, e
                                        .getMessage());
                            }
                        }

                    });

            listcell.appendChild(dateboxValidFrom);
            item.appendChild(listcell);
        }

        private void appendExpirationDateListcell(Listitem item,
                final CalendarAvailability calendarAvailability) {
            Listcell listcell = new Listcell();

            final Datebox datebox = new Datebox();
            Datebox dateboxExpirationDate = Util.bind(datebox,
                    new Util.Getter<Date>() {

                        @Override
                        public Date get() {
                            LocalDate endDate = calendarAvailability
                                    .getEndDate();
                            if (endDate != null) {
                                return endDate.toDateTimeAtStartOfDay()
                                        .toDate();
                            }
                            return null;
                        }

                    }, new Util.Setter<Date>() {

                        @Override
                        public void set(Date value) {
                            try {
                                LocalDate endDate = getAppropiateEndDate(
                                    calendarAvailability, value);

                                baseCalendarModel.setEndDate(
                                        calendarAvailability, endDate);
                            } catch (IllegalArgumentException e) {
                                throw new WrongValueException(datebox, e
                                        .getMessage());
                            }
                        }

                    });

            listcell.appendChild(dateboxExpirationDate);
            item.appendChild(listcell);
        }

        private LocalDate getAppropiateEndDate(
                CalendarAvailability calendarAvailability, Date endDate) {
            if (endDate == null) {
                if (baseCalendarModel
                        .isLastActivationPeriod(calendarAvailability)) {
                    return null;
                } else {
                    throw new IllegalArgumentException(
                            _("Only the last activation period allows to delete end date."));
                }
            }
            return new LocalDate(endDate);
        }

        private void appendAvailabilityCodeListcell(Listitem item,
                final CalendarAvailability availability) {
            item.setValue(availability);
            Listcell listcell = new Listcell();
            final Textbox code = new Textbox();

            if (getBaseCalendar() != null) {
                code.setDisabled(getBaseCalendar().isCodeAutogenerated());
            }

            Util.bind(code, new Util.Getter<String>() {

                @Override
                public String get() {
                    return availability.getCode();
                }
            }, new Util.Setter<String>() {

                @Override
                public void set(String value) {
                    try {
                        availability.setCode(value);
                    } catch (IllegalArgumentException e) {
                        throw new WrongValueException(code, e.getMessage());
                    }
                }
            });

            code.setConstraint("no empty:" + _("cannot be null or empty"));

            listcell.appendChild(code);
            item.appendChild(listcell);
        }

        private void appendOperationsListcell(Listitem item,
                CalendarAvailability calendarAvailability) {
            Listcell listcell = new Listcell();
            listcell.appendChild(createRemoveButton(calendarAvailability));
            item.appendChild(listcell);
        }

        private Button createRemoveButton(
                final CalendarAvailability calendarAvailability) {
            Button result = createButton("/common/img/ico_borrar1.png",
                    _("Delete"), "/common/img/ico_borrar.png", "icono",
                    new EventListener() {
                        @Override
                        public void onEvent(Event event) throws Exception {
                            baseCalendarModel
                                    .removeCalendarAvailability(calendarAvailability);
                            reloadDayInformation();
                            reloadActivationPeriods();
                        }
                    });
            return result;
        }

        private Button createButton(String image, String tooltip,
                String hoverImage, String styleClass,
                EventListener eventListener) {
            Button result = new Button("", image);
            result.setHoverImage(hoverImage);
            result.setSclass(styleClass);
            result.setTooltiptext(tooltip);
            result.addEventListener(Events.ON_CLICK, eventListener);
            return result;
        }

    }

    public void createCalendarAvailability() {
        baseCalendarModel.createCalendarAvailability();
        reloadDayInformation();
        reloadActivationPeriods();
    }

    private void reloadActivationPeriods() {
        Util.reloadBindings(window.getFellow("calendarAvailabilities"));
    }

    public void reloadExceptionsList() {
        Util.reloadBindings(window.getFellow("exceptionsList"));
    }

    public boolean isNotResourceCalendar() {
        BaseCalendar baseCalendar = baseCalendarModel.getBaseCalendar();
        if ((baseCalendar == null)
                || (baseCalendar instanceof ResourceCalendar)) {
            return false;
        }
        return true;
    }

    public void validateCalendarExceptionCodes(){
        Listbox listbox = (Listbox) window.getFellow("exceptionsList");
        if (listbox != null) {
            for (int i = 0; i < listbox.getItemCount(); i++) {
                Listitem item = (Listitem) listbox.getItems().get(i);
                if (item.getChildren().size() == 5) {
                    Textbox code = (Textbox) ((Listcell) item.getChildren()
                            .get(3)).getFirstChild();
                    if (code != null && !code.isDisabled()
                            && code.getValue().isEmpty()) {
                        throw new WrongValueException(code,
                                _("It can not be empty"));
                    }
                }
            }
        }
    }

    public void onCheckGenerateCode(Event e) {
        CheckEvent ce = (CheckEvent) e;
        if (ce.isChecked()) {
            try {
                baseCalendarModel.setCodeAutogenerated(ce.isChecked());
            } catch (ConcurrentModificationException err) {
                messagesForUser.showMessage(Level.ERROR, err.getMessage());
            }
        }
        reloadCodeInformation();
    }

    private void reloadCodeInformation() {
        Util.reloadBindings(window.getFellow("txtCode"));
        reloadExceptionsList();
        reloadActivationPeriods();
    }

    public void onSelectException(Event event) {
        Listbox listBox = (Listbox) event.getTarget();
        Listitem item = (Listitem) listBox.getSelectedItem();
        if (item != null) {
            CalendarException calendarException = (CalendarException) item
                .getValue();
            setSelectedDay(calendarException.getDate());
            reloadDayInformation();
        }
    }

}
