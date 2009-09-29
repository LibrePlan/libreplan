package org.navalplanner.business.planner.daos;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificDayAssignment;
import org.navalplanner.business.resources.entities.Criterion;
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

    List<ResourceAllocation<?>> findAllocationsRelatedTo(Resource resource);

    Map<Criterion, List<GenericResourceAllocation>> findGenericAllocationsByCriterion();

    List<SpecificDayAssignment> getSpecificAssignmentsBetween(
            Collection<Resource> relatedToOne,
            LocalDate start, LocalDate end);

}