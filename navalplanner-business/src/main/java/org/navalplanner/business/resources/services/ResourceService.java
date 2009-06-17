package org.navalplanner.business.resources.services;

import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;

/**
 * Interface for the resource management service.
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public interface ResourceService {

    /**
     * It updates or inserts the resource passed as a parameter. If the resource
     * is a composite resource, updating or inserting is cascaded to the
     * resources contained in it.
     * @throws ValidationException
     */
    public void saveResource(Resource resource);

    /**
     * It checks if the version of the detached object passed as a parameter is
     * older than the one in the database. If it is older, it throws
     * <code>org.springframework.dao.OptimisticLockingFailureException</code>.
     * It can not be called as part of a READ-WRITE transaction.
     */
    public void checkVersion(Resource resource);

    public Resource findResource(Long resourceId)
            throws InstanceNotFoundException;

    public int getResourceDailyCapacity(Long resourceId)
            throws InstanceNotFoundException;

    /**
     * It removes a resource. If the resource is a composite resource, the
     * resources contained in it are not removed.
     */
    public void removeResource(Long resourceId)
            throws InstanceNotFoundException;

    public List<Worker> getWorkers();

    public List<Resource> getResources();

    public <T extends Resource> List<T> getResources(Class<T> klass);

    public Set<Resource> getSetOfResourcesSatisfying(ICriterion criterion);

}
