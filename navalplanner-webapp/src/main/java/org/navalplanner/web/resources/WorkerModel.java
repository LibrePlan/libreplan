package org.navalplanner.web.resources;

import java.util.List;

import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.resources.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Model for worker <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class WorkerModel implements IWorkerModel {

    private final ResourceService resourceService;

    @Autowired
    public WorkerModel(ResourceService resourceService) {
        if (resourceService == null)
            throw new IllegalArgumentException("resourceService cannot be null");
        this.resourceService = resourceService;
    }

    @Override
    public Worker createNewInstance() {
        return new Worker();
    }

    @Override
    public void save(Worker worker) {
        resourceService.saveResource(worker);
    }

    @Override
    public List<Worker> getWorkers() {
        return resourceService.getWorkers();
    }

}
