package org.navalplanner.business.planner.daos;

import java.util.List;

import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.planner.entities.LimitingResourceQueueDependency;

/**
 * Interface for repositories to implement queies related to
 * @{link LimitingResourceQueueDependency} entities
 *
 * @author Javier Moran Rua <jmoran@igalia.com>
 *
 */
public interface ILimitingResourceQueueDependencyDAO extends
    IGenericDAO<LimitingResourceQueueDependency,Long> {

}
