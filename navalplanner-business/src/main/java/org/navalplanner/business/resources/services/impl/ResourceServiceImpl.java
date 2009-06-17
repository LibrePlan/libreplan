package org.navalplanner.business.resources.services.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.bootstrap.ICriterionsBootstrap;
import org.navalplanner.business.resources.daos.IResourceDao;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Resource;
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
    private ICriterionsBootstrap criterionsBootstrap;

    @Transactional
    public void saveResource(Resource resource) {
        checkResourceIsOk(resource);
        resourceDao.save(resource);
    }

    @Transactional(readOnly = true)
    public void checkVersion(Resource resource) {
        resourceDao.reattachForRead(resource);
    }

    private void checkResourceIsOk(Resource resource) {
        List<ICriterionType<?>> types = criterionsBootstrap.getTypes();
        resource.checkNotOverlaps(types);
    }

    @Transactional(readOnly = true)
    public Resource findResource(Long resourceId)
            throws InstanceNotFoundException {

        return resourceDao.find(resourceId);

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

    @Override
    public Set<Resource> getSetOfResourcesSatisfying(ICriterion criterion) {
        List<Resource> resources = resourceDao.list(Resource.class);
        HashSet<Resource> result = new HashSet<Resource>();
        for (Resource resource : resources) {
            if (criterion.isSatisfiedBy(resource)) {
                result.add(resource);
            }
        }
        return result;
    }

    @Override
    public List<Resource> getResources() {
        return resourceDao.list(Resource.class);
    }

    @Override
    public <T extends Resource> List<T> getResources(Class<T> klass) {
        return resourceDao.list(klass);
    }
}
