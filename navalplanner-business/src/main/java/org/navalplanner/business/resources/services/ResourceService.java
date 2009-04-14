package org.navalplanner.business.resources.services;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.entities.Resource;

// FIXME: Define validation approach for creating and updating entities.

// FIXME: Originally we though the interface in a less generic way (e.g.
// createWorker, createResourceGroup, etc.). Now it is completely generic.

// FIXME: The interface must be oriented to detached objects (e.g. 
// removeResource(resource) instead of removeResource(resourceId))??? - It
// depends on final requirements.
public interface ResourceService {
    
    /**
     * It updates or inserts the resource passed as a parameter. If the
     * resource is a composite resource, updating or inserting is cascaded to
     * the resources contained in it.
     */
    public void saveResource(Resource resource);
    
    public Resource findResource(Long resourceId) 
        throws InstanceNotFoundException;
    
    /**
     * It adds a resource to a resource group. It the resource already belongs 
     * to a resource group, the resource is moved to the new group.
     */
    public void addResourceToResourceGroup(Long resourceId, 
        Long resourceGroupId) throws InstanceNotFoundException;
    
// FIXME: Is the following signature better than the previous one??? - I prefer
// the previous one.
// public void addResourceToResourceGroup(Long resourceId, Long resourceGroupId)
//     throws ResourceNotFoundException, ResourceGroupNotFoundException;
    
    public int getResourceDailyCapacity(Long resourceId) 
        throws InstanceNotFoundException;
    
    /**
     * It removes a resource. If the resource is a composite resource, the 
     * resources contained in it are not removed.
     */
    public void removeResource(Long resourceId) 
        throws InstanceNotFoundException;

}
