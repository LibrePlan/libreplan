package org.navalplanner.business.planner.daos;

import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@ResourceAllocation}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ResourceAllocationDAO extends
        GenericDaoHibernate<ResourceAllocation, Long> implements
        IResourceAllocationDAO {

}