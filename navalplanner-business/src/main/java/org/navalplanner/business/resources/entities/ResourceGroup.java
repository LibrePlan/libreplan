package org.navalplanner.business.resources.entities;

import java.util.HashSet;
import java.util.Set;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.resources.daos.ResourcesDaoRegistry;

public class ResourceGroup extends Resource {
    
    private Set<Resource> resources = new HashSet<Resource>();
    
    public Set<Resource> getResources() {
        return resources;
    }

    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }
    
    public void addResource(Resource resource) {
        
        /* 
         * Remove resource from its current resource group (if it belongs to 
         * one). 
         */
        if (resource.getResourceGroup() != null) {
            resource.getResourceGroup().removeResource(resource);
        }
        
        /* Add resource to this resource group. */
        resource.setResourceGroup(this);
        resources.add(resource);
        
    }
    
    public void addResource(Long resourceId) throws InstanceNotFoundException {
        
        Resource resource = 
            ResourcesDaoRegistry.getResourceDao().find(resourceId);
        addResource(resource);
        
    }
    
    public void removeResource(Resource resource) {
        
        if (resources.contains(resource)) {
            resources.remove(resource);
            resource.setResourceGroup(null);
        }
        
    }
    
    @Override
    public int getDailyCapacity() {
        
        int dailyCapacity = 0;
        
        for (Resource r : resources) {
            dailyCapacity += r.getDailyCapacity();
        }
        
        return dailyCapacity;
        
    }
    
    @Override
    public void remove() {

        for (Resource r : resources) {
            r.setResourceGroup(null);
        }
        resources.clear();

        super.remove();

    }

    
}
