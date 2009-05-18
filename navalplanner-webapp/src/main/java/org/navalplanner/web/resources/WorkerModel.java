package org.navalplanner.web.resources;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.resources.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Model for worker <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class WorkerModel implements IWorkerModel {

    private final ResourceService resourceService;
    private Worker worker;
    private ClassValidator<Worker> workerValidator;

    @Autowired
    public WorkerModel(ResourceService resourceService) {
        if (resourceService == null)
            throw new IllegalArgumentException("resourceService cannot be null");
        this.resourceService = resourceService;
        this.workerValidator = new ClassValidator<Worker>(Worker.class);
    }

    @Override
    public void save() throws ValidationException {
        InvalidValue[] invalidValues = workerValidator
                .getInvalidValues(getWorker());
        if (invalidValues.length > 0) {
            throw new ValidationException(invalidValues);
        }
        resourceService.saveResource(worker);
    }

    @Override
    public List<Worker> getWorkers() {
        return resourceService.getWorkers();
    }

    @Override
    public Worker getWorker() {
        return worker;
    }

    @Override
    public void prepareForCreate() {
        worker = new Worker();
    }

    @Override
    public void prepareEditFor(Worker worker) {
        Validate.notNull(worker, "worker is not null");
        try {
            this.worker = (Worker) resourceService.findResource(worker.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
