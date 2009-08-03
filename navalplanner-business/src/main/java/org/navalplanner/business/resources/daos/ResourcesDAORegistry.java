package org.navalplanner.business.resources.daos;

import org.springframework.beans.factory.annotation.Autowired;

// FIXME: Improve with
// http://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom???
// (I think it is not necessary).

/**
 * A registry of resource DAOs. Classes in which dependency injection (DI) is
 * not directly supported by Spring (e.g. entities) must use this class to
 * access resource DAOs. For the rest of classes (e.g. services, tests, etc.),
 * Spring DI is a more convenient option.
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public final class ResourcesDAORegistry {

    private static ResourcesDAORegistry instance = new ResourcesDAORegistry();

    @Autowired
    private IResourceDAO resourceDao;

    @Autowired
    private IWorkerDAO workerDao;

    private ResourcesDAORegistry() {
    }

    public static ResourcesDAORegistry getInstance() {
        return instance;
    }

    public static IResourceDAO getResourceDao() {
        return getInstance().resourceDao;
    }

    public static IWorkerDAO getWorkerDao() {
        return getInstance().workerDao;
    }

}
