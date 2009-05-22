package org.navalplanner.web.resources;

import java.util.List;

import java.util.Set;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.Worker;

/**
 * Interface for workerModel. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IWorkerModel {

    void save() throws ValidationException;

    List<Worker> getWorkers();

    Worker getWorker();

    void prepareForCreate();

    void prepareEditFor(Worker worker);

    Set<CriterionSatisfaction> getCriterionSatisfactions(Worker worker);
}
