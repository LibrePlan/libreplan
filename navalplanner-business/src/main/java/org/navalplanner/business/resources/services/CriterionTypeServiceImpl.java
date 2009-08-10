package org.navalplanner.business.resources.services;


import java.util.List;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.CriterionTypeDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.CriterionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link ICriterionTypeService} using {@link CriterionTypeDAO} <br />
 * @author Diego Pino García <dpino@igalia.com>
 */
@Transactional
@Component
public class CriterionTypeServiceImpl implements ICriterionTypeService {

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Override
    public void createIfNotExists(CriterionType criterionType) {
        if (!exists(criterionType))
            save(criterionType);
    }

    @Override
    public boolean exists(CriterionType criterionType) {
        return criterionTypeDAO.exists(criterionType.getId())
                || criterionTypeDAO.existsByName(criterionType);
    }

    @Override
    public CriterionType findUniqueByName(CriterionType criterionType) {
        return findUniqueByName(criterionType.getName());
    }

    @Override
    public CriterionType findUniqueByName(String name) {
        try {
            return criterionTypeDAO.findUniqueByName(name);
        } catch (InstanceNotFoundException e) {
            return null;
        }
    }

    @Override
    public List<CriterionType> getAll() {
        return criterionTypeDAO.list(CriterionType.class);
    }

    public void remove(CriterionType criterionType) throws InstanceNotFoundException {
        if (criterionType.getId() != null ) {
            criterionTypeDAO.remove(criterionType.getId());
        } else {
            criterionTypeDAO.removeByName(criterionType);
        }
    }

    @Override
    public void save(CriterionType entity) {
        criterionTypeDAO.save(entity);
    }

}
