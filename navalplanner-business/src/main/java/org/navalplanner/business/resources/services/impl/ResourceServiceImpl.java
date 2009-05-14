package org.navalplanner.business.resources.services.impl;

import java.util.List;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.IResourceDao;
import org.navalplanner.business.resources.daos.IResourceGroupDao;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.ResourceGroup;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.resources.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the resource management service. Resource DAOs are
 * autowired.
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@Transactional
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private IResourceDao resourceDao;

    @Autowired
    private IResourceGroupDao resourceGroupDao;

    public void saveResource(Resource resource) {
        resourceDao.save(resource);
    }

    @Transactional(readOnly = true)
    public Resource findResource(Long resourceId)
            throws InstanceNotFoundException {

        return resourceDao.find(resourceId);

    }

    public void addResourceToResourceGroup(Long resourceId, Long resourceGroupId)
            throws InstanceNotFoundException {

        ResourceGroup resourceGroup = resourceGroupDao.find(resourceGroupId);

        resourceGroup.addResource(resourceId);

    }

    @Transactional(readOnly = true)
    public int getResourceDailyCapacity(Long resourceId)
            throws InstanceNotFoundException {

        return resourceDao.find(resourceId).getDailyCapacity();

    }

    public void removeResource(Long resourceId)
            throws InstanceNotFoundException {

        resourceDao.find(resourceId).remove();
    }

    @Override
    public List<Worker> getWorkers() {
        return resourceDao.list(Worker.class);
    }
}
