package org.navalplanner.web.resources.worker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
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

/*    private CriterionSatisfaction newRelationship = new CriterionSatisfaction(); */

    private CriterionSatisfaction editRelationship = new CriterionSatisfaction();

    private Collection<Criterion> workCriterions;

    private Listbox selectedWorkCriterion;

/*    private Datebox newWorkRelationshipStartDate;

    private Datebox newWorkRelationshipEndDate; */

    private HashMap<Criterion, CriterionWithItsType> fromCriterionToType;

    public WorkRelationshipsController(IWorkerModel workerModel,
            WorkerCRUDController workerCRUDController) {
        this.workerModel = workerModel;
        this.workerCRUDController = workerCRUDController;
        this.workCriterions = new ArrayList<Criterion>();
        Map<ICriterionType<?>, Collection<Criterion>> map =
                workerModel.getLaboralRelatedCriterions();
        this.fromCriterionToType = new HashMap<Criterion, CriterionWithItsType>();
        for (Entry<ICriterionType<?>, Collection<Criterion>> entry : map.entrySet()) {
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
            return workerModel.getLaboralRelatedCriterionSatisfactions(
                    this.workerCRUDController.getWorker());
        }
    }

    public void deleteCriterionSatisfaction(CriterionSatisfaction satisfaction)
            throws InstanceNotFoundException {
        workerCRUDController.getWorker().removeCriterionSatisfaction(satisfaction);
        this.workerCRUDController.goToEditForm();
    }

    public void setEditCriterionSatisfaction(CriterionSatisfaction crit) {
        // the component should be preselected.
        this.editRelationship = crit;
    }

    public void saveCriterionSatisfaction() throws InstanceNotFoundException {

        // Add new criterion
        Criterion selectedCriterion = (Criterion) selectedWorkCriterion.getSelectedItem().getValue();
        CriterionWithItsType criterionWithItsType = fromCriterionToType.get(selectedCriterion);
        System.out.println( "SAVE!!: " + selectedCriterion.getName() );

        if (editRelationship.getStartDate() == null) {
            this.workerCRUDController.getWorker().activate(
                    criterionWithItsType,
                    editRelationship.getStartDate());
        } else {
            this.workerCRUDController.getWorker().activate(
                    criterionWithItsType,
                    editRelationship.getStartDate(),
                    editRelationship.getEndDate());
        }

        // Delete the former one
        workerCRUDController.getWorker().
                removeCriterionSatisfaction(this.editRelationship);

        this.workerCRUDController.goToEditForm();
    }

    public void addCriterionSatisfaction() {
        Criterion selectedCriterion = (Criterion) selectedWorkCriterion.getSelectedItem().getValue();
        CriterionWithItsType criterionWithItsType = fromCriterionToType.get(selectedCriterion);
        // never accessed: Unnecesary as edition does this.
        System.out.println( "SELECTED: " + criterionWithItsType.toString() );

        if (editRelationship.getStartDate() == null) {
            this.workerCRUDController.getWorker().activate(
                    criterionWithItsType,
                    editRelationship.getStartDate());
        } else {
            this.workerCRUDController.getWorker().activate(
                    criterionWithItsType,
                    editRelationship.getStartDate(),
                    editRelationship.getEndDate());
        }
        this.workerCRUDController.goToEditForm();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.selectedWorkCriterion.setSelectedIndex(0);
    }

    public CriterionSatisfaction getEditRelationship() {
        return this.editRelationship;
    }

    public Collection<Criterion> getWorkCriterions() {
        return this.workCriterions;
    }

}
