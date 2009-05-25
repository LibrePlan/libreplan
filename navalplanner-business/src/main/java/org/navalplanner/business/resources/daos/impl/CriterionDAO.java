package org.navalplanner.business.resources.daos.impl;

import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.entities.Criterion;

/**
 * DAO implementation for Criterion. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CriterionDAO extends GenericDaoHibernate<Criterion, Long>
        implements ICriterionDAO {

    public Criterion findByNameAndType(Criterion criterion) {
        return (Criterion) getSession().createCriteria(Criterion.class).add(
                Restrictions.eq("name", criterion.getName())).add(
                Restrictions.eq("type", criterion.getType())).uniqueResult();
    }

    public boolean existsByNameAndType(Criterion criterion) {
        return findByNameAndType(criterion) != null;
    }

    @Override
    public Criterion find(Criterion criterion) throws InstanceNotFoundException {
        if (criterion.getId() != null)
            return super.find(criterion.getId());
        Criterion result = findByNameAndType(criterion);
        if (result == null)
            throw new InstanceNotFoundException(criterion, Criterion.class
                    .getName());
        return result;
    }

}
