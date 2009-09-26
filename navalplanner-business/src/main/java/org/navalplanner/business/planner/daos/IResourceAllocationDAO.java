package org.navalplanner.business.planner.daos;

import java.util.List;

import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.resources.entities.Resource;

/**
 * DAO interface for {@link ResourceAllocation}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IResourceAllocationDAO extends
        IGenericDAO<ResourceAllocation, Long> {

    List<ResourceAllocation<?>> findAllocationsRelatedToAnyOf(
            List<Resource> resources);

}