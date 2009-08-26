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
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.SimpleTreeNode;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;
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

    private Window historyWindow;

    private Window confirmRemove;

    private boolean confirmingRemove = false;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private HoursPerDayRenderer hoursPerDayRenderer = new HoursPerDayRenderer();

    private BaseCalendarsTreeitemRenderer baseCalendarsTreeitemRenderer = new BaseCalendarsTreeitemRenderer();

    public BaseCalendar getBaseCalendar() {
        return baseCalendarModel.getBaseCalendar();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setVariable("controller", this, true);
        historyWindow.setVisible(false);
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
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
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

            if (baseCalendarModel.isDerived()
                    && baseCalendarModel.isDefault(day)) {
                hoursIntbox.setDisabled(true);
            }

            if (baseCalendarModel.isViewingHistory()) {
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

                if (baseCalendarModel.isViewingHistory()) {
                    defaultCheckbox.setDisabled(true);
                }

                defaultListcell.appendChild(defaultCheckbox);
                item.appendChild(defaultListcell);
            }
        }

    }

    private void reloadCurrentWindow() {
        if (baseCalendarModel.isEditing()) {
            if (baseCalendarModel.isViewingHistory()) {
                Util.reloadBindings(historyWindow);
            } else {
                Util.reloadBindings(editWindow);
            }
        } else {
            Util.reloadBindings(createWindow);
        }
    }

    private void reloadDayInformation() {
        if (baseCalendarModel.isEditing()) {
            if (baseCalendarModel.isViewingHistory()) {
                Util.reloadBindings(historyWindow.getFellow("dayInformation"));
            } else {
                Util.reloadBindings(editWindow.getFellow("dayInformation"));
            }
        } else {
            Util.reloadBindings(createWindow.getFellow("dayInformation"));
        }
    }

    public void goToCreateDerivedForm(BaseCalendar baseCalendar) {
        baseCalendarModel.initCreateDerived(baseCalendar);
        if (baseCalendarModel.isDerived()) {
            prepareParentCombo();
        }
        setSelectedDay(new Date());
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    private void prepareParentCombo() {
        Combobox parentCalendars;
        if (baseCalendarModel.isViewingHistory()) {
            parentCalendars = (Combobox) historyWindow
                    .getFellow("parentCalendars");
        } else if (baseCalendarModel.isEditing()) {
            parentCalendars = (Combobox) editWindow
                    .getFellow("parentCalendars");
        } else {
            parentCalendars = (Combobox) createWindow
                    .getFellow("parentCalendars");
        }

        markSelectedParentCombo(parentCalendars);
        addListenerParentCombo(parentCalendars);
    }

    private void markSelectedParentCombo(final Combobox parentCalendars) {
        BaseCalendar parent = baseCalendarModel.getParent();

        List<BaseCalendar> possibleParentCalendars = getParentCalendars();
        for (BaseCalendar baseCalendar : possibleParentCalendars) {
            if (baseCalendar.getId().equals(parent.getId())) {
                parentCalendars.setSelectedIndex(possibleParentCalendars
                        .indexOf(baseCalendar));
                break;
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
        try {
            baseCalendarModel.setDateValidFrom(date);
        } catch (IllegalArgumentException e) {
            throw new WrongValueException(
                    editWindow.getFellow("dateValidFrom"),
                    e.getMessage());
        }
    }

    public Date getExpiringDate() {
        return baseCalendarModel.getExpiringDate();
    }

    public void goToCreateCopyForm(BaseCalendar baseCalendar) {
        baseCalendarModel.initCreateCopy(baseCalendar);
        if (baseCalendarModel.isDerived()) {
            prepareParentCombo();
        }
        setSelectedDay(new Date());
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

    public void goToHistoryView(BaseCalendar baseCalendar) {
        baseCalendarModel.initHistoryView(baseCalendar);
        if (baseCalendarModel.isDerived()) {
            prepareParentCombo();
        }

        try {
            Util.reloadBindings(historyWindow);
            historyWindow.setVisible(true);
            historyWindow.doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isViewingHistory() {
        return baseCalendarModel.isViewingHistory();
    }

    public boolean isNotViewingHistory() {
        return !isViewingHistory();
    }

    public boolean isEditingAndNotViewingHistory() {
        return isEditing() && !isViewingHistory();
    }

    public void back() {
        baseCalendarModel.cancelHistoryView();
        historyWindow.setVisible(false);

        Util.reloadBindings(editWindow);
        getVisibility().showOnly(editWindow);
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

        if (ancestorExceptionsDays.length() > 0) {
            ancestorExceptionsDays = ancestorExceptionsDays.substring(0,
                    ancestorExceptionsDays.length() - 1);
        }
        if (ownExceptionDays.length() > 0) {
            ownExceptionDays = ownExceptionDays.substring(0, ownExceptionDays
                    .length() - 1);
        }
        if (zeroHoursDays.length() > 0) {
            zeroHoursDays = zeroHoursDays.substring(0,
                    zeroHoursDays.length() - 1);
        }
        if (normalDays.length() > 0) {
            normalDays = normalDays.substring(0, normalDays.length() - 1);
        }

        Map<DayType, String> result = new HashMap<DayType, String>();

        result.put(DayType.ANCESTOR_EXCEPTION, ancestorExceptionsDays);
        result.put(DayType.OWN_EXCEPTION, ownExceptionDays);
        result.put(DayType.ZERO_HOURS, zeroHoursDays);
        result.put(DayType.NORMAL, normalDays);

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

}
