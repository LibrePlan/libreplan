package org.navalplanner.business.resources.daos.impl;

import java.util.List;
import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.impl.GenericDAOHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.CriterionType;
import org.springframework.stereotype.Component;

/**
 * DAO implementation for Criterion. <br />
 * @author Diego Pino Garcia <dpino@igalia.com>
 */

@Component
public class CriterionTypeDAO extends GenericDAOHibernate<CriterionType, Long>
        implements ICriterionTypeDAO {

    @Override
    public List<CriterionType> findByName(CriterionType criterionType) {
        Criteria c = getSession().createCriteria(CriterionType.class);

        c.add(Restrictions.eq("name", criterionType.getName()).ignoreCase());

        return (List<CriterionType>) c.list();
    }

    @Override
    public CriterionType findUniqueByName(CriterionType criterionType)
                throws InstanceNotFoundException {
        Validate.notNull(criterionType);

        return findUniqueByName(criterionType.getName());
    }

    @Override
    public CriterionType findUniqueByName(String name)
            throws InstanceNotFoundException {
          Criteria c = getSession().createCriteria(CriterionType.class);

          c.add(Restrictions.eq("name", name));

          return (CriterionType) c.uniqueResult();
    }

    @Override
    public boolean existsByName(CriterionType criterionType) {
        try {
            return findUniqueByName(criterionType) != null;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    public void removeByName(CriterionType criterionType) {
        try {
            CriterionType reloaded = findUniqueByName(criterionType);
            remove(reloaded.getId());
        } catch (InstanceNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
}
