package org.navalplanner.web.resources.worker;

import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.IRedirectorRegistry;
import org.navalplanner.web.common.Redirecter;
import org.navalplanner.web.common.Redirector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Redirects to the page composed by {@link WorkerCRUDController} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Redirecter
public class WorkerCRUDControllerRedirector implements IWorkerCRUDController {

    private Redirector<?> redirector;

    @Autowired
    public WorkerCRUDControllerRedirector(IRedirectorRegistry registry) {
        redirector = registry.getRedirectorFor(IWorkerCRUDController.class);
    }

    @Override
    public void goToCreateForm() {
        redirector.doRedirect();
    }

    @Override
    public void goToEditForm(Worker worker) {
        redirector.doRedirect(worker);
    }

    @Override
    public void goToWorkRelationshipsForm(Worker worker) {
        redirector.doRedirect(worker);
    }

}
