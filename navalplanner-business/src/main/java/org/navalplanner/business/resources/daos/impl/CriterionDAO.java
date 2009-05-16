package org.navalplanner.business.resources.daos.impl;

import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.entities.Criterion;

/**
 * DAO implementation for Criterion. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CriterionDAO extends GenericDaoHibernate<Criterion, Criterion>
        implements ICriterionDAO {

    @Override
    public boolean exists(Criterion criterion) {
        return getSession().createCriteria(Criterion.class).add(
                Restrictions.eq("name", criterion.getName())).add(
                Restrictions.eq("type", criterion.getType()))
                .uniqueResult() != null;
    }

}
