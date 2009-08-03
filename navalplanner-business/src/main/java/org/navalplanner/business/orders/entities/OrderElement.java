package org.navalplanner.business.orders.entities;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.NotEmpty;
import org.navalplanner.business.advance.entities.AdvanceAssigment;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssigmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.planner.entities.TaskElement;

public abstract class OrderElement {

    private Long id;

    private Long version;

    @NotEmpty
    private String name;

    private Date initDate;

    private Date endDate;

    private Boolean mandatoryInit = false;

    private Boolean mandatoryEnd = false;

    private String description;

    private Set<AdvanceAssigment> advanceAssigments = new HashSet<AdvanceAssigment>();

    @NotEmpty
    private String code;

    private Set<TaskElement> taskElements = new HashSet<TaskElement>();

    private OrderLineGroup parent;

    public OrderLineGroup getParent() {
        return parent;
    }

    protected void setParent(OrderLineGroup parent) {
        this.parent = parent;
    }

    public abstract Integer getWorkHours();

    public abstract List<HoursGroup> getHoursGroups();

    public Long getId() {
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

    public abstract OrderLine toLeaf();

    public abstract OrderLineGroup toContainer();

    public void makeTransientAgain() {
        // FIXME Review reattachment
        id = null;
        version = null;
    }

    public boolean isTransient() {
        // FIXME Review reattachment
        return id == null;
    }

    public Set<TaskElement> getTaskElements() {
        return Collections.unmodifiableSet(taskElements);
    }

    public boolean isScheduled() {
        return !taskElements.isEmpty();
    }

    public boolean checkAtLeastOneHoursGroup() {
        return (getHoursGroups().size() > 0);
    }

    public boolean isFormatCodeValid(String code) {

        if (code.contains("_"))
            return false;
        if (code.equals(""))
            return false;
        return true;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setAdvanceAssigments(Set<AdvanceAssigment> advanceAssigments) {
        this.advanceAssigments = advanceAssigments;
    }

    public Set<AdvanceAssigment> getAdvanceAssigments() {
        return this.advanceAssigments;
    }

    /**
     * Validate if the advanceAssigment can be added to the order element.The
     * list of advanceAssigments must be attached.
     * @param advanceAssigment
     *            must be attached
     */
    public void addAvanceAssigment(AdvanceAssigment newAdvanceAssigment)
            throws Exception {
        if (!this.advanceAssigments.isEmpty()) {
            for (AdvanceAssigment advanceAssigment : getAdvanceAssigments()) {
                if (advanceAssigment.getReportGlobalAdvance()
                        && newAdvanceAssigment.getReportGlobalAdvance())
                    throw new DuplicateValueTrueReportGlobalAdvanceException(
                            "Duplicate Value True ReportGlobalAdvance For Order Element",
                            this,
                            "org.navalplanner.business.orders.entities.OrderElement");
            }
        }
        existParentsWithSameAdvanceType(this, newAdvanceAssigment);
        this.advanceAssigments.add(newAdvanceAssigment);
    }

    private void existParentsWithSameAdvanceType(OrderElement orderElement,
            AdvanceAssigment newAdvanceAssigment)
            throws DuplicateAdvanceAssigmentForOrderElementException {
        for (AdvanceAssigment advanceAssigment : orderElement
                .getAdvanceAssigments()) {
            if (advanceAssigment.getAdvanceType().getId() == newAdvanceAssigment
                    .getAdvanceType().getId()) {
                throw new DuplicateAdvanceAssigmentForOrderElementException(
                        "Duplicate Advance Assigment For Order Element", this,
                        "org.navalplanner.business.orders.entities.OrderElement");
            }
        }
        if (orderElement.getParent() != null) {
            existParentsWithSameAdvanceType(orderElement.getParent(),
                    newAdvanceAssigment);
        }
    }

}
