package org.navalplanner.business.planner.entities;

import java.util.HashSet;
import java.util.Set;

import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.resources.entities.Worker;

public abstract class DayAssigment extends BaseEntity {

    private int hours;

    private Set<Worker> workers = new HashSet<Worker>();

    protected DayAssigment() {

    }

    protected DayAssigment(int hours, Set<Worker> workers) {
        this.hours = hours;
        this.workers = workers;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public Set<Worker> getWorkers() {
        return workers;
    }

    public void setWorkers(Set<Worker> workers) {
        this.workers = workers;
    }

}
