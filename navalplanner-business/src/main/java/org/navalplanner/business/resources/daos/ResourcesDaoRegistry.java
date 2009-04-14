package org.navalplanner.business.resources.daos;

import org.springframework.beans.factory.annotation.Autowired;

// FIXME: Improve with 
// http://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom???
// (I think it is not necessary).

/**
 * Classes in which dependency injection (DI) is not directly supported by
 * Spring (e.g. entities) must use ResourcesDaoRegistry to access 
 * resource DAOs. For the rest of classes (e.g. services, tests, etc.) Spring 
 * DI will be a more convenient option.
 */
public final class ResourcesDaoRegistry {
    
    private static ResourcesDaoRegistry instance = new ResourcesDaoRegistry();
    
    @Autowired
    private IResourceDao resourceDao;
    
    @Autowired
    private IWorkerDao workerDao;
    
    @Autowired
    private IResourceGroupDao resourceGroupDao;
    
    private ResourcesDaoRegistry() {}

    public static ResourcesDaoRegistry getInstance() {
        return instance;
    }
 
    public static IResourceDao getResourceDao() {
        return getInstance().resourceDao;
    }

    public static IWorkerDao getWorkerDao() {
        return getInstance().workerDao;
    }

    public static IResourceGroupDao getResourceGroupDao() {
        return getInstance().resourceGroupDao;
    }

}
