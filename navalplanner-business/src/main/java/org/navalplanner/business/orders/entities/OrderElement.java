package org.navalplanner.business.orders.entities;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotEmpty;
import org.navalplanner.business.advance.entities.AdvanceAssigment;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssigmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.planner.entities.TaskElement;

public abstract class OrderElement extends BaseEntity {

    @NotEmpty
    private String name;

    private Date initDate;

    private Date endDate;

    private Boolean mandatoryInit = false;

    private Boolean mandatoryEnd = false;

    private String description;

    protected Set<AdvanceAssigment> advanceAssigments = new HashSet<AdvanceAssigment>();

    private Set<Label> labels = new HashSet<Label>();

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

    public abstract Set<AdvanceAssigment> getAdvanceAssigments();

    protected Set<AdvanceAssigment> getAdvanceAssigmentsWithoutMerge() {
        return Collections.unmodifiableSet(this.advanceAssigments);
    }

    public void removeAdvanceAssigment(AdvanceAssigment advanceAssigment) {
        advanceAssigments.remove(advanceAssigment);
    }

    public Set<Label> getLabels() {
        return Collections.unmodifiableSet(labels);
    }

    public void addLabel(Label label) {
        Validate.notNull(label);
        labels.add(label);
    }

    public void removeLabel(Label label) {
        labels.remove(label);
    }

    /**
     * Validate if the advanceAssigment can be added to the order element.The
     * list of advanceAssigments must be attached.
     * @param advanceAssigment
     *            must be attached
     * @throws DuplicateValueTrueReportGlobalAdvanceException
     * @throws DuplicateAdvanceAssigmentForOrderElementException
     */
    public void addAdvanceAssigment(AdvanceAssigment newAdvanceAssigment)
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssigmentForOrderElementException {
        if(newAdvanceAssigment.getType().equals(AdvanceAssigment.Type.DIRECT)){
            checkNoOtherGlobalAdvanceAssignment(newAdvanceAssigment);
            checkAncestorsNoOtherAssignmentWithSameAdvanceType(this,
                    newAdvanceAssigment);
            checkChildrenNoOtherAssignmentWithSameAdvanceType(this,
                    newAdvanceAssigment);
        }
        this.advanceAssigments.add(newAdvanceAssigment);
    }

    private void checkNoOtherGlobalAdvanceAssignment(
            AdvanceAssigment newAdvanceAssigment)
            throws DuplicateValueTrueReportGlobalAdvanceException {
        if (!newAdvanceAssigment.getReportGlobalAdvance()) {
            return;
        }
        for (AdvanceAssigment advanceAssigment : advanceAssigments) {
            if((advanceAssigment.getType().equals(AdvanceAssigment.Type.DIRECT))
                    && (advanceAssigment.getReportGlobalAdvance()))
                throw new DuplicateValueTrueReportGlobalAdvanceException(
                        "Duplicate Value True ReportGlobalAdvance For Order Element",
                        this, OrderElement.class);
        }
    }

    /**
     * It checks there are no {@link AdvanceAssigment} with the same type in
     * orderElement and ancestors
     * @param orderElement
     * @param newAdvanceAssigment
     * @throws DuplicateAdvanceAssigmentForOrderElementException
     */
    private void checkAncestorsNoOtherAssignmentWithSameAdvanceType(
            OrderElement orderElement, AdvanceAssigment newAdvanceAssigment)
            throws DuplicateAdvanceAssigmentForOrderElementException {
        for (AdvanceAssigment advanceAssigment : orderElement.advanceAssigments) {
            if ((AdvanceType.equivalentInDB(advanceAssigment.getAdvanceType(),
                    newAdvanceAssigment.getAdvanceType())) &&
                    (advanceAssigment.getType().equals(AdvanceAssigment.Type.DIRECT))) {
                throw new DuplicateAdvanceAssigmentForOrderElementException(
                        "Duplicate Advance Assigment For Order Element", this,
                        OrderElement.class);
            }
        }
        if (orderElement.getParent() != null) {
            checkAncestorsNoOtherAssignmentWithSameAdvanceType(orderElement
                    .getParent(), newAdvanceAssigment);
        }
    }

    /**
     * It checks there are no {@link AdvanceAssigment} with the same type in
     * orderElement and its children
     * @param orderElement
     * @param newAdvanceAssigment
     * @throws DuplicateAdvanceAssigmentForOrderElementException
     */
    private void checkChildrenNoOtherAssignmentWithSameAdvanceType(
            OrderElement orderElement, AdvanceAssigment newAdvanceAssigment)
            throws DuplicateAdvanceAssigmentForOrderElementException {
        for (AdvanceAssigment advanceAssigment : orderElement.advanceAssigments) {
            if ((AdvanceType.equivalentInDB(advanceAssigment.getAdvanceType(),
                    newAdvanceAssigment.getAdvanceType())) &&
                    (advanceAssigment.getType().equals(AdvanceAssigment.Type.DIRECT))) {
                throw new DuplicateAdvanceAssigmentForOrderElementException(
                        "Duplicate Advance Assigment For Order Element", this,
                        OrderElement.class);
            }
        }
        if (!orderElement.getChildren().isEmpty()) {
            for (OrderElement child : orderElement.getChildren()) {
                checkChildrenNoOtherAssignmentWithSameAdvanceType(child,
                        newAdvanceAssigment);
            }
        }
    }

    public abstract BigDecimal getAdvancePercentage();

}
