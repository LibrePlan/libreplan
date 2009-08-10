package org.navalplanner.business.resources.services;


import java.util.List;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.CriterionType;

/**
 * Services for {@link CriterionType} <br />
 * @author Diego Pino García <dpino@igalia.com>
 */
public interface ICriterionTypeService {

    void createIfNotExists(CriterionType criterionType);

    boolean exists(CriterionType criterionType);

    CriterionType findUniqueByName(CriterionType criterionType);

    CriterionType findUniqueByName(String name);

    List<CriterionType> getAll();

    void remove(CriterionType criterionType) throws InstanceNotFoundException;

    void save(CriterionType entity);

}
