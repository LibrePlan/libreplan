package org.navalplanner.business.planner.limiting.daos;

import java.util.List;

import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.planner.entities.Dependency;
import org.navalplanner.business.planner.limiting.entities.LimitingResourceQueueDependency;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/*
 * DAO for the entity @{link LimitingResourceQueueDependency}
 *
 * @author Javier Moran Rua <jmoran@igalia.com>
 */

@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class LimitingResourceQueueDependencyDAO
    extends GenericDAOHibernate<LimitingResourceQueueDependency, Long>
    implements ILimitingResourceQueueDependencyDAO {

}
