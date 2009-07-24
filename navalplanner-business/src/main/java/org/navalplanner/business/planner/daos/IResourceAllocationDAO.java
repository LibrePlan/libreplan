package org.navalplanner.business.planner.daos;

import org.navalplanner.business.common.daos.IGenericDao;
import org.navalplanner.business.planner.entities.ResourceAllocation;

/**
 * DAO interface for {@link ResourceAllocation}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IResourceAllocationDAO extends
        IGenericDao<ResourceAllocation, Long> {

}