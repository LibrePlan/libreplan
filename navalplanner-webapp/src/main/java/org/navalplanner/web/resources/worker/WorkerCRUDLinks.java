package org.navalplanner.web.resources.worker;

import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.LinksDefiner;
import org.navalplanner.web.common.LinkToState;

/**
 * Contract for {@link WorkerCRUDController}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@LinksDefiner(page = "/resources/worker/worker.zul", beanName = "workerCRUD")
public interface WorkerCRUDLinks {

    @LinkToState("edit")
    public abstract void goToEditForm(Worker worker);

    @LinkToState("workRelationships")
    public abstract void goToWorkRelationshipsForm(Worker worker);

    @LinkToState("create")
    public abstract void goToCreateForm();

}