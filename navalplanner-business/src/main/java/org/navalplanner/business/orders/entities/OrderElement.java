package org.navalplanner.business.orders.entities;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotEmpty;
import org.joda.time.LocalDate;
import org.navalplanner.business.advance.entities.AdvanceAssignment;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.advance.entities.IndirectAdvanceAssignment;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
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

    protected Set<DirectAdvanceAssignment> directAdvanceAssignments = new HashSet<DirectAdvanceAssignment>();

    private Set<Label> labels = new HashSet<Label>();

    @NotEmpty
    private String code;

    private Set<TaskElement> taskElements = new HashSet<TaskElement>();

    protected OrderLineGroup parent;

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

    public Set<DirectAdvanceAssignment> getDirectAdvanceAssignments() {
        return Collections.unmodifiableSet(directAdvanceAssignments);
    }

    protected abstract Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignments();

    protected abstract Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignments(
            AdvanceType advanceType);

    protected abstract Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignmentsReportGlobal();

    public void removeAdvanceAssignment(AdvanceAssignment advanceAssignment) {
        directAdvanceAssignments.remove(advanceAssignment);
        OrderLineGroup parent = this.getParent();
        if (parent != null) {
            parent.removeIndirectAdvanceAssignment(advanceAssignment
                    .getAdvanceType());
        }
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
     * Validate if the advanceAssignment can be added to the order element.The
     * list of advanceAssignments must be attached.
     * @param advanceAssignment
     *            must be attached
     * @throws DuplicateValueTrueReportGlobalAdvanceException
     * @throws DuplicateAdvanceAssignmentForOrderElementException
     */
    public void addAdvanceAssignment(
            DirectAdvanceAssignment newAdvanceAssignment)
            throws DuplicateValueTrueReportGlobalAdvanceException,
            DuplicateAdvanceAssignmentForOrderElementException {
        checkNoOtherGlobalAdvanceAssignment(newAdvanceAssignment);
        checkAncestorsNoOtherAssignmentWithSameAdvanceType(this,
                newAdvanceAssignment);
        checkChildrenNoOtherAssignmentWithSameAdvanceType(this,
                newAdvanceAssignment);
        this.directAdvanceAssignments.add(newAdvanceAssignment);

        OrderLineGroup parent = this.getParent();
        if (parent != null) {
            IndirectAdvanceAssignment indirectAdvanceAssignment = IndirectAdvanceAssignment
                    .create();
            indirectAdvanceAssignment.setAdvanceType(newAdvanceAssignment
                    .getAdvanceType());
            indirectAdvanceAssignment.setOrderElement(parent);

            parent.addIndirectAdvanceAssignment(indirectAdvanceAssignment);
        }
    }

    private void checkNoOtherGlobalAdvanceAssignment(
            DirectAdvanceAssignment newAdvanceAssignment)
            throws DuplicateValueTrueReportGlobalAdvanceException {
        if (!newAdvanceAssignment.getReportGlobalAdvance()) {
            return;
        }
        for (DirectAdvanceAssignment directAdvanceAssignment : directAdvanceAssignments) {
            if (directAdvanceAssignment.getReportGlobalAdvance()) {
                throw new DuplicateValueTrueReportGlobalAdvanceException(
                        "Duplicate Value True ReportGlobalAdvance For Order Element",
                        this, OrderElement.class);
            }
        }
    }

    /**
     * It checks there are no {@link DirectAdvanceAssignment} with the same type
     * in {@link OrderElement} and ancestors
     *
     * @param orderElement
     * @param newAdvanceAssignment
     * @throws DuplicateAdvanceAssignmentForOrderElementException
     */
    private void checkAncestorsNoOtherAssignmentWithSameAdvanceType(
            OrderElement orderElement,
            DirectAdvanceAssignment newAdvanceAssignment)
            throws DuplicateAdvanceAssignmentForOrderElementException {
        for (DirectAdvanceAssignment directAdvanceAssignment : orderElement.directAdvanceAssignments) {
            if (AdvanceType.equivalentInDB(directAdvanceAssignment
                    .getAdvanceType(), newAdvanceAssignment.getAdvanceType())) {
                throw new DuplicateAdvanceAssignmentForOrderElementException(
                        "Duplicate Advance Assignment For Order Element", this,
                        OrderElement.class);
            }
        }
        if (orderElement.getParent() != null) {
            checkAncestorsNoOtherAssignmentWithSameAdvanceType(orderElement
                    .getParent(), newAdvanceAssignment);
        }
    }

    /**
     * It checks there are no {@link AdvanceAssignment} with the same type in
     * orderElement and its children
     * @param orderElement
     * @param newAdvanceAssignment
     * @throws DuplicateAdvanceAssignmentForOrderElementException
     */
    protected void checkChildrenNoOtherAssignmentWithSameAdvanceType(
            OrderElement orderElement,
            DirectAdvanceAssignment newAdvanceAssignment)
            throws DuplicateAdvanceAssignmentForOrderElementException {
        for (DirectAdvanceAssignment directAdvanceAssignment : orderElement.directAdvanceAssignments) {
            if (AdvanceType.equivalentInDB(directAdvanceAssignment
                    .getAdvanceType(), newAdvanceAssignment.getAdvanceType())) {
                throw new DuplicateAdvanceAssignmentForOrderElementException(
                        "Duplicate Advance Assignment For Order Element", this,
                        OrderElement.class);
            }
        }
        if (!orderElement.getChildren().isEmpty()) {
            for (OrderElement child : orderElement.getChildren()) {
                checkChildrenNoOtherAssignmentWithSameAdvanceType(child,
                        newAdvanceAssignment);
            }
        }
    }

    public BigDecimal getAdvancePercentage() {
        return getAdvancePercentage(new LocalDate());
    }

    protected abstract BigDecimal getAdvancePercentage(LocalDate date);

}
