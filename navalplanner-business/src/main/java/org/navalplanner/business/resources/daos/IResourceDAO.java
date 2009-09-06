package org.navalplanner.business.resources.daos;

import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

/**
 * DAO interface for the <code>Resource</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IResourceDAO extends IGenericDAO<Resource, Long> {
    public List<Worker> getWorkers();

    /**
     * Returns all {@link Resource} which satisfy a set of {@link Criterion}
     */
    List<Resource> getAllByCriterions(Set<Criterion> criterions);

}
