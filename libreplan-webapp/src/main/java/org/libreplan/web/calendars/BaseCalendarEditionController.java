/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.web.calendars;

import static org.libreplan.web.I18nHelper._;
import static org.libreplan.web.common.Util.findOrCreate;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.calendars.entities.CalendarAvailability;
import org.libreplan.business.calendars.entities.CalendarData;
import org.libreplan.business.calendars.entities.CalendarData.Days;
import org.libreplan.business.calendars.entities.CalendarException;
import org.libreplan.business.calendars.entities.CalendarExceptionType;
import org.libreplan.business.calendars.entities.Capacity;
import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.EffortDuration.Granularity;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.CapacityPicker;
import org.libreplan.web.common.components.EffortDurationPicker;
import org.zkoss.util.Locales;
import org.zkoss.zk.au.out.AuInvoke;
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
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Calendar;
import org.zkoss.zul.Window;

/**
 * Controller for edit and create one {@link BaseCalendar}.
 * It's separated of {@link BaseCalendarCRUDController} to be used from other parts of the application.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public abstract class BaseCalendarEditionController extends GenericForwardComposer {

    private static final String TEXT_COLOR_HIGHLIGHTED_DAY = "white";

    private static final String BACKGROUND_COLOR_ZERO_HOURS_DAY = "lightgrey";

    private IBaseCalendarModel baseCalendarModel;

    private Window window;

    private Window createNewVersionWindow;

    private HoursPerDayRenderer hoursPerDayRenderer = new HoursPerDayRenderer();

    private HistoryVersionsRenderer historyVersionsRenderer = new HistoryVersionsRenderer();

    private ParentCalendarsComboitemRenderer parentCalendarsComboitemRenderer = new ParentCalendarsComboitemRenderer();

    private CalendarExceptionRenderer calendarExceptionRenderer = new CalendarExceptionRenderer();

    private CalendarAvailabilityRenderer calendarAvailabilityRenderer = new CalendarAvailabilityRenderer();

    private boolean creatingNewVersion = false;

    private Date newWorkWeekStartDate;

    private Date newWorkWeekExpiringDate;

    private CapacityPicker capacityPicker;

    private IMessagesForUser messagesForUser;

    private Combobox exceptionTypes;

    public BaseCalendarEditionController(IBaseCalendarModel baseCalendarModel,
                                         Window window,
                                         Window createNewVersionWindow,
                                         IMessagesForUser messagesForUser) {

        this.baseCalendarModel = baseCalendarModel;
        this.window = window;
        this.createNewVersionWindow = createNewVersionWindow;
        this.messagesForUser = messagesForUser;
    }

    private static String asString(Capacity capacity) {
        String extraEffortString = capacity.isOverAssignableWithoutLimit()
                ? _("unl")
                : asString(capacity.getAllowedExtraEffort());

        return asString(capacity.getStandardEffort()) + " [" + extraEffortString + "]";
    }

    private static String asString(EffortDuration duration) {
        if ( duration == null ) {
            return "";
        }
        EnumMap<Granularity, Integer> decomposed = duration.decompose();

        String result = decomposed.get(Granularity.HOURS) + ":";
        result += decomposed.get(Granularity.MINUTES);

        if ( decomposed.get(Granularity.SECONDS) > 0 ) {
            result += " " + decomposed.get(Granularity.SECONDS) + "s";
        }

        return result;
    }

    public abstract void goToList();

    public abstract void save();

    public abstract void saveAndContinue();

    public abstract void cancel();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        prepareExceptionTypeCombo();
        capacityPicker = addEffortDurationPickerAtWorkableTimeRow(comp);
        updateWithCapacityFrom(getSelectedExceptionType());
    }

    private CapacityPicker addEffortDurationPickerAtWorkableTimeRow(Component comp) {
        Component normalEffortRow = comp.getFellow("exceptionDayNormalEffortRow");
        Component extraEffortRow = comp.getFellow("exceptionDayExtraEffortBox");

        EffortDurationPicker normalDuration = findOrCreateDurationPicker(normalEffortRow);
        normalDuration.initializeFor24HoursAnd0Minutes();
        EffortDurationPicker extraDuration = findOrCreateDurationPicker(extraEffortRow);
        Checkbox checkbox = findOrCreateUnlimitedCheckbox(extraEffortRow);

        return CapacityPicker.workWith(checkbox, normalDuration, extraDuration, Capacity.create(EffortDuration.zero()));
    }

    private EffortDurationPicker findOrCreateDurationPicker(Component parent) {
        return findOrCreate(parent, EffortDurationPicker.class, parent1 -> {
            EffortDurationPicker normalDuration = new EffortDurationPicker();
            parent1.appendChild(normalDuration);

            return normalDuration;
        });
    }

    private Checkbox findOrCreateUnlimitedCheckbox(Component parent) {
        return findOrCreate(parent, Checkbox.class, parent1 -> {
            Checkbox result = createUnlimitedCheckbox();
            parent1.appendChild(result);

            return result;
        });
    }

    private Checkbox createUnlimitedCheckbox() {
        Checkbox unlimited = new Checkbox();
        unlimited.setLabel(_("Unlimited"));
        unlimited.setTooltiptext(_("Infinitely Over Assignable"));

        return unlimited;
    }

    private void updateWithCapacityFrom(CalendarExceptionType exceptionType) {
        capacityPicker.setValue(exceptionType != null
                ? exceptionType.getCapacity()
                : Capacity.create(EffortDuration.zero()));
    }

    private CalendarExceptionType getSelectedExceptionType() {
        Comboitem selectedItem = exceptionTypes.getSelectedItem();

        return selectedItem != null
                ? selectedItem.getValue()
                : null;
    }

    private void prepareExceptionTypeCombo() {
        exceptionTypes = (Combobox) window.getFellow("exceptionTypes");
        fillExceptionTypesComboAndMarkSelectedItem(exceptionTypes);
        addSelectListener(exceptionTypes);
    }

    private void addSelectListener(final Combobox exceptionTypes) {
        exceptionTypes.addEventListener(Events.ON_SELECT, new EventListener() {

            @Override
            public void onEvent(Event event) {
                Comboitem selectedItem = getSelectedItem((SelectEvent) event);
                if ( selectedItem != null ) {
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

    private void fillExceptionTypesComboAndMarkSelectedItem(Combobox exceptionTypes) {
        exceptionTypes.getChildren().clear();
        CalendarExceptionType type = baseCalendarModel.getCalendarExceptionType();

        Comboitem defaultItem = new Comboitem("NO_EXCEPTION");
        exceptionTypes.appendChild(defaultItem);
        if ( type == null ) {
            exceptionTypes.setSelectedItem(defaultItem);
        }

        for (CalendarExceptionType calendarExceptionType : baseCalendarModel.getCalendarExceptionTypes()) {
            Comboitem item = new Comboitem(calendarExceptionType.getName());
            item.setValue(calendarExceptionType);
            exceptionTypes.appendChild(item);
            if ( (type != null) && (type.getName().equals(calendarExceptionType.getName())) ) {
                exceptionTypes.setSelectedItem(item);
            }
        }
    }

    public boolean isEditing() {
        return baseCalendarModel.isEditing();
    }

    public BaseCalendar getBaseCalendar() {
        return baseCalendarModel.getBaseCalendar();
    }

    /** Should be public! */
    public String getCalendarType() {
        if ( baseCalendarModel.isDerived() ) {
            String currentStartDate = this.getCurrentStartDateLabel();
            String currentExpiringDate = this.getCurrentExpiringDateLabel();

            return _("Derived of calendar {0}",  getNameParentCalendar()) + currentStartDate + currentExpiringDate;
        }

        return _("Root calendar");
    }

    private String getCurrentExpiringDateLabel() {
        Date date = baseCalendarModel.getCurrentExpiringDate();
        String label = "";

        if ( date != null ) {
            label = " " + _("to {0}", Util.formatDate(date));
        }

        return label;
    }

    private String getCurrentStartDateLabel() {
        Date date = baseCalendarModel.getCurrentStartDate();
        String label = "";

        if ( date != null ) {
            label = " " + _("from {0}", Util.formatDate(date));
        }

        return label;
    }

    public boolean isDerived() {
        return baseCalendarModel.isDerived();
    }

    public boolean isNotDerived() {
        return (!isDerived());
    }

    public List<Days> getHoursPerDay() {
        return Arrays.asList(Days.values());
    }

    public HoursPerDayRenderer getHoursPerDayRenderer() {
        return hoursPerDayRenderer;
    }

    public class HoursPerDayRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data, int i) {
            final Days day = (Days) data;

            addLabelCell(item, day);

            EffortDurationPicker normalDurationPicker = new EffortDurationPicker();
            normalDurationPicker.initializeFor24HoursAnd0Minutes();
            EffortDurationPicker extraDurationPicker = new EffortDurationPicker();
            Checkbox unlimitedCheckbox = createUnlimitedCheckbox();

            addNormalDurationCell(item, normalDurationPicker);
            addExtraEffortCell(item, extraDurationPicker, unlimitedCheckbox);

            CapacityPicker capacityPicker = CapacityPicker.workWith(
                    unlimitedCheckbox,
                    normalDurationPicker,
                    extraDurationPicker,
                    () -> baseCalendarModel.getCapacityAt(day),
                    value -> {
                        baseCalendarModel.setCapacityAt(day, value);
                        reloadDayInformation();
                    });

            capacityPicker.setDisabled(baseCalendarModel.isDerived() && baseCalendarModel.isDefault(day));

            Listcell inheritedListcell = new Listcell();
            if ( baseCalendarModel.isDerived() ) {

                Checkbox inheritedCheckbox = Util.bind(
                        new Checkbox(),
                        () -> baseCalendarModel.isDefault(day),
                        value -> {
                            if ( value ) {
                                baseCalendarModel.setDefault(day);
                            } else {
                                baseCalendarModel.unsetDefault(day);
                            }
                        });

                inheritedCheckbox.addEventListener(Events.ON_CHECK, (EventListener) event -> reloadCurrentWindow());

                inheritedListcell.appendChild(inheritedCheckbox);
            }
            item.appendChild(inheritedListcell);
        }

        private void addLabelCell(Listitem item, final Days day) {
            String days[] = DateFormatSymbols.getInstance(Locales.getCurrent()).getWeekdays();

            Listcell labelListcell = new Listcell();
            labelListcell.appendChild(new Label(days[day.getIndex()]));
            item.appendChild(labelListcell);
        }

        private void addNormalDurationCell(Listitem item, EffortDurationPicker normalDurationPicker) {
            Listcell normalEffortCell = new Listcell();
            normalEffortCell.appendChild(normalDurationPicker);
            item.appendChild(normalEffortCell);
        }

        private void addExtraEffortCell(Listitem item, EffortDurationPicker extraDurationPicker, Checkbox checkbox) {
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
        reloadWorkWeeksList();
        reloadTypeDatesAndDuration();
        highlightDaysOnCalendar();
    }

    private void reloadSelectDayInformation() {
        Util.reloadBindings(window.getFellow("dayInformation"));
        Util.reloadBindings(window.getFellow("exceptionInformation"));
        Util.reloadBindings(window.getFellow("hoursPerDay"));
        reloadTypeDatesAndDuration();
        highlightDaysOnCalendar();
    }

    private void reloadWorkWeeksList() {
        Util.reloadBindings(window.getFellow("historyInformation"));
        Util.reloadBindings(window.getFellow("calendarTypeLabel"));
    }

    private void reloadTypeDatesAndDuration() {
        LocalDate selectedDay = baseCalendarModel.getSelectedDay();

        CalendarExceptionType type = baseCalendarModel.getCalendarExceptionType(new LocalDate(selectedDay));
        Combobox exceptionTypes = (Combobox) window.getFellow("exceptionTypes");

        @SuppressWarnings("unchecked")
        List<Comboitem> items = exceptionTypes.getItems();

        for (Comboitem item : items) {
            CalendarExceptionType value = item.getValue();
            if ( (value == null) && (type == null) ) {
                exceptionTypes.setSelectedItem(item);
                break;
            }

            if ( (value != null) && (type != null) && (value.getName().equals(type.getName())) ) {
                exceptionTypes.setSelectedItem(item);
                break;
            }
        }

        Datebox dateboxStartDate = (Datebox) window.getFellow("exceptionStartDate");
        dateboxStartDate.setValue(toDate(selectedDay));
        Datebox dateboxEndDate = (Datebox) window.getFellow("exceptionEndDate");
        dateboxEndDate.setValue(toDate(selectedDay));
        capacityPicker.setValue(baseCalendarModel.getWorkableCapacity());
    }

    private Map<String, List<Integer>> getDaysCurrentMonthByColor() {
        LocalDate currentDate = baseCalendarModel.getSelectedDay();

        LocalDate minDate = currentDate.dayOfMonth().withMinimumValue();
        LocalDate maxDate = currentDate.dayOfMonth().withMaximumValue();

        Map<String, List<Integer>> colorsMap = new HashMap<>();

        BaseCalendar calendar = baseCalendarModel.getBaseCalendar();
        if ( calendar == null ) {
            return colorsMap;
        }

        for (LocalDate date = minDate; date.compareTo(maxDate) <= 0; date = date.plusDays(1)) {
            CalendarExceptionType calendarExceptionType = calendar.getExceptionType(date);

            if ( calendarExceptionType != null ) {

                if (calendar.getOwnExceptionDay(date) != null) {

                    addDayToColor(
                            colorsMap,
                            calendarExceptionType.getColor().getColorOwnException(),
                            date.getDayOfMonth());
                } else {
                    addDayToColor(
                            colorsMap,
                            calendarExceptionType.getColor().getColorDerivedException(),
                            date.getDayOfMonth());
                }
            } else {
                if ( calendar.getCapacityOn(PartialDay.wholeDay(date)).isZero() ) {
                    addDayToColor(colorsMap, BACKGROUND_COLOR_ZERO_HOURS_DAY, date.getDayOfMonth());
                }
            }
        }

        return colorsMap;
    }

    private void addDayToColor(Map<String, List<Integer>> colorsMap, String color, int day) {
        if ( colorsMap.get(color) == null)
            colorsMap.put(color, new ArrayList<>());

        colorsMap.get(color).add(day);
    }

    public void highlightDaysOnCalendar() {
        Calendar calendar = (Calendar) window.getFellow("calendarWidget");

        Clients.response(new AuInvoke(calendar, "resetHighlightedDates"));

        Map<String, List<Integer>> daysByColor = getDaysCurrentMonthByColor();
        for (String color : daysByColor.keySet()) {

            Clients.response(new AuInvoke(
                    calendar,
                    "highlightDates",
                    daysByColor.get(color).toArray(),
                    TEXT_COLOR_HIGHLIGHTED_DAY,
                    color));
        }
    }

    public Date getSelectedDay() {
        Date selectedDay = toDate(baseCalendarModel.getSelectedDay());

        return selectedDay == null ? new Date() : selectedDay;
    }

    private static Date toDate(LocalDate date) {
        return date == null ? null : date.toDateTimeAtStartOfDay().toDate();
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : LocalDate.fromDateFields(date);
    }

    public void setSelectedDay(LocalDate date) {
        baseCalendarModel.setSelectedDay(date);
        reloadSelectDayInformation();
    }

    public String getTypeOfDay() {
        BaseCalendar calendar = baseCalendarModel.getBaseCalendar();
        if ( calendar == null ) {
            return "";
        }

        LocalDate date = baseCalendarModel.getSelectedDay();
        CalendarException exceptionDay = calendar.getExceptionDay(date);
        if ( exceptionDay != null ) {
            if ( calendar.getOwnExceptionDay(date) != null ) {
                return _("Exception: {0}", exceptionDay.getType().getName());
            } else {
                return _("Exception: {0} (Inh)", exceptionDay.getType().getName());
            }
        }

        if ( calendar.getCapacityOn(PartialDay.wholeDay(date)).isZero() ) {
            return _("Not workable day");
        }

        return _("Normal");
    }

    public String getWorkableTime() {
        return asString(baseCalendarModel.getWorkableTime());
    }

    public void createException() {
        Combobox exceptionTypes = (Combobox) window.getFellow("exceptionTypes");
        CalendarExceptionType type = exceptionTypes.getSelectedItem().getValue();

        if ( type == null ) {
            throw new WrongValueException(exceptionTypes, _("Please, select type of exception"));
        } else {
            Clients.clearWrongValue(exceptionTypes);
        }

        Datebox dateboxStartDate = (Datebox) window.getFellow("exceptionStartDate");
        Date startDate = dateboxStartDate.getValue();

        if ( startDate == null ) {
            throw new WrongValueException(dateboxStartDate, _("You should select a start date for the exception"));
        } else {
            Clients.clearWrongValue(dateboxStartDate);
        }

        Datebox dateboxEndDate = (Datebox) window.getFellow("exceptionEndDate");
        Date endDate = dateboxEndDate.getValue();

        if ( endDate == null ) {
            throw new WrongValueException(dateboxEndDate, _("Please, select an End Date for the Exception"));
        } else {
            Clients.clearWrongValue(dateboxEndDate);
        }

        if ( new LocalDate(startDate).compareTo(new LocalDate(endDate)) > 0 ) {
            throw new WrongValueException(dateboxEndDate,
                    _("Exception end date should be greater or equals than start date"));
        } else {
            Clients.clearWrongValue(dateboxEndDate);
        }

        Capacity capacity = capacityPicker.getValue();

        baseCalendarModel.createException(
                type,
                LocalDate.fromDateFields(startDate),
                LocalDate.fromDateFields(endDate),
                capacity);

        reloadDayInformation();
    }

    public boolean isNotExceptional() {
        return !baseCalendarModel.isExceptional();
    }

    public void removeException() {
        baseCalendarModel.removeException();

        reloadDayInformation();
    }

    public List<CalendarData> getHistoryVersions() {
        return baseCalendarModel.getHistoryVersions();
    }

    public HistoryVersionsRenderer getHistoryVersionsRenderer() {
        return historyVersionsRenderer;
    }

    public class HistoryVersionsRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem listitem, Object o, int i) throws Exception {
            CalendarData calendarData = (CalendarData) o;
            listitem.setValue(calendarData);

            if ( isDerived() ) {
                appendParentCombobox(listitem, calendarData);
            } else {
                appendParentLabel(listitem, calendarData);
            }
            appendStartDatebox(listitem, calendarData);
            appendExpiringDatebox(listitem, calendarData);
            appendSummaryLabel(listitem, calendarData);
            appendOperationsListcell(listitem, calendarData);
            markAsSelected(listitem, calendarData);
            addEventListener(listitem);
        }

        private void appendSummaryLabel(final Listitem listItem, final CalendarData version) {
            BaseCalendar parent = version.getParent();

            List<String> summary = new ArrayList<>();
            for (Days day : Days.values()) {
                if ( version.isDefault(day) ) {
                    if ( parent == null ) {
                        summary.add("0");
                    } else {
                        summary.add(_("Inh"));
                    }
                } else {
                    summary.add(asString(version.getCapacityOn(day)));
                }
            }
            Label summaryLabel = new Label(StringUtils.join(summary, " - "));

            Listcell listCell = new Listcell();
            listCell.appendChild(summaryLabel);
            listItem.appendChild(listCell);
        }

        private void appendStartDatebox(final Listitem listItem, final CalendarData version) {
            Datebox datebox = new Datebox();

            final LocalDate dateValidFrom = baseCalendarModel.getValidFrom(version);
            if ( dateValidFrom != null ) {

                Util.bind(
                        datebox,
                        () -> dateValidFrom.toDateTimeAtStartOfDay().toDate());

                datebox.setDisabled(false);
            } else {
                datebox.setDisabled(true);
            }

            datebox.addEventListener(Events.ON_CHANGE, (EventListener) event -> reloadWorkWeeksList());

            datebox.setConstraint((comp, value) -> {
                try {
                    baseCalendarModel.checkAndChangeStartDate(version, ((Date) value));
                } catch (ValidationException e) {
                    throw new WrongValueException(comp, e.getMessage());
                }
            });

            Listcell listCell = new Listcell();
            listCell.appendChild(datebox);
            listItem.appendChild(listCell);
        }

        private void appendExpiringDatebox(final Listitem listItem, final CalendarData version) {
            Datebox datebox = new Datebox();

            final LocalDate expiringDate = version.getExpiringDate();
            if ( expiringDate != null ) {
                datebox.setDisabled(false);
            } else {
                datebox.setDisabled(true);
            }

            Util.bind(
                    datebox,
                    () -> {
                        LocalDate expiringDate1 = version.getExpiringDate();
                        if ( expiringDate1 != null ) {
                            return expiringDate1.minusDays(1).toDateTimeAtStartOfDay().toDate();
                        }

                        return null;
                    },
                    value -> {
                        LocalDate expiringDate1 = null;
                        if ( value != null ) {
                            expiringDate1 = new LocalDate(value).plusDays(1);
                        }
                        version.setExpiringDate(expiringDate1);
                    });

            datebox.addEventListener(Events.ON_CHANGE, (EventListener) event -> reloadWorkWeeksList());

            datebox.setConstraint((comp, value) -> {
                Date date = ((Date) value);
                try {
                    baseCalendarModel.checkChangeExpiringDate(version, date);
                } catch (ValidationException e) {
                    throw new WrongValueException(comp, e.getMessage());
                }
            });

            Listcell listCell = new Listcell();
            listCell.appendChild(datebox);
            listItem.appendChild(listCell);
        }

        private void appendParentLabel(Listitem listItem, CalendarData version) {
            final Label labelParent = new Label();
            if ( version.getParent() != null ) {
                labelParent.setValue(version.getParent().getName());
            }

            Listcell listCell = new Listcell();
            listCell.appendChild(labelParent);
            listItem.appendChild(listCell);
        }

        private void appendParentCombobox(final Listitem listItem, final CalendarData version) {
            final Combobox comboParents = new Combobox();
            final List<BaseCalendar> listParents = getParentCalendars();

            for (BaseCalendar parent : listParents) {
                Comboitem comboItem = new Comboitem();
                comboItem.setValue(parent);
                comboItem.setLabel(parent.getName());
                comboItem.setParent(comboParents);

                if ( (version.getParent() ) != null && (parent.getId().equals(version.getParent().getId())) ) {
                    comboParents.setSelectedItem(comboItem);
                }
            }

            comboParents.addEventListener(Events.ON_SELECT, (EventListener) event -> {
                if ( comboParents.getSelectedItem() != null ) {
                    BaseCalendar parent = comboParents.getSelectedItem().getValue();
                    version.setParent(parent);
                }
            });

            Util.bind(
                    comboParents,
                    () -> comboParents.getSelectedItem(),
                    comboItem -> {
                        if ( (comboItem != null) &&
                                (comboItem.getValue() != null ) &&
                                (comboItem.getValue() instanceof BaseCalendar) )  {

                            BaseCalendar parent = comboItem.getValue();
                            version.setParent(parent);
                        }
                    });

            Listcell listCell = new Listcell();
            listCell.appendChild(comboParents);
            listItem.appendChild(listCell);
        }

        private void markAsSelected(Listitem item, CalendarData calendarData) {
            CalendarData selected = baseCalendarModel.getCalendarData();
            if ( (selected != null) && (calendarData.equals(selected)) ) {
                item.setSelected(true);
            }
        }

        private void appendOperationsListcell(Listitem item, CalendarData calendarData) {
            Listcell listcell = new Listcell();
            listcell.appendChild(createRemoveButton(calendarData));
            item.appendChild(listcell);
        }

        private Button createRemoveButton(final CalendarData calendarData) {
            Button result = createButton(
                    "/common/img/ico_borrar1.png",
                    _("Delete"),
                    "/common/img/ico_borrar.png",
                    "icono",

                    event -> {
                        baseCalendarModel.removeCalendarData(calendarData);
                        reloadWorkWeeksList();
                    });

            if ( baseCalendarModel.getBaseCalendar() == null ||
                    baseCalendarModel.getBaseCalendar().getCalendarDataVersions().size() == 1 ) {

                result.setDisabled(true);
            } else {
                result.setDisabled(false);
            }

            return result;
        }

        private Button createButton(String image,
                                    String tooltip,
                                    String hoverImage,
                                    String styleClass,
                                    EventListener eventListener) {

            Button result = new Button("", image);
            result.setHoverImage(hoverImage);
            result.setSclass(styleClass);
            result.setTooltiptext(tooltip);
            result.addEventListener(Events.ON_CLICK, eventListener);

            return result;
        }

        private void addEventListener(Listitem item) {
            item.addEventListener(Events.ON_CLICK, (EventListener) event -> {
                Listitem item1 = (Listitem) event.getTarget();
                CalendarData calendarData = item1.getValue();

                LocalDate dateValidFrom = baseCalendarModel.getValidFrom(calendarData);
                LocalDate expiringDate = calendarData.getExpiringDate();

                if ( dateValidFrom != null ) {
                    goToDate(dateValidFrom.toDateTimeAtStartOfDay().toDate());
                } else if ( expiringDate != null ) {
                    goToDate(expiringDate.minusDays(1).toDateTimeAtStartOfDay().toDate());
                } else {
                    goToDate(new Date());
                }
            });
        }
    }

    public boolean isLastVersion(LocalDate selectedDate) {
        return baseCalendarModel.isLastVersion(selectedDate);
    }

    public boolean isFirstVersion(LocalDate selectedDate) {
        return baseCalendarModel.isFirstVersion(selectedDate);
    }

    public void goToDate(Date date) {
        setSelectedDay(toLocalDate(date));
    }

    public boolean isCreatingNewVersion() {
        return creatingNewVersion;
    }

    public void createNewVersion() {
        creatingNewVersion = true;
        initDatesToCreateNewVersion();
        try {
            Util.reloadBindings(createNewVersionWindow);
            createNewVersionWindow.doModal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initDatesToCreateNewVersion() {
        this.newWorkWeekStartDate = (new LocalDate()).plusDays(1).toDateTimeAtStartOfDay().toDate();
        this.newWorkWeekExpiringDate = (new LocalDate()).plusDays(2).toDateTimeAtStartOfDay().toDate();
    }

    public void acceptCreateNewVersion() {
        Component compStartDate = createNewVersionWindow.getFellow("startDateValidFromNewVersion");
        LocalDate startDate = getLocalDateFrom((Datebox) compStartDate);

        Component compExpiringDate = createNewVersionWindow.getFellow("expiringDateValidFromNewVersion");
        LocalDate expiringDate = getLocalDateFrom((Datebox) compExpiringDate);

        BaseCalendar selected = null;
        if ( isDerived() ) {
            Combobox parentCalendars = (Combobox) createNewVersionWindow.getFellow("parentCalendars");
            if ( parentCalendars.getSelectedItem() == null ) {
                throw new WrongValueException(parentCalendars, _("cannot be empty"));
            }
            selected = parentCalendars.getSelectedItem().getValue();
        }

        try {
            baseCalendarModel.createNewVersion(startDate, expiringDate, selected);
        } catch (IllegalArgumentException e) {
            if ( e.getMessage().contains("Wrong expiring date") ) {
                throw new WrongValueException(compExpiringDate, _(e.getMessage()));
            } else {
                throw new WrongValueException(compStartDate, _(e.getMessage()));
            }
        }

        Clients.clearWrongValue(compStartDate);
        Clients.clearWrongValue(compExpiringDate);
        creatingNewVersion = false;
        Util.reloadBindings(createNewVersionWindow);

        setSelectedDay(startDate);
        if ( (startDate == null) && (expiringDate != null) ) {
            setSelectedDay(expiringDate.minusDays(1));
        }

        reloadCurrentWindow();
    }

    private static LocalDate getLocalDateFrom(Datebox datebox) {
        Date value = datebox.getValue();

        return value == null ? null : LocalDate.fromDateFields(value);
    }

    public void cancelNewVersion() {
        creatingNewVersion = false;
        Util.reloadBindings(createNewVersionWindow);
    }

    public Date getDateValidFromNewVersion() {
        return newWorkWeekStartDate;
    }

    public void setDateValidFromNewVersion(Date date) {
        this.newWorkWeekStartDate = date;
    }

    public Date getDateValidToNewVersion() {
        return newWorkWeekExpiringDate;
    }

    public void setDateValidToNewVersion(Date date) {
        this.newWorkWeekExpiringDate = date;
    }

    public List<BaseCalendar> getParentCalendars() {
        return baseCalendarModel.getPossibleParentCalendars();
    }

    public List<CalendarException> getCalendarExceptions() {
        List<CalendarException> calendarExceptions = new ArrayList<>(baseCalendarModel.getCalendarExceptions());
        Collections.sort(calendarExceptions, (o1, o2) -> o1.getDate().compareTo(o2.getDate()));

        return calendarExceptions;
    }

    public ParentCalendarsComboitemRenderer getParentCalendarsComboitemRenderer() {
        return parentCalendarsComboitemRenderer;
    }

    public class ParentCalendarsComboitemRenderer implements ComboitemRenderer {

        @Override
        public void render(Comboitem comboitem, Object o, int i) throws Exception {
            BaseCalendar calendar = (BaseCalendar) o;
            comboitem.setLabel(calendar.getName());
            comboitem.setValue(calendar);

            Combobox combobox = (Combobox) comboitem.getParent();
            if ( combobox.getSelectedIndex() != 0 ) {
                combobox.setSelectedIndex(0);
            }
        }
    }

    public CalendarExceptionRenderer getCalendarExceptionRenderer() {
        return calendarExceptionRenderer;
    }

    public class CalendarExceptionRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem listitem, Object o, int i) throws Exception {
            CalendarException calendarException = (CalendarException) o;
            listitem.setValue(calendarException);

            appendDayListcell(listitem, calendarException);
            appendExceptionTypeListcell(listitem, calendarException);
            appendStandardEffortListcell(listitem, calendarException.getCapacity());
            appendExtraEffortListcell(listitem, calendarException.getCapacity());
            appendCodeListcell(listitem, calendarException);
            appendOriginListcell(listitem, calendarException);
            appendOperationsListcell(listitem, calendarException);

            markAsSelected(listitem, calendarException);

        }

        private void markAsSelected(Listitem item, CalendarException calendarException) {
            LocalDate selectedDay = baseCalendarModel.getSelectedDay();

            if ( selectedDay != null && calendarException.getDate().equals(selectedDay) )
                item.setSelected(true);
        }
    }

    private void appendDayListcell(Listitem item, CalendarException calendarException) {
        Listcell listcell = new Listcell();
        listcell.appendChild(new Label(calendarException.getDate().toString()));
        item.appendChild(listcell);
    }

    private void appendStandardEffortListcell(Listitem item, Capacity capacity) {
        Listcell listcell = new Listcell();
        listcell.appendChild(new Label(_(capacity.getStandardEffortString())));
        item.appendChild(listcell);
    }

    private void appendExtraEffortListcell(Listitem item, Capacity capacity) {
        Listcell listcell = new Listcell();
        listcell.appendChild(new Label(_(capacity.getExtraEffortString())));
        item.appendChild(listcell);
    }

    private void appendExceptionTypeListcell(Listitem item, CalendarException calendarException) {
        Listcell listcell = new Listcell();
        String type = "";
        if ( calendarException.getType() != null ) {
            type = calendarException.getType().getName();
        }
        listcell.appendChild(new Label(type));
        item.appendChild(listcell);
    }

    private void appendCodeListcell(final Listitem item, final CalendarException calendarException) {
        item.setValue(calendarException);
        Listcell listcell = new Listcell();
        final Textbox code = new Textbox();

        if ( getBaseCalendar() != null ) {
            code.setDisabled(getBaseCalendar().isCodeAutogenerated());
        }

        Util.bind(
                code,
                () -> calendarException.getCode(),
                value -> {
                    try {
                        calendarException.setCode(value);
                    } catch (IllegalArgumentException e) {
                        throw new WrongValueException(code, e.getMessage());
                    }
                });

        code.setConstraint("no empty:" + _("cannot be empty"));

        listcell.appendChild(code);
        item.appendChild(listcell);
    }

    private void appendOriginListcell(Listitem item, CalendarException calendarException) {
        Listcell listcell = new Listcell();
        String origin = _("Inherited");

        if ( baseCalendarModel.isOwnException(calendarException) )
            origin = _("Direct");

        listcell.appendChild(new Label(origin));
        item.appendChild(listcell);
    }

    private void appendOperationsListcell(Listitem item, CalendarException calendarException) {
        Listcell listcell = new Listcell();
        listcell.appendChild(createRemoveButton(calendarException));
        item.appendChild(listcell);
    }

    private Button createRemoveButton(final CalendarException calendarException) {
        Button result = createButton(
                "/common/img/ico_borrar1.png",
                _("Delete"),
                "/common/img/ico_borrar.png",
                "icono",
                event -> {
                    baseCalendarModel.removeException(calendarException.getDate());
                    reloadDayInformation();
                });

        if ( !baseCalendarModel.isOwnException(calendarException) ) {
            result.setDisabled(true);
            result.setTooltiptext(_("inherited exception can not be removed"));
        }

        return result;
    }

    private Button createButton(String image,
                                String tooltip,
                                String hoverImage,
                                String styleClass,
                                EventListener eventListener) {

        Button result = new Button("", image);
        result.setHoverImage(hoverImage);
        result.setSclass(styleClass);
        result.setTooltiptext(tooltip);
        result.addEventListener(Events.ON_CLICK, eventListener);

        return result;
    }


    public boolean isOwnExceptionDay() {
        return baseCalendarModel.isOwnExceptionDay();
    }

    public boolean isNotOwnExceptionDay() {
        return !isOwnExceptionDay();
    }

    public void updateException() {
        Combobox exceptionTypes = (Combobox) window.getFellow("exceptionTypes");
        CalendarExceptionType type = exceptionTypes.getSelectedItem().getValue();

        Datebox dateboxStartDate = (Datebox) window.getFellow("exceptionStartDate");
        Date startDate = dateboxStartDate.getValue();

        if ( startDate == null ) {
            throw new WrongValueException(dateboxStartDate, _("You should select a start date for the exception"));
        } else {
            Clients.clearWrongValue(dateboxStartDate);
        }

        Datebox dateboxEndDate = (Datebox) window.getFellow("exceptionEndDate");
        Date endDate = dateboxEndDate.getValue();

        if ( endDate == null ) {
            throw new WrongValueException(dateboxEndDate, _("Please, select an End Date for the Exception"));
        } else {
            Clients.clearWrongValue(dateboxEndDate);
        }

        if ( startDate.compareTo(endDate) > 0 ) {
            throw new WrongValueException(dateboxEndDate,
                    _("Exception end date should be greater or equals than start date"));
        } else {
            Clients.clearWrongValue(dateboxEndDate);
        }

        Capacity capacity = capacityPicker.getValue();

        baseCalendarModel.updateException(
                type,
                LocalDate.fromDateFields(startDate),
                LocalDate.fromDateFields(endDate), capacity);

        reloadDayInformation();
    }

    public String getNameParentCalendar() {
        BaseCalendar parent = baseCalendarModel.getCurrentParent();
        return parent != null ? parent.getName() : "";
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
        public void render(Listitem listitem, Object o, int i) throws Exception {
            CalendarAvailability calendarAvailability = (CalendarAvailability) o;
            listitem.setValue(calendarAvailability);

            appendValidFromListcell(listitem, calendarAvailability);
            appendExpirationDateListcell(listitem, calendarAvailability);
            appendAvailabilityCodeListcell(listitem, calendarAvailability);
            appendOperationsListcell(listitem, calendarAvailability);
        }

        private void appendValidFromListcell(Listitem item, final CalendarAvailability calendarAvailability) {
            Listcell listcell = new Listcell();

            final Datebox datebox = new Datebox();

            Datebox dateboxValidFrom = Util.bind(
                    datebox,
                    () -> {
                        LocalDate startDate = calendarAvailability.getStartDate();

                        return startDate != null ? startDate.toDateTimeAtStartOfDay().toDate() : null;
                    },
                    value -> {
                        LocalDate startDate = new LocalDate(value);
                        try {
                            baseCalendarModel.setStartDate(calendarAvailability, startDate);
                        } catch (IllegalArgumentException e) {
                            throw new WrongValueException(datebox, e.getMessage());
                        }
                    });

            listcell.appendChild(dateboxValidFrom);
            item.appendChild(listcell);
        }

        private void appendExpirationDateListcell(Listitem item, final CalendarAvailability calendarAvailability) {
            Listcell listcell = new Listcell();

            final Datebox datebox = new Datebox();

            Datebox dateboxExpirationDate = Util.bind(
                    datebox,
                    () -> {
                        LocalDate endDate = calendarAvailability.getEndDate();

                        return endDate != null ? endDate.toDateTimeAtStartOfDay().toDate() : null;
                    },
                    value -> {
                        try {
                            LocalDate endDate = getAppropiateEndDate(calendarAvailability, value);
                            baseCalendarModel.setEndDate(calendarAvailability, endDate);
                        } catch (IllegalArgumentException e) {
                            throw new WrongValueException(datebox, e.getMessage());
                        }
                    });

            listcell.appendChild(dateboxExpirationDate);
            item.appendChild(listcell);
        }

        private LocalDate getAppropiateEndDate(CalendarAvailability calendarAvailability, Date endDate) {
            if ( endDate == null ) {
                if (baseCalendarModel.isLastActivationPeriod(calendarAvailability)) {
                    return null;
                } else {
                    throw new IllegalArgumentException(_("End date can only be deleted in the last activation"));
                }
            }

            return new LocalDate(endDate);
        }

        private void appendAvailabilityCodeListcell(Listitem item, final CalendarAvailability availability) {
            item.setValue(availability);
            Listcell listcell = new Listcell();
            final Textbox code = new Textbox();

            if ( getBaseCalendar() != null ) {
                code.setDisabled(getBaseCalendar().isCodeAutogenerated());
            }

            Util.bind(
                    code,
                    () -> availability.getCode(),
                    value -> {
                        try {
                            availability.setCode(value);
                        } catch (IllegalArgumentException e) {
                            throw new WrongValueException(code, e.getMessage());
                        }
                    });

            code.setConstraint("no empty:" + _("cannot be empty"));

            listcell.appendChild(code);
            item.appendChild(listcell);
        }

        private void appendOperationsListcell(Listitem item, CalendarAvailability calendarAvailability) {
            Listcell listcell = new Listcell();
            listcell.appendChild(createRemoveButton(calendarAvailability));
            item.appendChild(listcell);
        }

        private Button createRemoveButton(final CalendarAvailability calendarAvailability) {
            return createButton(
                    "/common/img/ico_borrar1.png",
                    _("Delete"),
                    "/common/img/ico_borrar.png",
                    "icono",
                    event -> {
                        baseCalendarModel.removeCalendarAvailability(calendarAvailability);
                        reloadDayInformation();
                        reloadActivationPeriods();
                    });
        }

        private Button createButton(String image,
                                    String tooltip,
                                    String hoverImage,
                                    String styleClass,
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
        return !((baseCalendar == null) || (baseCalendar instanceof ResourceCalendar));
    }

    public void validateCalendarExceptionCodes(){
        Listbox listbox = (Listbox) window.getFellow("exceptionsList");
        if ( listbox != null ) {

            for (int i = 0; i < listbox.getItemCount(); i++) {
                Listitem item = listbox.getItems().get(i);

                if ( item.getChildren().size() == 5)  {
                    Textbox code = (Textbox) (item.getChildren().get(3)).getFirstChild();

                    if ( code != null && !code.isDisabled() && code.getValue().isEmpty() ) {
                        throw new WrongValueException(code, _("It cannot be empty"));
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
        Listitem item = listBox.getSelectedItem();

        if ( item != null ) {
            CalendarException calendarException = item.getValue();
            setSelectedDay(calendarException.getDate());
            reloadDayInformation();
        }
    }

    public boolean isVirtualWorker() {
        return baseCalendarModel.isVirtualWorker();
    }

    public Integer getCapacity() {
        return baseCalendarModel.getCapacity();
    }

    public void setCapacity(Integer capacity) {
        baseCalendarModel.setCapacity(capacity);
    }

}