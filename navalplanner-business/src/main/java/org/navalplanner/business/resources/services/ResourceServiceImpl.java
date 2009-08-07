package org.navalplanner.business.resources.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.bootstrap.ICriterionsBootstrap;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the resource management service. Resource DAOs are
 * autowired.
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Transactional
public class ResourceServiceImpl implements IResourceService {

    @Autowired
    private IResourceDAO resourceDao;

    @Autowired
    private ICriterionsBootstrap criterionsBootstrap;

    @Autowired
    private ICriterionTypeService criterionTypeService;


    @Transactional
    public void saveResource(Resource resource) {
        checkResourceIsOk(resource);
        resourceDao.save(resource);
    }

    @Transactional(readOnly = true)
    public void checkVersion(Resource resource) {
        resourceDao.checkVersion(resource);
    }

    private void checkResourceIsOk(Resource resource) {
        List<CriterionType> types = criterionTypeService.getAll();
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
        resourceDao.remove(resourceId);
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
