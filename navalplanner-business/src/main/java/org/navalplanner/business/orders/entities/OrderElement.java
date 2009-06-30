package org.navalplanner.business.orders.entities;

import java.util.Date;
import java.util.List;

import org.hibernate.validator.NotNull;

public abstract class OrderElement {

    private Long id;

    private Long version;

    @NotNull
    private String name;

    private Date initDate;

    private Date endDate;

    private Boolean mandatoryInit = false;

    private Boolean mandatoryEnd = false;

    private String description;

    public abstract Integer getWorkHours();

    public abstract List<HoursGroup> getHoursGroups();

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

    public abstract List<OrderElement> getChildren();

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

    public abstract OrderLineGroup asContainer();

    public abstract void forceLoadHourGroups();

    public abstract void forceLoadHourGroupsCriterions();

    public void makeTransientAgain() {
        // FIXME Review reattachment
        id = null;
        version = null;
        for (HoursGroup hoursGroup : getHoursGroups()) {
            hoursGroup.makeTransientAgain();
        }
    }

    public boolean isTransient() {
        // FIXME Review reattachment
        return id == null;
    }

}
