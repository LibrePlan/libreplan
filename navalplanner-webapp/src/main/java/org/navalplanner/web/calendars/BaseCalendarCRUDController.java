package org.navalplanner.web.calendars;

import static org.navalplanner.web.I18nHelper._;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.BaseCalendar.DayType;
import org.navalplanner.business.calendars.entities.BaseCalendar.Days;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
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
import org.zkoss.zul.SimpleTreeNode;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.api.Datebox;
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

    private Window createNewVersion;

    private boolean confirmingRemove = false;

    private boolean creatingNewVersion = false;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private HoursPerDayRenderer hoursPerDayRenderer = new HoursPerDayRenderer();

    private BaseCalendarsTreeitemRenderer baseCalendarsTreeitemRenderer = new BaseCalendarsTreeitemRenderer();

    private HistoryVersionsRenderer historyVersionsRenderer = new HistoryVersionsRenderer();

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
        if (baseCalendarModel.isDerived()) {
            prepareParentCombo();
        }
        setSelectedDay(new Date());
        highlightDaysOnCalendar();
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    private void highlightDaysOnCalendar() {
        if (baseCalendarModel.isEditing()) {
            ((CalendarHighlightedDays) editWindow.getFellow("calendarWidget"))
                    .highlightDays();
        } else {
            ((CalendarHighlightedDays) createWindow.getFellow("calendarWidget"))
                    .highlightDays();
        }
    }

    public void save() {
        try {
            baseCalendarModel.confirmSave();
            messagesForUser.showMessage(Level.INFO, _(
                    "Base calendar \"{0}\" saved", baseCalendarModel
                            .getBaseCalendar().getName()));
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
        if (baseCalendarModel.isParent()) {
            hideConfirmingWindow();
            messagesForUser
                    .showMessage(Level.ERROR,
                            _("The calendar was not removed because it still has children. "
                                    + "Some other calendar is derived from this."));
        } else {
            baseCalendarModel.confirmRemove();
            hideConfirmingWindow();
            Util.reloadBindings(listWindow);
            messagesForUser.showMessage(Level.INFO, _(
                    "Removed calendar \"{0}\"", name));
        }
    }

    public void goToCreateForm() {
        baseCalendarModel.initCreate();
        setSelectedDay(new Date());
        highlightDaysOnCalendar();
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

    public void setSelectedDay(Date date) {
        baseCalendarModel.setSelectedDay(date);

        reloadDayInformation();
    }

    public Date getSelectedDay() {
        Date selectedDay = baseCalendarModel.getSelectedDay();
        if (selectedDay == null) {
            return new Date();
        }
        return selectedDay;
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

    public void createException() {
        Component exceptionHoursIntbox;
        if (baseCalendarModel.isEditing()) {
            exceptionHoursIntbox = editWindow.getFellow("exceptionHours");
        } else {
            exceptionHoursIntbox = createWindow.getFellow("exceptionHours");
        }

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

    public List<Days> getHoursPerDay() {
        return Arrays.asList(Days.values());
    }

    public boolean isNotExceptional() {
        return !baseCalendarModel.isExceptional();
    }

    public void removeException() {
        baseCalendarModel.removeException();

        reloadDayInformation();
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
            Intbox hoursIntbox = Util.bind(intBox,
                    new Util.Getter<Integer>() {

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
                                throw new WrongValueException(intBox, e
                                        .getMessage());
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
                        reloadDayInformation();
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
        if (baseCalendarModel.isEditing()) {
            Util.reloadBindings(editWindow);
        } else {
            Util.reloadBindings(createWindow);
        }
        highlightDaysOnCalendar();
    }

    private void reloadDayInformation() {
        if (baseCalendarModel.isEditing()) {
            Util.reloadBindings(editWindow.getFellow("dayInformation"));
        } else {
            Util.reloadBindings(createWindow.getFellow("dayInformation"));
        }
        highlightDaysOnCalendar();
    }

    public void goToCreateDerivedForm(BaseCalendar baseCalendar) {
        baseCalendarModel.initCreateDerived(baseCalendar);
        if (baseCalendarModel.isDerived()) {
            prepareParentCombo();
        }
        setSelectedDay(new Date());
        highlightDaysOnCalendar();
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    private void prepareParentCombo() {
        Combobox parentCalendars;
        if (baseCalendarModel.isEditing()) {
            parentCalendars = (Combobox) editWindow
                    .getFellow("parentCalendars");
        } else {
            parentCalendars = (Combobox) createWindow
                    .getFellow("parentCalendars");
        }

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
                        .getSelectedItem()
                        .getValue();
                baseCalendarModel.setParent(selected);
                reloadCurrentWindow();
            }

        });
    }

    public boolean isDerived() {
        return baseCalendarModel.isDerived();
    }

    public String getCalendarType() {
        if (baseCalendarModel.isDerived()) {
            return _("Derived");
        }
        return _("Normal");
    }

    public List<BaseCalendar> getParentCalendars() {
        return baseCalendarModel.getPossibleParentCalendars();
    }

    public boolean isEditing() {
        return baseCalendarModel.isEditing();
    }

    public Date getDateValidFrom() {
        return baseCalendarModel.getDateValidFrom();
    }

    public void setDateValidFrom(Date date) {
        Component component = editWindow.getFellow("dateValidFrom");

        try {
            baseCalendarModel.setDateValidFrom(date);
        } catch (IllegalArgumentException e) {
            throw new WrongValueException(component, e.getMessage());
        } catch (UnsupportedOperationException e) {
            throw new WrongValueException(component, e.getMessage());
        }
        Clients.closeErrorBox(component);
    }

    public Date getExpiringDate() {
        return baseCalendarModel.getExpiringDate();
    }

    public void setExpiringDate(Date date) {
        Component component = editWindow.getFellow("expiringDate");

        try {
            baseCalendarModel.setExpiringDate((new LocalDate(date)).plusDays(1)
                    .toDateTimeAtStartOfDay().toDate());
        } catch (IllegalArgumentException e) {
            throw new WrongValueException(component, e.getMessage());
        } catch (UnsupportedOperationException e) {
            throw new WrongValueException(component, e.getMessage());
        }
        Clients.closeErrorBox(component);
    }

    public void goToCreateCopyForm(BaseCalendar baseCalendar) {
        baseCalendarModel.initCreateCopy(baseCalendar);
        if (baseCalendarModel.isDerived()) {
            prepareParentCombo();
        }
        setSelectedDay(new Date());
        highlightDaysOnCalendar();
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    public BaseCalendarsTreeModel getBaseCalendarsTreeModel() {
        return new BaseCalendarsTreeModel(new BaseCalendarTreeRoot(
                baseCalendarModel.getBaseCalendars()));
    }

    public BaseCalendarsTreeitemRenderer getBaseCalendarsTreeitemRenderer() {
        return baseCalendarsTreeitemRenderer;
    }

    public class BaseCalendarsTreeitemRenderer implements TreeitemRenderer {

        @Override
        public void render(Treeitem item, Object data) throws Exception {
            SimpleTreeNode simpleTreeNode = (SimpleTreeNode) data;
            final BaseCalendar baseCalendar = (BaseCalendar) simpleTreeNode
                    .getData();
            item.setValue(data);

            Treerow treerow = new Treerow();

            Treecell nameTreecell = new Treecell();
            Label nameLabel = new Label(baseCalendar.getName());
            nameTreecell.appendChild(nameLabel);
            treerow.appendChild(nameTreecell);

            Treecell operationsTreecell = new Treecell();

            Button createDerivedButton = new Button(_("Create derived"));
            createDerivedButton.addEventListener(Events.ON_CLICK,
                    new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    goToCreateDerivedForm(baseCalendar);
                }

            });
            operationsTreecell.appendChild(createDerivedButton);

            Button createCopyButton = new Button(_("Create copy"));
            createCopyButton.addEventListener(Events.ON_CLICK,
                    new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    goToCreateCopyForm(baseCalendar);
                }

            });
            operationsTreecell.appendChild(createCopyButton);

            Button editButton = new Button(_("Edit"));
            editButton.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    goToEditForm(baseCalendar);
                }

            });
            operationsTreecell.appendChild(editButton);

            Button removeButton = new Button(_("Remove"));
            removeButton.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    confirmRemove(baseCalendar);
                }

            });
            operationsTreecell.appendChild(removeButton);

            treerow.appendChild(operationsTreecell);

            item.appendChild(treerow);

            // Show the tree expanded at start
            item.setOpen(true);
        }

    }

    public List<BaseCalendar> getHistoryVersions() {
        return baseCalendarModel.getHistoryVersions();
    }

    private Map<DayType, String> getDaysCurrentMonthByType() {
        LocalDate currentDate = new LocalDate(baseCalendarModel
                .getSelectedDay());

        LocalDate minDate = currentDate.dayOfMonth().withMinimumValue();
        LocalDate maxDate = currentDate.dayOfMonth().withMaximumValue();

        String ancestorExceptionsDays = "";
        String ownExceptionDays = "";
        String zeroHoursDays = "";
        String normalDays = "";

        for (LocalDate date = minDate; date.compareTo(maxDate) <= 0; date = date
                .plusDays(1)) {
            DayType typeOfDay = baseCalendarModel.getTypeOfDay(date);
            if (typeOfDay != null) {
                switch (typeOfDay) {
                case ANCESTOR_EXCEPTION:
                    ancestorExceptionsDays += date.getDayOfMonth() + ",";
                    break;
                case OWN_EXCEPTION:
                    ownExceptionDays += date.getDayOfMonth() + ",";
                    break;
                case ZERO_HOURS:
                    zeroHoursDays += date.getDayOfMonth() + ",";
                    break;
                case NORMAL:
                default:
                    normalDays += date.getDayOfMonth() + ",";
                    break;
                }
            }
        }

        ancestorExceptionsDays = removeLastCommaIfNeeded(ancestorExceptionsDays);
        ownExceptionDays = removeLastCommaIfNeeded(ownExceptionDays);
        zeroHoursDays = removeLastCommaIfNeeded(zeroHoursDays);
        normalDays = removeLastCommaIfNeeded(normalDays);

        Map<DayType, String> result = new HashMap<DayType, String>();

        result.put(DayType.ANCESTOR_EXCEPTION, ancestorExceptionsDays);
        result.put(DayType.OWN_EXCEPTION, ownExceptionDays);
        result.put(DayType.ZERO_HOURS, zeroHoursDays);
        result.put(DayType.NORMAL, normalDays);

        return result;
    }

    private String removeLastCommaIfNeeded(String string) {
        if (string.length() > 0) {
            string = string.substring(0, string.length() - 1);
        }
        return string;
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

    public void goToCalendarVersion(BaseCalendar calendar) {
        if (calendar.getPreviousCalendar() != null) {
            setSelectedDay(calendar.getPreviousCalendar().getExpiringDate()
                    .toDateTimeAtStartOfDay().toDate());
        } else if (calendar.getExpiringDate() != null) {
            setSelectedDay(calendar.getExpiringDate().minusDays(1)
                    .toDateTimeAtStartOfDay()
                    .toDate());
        } else {
            setSelectedDay(new Date());
        }

        ((Tab) editWindow.getFellow("dataTab")).setSelected(true);
        Util.reloadBindings(editWindow);
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

    public HistoryVersionsRenderer getHistoryVersionsRenderer() {
        return historyVersionsRenderer;
    }

    public class HistoryVersionsRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            final BaseCalendar calendar = (BaseCalendar) data;

            Listcell nameListcell = new Listcell();
            nameListcell.appendChild(new Label(calendar.getName()));
            item.appendChild(nameListcell);

            Listcell validFromListcell = new Listcell();
            Label validFromLabel = new Label();
            if (calendar.getPreviousCalendar() != null) {
                LocalDate validFrom = calendar.getPreviousCalendar()
                        .getExpiringDate();
                validFromLabel.setValue(validFrom.toString());
            }
            validFromListcell.appendChild(validFromLabel);
            item.appendChild(validFromListcell);

            Listcell expiringDateListcell = new Listcell();
            LocalDate expiringDate = calendar.getExpiringDate();
            Label expiringDateLabel = new Label();
            if (expiringDate != null) {
                LocalDate date = new LocalDate(expiringDate).minusDays(1);
                expiringDateLabel.setValue(date.toString());
            }
            expiringDateListcell.appendChild(expiringDateLabel);
            item.appendChild(expiringDateListcell);

            Listcell summaryListcell = new Listcell();
            String summary = "";
            for (Days day : Days.values()) {
                Integer hours = calendar.getHours(day);
                if (hours == null) {
                    summary += "D - ";
                } else {
                    summary += hours + " - ";
                }
            }
            summary = summary.substring(0, summary.length() - 3);
            summaryListcell.appendChild(new Label(summary));
            item.appendChild(summaryListcell);

            Listcell buttonListcell = new Listcell();
            Button button = new Button(_("Go to this calendar"));
            button.addEventListener(Events.ON_CLICK, new EventListener() {

                @Override
                public void onEvent(Event event) throws Exception {
                    goToCalendarVersion(calendar);
                }
            });
            buttonListcell.appendChild(button);
            item.appendChild(buttonListcell);
        }

    }

    public Date getDateValidFromNewVersion() {
        return (new LocalDate()).plusDays(1).toDateTimeAtStartOfDay().toDate();
    }

    public void setDateValidFromNewVersion(Date date) {
        // Just for ZK binding not needed
    }

    public boolean isCreatingNewVersion() {
        return creatingNewVersion;
    }

    public void createNewVersion() {
        creatingNewVersion = true;
        try {
            Util.reloadBindings(createNewVersion);
            createNewVersion.doModal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void acceptCreateNewVersion() {
        Component component = createNewVersion
                .getFellow("dateValidFromNewVersion");
        Date date = ((Datebox) component).getValue();

        try {
            baseCalendarModel.createNewVersion(date);
        } catch (IllegalArgumentException e) {
            throw new WrongValueException(component, e.getMessage());
        }

        Clients.closeErrorBox(component);
        creatingNewVersion = false;
        Util.reloadBindings(createNewVersion);
        setSelectedDay(date);
        Util.reloadBindings(editWindow);
    }

    public void cancelNewVersion() {
        creatingNewVersion = false;
        Util.reloadBindings(createNewVersion);
    }

    public boolean isLastVersion() {
        return baseCalendarModel.isLastVersion();
    }

}
