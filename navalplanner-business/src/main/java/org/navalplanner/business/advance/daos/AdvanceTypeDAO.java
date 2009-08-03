package org.navalplanner.business.advance.daos;

import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * Dao for {@link AdvanceType}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class AdvanceTypeDAO extends GenericDAOHibernate<AdvanceType, Long> implements IAdvanceTypeDAO{
}
