package org.navalplanner.web.resources.worker;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Worker;

/**
 * Interface for {@link WorkerModel}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface IWorkerModel {

    void save() throws ValidationException;

    List<Worker> getWorkers();

    Worker getWorker();

    void prepareForCreate();

    void prepareEditFor(Worker worker);

    IMultipleCriterionActiveAssigner getLocalizationsAssigner();

    boolean isCreating();

    Set<CriterionSatisfaction> getCriterionSatisfactions(Worker worker);

    Worker findResource(long workerId);

    Map<ICriterionType<?>, Collection<Criterion>> getLaboralRelatedCriterions();

    Set<CriterionSatisfaction> getLaboralRelatedCriterionSatisfactions(
            Worker worker);

}