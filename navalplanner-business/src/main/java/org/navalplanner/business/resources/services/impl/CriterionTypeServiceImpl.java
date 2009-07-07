package org.navalplanner.business.resources.services.impl;


import java.util.List;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.daos.impl.CriterionTypeDAO;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.services.CriterionTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link CriterionTypeService} using {@link CriterionTypeDAO} <br />
 * @author Diego Pino García <dpino@igalia.com>
 */
@Transactional
@Component
public class CriterionTypeServiceImpl implements CriterionTypeService {

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Override
    public void createIfNotExists(CriterionType criterionType) throws ValidationException {
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

    @Override
    public void remove(CriterionType criterionType) throws InstanceNotFoundException {
        if (criterionType.getId() != null ) {
            criterionTypeDAO.remove(criterionType.getId());
        } else {
            criterionTypeDAO.removeByName(criterionType);
        }
    }

    @Transactional(rollbackFor=ValidationException.class)
    @Override
    public void save(CriterionType entity) throws ValidationException {
        criterionTypeDAO.save(entity);

        if (criterionTypeDAO.findByName(entity).size() > 1) {

            InvalidValue[] invalidValues = {
                new InvalidValue(entity.getName() + " already exists",
                    CriterionType.class, "name", entity.getName(), entity)
            };

            throw new ValidationException(invalidValues,
                "Couldn't save new criterionType");
        }
    }

}
