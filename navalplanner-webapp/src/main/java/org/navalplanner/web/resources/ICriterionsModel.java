package org.navalplanner.web.resources;

import java.util.Collection;
import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Resource;

/**
 * CriterionsModel contract <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ICriterionsModel {

    List<ICriterionType<?>> getTypes();

    Collection<Criterion> getCriterionsFor(ICriterionType<?> type);

    Criterion getCriterion();

    void prepareForCreate(ICriterionType<?> criterionType);

    void prepareForEdit(Criterion criterion);

    ICriterionType<?> getTypeFor(Criterion criterion);

    String getNameForCriterion();

    void setNameForCriterion(String name);

    void saveCriterion() throws ValidationException;

    boolean isEditing();

    boolean isCriterionActive();

    void setCriterionActive(boolean active);

    boolean isApplyableToWorkers();

    <T extends Resource> List<T> getResourcesSatisfyingCurrentCriterionOfType(
            Class<T> klass);

}
