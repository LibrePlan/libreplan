package org.navalplanner.business.workorders.entities;

import java.util.Date;
import java.util.List;

import org.hibernate.validator.NotNull;

public abstract class TaskWork {
    private long id;

    @NotNull
    private String name;

    private Date initDate;

    private Date endDate;

    private Boolean mandatoryInit = false;

    private Boolean mandatoryEnd = false;

    private String description;

    public abstract Integer getWorkHours();

    public abstract List<ActivityWork> getActivities();

    public long getId() {
        return id;
    }

    /**
     * @return the duration in milliseconds
     */
    public long getDuration() {
        return endDate.getTime() - initDate.getTime();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract boolean isLeaf();

    public abstract List<TaskWork> getChildren();

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setMandatoryInit(Boolean mandatoryInit) {
        this.mandatoryInit = mandatoryInit;
    }

    public Boolean isMandatoryInit() {
        return mandatoryInit;
    }

    public void setMandatoryEnd(Boolean mandatoryEnd) {
        this.mandatoryEnd = mandatoryEnd;
    }

    public Boolean isMandatoryEnd() {
        return mandatoryEnd;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public abstract TaskWorkContainer asContainer();

    public abstract void forceLoadActivities();

}
