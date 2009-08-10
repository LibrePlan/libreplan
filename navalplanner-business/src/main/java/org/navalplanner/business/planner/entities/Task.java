package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.joda.time.DateTime;
import org.joda.time.Days;
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

    private Boolean fixedDuration = false;

    /**
     * Duration in days of the Task
     */
    private Integer duration;

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

    public void setFixedDuration(Boolean fixed_duration) {
        this.fixedDuration = fixed_duration;
    }

    public Boolean isFixedDuration() {
        return fixedDuration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;

        DateTime endDate = (new DateTime(getStartDate())).plusDays(duration);
        setEndDate(endDate.toDate());
    }

    @Override
    public void setEndDate(Date endDate) {
        super.setEndDate(endDate);

        DateTime startDateTime = new DateTime(getStartDate());
        DateTime endDateTime = new DateTime(endDate);
        Days days = Days.daysBetween(startDateTime, endDateTime);

        this.duration = days.getDays();
    }

    public Integer getDuration() {
        if ((isFixedDuration() == null) || !isFixedDuration()) {
            // If it is not fixed, the duration is calculated
            Integer duration = calculateDaysDuration();
            setDuration(duration);
            return duration;
        }

        return duration;
    }

    /**
     * Calculates the number of days needed to complete the Task taking into
     * account the Resources assigned and their dedication.
     *
     * If the Task has not yet Resources assigned then a typical 8 hours day
     * will be considered.
     *
     * @return The days of duration
     */
    private Integer calculateDaysDuration() {
        BigDecimal hoursPerDay = new BigDecimal(0).setScale(2);

        for (ResourceAllocation resourceAllocation : resourceAllocations) {
            if (resourceAllocation instanceof SpecificResourceAllocation) {
                BigDecimal percentage = resourceAllocation.getPercentage();
                Integer hours = ((SpecificResourceAllocation) resourceAllocation)
                        .getWorker().getDailyCapacity();

                hoursPerDay = hoursPerDay.add(percentage
                        .multiply(new BigDecimal(hours).setScale(2)));
            }
        }

        BigDecimal taskHours = new BigDecimal(getWorkHours()).setScale(2);

        if (hoursPerDay.compareTo(new BigDecimal(0).setScale(2)) == 0) {
            // FIXME Review, by default 8 hours per day
            hoursPerDay = new BigDecimal(8).setScale(2);
        }

        return taskHours.divide(hoursPerDay, BigDecimal.ROUND_DOWN).intValue();
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
    public Integer defaultWorkHours() {
        return hoursGroup.getWorkingHours();
    }

    public TaskGroup split(int... shares) {
        int totalSumOfHours = sum(shares);
        if (totalSumOfHours != getWorkHours())
            throw new IllegalArgumentException(
                    "the shares don't sum up the work hours");
        TaskGroup result = new TaskGroup();
        result.copyPropertiesFrom(this);
        result.shareOfHours = this.shareOfHours;
        copyParenTo(result);
        for (int i = 0; i < shares.length; i++) {
            Task task = Task.createTask(hoursGroup);
            task.copyPropertiesFrom(this);
            result.addTaskElement(task);
            task.shareOfHours = shares[i];
        }
        copyDependenciesTo(result);
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
