package org.navalplanner.business.resources.daos;

import java.util.List;
import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.Criterion;

/**
 * Contract for {@link CriterionDAO} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ICriterionDAO extends IGenericDAO<Criterion, Long> {

    public void removeByNameAndType(Criterion criterion);

    List<Criterion> findByNameAndType(Criterion criterion);

    Criterion findUniqueByNameAndType(Criterion criterion) throws InstanceNotFoundException;

    boolean existsByNameAndType(Criterion entity);

    Criterion find(Criterion criterion) throws InstanceNotFoundException;

}
