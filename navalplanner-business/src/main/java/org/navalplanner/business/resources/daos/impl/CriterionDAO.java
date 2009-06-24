package org.navalplanner.business.resources.daos.impl;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.entities.Criterion;

/**
 * DAO implementation for Criterion. <br />
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
public class CriterionDAO extends GenericDaoHibernate<Criterion, Long>
        implements ICriterionDAO {

    private static final Log log = LogFactory.getLog(CriterionDAO.class);

    @Override
    public List<Criterion> findByNameAndType(Criterion criterion) {
        Criteria c = getSession().createCriteria(Criterion.class);

        c.add(Restrictions.eq("name", criterion.getName()).ignoreCase());
        c.add(Restrictions.eq("type", criterion.getType()).ignoreCase());

        List result = (List<Criterion>) c.list();

        return result;
    }

    public Criterion findUniqueByNameAndType(Criterion criterion) throws InstanceNotFoundException {
        List<Criterion> list = findByNameAndType(criterion);

        if (list.size() != 1)
            throw new InstanceNotFoundException(criterion, Criterion.class
                    .getName());

        return list.get(0);
    }

    public boolean existsByNameAndType(Criterion criterion) {
        try {
            return findUniqueByNameAndType(criterion) != null;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    public Criterion find(Criterion criterion) throws InstanceNotFoundException {
        if (criterion.getId() != null)
            return super.find(criterion.getId());
        Criterion result = findUniqueByNameAndType(criterion);

        return result;
    }

    @Override
    public void removeByNameAndType(Criterion criterion) {
        try {
            Criterion reloaded = findUniqueByNameAndType(criterion);
            remove(reloaded.getId());
        } catch (InstanceNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

}
