package org.navalplanner.web.resources.worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.resources.worker.IWorkerModel.AddingSatisfactionResult;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Listbox;

/**
 * Subcontroller for {@link Worker} resource <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class WorkRelationshipsController extends GenericForwardComposer {

    private IWorkerModel workerModel;

    private WorkerCRUDController workerCRUDController;

    /*
     * private CriterionSatisfaction newRelationship = new
     * CriterionSatisfaction();
     */

    private CriterionSatisfaction satisfactionEdited = new CriterionSatisfaction();

    private List<Criterion> workCriterions;

    private Listbox selectedWorkCriterion;

    private HashMap<Criterion, CriterionWithItsType> fromCriterionToType;

    private boolean editing;

    private Component containerComponent;

    private CriterionSatisfaction originalSatisfaction;

    private final IMessagesForUser messagesForUser;

    public WorkRelationshipsController(IWorkerModel workerModel,
            WorkerCRUDController workerCRUDController,
            IMessagesForUser messagesForUser) {
        this.workerModel = workerModel;
        this.workerCRUDController = workerCRUDController;
        this.messagesForUser = messagesForUser;
        this.workCriterions = new ArrayList<Criterion>();
        Map<ICriterionType<?>, Collection<Criterion>> map = workerModel
                .getLaboralRelatedCriterions();
        this.fromCriterionToType = new HashMap<Criterion, CriterionWithItsType>();
        for (Entry<ICriterionType<?>, Collection<Criterion>> entry : map
                .entrySet()) {
            this.workCriterions.addAll(entry.getValue());
            for (Criterion criterion : entry.getValue()) {
                this.fromCriterionToType.put(criterion,
                        new CriterionWithItsType(entry.getKey(), criterion));
            }
        }
    }

    public List<CriterionSatisfaction> getCriterionSatisfactions() {
        if (getWorker() == null) {
            return new ArrayList<CriterionSatisfaction>();
        } else {
            return workerModel
                    .getLaboralRelatedCriterionSatisfactions(getWorker());
        }
    }

    public void deleteCriterionSatisfaction(CriterionSatisfaction satisfaction)
            throws InstanceNotFoundException {
        workerCRUDController.getWorker().removeCriterionSatisfaction(
                satisfaction);
        this.workerCRUDController.goToEditForm();
    }

    public void prepareForCreate() {
        this.satisfactionEdited = new CriterionSatisfaction();
        this.originalSatisfaction = this.satisfactionEdited;
        Util.reloadBindings(containerComponent);
        editing = false;
    }

    public void prepareForEdit(CriterionSatisfaction criterionSatisfaction) {
        this.satisfactionEdited = criterionSatisfaction.copy();
        this.originalSatisfaction = criterionSatisfaction;
        Util.reloadBindings(containerComponent);
        this.satisfactionEdited.setCriterion(select(this.satisfactionEdited
                .getCriterion()));
        // the criterion retrieved is used instead of the original one, so the
        // call fromCriterionToType.get(criterion) works
        editing = true;
    }

    private Criterion select(Criterion criterion) {
        int i = 0;
        for (Criterion c : workCriterions) {
            if (c.isEquivalent(criterion)) {
                selectedWorkCriterion.setSelectedIndex(i);
                return c;
            }
            i++;
        }
        throw new RuntimeException("not found criterion" + criterion);
    }

    public void saveCriterionSatisfaction() {
        Criterion choosenCriterion = getChoosenCriterion();
        CriterionWithItsType criterionWithItsType = fromCriterionToType
                .get(choosenCriterion);
        satisfactionEdited.setCriterion(choosenCriterion);
        AddingSatisfactionResult addSatisfaction = workerModel.addSatisfaction(
                criterionWithItsType.getType(), originalSatisfaction,
                satisfactionEdited);
        switch (addSatisfaction) {
        case OK:
            messagesForUser.showMessage(Level.INFO, "Periodo gardado");
            this.workerCRUDController.goToEditForm();
            break;
        case SATISFACTION_WRONG:
            messagesForUser
                    .showMessage(Level.WARNING,
                            "O periodo ten datos inválidos. A fecha de fin debe ser posterior á de inicio");
            break;
        case DONT_COMPLY_OVERLAPPING_RESTRICTIONS:
            messagesForUser
                    .showMessage(Level.WARNING,
                            "O periodo non se puido gardar. Solápase cun periodo non compatible.");
            this.workerCRUDController.goToEditForm();
            break;
        default:
            throw new RuntimeException("unexpected: " + addSatisfaction);
        }
    }

    private Criterion getChoosenCriterion() {
        Criterion criterion;
        if (editing) {
            criterion = satisfactionEdited.getCriterion();
        } else {
            criterion = (Criterion) this.selectedWorkCriterion
                    .getSelectedItemApi().getValue();
        }
        return criterion;
    }

    private Worker getWorker() {
        return this.workerModel.getWorker();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.containerComponent = comp;
        this.selectedWorkCriterion.setSelectedIndex(0);
    }

    public CriterionSatisfaction getEditRelationship() {
        return this.satisfactionEdited;
    }

    public Collection<Criterion> getWorkCriterions() {
        return this.workCriterions;
    }

    public boolean isEditing() {
        return editing;
    }

}
