package org.navalplanner.business.planner.daos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.resources.entities.Resource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@ResourceAllocation}
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ResourceAllocationDAO extends
        GenericDAOHibernate<ResourceAllocation, Long> implements
        IResourceAllocationDAO {

    @Override
    public List<ResourceAllocation<?>> findAllocationsRelatedToAnyOf(
            List<Resource> resources) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        result.addAll(findSpecificAllocationsRelatedTo(resources));
        result.addAll(findGenericAllocationsFor(resources));
        return result;
    }

    private List<GenericResourceAllocation> findGenericAllocationsFor(
            List<Resource> resources) {
        return (List<GenericResourceAllocation>) getSession().createCriteria(
                GenericResourceAllocation.class).setResultTransformer(
                Criteria.DISTINCT_ROOT_ENTITY)
                .createCriteria("genericDayAssignments").add(
                Restrictions.in("resource", resources)).list();
    }

    private List<SpecificResourceAllocation> findSpecificAllocationsRelatedTo(
            List<Resource> resources) {
        return (List<SpecificResourceAllocation>) getSession().createCriteria(
                SpecificResourceAllocation.class).add(
                Restrictions.in("resource", resources)).list();
    }

    @Override
    public List<ResourceAllocation<?>> findAllocationsRelatedTo(
            Resource resource) {
        return findAllocationsRelatedToAnyOf(Arrays.asList(resource));
    }

}