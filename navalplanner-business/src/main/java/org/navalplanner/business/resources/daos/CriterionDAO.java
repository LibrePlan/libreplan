package org.navalplanner.business.resources.daos;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO implementation for Criterion. <br />
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class CriterionDAO extends GenericDAOHibernate<Criterion, Long>
        implements ICriterionDAO {

    private static final Log log = LogFactory.getLog(CriterionDAO.class);

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public boolean thereIsOtherWithSameNameAndType(Criterion criterion) {
        List<Criterion> withSameNameAndType = findByNameAndType(criterion);
        if (withSameNameAndType.isEmpty())
            return false;
        if (withSameNameAndType.size() > 1)
            return true;
        return areDifferentInDB(withSameNameAndType.get(0), criterion);
    }

    private boolean areDifferentInDB(Criterion existentCriterion,
            Criterion other) {
        return !existentCriterion.getId().equals(other.getId());
    }

    @Override
    public List<Criterion> findByNameAndType(Criterion criterion) {
        if (criterion.getType() == null) return new ArrayList<Criterion>();

        Criteria c = getSession().createCriteria(Criterion.class);
        c.add(Restrictions.eq("name", criterion.getName()).ignoreCase())
                .createCriteria("type")
                .add(Restrictions.eq("name", criterion.getType().getName()).ignoreCase());

        return (List<Criterion>) c.list();
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

    @Override
    public List<Criterion> findByType(ICriterionType<?> type) {
        List<Criterion> list = list(Criterion.class);
        ArrayList<Criterion> result = new ArrayList<Criterion>();
        for (Criterion criterion : list) {
            if (type.contains(criterion)) {
                result.add(criterion);
            }
        }
        return result;
    }

    public List<Criterion> getAll() {
        return list(Criterion.class);
    }

}
