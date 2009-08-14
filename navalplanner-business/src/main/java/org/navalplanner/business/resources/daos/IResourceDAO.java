package org.navalplanner.business.resources.daos;

import java.util.List;

import org.navalplanner.business.common.daos.IGenericDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

/**
 * DAO interface for the <code>Resource</code> entity.
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public interface IResourceDAO extends IGenericDAO<Resource, Long> {
    public List<Worker> getWorkers();
}
