/**
 *
 */
package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;

import org.hibernate.validator.NotNull;

/**
 * Resources are allocated to planner tasks.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public abstract class ResourceAllocation {

    private Long id;

    private Long version;

    @NotNull
    private Task task;

    /**
     * Allocation percentage of the resource.
     *
     * It's one based, instead of one hundred based.
     */
    private BigDecimal percentage = new BigDecimal(0).setScale(2);

    /**
     * For hibernate, DO NOT USE
     */
    public ResourceAllocation() {
    }

    public ResourceAllocation(Task task) {
        this.task = task;
    }

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public Task getTask() {
        return task;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    /**
     * @param proportion
     *            It's one based, instead of one hundred based.
     */
    public void setPercentage(BigDecimal proportion) {
        this.percentage = proportion;
    }

    public boolean isTransient() {
        return id == null;
    }

    public void makeTransientAgain() {
        id = null;
        version = null;
    }

}
