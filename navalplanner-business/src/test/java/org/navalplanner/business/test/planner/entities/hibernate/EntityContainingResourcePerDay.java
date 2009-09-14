package org.navalplanner.business.test.planner.entities.hibernate;

import org.navalplanner.business.planner.entities.ResourcesPerDay;

public class EntityContainingResourcePerDay {

    private Long id;

    private ResourcesPerDay resourcesPerDay;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResourcesPerDay getResourcesPerDay() {
        return resourcesPerDay;
    }

    public void setResourcesPerDay(ResourcesPerDay resourcesPerDay) {
        this.resourcesPerDay = resourcesPerDay;
    }

}
