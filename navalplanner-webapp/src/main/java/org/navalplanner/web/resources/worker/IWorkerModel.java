package org.navalplanner.web.resources.worker;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    Map<ICriterionType<?>, Collection<Criterion>> getLaboralRelatedCriterions();

    List<CriterionSatisfaction> getLaboralRelatedCriterionSatisfactions();

    public enum AddingSatisfactionResult {
        OK, SATISFACTION_WRONG, DONT_COMPLY_OVERLAPPING_RESTRICTIONS;
    }

    AddingSatisfactionResult addSatisfaction(ICriterionType<?> type,
            CriterionSatisfaction originalSatisfaction,
            CriterionSatisfaction edited);

    void removeSatisfaction(CriterionSatisfaction satisfaction);

    public void assignCriteria(Collection<? extends Criterion> criteria);

    void unassignSatisfactions(
            Collection<? extends CriterionSatisfaction> satisfactions);

}