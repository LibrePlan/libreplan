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

package org.navalplanner.web.resources.worker;

import static org.navalplanner.web.I18nHelper._;

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.ResourceType;
import org.navalplanner.business.resources.entities.VirtualWorker;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.calendars.BaseCalendarEditionController;
import org.navalplanner.web.calendars.IBaseCalendarModel;
import org.navalplanner.web.common.BaseCRUDController.CRUDControllerState;
import org.navalplanner.web.common.ConstraintChecker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.bandboxsearch.BandboxMultipleSearch;
import org.navalplanner.web.common.components.finders.FilterPair;
import org.navalplanner.web.common.entrypoints.EntryPointsHandler;
import org.navalplanner.web.common.entrypoints.IURLHandlerRegistry;
import org.navalplanner.web.costcategories.ResourcesCostCategoryAssignmentController;
import org.navalplanner.web.resources.search.ResourcePredicate;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
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
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Caption;
import org.zkoss.zul.api.Window;

/**
 * Controller for {@link Worker} resource <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class WorkerCRUDController extends GenericForwardComposer implements
        IWorkerCRUDControllerEntryPoints {

    private Window listWindow;

    private Window editWindow;

    private IWorkerModel workerModel;

    private IURLHandlerRegistry URLHandlerRegistry;

    private OnlyOneVisible visibility;

    private IMessagesForUser messages;

    private Component messagesContainer;

    private CriterionsController criterionsController;

    private LocalizationsController localizationsForEditionController;

    private LocalizationsController localizationsForCreationController;

    private ResourcesCostCategoryAssignmentController resourcesCostCategoryAssignmentController;

    private IWorkerCRUDControllerEntryPoints workerCRUD;

    private Window editCalendarWindow;

    private BaseCalendarEditionController baseCalendarEditionController;

    private IBaseCalendarModel resourceCalendarModel;

    private Window createNewVersionWindow;

    private BaseCalendarsComboitemRenderer baseCalendarsComboitemRenderer = new BaseCalendarsComboitemRenderer();

    private Grid listing;

    private Datebox filterStartDate;

    private Datebox filterFinishDate;

    private Listbox filterLimitingResource;

    private BandboxMultipleSearch bdFilters;

    private Textbox txtfilter;

    private Tab personalDataTab;

    private Tab assignedCriteriaTab;

    private Tab costCategoryAssignmentTab;

    private CRUDControllerState state = CRUDControllerState.LIST;

    public WorkerCRUDController() {
    }

    public WorkerCRUDController(Window listWindow, Window editWindow,
            Window editCalendarWindow,
            IWorkerModel workerModel,
            IMessagesForUser messages,
            IWorkerCRUDControllerEntryPoints workerCRUD) {
        this.listWindow = listWindow;
        this.editWindow = editWindow;
        this.workerModel = workerModel;
        this.messages = messages;
        this.workerCRUD = workerCRUD;
        this.editCalendarWindow = editCalendarWindow;
    }

    public Worker getWorker() {
        return workerModel.getWorker();
    }

    public List<Worker> getWorkers() {
        return workerModel.getWorkers();
    }

    public List<Worker> getRealWorkers() {
        return workerModel.getRealWorkers();
    }

    public List<Worker> getVirtualWorkers() {
        return workerModel.getVirtualWorkers();
    }

    public LocalizationsController getLocalizations() {
        if (workerModel.isCreating()) {
            return localizationsForCreationController;
        }
        return localizationsForEditionController;
    }

    public void saveAndExit() {
        if (save()) {
            goToList();
        }
    }

    public void saveAndContinue() {
        if (save()) {
            if (!getWorker().isVirtual()) {
                goToEditForm(getWorker());
            } else {
                this.goToEditVirtualWorkerForm(getWorker());
            }
        }
    }

    public boolean save() {
        validateConstraints();

        // Validate 'Cost category assignment' tab is correct
        if (resourcesCostCategoryAssignmentController != null) {
            if (!resourcesCostCategoryAssignmentController.validate()) {
                return false;
            }
        }

        try {
            if (baseCalendarEditionController != null) {
                baseCalendarEditionController.save();
            }
            if (criterionsController != null){
                if(!criterionsController.validate()){
                    return false;
                }
            }
            if (workerModel.getWorker().isVirtual()) {
                workerModel.setCapacity(getVirtualWorkerCapacity());
            }
            if (workerModel.getCalendar() == null) {
                createCalendar();
            }
            workerModel.save();
            messages.showMessage(Level.INFO, _("Worker saved"));
            return true;
        } catch (ValidationException e) {
            messages.showInvalidValues(e);
        }
        return false;
    }

    private void validateConstraints() {
        Tab selectedTab = personalDataTab;
        try {
            validatePersonalDataTab();

            selectedTab = assignedCriteriaTab;
            validateAssignedCriteriaTab();

            selectedTab = costCategoryAssignmentTab;
            validateCostCategoryAssigmentTab();

            //TODO: check 'calendar' tab
        }
        catch (WrongValueException e) {
            selectedTab.setSelected(true);
            throw e;
        }
    }

    private void validatePersonalDataTab() {
        ConstraintChecker.isValid(editWindow.getFellowIfAny("personalDataTabpanel"));
    }

    private void validateAssignedCriteriaTab() {
        criterionsController.validateConstraints();
    }

    private void validateCostCategoryAssigmentTab() {
        resourcesCostCategoryAssignmentController.validateConstraints();
    }

    public void cancel() {
        goToList();
    }

    @Override
    public void goToList() {
        state = CRUDControllerState.LIST;
        getVisibility().showOnly(listWindow);
        Util.reloadBindings(listWindow);
    }

    @Override
    public void goToEditForm(Worker worker) {
        state = CRUDControllerState.EDIT;
        getBookmarker().goToEditForm(worker);
        workerModel.prepareEditFor(worker);
        resourcesCostCategoryAssignmentController.setResource(workerModel
                .getWorker());
        if (isCalendarNotNull()) {
            editCalendar();
        }
        editAsignedCriterions();
        showEditWindow(_("Edit Worker: {0}", worker.getHumanId()));
    }

    public void goToEditVirtualWorkerForm(Worker worker) {
        state = CRUDControllerState.EDIT;
        workerModel.prepareEditFor(worker);
        resourcesCostCategoryAssignmentController.setResource(workerModel
                .getWorker());
        if (isCalendarNotNull()) {
            editCalendar();
        }
        editAsignedCriterions();
        showEditWindow(_("Edit Virtual Workers Group: {0}", worker.getHumanId()));
    }

    public void goToEditForm() {
        state = CRUDControllerState.EDIT;
        if (isCalendarNotNull()) {
            editCalendar();
        }
        showEditWindow(_("Edit Worker: {0}", getWorker().getHumanId()));
    }

    @Override
    public void goToCreateForm() {
        state = CRUDControllerState.CREATE;
        getBookmarker().goToCreateForm();
        workerModel.prepareForCreate();
        createAsignedCriterions();
        resourcesCostCategoryAssignmentController.setResource(workerModel
                .getWorker());
        showEditWindow(_("Create Worker"));
        resourceCalendarModel.cancel();
    }

    private void showEditWindow(String title) {
        personalDataTab.setSelected(true);
        ((Caption) editWindow.getFellow("caption")).setLabel(title);
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        localizationsForEditionController = createLocalizationsController(comp,
                "editWindow");
        localizationsForCreationController = createLocalizationsController(
                comp, "editWindow");
        comp.setVariable("controller", this, true);
        if (messagesContainer == null) {
            throw new RuntimeException(_("MessagesContainer is needed"));
        }
        messages = new MessagesForUser(messagesContainer);
        setupResourcesCostCategoryAssignmentController(comp);

        final EntryPointsHandler<IWorkerCRUDControllerEntryPoints> handler = URLHandlerRegistry
                .getRedirectorFor(IWorkerCRUDControllerEntryPoints.class);
        handler.register(this, page);
        getVisibility().showOnly(listWindow);
        initFilterComponent();
        setupFilterLimitingResourceListbox();
        initializeTabs();
    }

    private void initializeTabs() {
        personalDataTab = (Tab) editWindow.getFellow("personalDataTab");
        assignedCriteriaTab = (Tab) editWindow.getFellow("assignedCriteriaTab");
        costCategoryAssignmentTab = (Tab) editWindow.getFellow("costCategoryAssignmentTab");
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

    private void setupResourcesCostCategoryAssignmentController(Component comp) {
        Component costCategoryAssignmentContainer =
            editWindow.getFellowIfAny("costCategoryAssignmentContainer");
        resourcesCostCategoryAssignmentController = (ResourcesCostCategoryAssignmentController)
            costCategoryAssignmentContainer.getVariable("assignmentController", true);
    }

    private void editAsignedCriterions() {
        try{
            setupCriterionsController();
            criterionsController.prepareForEdit( workerModel.getWorker());
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private void createAsignedCriterions(){
        try{
            setupCriterionsController();
            criterionsController.prepareForCreate( workerModel.getWorker());
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private void setupCriterionsController() throws Exception {
        criterionsController = new CriterionsController(workerModel);
        criterionsController.doAfterCompose(getCurrentWindow().
                getFellow("criterionsContainer"));
    }

    public BaseCalendarEditionController getEditionController() {
        return baseCalendarEditionController;
    }

    private LocalizationsController createLocalizationsController(
            Component comp, String localizationsContainerName) throws Exception {
        LocalizationsController localizationsController = new LocalizationsController(
                workerModel);
        localizationsController
                .doAfterCompose(comp.getFellow(localizationsContainerName)
                        .getFellow("localizationsContainer"));
        return localizationsController;
    }

    private OnlyOneVisible getVisibility() {
        if (visibility == null) {
            visibility = new OnlyOneVisible(listWindow, editWindow);
        }
        return visibility;
    }

    private IWorkerCRUDControllerEntryPoints getBookmarker() {
        return workerCRUD;
    }

    public List<BaseCalendar> getBaseCalendars() {
        return workerModel.getBaseCalendars();
    }

    public boolean isCalendarNull() {
        if (workerModel.getCalendar() != null) {
            return false;
        }
        return true;
    }

    public boolean isCalendarNotNull() {
        return !isCalendarNull();
    }

    private void createCalendar() {
        Combobox combobox = (Combobox) getCurrentWindow().getFellow(
                "createDerivedCalendar");
        Comboitem selectedItem = combobox.getSelectedItem();
        if (selectedItem == null) {
            throw new WrongValueException(combobox,
                    "You should select one calendar");
        }
        BaseCalendar parentCalendar = (BaseCalendar) combobox.getSelectedItem()
                .getValue();
        if (parentCalendar == null) {
            parentCalendar = workerModel.getDefaultCalendar();
        }

        resourceCalendarModel.initCreateDerived(parentCalendar);
        resourceCalendarModel.generateCalendarCodes();
        workerModel.setCalendar((ResourceCalendar) resourceCalendarModel
                .getBaseCalendar());
    }

    public void editCalendar() {
        updateCalendarController();
        resourceCalendarModel.initEdit(workerModel.getCalendar());
        try {
            baseCalendarEditionController.doAfterCompose(editCalendarWindow);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        baseCalendarEditionController.setSelectedDay(new LocalDate());
        Util.reloadBindings(editCalendarWindow);
        Util.reloadBindings(createNewVersionWindow);
    }

    public BaseCalendarEditionController getBaseCalendarEditionController() {
        return baseCalendarEditionController;
    }

    private void reloadCurrentWindow() {
        Util.reloadBindings(getCurrentWindow());
    }

    private Window getCurrentWindow() {
            return editWindow;
    }

    private void updateCalendarController() {
        editCalendarWindow = (Window) getCurrentWindow().getFellow(
                "editCalendarWindow");
        createNewVersionWindow = (Window) getCurrentWindow().getFellow(
                "createNewVersion");

        createNewVersionWindow.setVisible(true);
        createNewVersionWindow.setVisible(false);

        baseCalendarEditionController = new BaseCalendarEditionController(
                resourceCalendarModel, editCalendarWindow,
                createNewVersionWindow, messages) {

            @Override
            public void goToList() {
                workerModel
                        .setCalendar((ResourceCalendar) resourceCalendarModel
                                .getBaseCalendar());
                reloadCurrentWindow();
            }

            @Override
            public void cancel() {
                workerModel.removeCalendar();
                resourceCalendarModel.cancel();
                reloadCurrentWindow();
            }

            @Override
            public void save() {
                validateCalendarExceptionCodes();
                Integer capacity = workerModel.getCapacity();
                ResourceCalendar calendar = (ResourceCalendar) resourceCalendarModel
                        .getBaseCalendar();
                if (calendar != null) {
                    resourceCalendarModel.generateCalendarCodes();
                    workerModel.setCalendar(calendar);
                }
                reloadCurrentWindow();
                workerModel.setCapacity(capacity);
            }

            @Override
            public void saveAndContinue() {
                save();
            }

        };

        editCalendarWindow.setVariable("calendarController", this, true);
        createNewVersionWindow.setVariable("calendarController", this, true);
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
            BaseCalendar defaultCalendar = workerModel.getDefaultCalendar();
            return defaultCalendar.getId().equals(calendar.getId());
        }
    }

    public void goToCreateVirtualWorkerForm() {
        state = CRUDControllerState.CREATE;
        workerModel.prepareForCreate(true);
        createAsignedCriterions();
        resourcesCostCategoryAssignmentController.setResource(workerModel
                .getWorker());
        showEditWindow(_("Create Virtual Workers Group"));
        resourceCalendarModel.cancel();
    }

    public boolean isVirtualWorker() {
        boolean isVirtual = false;
        if (this.workerModel != null) {
            if (this.workerModel.getWorker() != null ) {
                isVirtual = this.workerModel.getWorker().isVirtual();
            }
        }
        return isVirtual;
    }

    public boolean isRealWorker() {
        return !isVirtualWorker();
    }

    public String getVirtualWorkerObservations() {
        if (isVirtualWorker()) {
            return ((VirtualWorker) this.workerModel.getWorker())
                    .getObservations();
        } else {
            return "";
        }
    }

    public void setVirtualWorkerObservations(String observations) {
        if (isVirtualWorker()) {
            ((VirtualWorker) this.workerModel.getWorker())
                    .setObservations(observations);
        }
    }

    public Integer getVirtualWorkerCapacity() {
        if (isVirtualWorker()) {
            if (this.workerModel.getCalendar() != null) {
                return this.workerModel.getCapacity();
            }
        }
        return 1;
    }

    public void setVirtualWorkerCapacity(Integer capacity) {
        this.workerModel.setCapacity(capacity);
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
                            _("must be greater than start date"));
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
                            _("must be lower than finish date"));
                }
            }
        };
    }

    public void onApplyFilter() {
        ResourcePredicate predicate = createPredicate();
        if (predicate != null) {
            filterByPredicate(predicate);
        } else {
            showAllWorkers();
        }
    }

    private ResourcePredicate createPredicate() {
        List<FilterPair> listFilters = bdFilters
                .getSelectedElements();

        String personalFilter = txtfilter.getValue();

        // Get the dates filter
        LocalDate startDate = null;
        LocalDate finishDate = null;
        if (filterStartDate.getValue() != null) {
            startDate = LocalDate.fromDateFields(filterStartDate.getValue());
        }
        if (filterFinishDate.getValue() != null) {
            finishDate = LocalDate.fromDateFields(filterFinishDate.getValue());
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
        List<Worker> filteredResources = workerModel
                .getFilteredWorker(predicate);
        listing.setModel(new SimpleListModel(filteredResources.toArray()));
        listing.invalidate();
    }

    private void clearFilterDates() {
        filterStartDate.setValue(null);
        filterFinishDate.setValue(null);
    }

    public void showAllWorkers() {
        listing.setModel(new SimpleListModel(workerModel.getAllCurrentWorkers()
                .toArray()));
        listing.invalidate();
    }

    public enum LimitingResourceEnum {
        ALL(_("ALL")),
        LIMITING_RESOURCE(_("LIMITING RESOURCE")),
        NON_LIMITING_RESOURCE(_("NON LIMITING RESOURCE"));

        /**
         * Forces to mark the string as needing translation
         */
        private static String _(String string) {
            return string;
        }

        private String option;

        private LimitingResourceEnum(String option) {
            this.option = option;
        }

        @Override
        public String toString() {
            return _(option);
        }

        public static LimitingResourceEnum valueOf(Boolean isLimitingResource) {
            return (Boolean.TRUE.equals(isLimitingResource)) ? LIMITING_RESOURCE : NON_LIMITING_RESOURCE;
        }

        public static Boolean valueOf(LimitingResourceEnum option) {
            if (LIMITING_RESOURCE.equals(option)) {
                return true;
            } else if (NON_LIMITING_RESOURCE.equals(option)) {
                return false;
            } else {
                return null;
            }
        }

        public static Set<LimitingResourceEnum> getLimitingResourceOptionList() {
            return EnumSet.of(
                    LimitingResourceEnum.LIMITING_RESOURCE,
                    LimitingResourceEnum.NON_LIMITING_RESOURCE);
        }

        public static Set<LimitingResourceEnum> getLimitingResourceFilterOptionList() {
            return EnumSet.of(LimitingResourceEnum.ALL,
                    LimitingResourceEnum.LIMITING_RESOURCE,
                    LimitingResourceEnum.NON_LIMITING_RESOURCE);
        }

        public static ResourceType toResourceType(LimitingResourceEnum limitingResource) {
            if (LIMITING_RESOURCE.equals(limitingResource)) {
                return ResourceType.LIMITING_RESOURCE;
            }
            return ResourceType.NON_LIMITING_RESOURCE;
        }

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
        final Worker worker = getWorker();
        return (worker != null) ? LimitingResourceEnum.valueOf(worker
                .isLimitingResource())
                : LimitingResourceEnum.NON_LIMITING_RESOURCE;         // Default option
    }

    public void setLimitingResource(LimitingResourceEnum option) {
        Worker worker = getWorker();
        if (worker != null) {
            worker.setResourceType(LimitingResourceEnum.toResourceType(option));
        }
    }

    public boolean isEditing() {
        return (getWorker() != null && !getWorker().isNewObject());
    }

    public void onCheckGenerateCode(Event e) {
        CheckEvent ce = (CheckEvent) e;
        if (ce.isChecked()) {
            // we have to auto-generate the code if it's unsaved
            try {
                workerModel.setCodeAutogenerated(ce.isChecked());
            } catch (ConcurrentModificationException err) {
                messages.showMessage(Level.ERROR, err.getMessage());
            }
        }
        Util.reloadBindings(editWindow);
    }

    public void confirmRemove(Worker worker) {
        try {
            int status = Messagebox.show(_("Confirm deleting this worker. Are you sure?"), _("Delete"),
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);
            if (Messagebox.OK != status) {
                return;
            }
            if(workerModel.canRemove(worker)) {
                workerModel.confirmRemove(worker);
                messages.showMessage(Level.INFO, _("Worker deleted"));
                goToList();
            }
            else {
                messages.showMessage(Level.WARNING,
                        _("This worker cannot be deleted because it has assignments to projects or imputed hours"));
            }
        } catch (InterruptedException e) {
            messages.showMessage(
                    Level.ERROR, e.getMessage());
        } catch (InstanceNotFoundException e) {
            messages.showMessage(
                    Level.INFO, _("This worker was already removed by other user"));
        }
    }

    public RowRenderer getWorkersRenderer() {
        return new RowRenderer() {

            @Override
            public void render(Row row, Object data) {
                final Worker worker = (Worker) data;
                row.setValue(worker);

                row.addEventListener(Events.ON_CLICK,
                        new EventListener() {
                            @Override
                    public void onEvent(Event event) {
                                goToEditForm(worker);
                            }
                        });

                row.appendChild(new Label(worker.getSurname()));
                row.appendChild(new Label(worker.getFirstName()));
                row.appendChild(new Label(worker.getNif()));
                row.appendChild(new Label(worker.getCode()));
                row.appendChild(new Label((Boolean.TRUE.equals(worker
                        .isLimitingResource())) ? _("yes") : _("no")));

                Hbox hbox = new Hbox();
                hbox.appendChild(Util.createEditButton(new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        goToEditForm(worker);
                    }
                }));
                hbox.appendChild(Util.createRemoveButton(new EventListener() {
                    @Override
                    public void onEvent(Event event) {
                        confirmRemove(worker);
                    }
                }));
                row.appendChild(hbox);
            }

        };
    }

    public void updateWindowTitle() {
        if (editWindow != null && state != CRUDControllerState.LIST) {
            Worker worker = getWorker();

            String entityType = _("Worker");
            if (worker.isVirtual()) {
                entityType = _("Virtual Workers Group");
            }

            String humanId = worker.getHumanId();

            String title;
            switch (state) {
            case CREATE:
                if (StringUtils.isEmpty(humanId)) {
                    title = _("Create {0}", entityType);
                } else {
                    title = _("Create {0}: {1}", entityType, humanId);
                }
                break;
            case EDIT:
                title = _("Edit {0}: {1}", entityType, humanId);
                break;
            default:
                throw new IllegalStateException(
                        "You should be in creation or edition mode to use this method");
            }
            ((Caption) editWindow.getFellow("caption")).setLabel(title);
        }
    }

}
