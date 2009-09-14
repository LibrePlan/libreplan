package org.navalplanner.business.planner.entities;


import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;

/**
 * Resources are allocated to planner tasks.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class ResourceAllocation extends BaseEntity {

    @NotNull
    private Task task;

    private AssigmentFunction assigmentFunction;

    @NotNull
    private ResourcesPerDay resourcesPerDay;

    /**
     * Constructor for hibernate. Do not use!
     */
    public ResourceAllocation() {

    }

    protected void setResourcesPerDay(ResourcesPerDay resourcesPerDay) {
        Validate.notNull(resourcesPerDay);
        this.resourcesPerDay = resourcesPerDay;
    }

    public ResourceAllocation(Task task) {
        this(task, null);
    }

    public ResourceAllocation(Task task, AssigmentFunction assignmentFunction) {
        Validate.notNull(task);
        this.task = task;
        assigmentFunction = assignmentFunction;
    }

    protected ResourceAllocation(ResourcesPerDay resourcesPerDay, Task task) {
        this(task);
        Validate.notNull(resourcesPerDay);
        this.resourcesPerDay = resourcesPerDay;
    }

    public Task getTask() {
        return task;
    }

    public AssigmentFunction getAssigmentFunction() {
        return assigmentFunction;
    }

    public int getAssignedHours() {
        int total = 0;
        for (DayAssigment dayAssigment : getAssignments()) {
            total += dayAssigment.getHours();
        }
        return total;
    }

    protected abstract List<? extends DayAssigment> getAssignments();


    public ResourcesPerDay getResourcesPerDay() {
        return resourcesPerDay;
    }

}
