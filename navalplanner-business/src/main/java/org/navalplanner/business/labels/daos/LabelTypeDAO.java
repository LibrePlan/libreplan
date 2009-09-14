package org.navalplanner.business.labels.daos;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.labels.entities.LabelType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@link LabelType}
 *
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class LabelTypeDAO extends GenericDAOHibernate<LabelType, Long> implements
        ILabelTypeDAO {

    @Override
    public List<LabelType> getAll() {
        return list(LabelType.class);
    }

    public boolean existsByName(LabelType labelType) {
        try {
            return findUniqueByName(labelType) != null;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    private LabelType findUniqueByName(LabelType labelType)
            throws InstanceNotFoundException {
        List<LabelType> list = findByName(labelType);
        if (list != null && list.size() != 1) {
            throw new InstanceNotFoundException(labelType, LabelType.class
                    .getName());
        }
        return list.get(0);
    }

    @SuppressWarnings("unchecked")
    private List<LabelType> findByName(LabelType labelType) {
        return getSession().createCriteria(LabelType.class).add(
                Restrictions.eq("name", labelType.getName())).list();
    }

}
