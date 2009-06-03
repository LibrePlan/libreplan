package org.navalplanner.web.resources.worker;

import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.entrypoints.EntryPoint;
import org.navalplanner.web.common.entrypoints.EntryPoints;

/**
 * Contract for {@link WorkerCRUDController}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@EntryPoints(page = "/resources/worker/worker.zul", registerAs = "workerCRUD")
public interface IWorkerCRUDControllerEntryPoints {

    @EntryPoint("edit")
    public abstract void goToEditForm(Worker worker);

    @EntryPoint("workRelationships")
    public abstract void goToWorkRelationshipsForm(Worker worker);

    @EntryPoint("create")
    public abstract void goToCreateForm();

}