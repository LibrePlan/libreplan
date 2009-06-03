package org.navalplanner.web.resources.worker;

import java.util.Date;
import java.util.List;

import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionTypeBase;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.PredefinedCriterionTypes;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.resources.entities.WorkingRelationship;
import org.navalplanner.business.resources.services.CriterionService;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.IRedirectorRegistry;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Redirector;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.api.Window;

/**
 * Controller for {@link Worker} resource <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class WorkerCRUDController extends GenericForwardComposer implements
        WorkerCRUDLinks {

    private Window createWindow;

    private Window listWindow;

    private Window editWindow;

    private Window workRelationshipsWindow;

    private Window addWorkRelationshipWindow;

    private Window editWorkRelationshipWindow;

    private IWorkerModel workerModel;

    private IRedirectorRegistry redirectorRegistry;

    private OnlyOneVisible visibility;

    private IMessagesForUser messages;

    private Component messagesContainer;

    private WorkRelationshipsController workRelationship;

    private LocalizationsController localizationsForEditionController;

    private LocalizationsController localizationsForCreationController;

    private CriterionService criterionService;

    public WorkerCRUDController() {
    }

    public WorkerCRUDController(Window createWindow, Window listWindow,
            Window editWindow, Window workRelationshipsWindow,
            Window addWorkRelationshipWindow, Window editWorkRelationshipWindow,
            IWorkerModel workerModel, IMessagesForUser messages) {
        this.createWindow = createWindow;
        this.listWindow = listWindow;
        this.editWindow = editWindow;
        this.workRelationshipsWindow = workRelationshipsWindow;
        this.addWorkRelationshipWindow = addWorkRelationshipWindow;
        this.editWorkRelationshipWindow = editWorkRelationshipWindow;
        this.workerModel = workerModel;
        this.messages = messages;
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
            getVisibility().showOnly(listWindow);
            Util.reloadBindings(listWindow);
            messages.showMessage(Level.INFO, "traballador gardado");
        } catch (ValidationException e) {
            for (InvalidValue invalidValue : e.getInvalidValues()) {
                messages.invalidValue(invalidValue);
            }
        }
    }

    public void cancel() {
        getVisibility().showOnly(listWindow);
    }

    public void goToEditForm(Worker worker) {
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
       Criterion selectedCriterion = criterionService.load(
               WorkingRelationship.HIRED.criterion());
       CriterionWithItsType criteriontype = new CriterionWithItsType(
               PredefinedCriterionTypes.WORK_RELATIONSHIP, selectedCriterion);
       this.workerModel.getWorker().activate(criteriontype,new Date());
       /* CriterionSatisfaction newSatisfaction =
                new CriterionSatisfaction(
                    new Date(),
                    selectedCriterion,
                    this.workerModel.getWorker()); */
       CriterionSatisfaction newSatisfaction =
               this.workerModel.getWorker().
                getActiveSatisfactionsFor(selectedCriterion).iterator().next();
        this.workRelationship.setEditCriterionSatisfaction(newSatisfaction);
        getVisibility().showOnly(addWorkRelationshipWindow);
        Util.reloadBindings(addWorkRelationshipWindow);
//        getVisibility().showOnly(editWorkRelationshipWindow);
//        Util.reloadBindings(editWorkRelationshipWindow);
    }

    public void goToCreateForm() {
        workerModel.prepareForCreate();
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    public void goToEditWorkRelationshipForm(CriterionSatisfaction satisfaction) {
        this.workRelationship.setEditCriterionSatisfaction(satisfaction);
        getVisibility().showOnly(editWorkRelationshipWindow);
        Util.reloadBindings(editWorkRelationshipWindow);
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        localizationsForEditionController = createLocalizationsController(comp,
                "editWindow");
        localizationsForCreationController = createLocalizationsController(
                comp, "createWindow");
        comp.setVariable("controller", this, true);
        getVisibility().showOnly(listWindow);
        if (messagesContainer == null)
            throw new RuntimeException("messagesContainer is needed");
        messages = new MessagesForUser(messagesContainer);
        this.workRelationship =
                 new WorkRelationshipsController(this.workerModel,this);
        this.workRelationship.doAfterCompose(
                comp.getFellow("addWorkRelationshipWindow"));
        Redirector<WorkerCRUDLinks> redirector = redirectorRegistry
                .getRedirectorFor(WorkerCRUDLinks.class);
        redirector.applyTo(this);
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
                   addWorkRelationshipWindow, editWorkRelationshipWindow );
        }
        return visibility;
    }

    public GenericForwardComposer getWorkRelationship() {
        return this.workRelationship;
    }

}
