/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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
import static org.navalplanner.web.common.ConcurrentModificationDetector.addAutomaticHandlingOfConcurrentModification;

import java.util.Date;
import java.util.List;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.VirtualWorker;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.calendars.BaseCalendarEditionController;
import org.navalplanner.web.calendars.IBaseCalendarModel;
import org.navalplanner.web.common.ConstraintChecker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.entrypoints.IURLHandlerRegistry;
import org.navalplanner.web.common.entrypoints.URLHandler;
import org.navalplanner.web.costcategories.ResourcesCostCategoryAssignmentController;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Tab;
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

    public void save() {
        validateConstraints();
        try {
            if (baseCalendarEditionController != null) {
                baseCalendarEditionController.save();
            }
            if(criterionsController != null){
                if(!criterionsController.validate()){
                    return;
                }
            }
            if (workerModel.getWorker().isVirtual()) {
                workerModel.setCapacity(getVirtualWorkerCapacity());
            }
            if (workerModel.getCalendar() == null) {
                createCalendar();
            }
            workerModel.save();
            goToList();
            Util.reloadBindings(listWindow);
            messages.showMessage(Level.INFO, _("Worker saved"));
        } catch (ValidationException e) {
            messages.showInvalidValues(e);
        }
    }

    private void validateConstraints() {
        Tab tab = (Tab) editWindow.getFellowIfAny("personalDataTab");
        try {
            validatePersonalDataTab();
            tab = (Tab) editWindow.getFellowIfAny("assignedCriteriaTab");
            criterionsController.validateConstraints();
            tab = (Tab) editWindow.getFellowIfAny("costCategoryAssignmentTab");
            resourcesCostCategoryAssignmentController.validateConstraints();
            //TODO: check 'calendar' tab
        }
        catch(WrongValueException e) {
            tab.setSelected(true);
            throw e;
        }
    }

    private void validatePersonalDataTab() {
        ConstraintChecker.isValid(editWindow.getFellowIfAny("personalDataTabpanel"));
    }

    public void cancel() {
        goToList();
    }

    public void goToList() {
        getVisibility().showOnly(listWindow);
    }

    public void goToEditForm(Worker worker) {
            getBookmarker().goToEditForm(worker);
            workerModel.prepareEditFor(worker);
            resourcesCostCategoryAssignmentController.setResource(workerModel.getWorker());
            if (isCalendarNotNull()) {
                editCalendar();
            }
            editAsignedCriterions();
            editWindow.setTitle(_("Edit Worker"));
            getVisibility().showOnly(editWindow);
            Util.reloadBindings(editWindow);
    }

    public void goToEditVirtualWorkerForm(Worker worker) {
        workerModel.prepareEditFor(worker);
        resourcesCostCategoryAssignmentController.setResource(workerModel
                .getWorker());
        if (isCalendarNotNull()) {
            editCalendar();
        }
        editAsignedCriterions();
        editWindow.setTitle(_("Edit virtual worker groups"));
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }


    public void goToEditForm() {
        if (isCalendarNotNull()) {
            editCalendar();
        }
        editWindow.setTitle(_("Edit Worker"));
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    public void goToCreateForm() {
            getBookmarker().goToCreateForm();
            workerModel.prepareForCreate();
        if (workerModel.getCalendar() == null) {
            createCalendar();
        }
            createAsignedCriterions();
            resourcesCostCategoryAssignmentController.setResource(workerModel.getWorker());
            editWindow.setTitle(_("Create Worker"));
            getVisibility().showOnly(editWindow);
            Util.reloadBindings(editWindow);
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.workerModel = addAutomaticHandlingOfConcurrentModification(
                IWorkerModel.class, this.workerModel,
                "/resources/worker/worker.zul");
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

        final URLHandler<IWorkerCRUDControllerEntryPoints> handler = URLHandlerRegistry
                .getRedirectorFor(IWorkerCRUDControllerEntryPoints.class);
        handler.registerListener(this, page);
        getVisibility().showOnly(listWindow);
    }

    private void setupResourcesCostCategoryAssignmentController(Component comp)
    throws Exception {
        Component costCategoryAssignmentContainer =
            editWindow.getFellowIfAny("costCategoryAssignmentContainer");
        resourcesCostCategoryAssignmentController = (ResourcesCostCategoryAssignmentController)
            costCategoryAssignmentContainer.getVariable("assignmentController", true);
    }

    private void editAsignedCriterions(){
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

    private void setupCriterionsController()throws Exception {
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
        workerModel.setCalendar(parentCalendar.newDerivedResourceCalendar());
    }

    public void editCalendar() {
        updateCalendarController();
        resourceCalendarModel.initEdit(workerModel.getCalendar());
        try {
            baseCalendarEditionController.doAfterCompose(editCalendarWindow);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        baseCalendarEditionController.setSelectedDay(new Date());
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
                createNewVersionWindow) {

            @Override
            public void goToList() {
                workerModel
                        .setCalendar((ResourceCalendar) resourceCalendarModel
                                .getBaseCalendar());
                reloadCurrentWindow();
            }

            @Override
            public void cancel() {
                resourceCalendarModel.cancel();
                workerModel.setCalendar(null);
                reloadCurrentWindow();
            }

            @Override
            public void save() {
                Integer capacity = workerModel.getCapacity();
                workerModel
                        .setCalendar((ResourceCalendar) resourceCalendarModel
                                .getBaseCalendar());
                reloadCurrentWindow();
                workerModel.setCapacity(capacity);
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
        public void render(Comboitem item, Object data) throws Exception {
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
        workerModel.prepareForCreate(true);
        if (workerModel.getCalendar() == null) {
            createCalendar();
        }
        createAsignedCriterions();
        resourcesCostCategoryAssignmentController.setResource(workerModel
                .getWorker());
        editWindow.setTitle(_("Create virtual resource"));
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
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

}
