/**
 *
 */
package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;

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

    /**
     * Allocation percentage of the resource.
     *
     * It's one based, instead of one hundred based.
     */
    private BigDecimal percentage = new BigDecimal(0).setScale(2);

    /**
     * Constructor for hibernate. Do not use!
     */
    public ResourceAllocation() {

    }

    public ResourceAllocation(Task task) {
        this(task, null);
    }

    public ResourceAllocation(Task task, AssigmentFunction assignmentFunction) {
        Validate.notNull(task);
        this.task = task;
        assigmentFunction = assignmentFunction;
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

    public AssigmentFunction getAssigmentFunction() {
        return assigmentFunction;
    }

}
