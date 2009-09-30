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
public interface ICriterionsModel_V2 {

    List<CriterionType> getTypes();

    Collection<Criterion> getCriterionsFor(ICriterionType<?> type);

    Criterion getCriterion();

    ICriterionTreeModel getCriterionTreeModel();

    ICriterionType<?> getCriterionType();

    void prepareForCreate();

    void prepareForCreate(CriterionType criterionType);

    public void prepareForRemove(CriterionType criterionType);

    public void prepareForEdit(CriterionType criterionType);

    public void remove(CriterionType criterionType);

    ICriterionType<?> getTypeFor(Criterion criterion);

    void saveCriterionType() throws ValidationException;

    boolean isEditing();

    boolean isApplyableToWorkers(Criterion criterion);

    <T extends Resource> List<T> getResourcesSatisfyingCurrentCriterionOfType(
            Class<T> klass);

    List<Worker> getAllWorkers();

    boolean getAllowHierarchy();

    void disableHierarchy();

    void updateEnabledCriterions(boolean isChecked);
}
