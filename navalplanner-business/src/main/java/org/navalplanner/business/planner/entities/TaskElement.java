package org.navalplanner.business.planner.entities;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.orders.entities.OrderElement;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class TaskElement {

    private Long id;

    private Long version;

    private Date startDate;

    private Date endDate;

    @NotNull
    private OrderElement orderElement;

    private Set<Dependency> dependenciesWithThisOrigin = new HashSet<Dependency>();

    private Set<Dependency> dependenciesWithThisDestination = new HashSet<Dependency>();

    public void setOrderElement(OrderElement orderElement)
            throws IllegalArgumentException, IllegalStateException {
        Validate.notNull(orderElement, "orderElement must be not null");
        if (this.orderElement != null)
            throw new IllegalStateException(
                    "once a orderElement is set, it cannot be changed");
        this.orderElement = orderElement;
    }

    public OrderElement getOrderElement() {
        return orderElement;
    }

    public Set<Dependency> getDependenciesWithThisOrigin() {
        return Collections.unmodifiableSet(dependenciesWithThisOrigin);
    }

    public Set<Dependency> getDependenciesWithThisDestination() {
        return Collections.unmodifiableSet(dependenciesWithThisDestination);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    void add(Dependency dependency) {
        if (this.equals(dependency.getOrigin())) {
            dependenciesWithThisOrigin.add(dependency);
        }
        if (this.equals(dependency.getDestination())) {
            dependenciesWithThisDestination.add(dependency);
        }
    }

    public Long getVersion() {
        return version;
    }

    public Long getId() {
        return id;
    }

    public abstract boolean isLeaf();

    public abstract List<TaskElement> getChildren();

}
