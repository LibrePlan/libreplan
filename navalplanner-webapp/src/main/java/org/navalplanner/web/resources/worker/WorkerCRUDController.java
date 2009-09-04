package org.navalplanner.web.resources.worker;

import static org.navalplanner.web.I18nHelper._;
import static org.navalplanner.web.common.ConcurrentModificationDetector.addAutomaticHandlingOfConcurrentModification;

import java.util.Date;
import java.util.List;

import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.calendars.BaseCalendarEditionController;
import org.navalplanner.web.calendars.IBaseCalendarModel;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.entrypoints.IURLHandlerRegistry;
import org.navalplanner.web.common.entrypoints.URLHandler;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Radio;
import org.zkoss.zul.api.Window;

/**
 * Controller for {@link Worker} resource <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class WorkerCRUDController extends GenericForwardComposer implements
        IWorkerCRUDControllerEntryPoints {

    private Window createWindow;

    private Window listWindow;

    private Window editWindow;

    private Window workRelationshipsWindow;

    private Window addWorkRelationshipWindow;

    private Window editWorkRelationshipWindow;

    private IWorkerModel workerModel;

    private IURLHandlerRegistry URLHandlerRegistry;

    private OnlyOneVisible visibility;

    private IMessagesForUser messages;

    private Component messagesContainer;

    private WorkRelationshipsController addWorkRelationship;

    private LocalizationsController localizationsForEditionController;

    private LocalizationsController localizationsForCreationController;

    private WorkRelationshipsController editWorkRelationship;

    private IWorkerCRUDControllerEntryPoints workerCRUD;

    private Window editCalendarWindow;

    private BaseCalendarEditionController baseCalendarEditionController;

    private IBaseCalendarModel resourceCalendarModel;

    private Window createNewVersionWindow;

    public WorkerCRUDController() {
    }

    public WorkerCRUDController(Window createWindow, Window listWindow,
            Window editWindow, Window workRelationshipsWindow,
            Window addWorkRelationshipWindow,
            Window editWorkRelationshipWindow, Window editCalendarWindow,
            IWorkerModel workerModel,
            IMessagesForUser messages,
            IWorkerCRUDControllerEntryPoints workerCRUD) {
        this.createWindow = createWindow;
        this.listWindow = listWindow;
        this.editWindow = editWindow;
        this.workRelationshipsWindow = workRelationshipsWindow;
        this.addWorkRelationshipWindow = addWorkRelationshipWindow;
        this.editWorkRelationshipWindow = editWorkRelationshipWindow;
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

    public LocalizationsController getLocalizations() {
        if (workerModel.isCreating())
            return localizationsForCreationController;
        return localizationsForEditionController;
    }

    public void save() {
        try {
            baseCalendarEditionController.save();
            workerModel.save();
            goToList();
            Util.reloadBindings(listWindow);
            messages.showMessage(Level.INFO, _("Worker saved"));
        } catch (ValidationException e) {
            for (InvalidValue invalidValue : e.getInvalidValues()) {
                messages.invalidValue(invalidValue);
            }
        }
    }

    public void cancel() {
        goToList();
    }

    public void goToList() {
        getBookmarker().goToList();
        getVisibility().showOnly(listWindow);
    }

    public void goToEditForm(Worker worker) {
        getBookmarker().goToEditForm(worker);
        workerModel.prepareEditFor(worker);
        if (isCalendarNotNull()) {
            editCalendar();
        }
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    public void goToEditForm() {
        if (isCalendarNotNull()) {
            editCalendar();
        }
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    public void goToWorkRelationshipsForm(Worker worker) {
        getVisibility().showOnly(workRelationshipsWindow);
        Util.reloadBindings(workRelationshipsWindow);
    }

    public void goToWorkRelationshipsForm() {
        getVisibility().showOnly(workRelationshipsWindow);
        Util.reloadBindings(workRelationshipsWindow);
    }

    public void goToAddWorkRelationshipForm() {
        this.addWorkRelationship.prepareForCreate();
        getVisibility().showOnly(addWorkRelationshipWindow);
    }

    public void goToCreateForm() {
        getBookmarker().goToCreateForm();
        workerModel.prepareForCreate();
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    public void goToEditWorkRelationshipForm(CriterionSatisfaction satisfaction) {
        this.editWorkRelationship.prepareForEdit(satisfaction);
        getVisibility().showOnly(editWorkRelationshipWindow);
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
                comp, "createWindow");
        comp.setVariable("controller", this, true);
        if (messagesContainer == null)
            throw new RuntimeException(_("MessagesContainer is needed"));
        messages = new MessagesForUser(messagesContainer);
        this.addWorkRelationship = new WorkRelationshipsController(
                this.workerModel, this, messages);
        setupWorkRelationshipController(this.addWorkRelationship,
                this.addWorkRelationshipWindow);
        setupWorkRelationshipController(
                this.editWorkRelationship = new WorkRelationshipsController(
                        this.workerModel, this, messages),
                editWorkRelationshipWindow);

        final URLHandler<IWorkerCRUDControllerEntryPoints> handler = URLHandlerRegistry
                .getRedirectorFor(IWorkerCRUDControllerEntryPoints.class);
        handler.registerListener(this, page);
        getVisibility().showOnly(listWindow);

        editCalendarWindow = (Window) getCurrentWindow()
                .getFellow("editCalendarWindow");
        createNewVersionWindow = (Window) getCurrentWindow()
                .getFellow("createNewVersion");
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
                workerModel
                        .setCalendar((ResourceCalendar) resourceCalendarModel
                                .getBaseCalendar());
                reloadCurrentWindow();
            }

        };
        editCalendarWindow.setVariable("calendarController", this, true);
        createNewVersionWindow.setVariable("calendarController", this, true);
    }

    public BaseCalendarEditionController getEditionController() {
        return baseCalendarEditionController;
    }

    private void setupWorkRelationshipController(
            WorkRelationshipsController workRelationshipController,
            Window workRelationshipWindow) throws Exception {
        workRelationshipController.doAfterCompose(workRelationshipWindow);
        workRelationshipWindow.setVariable("workRelationship",
                workRelationshipController, true);
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
            visibility = new OnlyOneVisible(listWindow, editWindow,
                    createWindow, workRelationshipsWindow,
                    addWorkRelationshipWindow, editWorkRelationshipWindow);
        }
        return visibility;
    }

    public GenericForwardComposer getWorkRelationship() {
        return this.addWorkRelationship;
    }

    private IWorkerCRUDControllerEntryPoints getBookmarker() {
        return workerCRUD;
    }

    public List<BaseCalendar> getBaseCalendars() {
        return workerModel.getBaseCalendars();
    }

    public void calendarChecked(Radio radio) {
        Combobox comboboxDerived = (Combobox) radio
                .getFellow("createDerivedCalendar");
        Combobox comboboxCopy = (Combobox) radio
                .getFellow("createCopyCalendar");

        String selectedId = radio.getId();
        if (selectedId.equals("createFromScratch")) {
            comboboxDerived.setDisabled(true);
            comboboxCopy.setDisabled(true);
        } else if (selectedId.equals("createDerived")) {
            comboboxDerived.setDisabled(false);
            comboboxCopy.setDisabled(true);
        } else if (selectedId.equals("createCopy")) {
            comboboxDerived.setDisabled(true);
            comboboxCopy.setDisabled(false);
        }
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

    public void createCalendar(String optionId) {
        if (optionId.equals("createFromScratch")) {
            resourceCalendarModel.initCreate();
        } else if (optionId.equals("createDerived")) {
            Combobox combobox = (Combobox) getCurrentWindow().getFellow(
                    "createDerivedCalendar");
            Comboitem selectedItem = combobox.getSelectedItem();
            if (selectedItem == null) {
                throw new WrongValueException(combobox,
                        "You should select one calendar");
            }
            BaseCalendar parentCalendar = (BaseCalendar) combobox
                    .getSelectedItem().getValue();
            resourceCalendarModel.initCreateDerived(parentCalendar);
        } else if (optionId.equals("createCopy")) {
            Combobox combobox = (Combobox) getCurrentWindow().getFellow(
                    "createCopyCalendar");
            Comboitem selectedItem = combobox.getSelectedItem();
            if (selectedItem == null) {
                throw new WrongValueException(combobox,
                        "You should select one calendar");
            }
            BaseCalendar origCalendar = (BaseCalendar) combobox
                    .getSelectedItem().getValue();
            resourceCalendarModel.initCreateCopy(origCalendar);
        } else {
            throw new RuntimeException(
                    "Unknow option '" + optionId
                    + "' to create a resource calendar");
        }

        workerModel.setCalendar((ResourceCalendar) resourceCalendarModel
                .getBaseCalendar());
        try {
            baseCalendarEditionController.doAfterCompose(editCalendarWindow);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        baseCalendarEditionController.setSelectedDay(new Date());
        Util.reloadBindings(editCalendarWindow);
        Util.reloadBindings(createNewVersionWindow);
        reloadCurrentWindow();
    }

    public void editCalendar() {
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
        if (workerModel.isCreating()) {
            return createWindow;
        } else {
            return editWindow;
        }
    }

}
