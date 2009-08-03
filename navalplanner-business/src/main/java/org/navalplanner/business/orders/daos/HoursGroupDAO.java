package org.navalplanner.business.orders.daos;

import org.navalplanner.business.common.daos.impl.GenericDAOHibernate;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * Dao for {@link HoursGroup}
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class HoursGroupDAO extends GenericDAOHibernate<HoursGroup, Long>
        implements IHoursGroupDAO {
}
