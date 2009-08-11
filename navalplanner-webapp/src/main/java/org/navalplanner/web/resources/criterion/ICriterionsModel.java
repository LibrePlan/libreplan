package org.navalplanner.web.resources.criterion;

import java.util.Collection;
import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

/**
 * CriterionsModel contract <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ICriterionsModel {

    List<CriterionType> getTypes();

    Collection<Criterion> getCriterionsFor(ICriterionType<?> type);

    Criterion getCriterion();

    void prepareForCreate(ICriterionType<?> criterionType);

    void workOn(Criterion criterion);

    ICriterionType<?> getTypeFor(Criterion criterion);

    void saveCriterion() throws ValidationException;

    boolean isEditing();

    boolean isApplyableToWorkers(Criterion criterion);

    <T extends Resource> List<T> getResourcesSatisfyingCurrentCriterionOfType(
            Class<T> klass);

    List<Worker> getAllWorkers();

    boolean isChangeAssignmentsDisabled();

    void activateAll(Collection<? extends Resource> selected);

    void deactivateAll(Collection<? extends Resource> unSelectedWorkers);

    void save(Criterion criterion) throws ValidationException;

}
