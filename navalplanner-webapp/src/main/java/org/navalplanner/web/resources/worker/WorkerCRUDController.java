package org.navalplanner.web.resources.worker;

import static org.navalplanner.web.common.ConcurrentModificationDetector.detectConcurrentModification;

import java.util.List;

import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.entrypoints.IURLHandlerRegistry;
import org.navalplanner.web.common.entrypoints.URLHandler;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
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

    public WorkerCRUDController() {
    }

    public WorkerCRUDController(Window createWindow, Window listWindow,
            Window editWindow, Window workRelationshipsWindow,
            Window addWorkRelationshipWindow,
            Window editWorkRelationshipWindow, IWorkerModel workerModel,
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
            workerModel.save();
            goToList();
            Util.reloadBindings(listWindow);
            messages.showMessage(Level.INFO, "traballador gardado");
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
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    public void goToEditForm() {
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
        this.workerModel = detectConcurrentModification(IWorkerModel.class,
                this.workerModel, "/resources/worker/worker.zul");
        localizationsForEditionController = createLocalizationsController(comp,
                "editWindow");
        localizationsForCreationController = createLocalizationsController(
                comp, "createWindow");
        comp.setVariable("controller", this, true);
        if (messagesContainer == null)
            throw new RuntimeException("messagesContainer is needed");
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

}
