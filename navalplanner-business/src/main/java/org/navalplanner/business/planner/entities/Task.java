package org.navalplanner.business.planner.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.resources.entities.Worker;

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

    private Integer shareOfHours;

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

    /**
     * Checks if there isn't any {@link Worker} repeated in the {@link Set} of
     * {@link ResourceAllocation} of this {@link Task}.
     *
     * @return <code>true</code> if the {@link Task} is valid, that means there
     *         isn't any {@link Worker} repeated.
     */
    public boolean isValidResourceAllocationWorkers() {
        Set<Long> workers = new HashSet<Long>();

        for (ResourceAllocation resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof SpecificResourceAllocation) {
                Worker worker = ((SpecificResourceAllocation) resourceAllocation)
                        .getWorker();
                if (worker != null) {
                    if (workers.contains(worker.getId())) {
                        return false;
                    } else {
                        workers.add(worker.getId());
                    }
                }
            }
        }

        return true;
	}

    @Override
    public Integer getWorkHours() {
        if (shareOfHours != null)
            return shareOfHours;
        return hoursGroup.getWorkingHours();
    }

    public TaskGroup split(int... shares) {
        int totalSumOfHours = sum(shares);
        if (totalSumOfHours != getWorkHours())
            throw new IllegalArgumentException(
                    "the shares don't sum up the work hours");
        TaskGroup result = new TaskGroup();
        result.copyPropertiesFrom(this);
        if (this.getParent() != null) {
            this.getParent().addTaskElement(result);
        }
        for (int i = 0; i < shares.length; i++) {
            Task task = Task.createTask(hoursGroup);
            task.copyPropertiesFrom(this);
            result.addTaskElement(task);
            task.shareOfHours = shares[i];
        }
        for (Dependency dependency : getDependenciesWithThisOrigin()) {
            Dependency.createDependency(result, dependency
                    .getDestination(), dependency.getType());
        }
        for (Dependency dependency : getDependenciesWithThisDestination()) {
            Dependency.createDependency(dependency.getOrigin(), result,
                    dependency.getType());
        }
        return result;
    }

    private int sum(int[] shares) {
        int result = 0;
        for (int share : shares) {
            result += share;
        }
        return result;
    }
}
