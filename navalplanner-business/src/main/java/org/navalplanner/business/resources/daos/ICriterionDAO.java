package org.navalplanner.business.resources.daos;

import org.navalplanner.business.common.daos.IGenericDao;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.impl.CriterionDAO;
import org.navalplanner.business.resources.entities.Criterion;

/**
 * Contract for {@link CriterionDAO} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ICriterionDAO extends IGenericDao<Criterion, Long> {

    Criterion findByNameAndType(Criterion criterion);

    boolean existsByNameAndType(Criterion entity);

    Criterion find(Criterion criterion) throws InstanceNotFoundException;

}
