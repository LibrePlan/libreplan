package org.navalplanner.web.resources;

import java.util.HashSet;
import java.util.Set;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.zkoss.zk.ui.util.GenericForwardComposer;

/**
 * Subcontroller for {@link Worker} resource <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class WorkRelationshipsController extends GenericForwardComposer {

    private IWorkerModel workerModel;
    private WorkerCRUDController workerCRUDController;

    public WorkRelationshipsController(IWorkerModel workerModel,
            WorkerCRUDController workerCRUDController) {
        this.workerModel = workerModel;
        this.workerCRUDController = workerCRUDController;
    }

    public Set<CriterionSatisfaction> getCriterionSatisfactions() {
        if (this.workerCRUDController.getWorker() == null) {
            return new HashSet();
        } else {
            return workerModel.getCriterionSatisfactions(
                    this.workerCRUDController.getWorker());
        }
    }
}
