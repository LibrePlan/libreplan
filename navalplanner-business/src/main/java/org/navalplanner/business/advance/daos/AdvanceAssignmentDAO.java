package org.navalplanner.business.advance.daos;

import org.navalplanner.business.advance.entities.AdvanceAssignment;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * Dao for {@link AdvanceAssignment}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class AdvanceAssignmentDAO extends
        GenericDAOHibernate<AdvanceAssignment, Long> implements
        IAdvanceAssignmentDAO {
}