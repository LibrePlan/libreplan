package org.navalplanner.web.resources.worker;

import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.Linkable;
import org.navalplanner.web.common.Page;

/**
 * Contract for {@link WorkerCRUDController}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Page("/resources/worker/worker.zul")
public interface IWorkerCRUDController {

    @Linkable("edit")
    public abstract void goToEditForm(Worker worker);

    @Linkable("workRelationships")
    public abstract void goToWorkRelationshipsForm(Worker worker);

    @Linkable("create")
    public abstract void goToCreateForm();

}