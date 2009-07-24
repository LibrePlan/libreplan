package org.navalplanner.business.planner.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.orders.entities.HoursGroup;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Task extends TaskElement {

    public static Task createTask(HoursGroup hoursGroup) {
        return new Task(hoursGroup);
    }

    @NotNull
    private HoursGroup hoursGroup;

    private Set<ResourceAllocation> resourceAllocations = new HashSet<ResourceAllocation>();

    /**
     * For hibernate, DO NOT USE
     */
    public Task() {

    }

    private Task(HoursGroup hoursGroup) {
        Validate.notNull(hoursGroup);
        this.hoursGroup = hoursGroup;
    }

    public HoursGroup getHoursGroup() {
        return this.hoursGroup;
    }

    public Integer getHours() {
        return hoursGroup.getWorkingHours();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public List<TaskElement> getChildren() {
        throw new UnsupportedOperationException();
    }

    public Set<ResourceAllocation> getResourceAllocations() {
        return Collections.unmodifiableSet(resourceAllocations);
    }

    public void addResourceAllocation(ResourceAllocation resourceAllocation) {
        resourceAllocations.add(resourceAllocation);
    }

    public void removeResourceAllocation(ResourceAllocation resourceAllocation) {
        resourceAllocations.remove(resourceAllocation);
    }

    public void forceLoadResourceAllocations() {
        resourceAllocations.size();
    }

}
