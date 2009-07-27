package org.navalplanner.business.orders.daos;

import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
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
public class HoursGroupDao extends GenericDaoHibernate<HoursGroup, Long>
        implements IHoursGroupDao {
}
