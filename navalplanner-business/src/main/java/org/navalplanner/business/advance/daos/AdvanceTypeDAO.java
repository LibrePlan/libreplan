package org.navalplanner.business.advance.daos;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * Dao for {@link AdvanceType}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class AdvanceTypeDAO extends GenericDAOHibernate<AdvanceType, Long>
        implements IAdvanceTypeDAO {
    public boolean existsNameAdvanceType(String unitName) {
        try {

            return getSession().createCriteria(AdvanceType.class).add(
                    Restrictions.eq("unitName", unitName)).uniqueResult() != null;

        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }
    }

    @Override
    public AdvanceType findByName(String name) {
        return (AdvanceType) getSession().createCriteria(AdvanceType.class)
                .add(Restrictions.eq("unitName", name)).uniqueResult();
    }
}
