package org.navalplanner.business.resources.daos;

import java.util.List;
import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.CriterionType;

/**
 * DAO for {@link CriterionTypeDAO} <br />
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface ICriterionTypeDAO extends IGenericDAO<CriterionType, Long> {

    CriterionType findUniqueByName(String name)
            throws InstanceNotFoundException;

    CriterionType findUniqueByName(CriterionType criterionType)
            throws InstanceNotFoundException;

    List<CriterionType> findByName(CriterionType criterionType);

    public boolean existsByName(CriterionType criterionType);

    public void removeByName(CriterionType criterionType);
}
