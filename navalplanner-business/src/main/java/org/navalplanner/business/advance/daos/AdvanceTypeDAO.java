package org.navalplanner.business.advance.daos;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dao for {@link AdvanceType}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class AdvanceTypeDAO extends GenericDAOHibernate<AdvanceType, Long>
        implements IAdvanceTypeDAO {
    public boolean existsNameAdvanceType(String unitName) {
        return getSession().createCriteria(AdvanceType.class).add(
                Restrictions.eq("unitName", unitName)).uniqueResult() != null;
    }

    @Override
    @Transactional(readOnly = true)
    public AdvanceType findByName(String name) {
        return (AdvanceType) getSession().createCriteria(AdvanceType.class)
                .add(Restrictions.eq("unitName", name)).uniqueResult();
    }

    @Override
    public List<AdvanceType> findActivesAdvanceTypes() {
        return getSession().createCriteria(AdvanceType.class).add(
                Restrictions.eq("active", Boolean.TRUE)).list();
    }
}
