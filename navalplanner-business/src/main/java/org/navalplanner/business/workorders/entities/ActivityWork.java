package org.navalplanner.business.workorders.entities;

import org.hibernate.validator.NotNull;

public class ActivityWork {

    private Long id;

    public Long getId() {
        return id;
    }

    @NotNull
    private Integer workingHours;

    public void setWorkingHours(Integer workingHours) {
        this.workingHours = workingHours;
    }

    public int getWorkingHours() {
        return workingHours;
    }

}
