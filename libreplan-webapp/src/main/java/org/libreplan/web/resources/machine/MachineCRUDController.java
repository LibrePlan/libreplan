/*
 * This file is part of LibrePlan
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

package org.libreplan.web.resources.machine;

import static org.libreplan.web.I18nHelper._;

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.resources.entities.Machine;
import org.libreplan.web.calendars.BaseCalendarEditionController;
import org.libreplan.web.calendars.IBaseCalendarModel;
import org.libreplan.web.common.BaseCRUDController;
import org.libreplan.web.common.ConstraintChecker;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.Util;
import org.libreplan.web.common.components.bandboxsearch.BandboxMultipleSearch;
import org.libreplan.web.common.components.finders.FilterPair;
import org.libreplan.web.costcategories.ResourcesCostCategoryAssignmentController;
import org.libreplan.web.resources.search.ResourcePredicate;
import org.libreplan.web.resources.worker.CriterionsController;
import org.libreplan.web.resources.worker.CriterionsMachineController;
import org.libreplan.web.resources.worker.WorkerCRUDController.LimitingResourceEnum;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Window;

/**
 * Controller for {@link Machine} resource <br />
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class MachineCRUDController extends BaseCRUDController<Machine> {

    private IMachineModel machineModel;

    private Component configurationUnits;

    private CriterionsMachineController criterionsController;

    private MachineConfigurationController configurationController;

    private ResourcesCostCategoryAssignmentController resourcesCostCategoryAssignmentController;

    private Grid listing;

    private Datebox filterStartDate;

    private Datebox filterFinishDate;

    private Listbox filterLimitingResource;

    private Textbox txtfilter;

    private BandboxMultipleSearch bdFilters;

    private BaseCalendarsComboitemRenderer baseCalendarsComboitemRenderer = new BaseCalendarsComboitemRenderer();

    public List<Machine> getMachines() {
        return machineModel.getMachines();
    }

    public Machine getMachine() {
        return machineModel.getMachine();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        setupCriterionsController();
        setupConfigurationController();
        setupResourcesCostCategoryAssignmentController(comp);
        showListWindow();
        initFilterComponent();
        setupFilterLimitingResourceListbox();
    }

    private void initFilterComponent() {
        this.filterFinishDate = (Datebox) listWindow
                .getFellowIfAny("filterFinishDate");
        this.filterStartDate = (Datebox) listWindow
                .getFellowIfAny("filterStartDate");
        this.filterLimitingResource = (Listbox) listWindow
                .getFellowIfAny("filterLimitingResource");
        this.bdFilters = (BandboxMultipleSearch) listWindow
                .getFellowIfAny("bdFilters");
        this.txtfilter = (Textbox) listWindow.getFellowIfAny("txtfilter");
        this.listing = (Grid) listWindow.getFellowIfAny("listing");
        clearFilterDates();
    }

    private void setupCriterionsController() throws Exception {
        final Component comp = editWindow.getFellowIfAny("criterionsContainer");
        criterionsController = new CriterionsMachineController();
        criterionsController.doAfterCompose(comp);
    }

    private void setupConfigurationController() {
        configurationUnits = editWindow.getFellow("configurationUnits");
        configurationController = (MachineConfigurationController) configurationUnits
                .getVariable("configurationController", true);
    }

    private void setupResourcesCostCategoryAssignmentController(Component comp) {
        Component costCategoryAssignmentContainer =
            editWindow.getFellowIfAny("costCategoryAssignmentContainer");
        resourcesCostCategoryAssignmentController = (ResourcesCostCategoryAssignmentController)
            costCategoryAssignmentContainer.getVariable("assignmentController", true);
    }

    @Override
    protected void initCreate() {
        machineModel.initCreate();
        criterionsController.prepareForCreate(machineModel.getMachine());
        configurationController.initConfigurationController(machineModel);
        resourcesCostCategoryAssignmentController.setResource(machineModel.getMachine());
        selectMachineDataTab();
        resourceCalendarModel.cancel();
    }

    @Override
    protected void initEdit(Machine machine) {
        machineModel.initEdit(machine);
        prepareCriterionsForEdit();
        prepareCalendarForEdit();
        selectMachineDataTab();
        configurationController.initConfigurationController(machineModel);
        resourcesCostCategoryAssignmentController.setResource(machineModel.getMachine());
    }

    private void selectMachineDataTab() {
        Tab tabMachineData = (Tab) editWindow.getFellow("tbMachineData");
        tabMachineData.setSelected(true);
    }

    private void prepareCriterionsForEdit() {
        criterionsController.prepareForEdit(machineModel.getMachine());
    }

    private void prepareCalendarForEdit() {
        if (isCalendarNull()) {
            return;
        }

        updateCalendarController();
        resourceCalendarModel.initEdit(machineModel.getCalendarOfMachine());
        try {
            baseCalendarEditionController.doAfterCompose(editCalendarWindow);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        baseCalendarEditionController.setSelectedDay(new LocalDate());
        Util.reloadBindings(editCalendarWindow);
        Util.reloadBindings(createNewVersionWindow);
    }

    @Override
    protected void beforeSaving() throws ValidationException {
        validateConstraints();
    }

    @Override
    protected void save() throws ValidationException {
        saveCalendar();
        if (!confirmCriterions()) {
            return;
        }
        machineModel.confirmSave();
    }

    private void validateConstraints() {
        Tab tab = (Tab) editWindow.getFellowIfAny("tbMachineData");
        try {
            validateMachineDataTab();
            tab = (Tab) editWindow.getFellowIfAny("assignedCriteriaTab");
            criterionsController.validateConstraints();
            tab = (Tab) editWindow.getFellowIfAny("costCategoryAssignmentTab");
            resourcesCostCategoryAssignmentController.validateConstraints();
            // TODO: check 'calendar' tab
        } catch (WrongValueException e) {
            tab.setSelected(true);
            throw e;
        }
    }

    private void validateMachineDataTab() {
        ConstraintChecker.isValid(editWindow
                .getFellowIfAny("machineDataTabpanel"));
    }

    private void saveCalendar() throws ValidationException {
        if (baseCalendarEditionController != null) {
            baseCalendarEditionController.save();
        }
        if (machineModel.getCalendar() == null) {
            createCalendar();
        }
    }

    private boolean confirmCriterions() throws ValidationException {
        if (criterionsController != null) {
            if (!criterionsController.validate()) {
                return false;
            }
            criterionsController.save();
        }
        return true;
    }

    public List<BaseCalendar> getBaseCalendars() {
        return machineModel.getBaseCalendars();
    }

    private IBaseCalendarModel resourceCalendarModel;

    private void createCalendar() {
        Combobox combobox = (Combobox) editWindow
                .getFellow("createDerivedCalendar");
        Comboitem selectedItem = combobox.getSelectedItem();
        if (selectedItem == null) {
            throw new WrongValueException(combobox,
                    _("Please, select a calendar"));
        }

        BaseCalendar parentCalendar = (BaseCalendar) combobox.getSelectedItem()
                .getValue();
        if (parentCalendar == null) {
            parentCalendar = machineModel.getDefaultCalendar();
        }

        resourceCalendarModel.initCreateDerived(parentCalendar);
        resourceCalendarModel.generateCalendarCodes();
        machineModel.setCalendar((ResourceCalendar) resourceCalendarModel
                .getBaseCalendar());
    }

    private Window editCalendarWindow;

    private Window createNewVersionWindow;

    private BaseCalendarEditionController baseCalendarEditionController;

    private void updateCalendarController() {
        editCalendarWindow = (Window) editWindow
                .getFellowIfAny("editCalendarWindow");
        createNewVersionWindow = (Window) editWindow
                .getFellowIfAny("createNewVersion");

        createNewVersionWindow.setVisible(true);
        createNewVersionWindow.setVisible(false);

        baseCalendarEditionController = new BaseCalendarEditionController(
                resourceCalendarModel, editCalendarWindow,
                createNewVersionWindow, messagesForUser) {

            @Override
            public void goToList() {
                machineModel
                        .setCalendarOfMachine((ResourceCalendar) resourceCalendarModel
                                .getBaseCalendar());
                reloadWindow();
            }

            @Override
            public void cancel() {
                machineModel.removeCalendar();
                resourceCalendarModel.cancel();
                reloadWindow();
            }

            @Override
            public void save() {
                validateCalendarExceptionCodes();
                ResourceCalendar calendar = (ResourceCalendar) resourceCalendarModel
                        .getBaseCalendar();
                if (calendar != null) {
                    resourceCalendarModel.generateCalendarCodes();
                    machineModel.setCalendarOfMachine(calendar);
                }
                reloadWindow();
            }

            @Override
            public void saveAndContinue() {
                save();
            }

        };

        editCalendarWindow.setVariable("calendarController", this, true);
        createNewVersionWindow.setVariable("calendarController", this, true);
    }

    private void reloadWindow() {
        Util.reloadBindings(editWindow);
    }

    public boolean isCalendarNull() {
        return (machineModel.getCalendarOfMachine() == null);
    }

    public boolean isCalendarNotNull() {
        return !isCalendarNull();
    }

    public BaseCalendarEditionController getEditionController() {
        return baseCalendarEditionController;
    }

    @SuppressWarnings("unused")
    private CriterionsController getCriterionsController() {
        return (CriterionsController) editWindow.getFellow(
                "criterionsContainer").getAttribute(
                "assignedCriterionsController");
    }

    public MachineConfigurationController getConfigurationController() {
        return configurationController;
    }

    public BaseCalendarsComboitemRenderer getBaseCalendarsComboitemRenderer() {
        return baseCalendarsComboitemRenderer;
    }

    private class BaseCalendarsComboitemRenderer implements ComboitemRenderer {

        @Override
        public void render(Comboitem item, Object data) {
            BaseCalendar calendar = (BaseCalendar) data;
            item.setLabel(calendar.getName());
            item.setValue(calendar);

            if (isDefaultCalendar(calendar)) {
                Combobox combobox = (Combobox) item.getParent();
                combobox.setSelectedItem(item);
            }
        }

        private boolean isDefaultCalendar(BaseCalendar calendar) {
            BaseCalendar defaultCalendar = machineModel.getDefaultCalendar();
            return defaultCalendar.getId().equals(calendar.getId());
        }

    }

    /**
     * Operations to filter the machines by multiple filters
     */

    public Constraint checkConstraintFinishDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date finishDate = (Date) value;
                if ((finishDate != null)
                        && (filterStartDate.getValue() != null)
                        && (finishDate.compareTo(filterStartDate.getValue()) < 0)) {
                    filterFinishDate.setValue(null);
                    throw new WrongValueException(comp,
                            _("must be after start date"));
                }
            }
        };
    }

    public Constraint checkConstraintStartDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date startDate = (Date) value;
                if ((startDate != null)
                        && (filterFinishDate.getValue() != null)
                        && (startDate.compareTo(filterFinishDate.getValue()) > 0)) {
                    filterStartDate.setValue(null);
                    throw new WrongValueException(comp,
                            _("must be lower than end date"));
                }
            }
        };
    }

    public void onApplyFilter() {
        ResourcePredicate predicate = createPredicate();
        if (predicate != null) {
            filterByPredicate(predicate);
        } else {
            showAllMachines();
        }
    }

    private ResourcePredicate createPredicate() {
        List<FilterPair> listFilters = (List<FilterPair>) bdFilters
                .getSelectedElements();

        String personalFilter = txtfilter.getValue();
        // Get the dates filter
        LocalDate startDate = null;
        LocalDate finishDate = null;
        if (filterStartDate.getValue() != null) {
            startDate = LocalDate.fromDateFields(filterStartDate
                .getValue());
        }
        if (filterFinishDate.getValue() != null) {
            finishDate = LocalDate.fromDateFields(filterFinishDate
                .getValue());
        }

        final Listitem item = filterLimitingResource.getSelectedItem();
        Boolean isLimitingResource = (item != null) ? LimitingResourceEnum
                .valueOf((LimitingResourceEnum) item.getValue()) : null;

        if (listFilters.isEmpty()
                && (personalFilter == null || personalFilter.isEmpty())
                && startDate == null && finishDate == null
                && isLimitingResource == null) {
            return null;
        }
        return new ResourcePredicate(listFilters, personalFilter, startDate,
                finishDate, isLimitingResource);
    }

    private void filterByPredicate(ResourcePredicate predicate) {
        List<Machine> filteredResources = machineModel
                .getFilteredMachines(predicate);
        listing.setModel(new SimpleListModel(filteredResources.toArray()));
        listing.invalidate();
    }

    private void clearFilterDates() {
        filterStartDate.setValue(null);
        filterFinishDate.setValue(null);
    }

    public void showAllMachines() {
        listing.setModel(new SimpleListModel(machineModel.getAllMachines()
                .toArray()));
        listing.invalidate();
    }

    private void setupFilterLimitingResourceListbox() {
        for(LimitingResourceEnum resourceEnum :
            LimitingResourceEnum.getLimitingResourceFilterOptionList()) {
            Listitem item = new Listitem();
            item.setParent(filterLimitingResource);
            item.setValue(resourceEnum);
            item.appendChild(new Listcell(resourceEnum.toString()));
            filterLimitingResource.appendChild(item);
        }
        filterLimitingResource.setSelectedIndex(0);
    }

    public Set<LimitingResourceEnum> getLimitingResourceOptionList() {
        return LimitingResourceEnum.getLimitingResourceOptionList();
    }

    public Object getLimitingResource() {
        final Machine machine = getMachine();
        return (machine != null) ? LimitingResourceEnum.valueOf(machine
                .isLimitingResource())
                : LimitingResourceEnum.NON_LIMITING_RESOURCE;         // Default option
    }

    public void setLimitingResource(LimitingResourceEnum option) {
        Machine machine = getMachine();
        if (machine != null) {
            machine.setResourceType(LimitingResourceEnum.toResourceType(option));
        }
    }

    public boolean isEditing() {
        return (getMachine() != null && !getMachine().isNewObject());
    }

    public void onCheckGenerateCode(Event e) {
        CheckEvent ce = (CheckEvent) e;
        if (ce.isChecked()) {
            // we have to auto-generate the code if it's unsaved
            try {
                machineModel.setCodeAutogenerated(ce.isChecked());
            } catch (ConcurrentModificationException err) {
                messagesForUser.showMessage(Level.ERROR, err.getMessage());
            }
            Util.reloadBindings(editWindow);
        }
    }

    @Override
    protected boolean beforeDeleting(Machine machine) {
        if (!machineModel.canRemove(machine)) {
            messagesForUser
                    .showMessage(
                            Level.WARNING,
                            _("Machine cannot be deleted. Machine is allocated to a project or contains imputed hours"));
            return false;
        }
        return true;
    }

    @Override
    protected void delete(Machine machine) {
        try {
            machineModel.confirmRemove(machine);
        } catch (InstanceNotFoundException e) {
            messagesForUser.showMessage(Level.INFO,
                    _("Machine was already removed"));
        }
    }

    public RowRenderer getMachinesRenderer() {
        return new RowRenderer() {

            @Override
            public void render(Row row, Object data) {
                final Machine machine = (Machine) data;
                row.setValue(machine);

                row.addEventListener(Events.ON_CLICK,
                        new EventListener() {
                            @Override
                    public void onEvent(Event event) {
                                goToEditForm(machine);
                            }
                        });

                row.appendChild(new Label(machine.getName()));
                row.appendChild(new Label(machine.getDescription()));
                row.appendChild(new Label(machine.getCode()));
                row.appendChild(new Label((Boolean.TRUE.equals(machine
                        .isLimitingResource())) ? _("yes") : _("no")));

                Hbox hbox = new Hbox();
                hbox.appendChild(Util.createEditButton(new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        goToEditForm(machine);
                    }
                }));
                hbox.appendChild(Util.createRemoveButton(new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        confirmDelete(machine);
                    }
                }));
                row.appendChild(hbox);
            }

        };
    }

    @Override
    protected String getEntityType() {
        return "Machine";
    }

    @Override
    protected String getPluralEntityType() {
        return "Machines";
    }

    @Override
    protected Machine getEntityBeingEdited() {
        return machineModel.getMachine();
    }

}
