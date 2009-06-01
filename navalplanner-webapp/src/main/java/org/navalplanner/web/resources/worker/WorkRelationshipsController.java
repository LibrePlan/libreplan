package org.navalplanner.web.resources.worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Worker;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Listbox;

/**
 * Subcontroller for {@link Worker} resource <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class WorkRelationshipsController extends GenericForwardComposer {

    private IWorkerModel workerModel;
    private WorkerCRUDController workerCRUDController;
    private CriterionSatisfaction newRelationship = new CriterionSatisfaction();
    private Collection<Criterion> workCriterions;
    private Listbox selectedWorkCriterion;
    private Datebox newWorkRelationshipStartDate;
    private Datebox newWorkRelationshipEndDate;
    private HashMap<Criterion, CriterionWithItsType> fromCriterionToType;

    public WorkRelationshipsController(IWorkerModel workerModel,
            WorkerCRUDController workerCRUDController) {
        this.workerModel = workerModel;
        this.workerCRUDController = workerCRUDController;
        this.newRelationship = new CriterionSatisfaction();
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

    public Set<CriterionSatisfaction> getCriterionSatisfactions() {
        if (this.workerCRUDController.getWorker() == null) {
            return new HashSet<CriterionSatisfaction>();
        } else {
            // Obtain just workRelationshipSatisfactions
            return workerModel
                    .getLaboralRelatedCriterionSatisfactions(this.workerCRUDController
                            .getWorker());
        }
    }

    public void addCriterionSatisfaction() {

        Criterion selectedCriterion = (Criterion) selectedWorkCriterion
                .getSelectedItem().getValue();
        CriterionWithItsType criterionWithItsType = fromCriterionToType
                .get(selectedCriterion);
        if (newWorkRelationshipEndDate == null) {
            this.workerCRUDController.getWorker().activate(
                    criterionWithItsType,
                    newWorkRelationshipStartDate.getValue());
        } else {
            this.workerCRUDController.getWorker().activate(
                    criterionWithItsType,
                    newWorkRelationshipStartDate.getValue(),
                    newWorkRelationshipEndDate.getValue());
        }

        this.workerCRUDController
                .goToWorkRelationshipsForm(this.workerCRUDController
                        .getWorker());

    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.selectedWorkCriterion.setSelectedIndex(0);
    }

    public CriterionSatisfaction getNewRelationship() {
        return this.newRelationship;
    }

    public Collection<Criterion> getWorkCriterions() {
        return this.workCriterions;
    }

}
