package org.navalplanner.business.planner.daos;

import java.util.List;

import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueDependency;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * DAO for entity @{link Dedenpency}
 * @author Javier Moran Rua <jmoran@igalia.com>
 *
 */

@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class DependencyDAO extends GenericDAOHibernate<Dependency,Long>
    implements IDependencyDAO {

}
