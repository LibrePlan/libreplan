package org.navalplanner.business.planner.entities;

import org.hibernate.validator.NotNull;
import org.navalplanner.business.resources.entities.Worker;

/**
 * Represents the relation between {@link Task} and a specific {@link Worker}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class SpecificResourceAllocation extends ResourceAllocation {

    @NotNull
    private Worker worker;

    /**
     * For hibernate, DO NOT USE
     */
    public SpecificResourceAllocation() {
    }

    public SpecificResourceAllocation(Task task) {
        super(task);
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public void forceLoadWorker() {
        if (worker != null) {
            worker.getId();
        }
    }

}
