package org.navalplanner.business.planner.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.NotNull;
import org.navalplanner.business.resources.entities.Worker;

/**
 * Represents the relation between {@link Task} and a specific {@link Worker}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class SpecificResourceAllocation extends ResourceAllocation {

    public static SpecificResourceAllocation create(Task task) {
        return (SpecificResourceAllocation) create(new SpecificResourceAllocation(
                task));
    }

    @NotNull
    private Worker worker;

    private Set<SpecificDayAssigment> specificDaysAssigment = new HashSet<SpecificDayAssigment>();

    /**
     * Constructor for hibernate. Do not use!
     */
    public SpecificResourceAllocation() {

    }

    private SpecificResourceAllocation(Task task) {
        super(task);
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public Set<SpecificDayAssigment> getSpecificDaysAssigment() {
        return Collections.unmodifiableSet(specificDaysAssigment);
    }
}
