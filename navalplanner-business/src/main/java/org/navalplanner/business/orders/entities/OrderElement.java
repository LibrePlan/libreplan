/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.business.orders.entities;

import static org.navalplanner.business.i18n.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.InvalidValue;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.Valid;
import org.joda.time.LocalDate;
import org.navalplanner.business.advance.entities.AdvanceAssignment;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.advance.entities.IndirectAdvanceAssignment;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.materials.entities.MaterialAssignment;
import org.navalplanner.business.orders.entities.SchedulingState.ITypeChangedListener;
import org.navalplanner.business.orders.entities.SchedulingState.Type;
import org.navalplanner.business.orders.entities.TaskSource.TaskSourceSynchronization;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.qualityforms.entities.TaskQualityForm;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.requirements.entities.DirectCriterionRequirement;
import org.navalplanner.business.requirements.entities.IndirectCriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;
public abstract class OrderElement extends BaseEntity {

    @NotEmpty(message = "name not specified")
    private String name;

    private Date initDate;

    private Date deadline;

    private Boolean mandatoryInit = false;

    private Boolean mandatoryEnd = false;

    private String description;

    protected Set<DirectAdvanceAssignment> directAdvanceAssignments = new HashSet<DirectAdvanceAssignment>();

    @Valid
    protected Set<MaterialAssignment> materialAssignments = new HashSet<MaterialAssignment>();

    @Valid
    private Set<Label> labels = new HashSet<Label>();

    private Set<TaskQualityForm> taskQualityForms = new HashSet<TaskQualityForm>();

    @NotEmpty(message = "code not specified")
    private String code;

    private Set<CriterionRequirement> criterionRequirements = new HashSet<CriterionRequirement>();

    protected OrderLineGroup parent;

    protected CriterionRequirementHandler criterionRequirementHandler = CriterionRequirementHandler
            .getInstance();

    private SchedulingState.Type schedulingStateType = Type.NO_SCHEDULED;

    /**
     * This field is transient
     */
    private SchedulingState schedulingState = null;

    private TaskSource taskSource;

    public SchedulingState getSchedulingState() {
        if (schedulingState == null) {
            schedulingState = createSchedulingState();
        }
        return schedulingState;
    }

    private SchedulingState createSchedulingState() {
        List<SchedulingState> childrenStates = getChildrenStates();
        SchedulingState result = childrenStates.isEmpty() ? new SchedulingState(
                getSchedulingStateType())
                : new SchedulingState(schedulingStateType, childrenStates);
        schedulingStateType = result.getType();
        result.addTypeChangeListener(new ITypeChangedListener() {
            @Override
            public void typeChanged(Type newType) {
                schedulingStateType = newType;
            }
        });
        return result;
    }

    private List<SchedulingState> getChildrenStates() {
        List<SchedulingState> result = new ArrayList<SchedulingState>();
        for (OrderElement each : getChildren()) {
            result.add(each.getSchedulingState());
        }
        return result;
    }

    public List<TaskSourceSynchronization> calculateSynchronizationsNeeded() {
        List<TaskSourceSynchronization> result = new ArrayList<TaskSourceSynchronization>();
        if (isSchedulingPoint()) {
            result.add(synchronizationForSchedulingPoint());
        } else if (isSuperElementPartialOrCompletelyScheduled()) {
            removeUnscheduled(result);
            result.add(synchronizationForSuperelement());
        } else if (schedulingState.isNoScheduled()) {
            removeTaskSource(result);
        }
        return result;
    }

    private TaskSourceSynchronization synchronizationForSuperelement() {
        List<TaskSourceSynchronization> childrenSynchronizations = childrenSynchronizations();
        if (thereIsNoTaskSource()) {
            taskSource = TaskSource.createForGroup(this);
            return TaskSource
                    .mustAddGroup(taskSource,
                    childrenSynchronizations);
        } else {
            return taskSource.modifyGroup(childrenSynchronizations);
        }
    }

    private List<TaskSourceSynchronization> childrenSynchronizations() {
        List<TaskSourceSynchronization> childrenOfGroup = new ArrayList<TaskSourceSynchronization>();
        for (OrderElement orderElement : getSomewhatScheduledOrderElements()) {
            childrenOfGroup.addAll(orderElement
                    .calculateSynchronizationsNeeded());
        }
        return childrenOfGroup;
    }

    private void removeUnscheduled(List<TaskSourceSynchronization> result) {
        for (OrderElement orderElement : getNoScheduledOrderElements()) {
            orderElement.removeTaskSource(result);
        }
    }

    private TaskSourceSynchronization synchronizationForSchedulingPoint() {
        if (thereIsNoTaskSource()) {
            taskSource = TaskSource.create(this, getHoursGroups());
            return TaskSource.mustAdd(taskSource);
        } else {
            if (taskSource.getTask().isLeaf()) {
                return taskSource.withCurrentHoursGroup(getHoursGroups());
            } else {
                List<TaskSource> toBeRemoved = getTaskSourcesFromBottomToTop();
                taskSource = TaskSource.create(this, getHoursGroups());
                return TaskSource.mustReplace(toBeRemoved, taskSource);
            }
        }
    }

    private boolean thereIsNoTaskSource() {
        return taskSource == null;
    }

    private List<OrderElement> getSomewhatScheduledOrderElements() {
        List<OrderElement> result = new ArrayList<OrderElement>();
        for (OrderElement orderElement : getChildren()) {
            if (orderElement.getSchedulingStateType().isSomewhatScheduled()) {
                result.add(orderElement);
            }
        }
        return result;
    }

    private List<OrderElement> getNoScheduledOrderElements() {
        List<OrderElement> result = new ArrayList<OrderElement>();
        for (OrderElement orderElement : getChildren()) {
            if (orderElement.getSchedulingState().isNoScheduled()) {
                result.add(orderElement);
            }
        }
        return result;
    }

    private void removeTaskSource(List<TaskSourceSynchronization> result) {
        removeChildrenTaskSource(result);
        if (taskSource != null) {
            result.add(TaskSource.mustRemove(taskSource));
            taskSource = null;
        }
    }

    private void removeChildrenTaskSource(List<TaskSourceSynchronization> result) {
        List<OrderElement> children = getChildren();
        for (OrderElement each : children) {
            each.removeTaskSource(result);
        }
    }

    private boolean isSuperElementPartialOrCompletelyScheduled() {
        return getSchedulingState().isSomewhatScheduled();
    }

    private boolean isSchedulingPoint() {
        return getSchedulingState().getType() == Type.SCHEDULING_POINT;
    }

    public OrderLineGroup getParent() {
        return parent;
    }

    public TaskElement getAssociatedTaskElement() {
        if (taskSource == null) {
            return null;
        } else {
            return taskSource.getTask();
        }
    }

    protected void setParent(OrderLineGroup parent) {
        this.parent = parent;
    }

    public abstract Integer getWorkHours();

    public abstract List<HoursGroup> getHoursGroups();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract boolean isLeaf();

    public abstract List<OrderElement> getChildren();

    private static Date copy(Date date) {
        return date != null ? new Date(date.getTime()) : date;
    }

    public Date getInitDate() {
        return copy(initDate);
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getDeadline() {
        return copy(deadline);
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
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

    public boolean isScheduled() {
        return taskSource != null;
    }

    public boolean checkAtLeastOneHoursGroup() {
        return (getHoursGroups().size() > 0);
    }

    public boolean isFormatCodeValid(String code) {

        if (code.contains("_")) {
            return false;
        }
        if (code.equals("")) {
            return false;
        }
        return true;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public abstract DirectAdvanceAssignment getReportGlobalAdvanceAssignment();

    public Set<DirectAdvanceAssignment> getDirectAdvanceAssignments() {
        return Collections.unmodifiableSet(directAdvanceAssignments);
    }

    protected abstract Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignments();

    protected abstract Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignments(
            AdvanceType advanceType);

    protected abstract Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignmentsReportGlobal();

    public void removeAdvanceAssignment(AdvanceAssignment advanceAssignment) {
        directAdvanceAssignments.remove(advanceAssignment);
        if (this.getParent() != null) {
            this.getParent().removeIndirectAdvanceAssignment(advanceAssignment
                    .getAdvanceType());
        }
    }

    public Set<Label> getLabels() {
        return Collections.unmodifiableSet(labels);
    }

    public void setLabels(Set<Label> labels) {
        this.labels = labels;
    }

    public void addLabel(Label label) {
        Validate.notNull(label);

        if (!checkAncestorsNoOtherLabelRepeated(label)) {
            throw new IllegalArgumentException(
                    _("Some ancestor has the same label assigned, "
                            + "so this element is already inheriting this label"));
        }

        removeLabelOnChildren(label);

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

        newAdvanceAssignment.setOrderElement(this);
        this.directAdvanceAssignments.add(newAdvanceAssignment);

        if (this.getParent() != null) {
            IndirectAdvanceAssignment indirectAdvanceAssignment = IndirectAdvanceAssignment
                    .create();
            indirectAdvanceAssignment.setAdvanceType(newAdvanceAssignment
                    .getAdvanceType());
            indirectAdvanceAssignment.setOrderElement(this.getParent());

            this.getParent().addIndirectAdvanceAssignment(indirectAdvanceAssignment);
        }
    }

    protected void checkNoOtherGlobalAdvanceAssignment(
            DirectAdvanceAssignment newAdvanceAssignment)
            throws DuplicateValueTrueReportGlobalAdvanceException {
        if (!newAdvanceAssignment.getReportGlobalAdvance()) {
            return;
        }
        for (DirectAdvanceAssignment directAdvanceAssignment : directAdvanceAssignments) {
            if (directAdvanceAssignment.getReportGlobalAdvance()) {
                throw new DuplicateValueTrueReportGlobalAdvanceException(
                        _("Duplicate Value True ReportGlobalAdvance For Order Element"),
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
                        _("Duplicate Advance Assignment For Order Element"),
                        this,
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
                        _("Duplicate Advance Assignment For Order Element"),
                        this,
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
        return getAdvancePercentage(null);
    }

    public abstract BigDecimal getAdvancePercentage(LocalDate date);

    public List<OrderElement> getAllChildren() {
        List<OrderElement> children = getChildren();
        List<OrderElement> result = new ArrayList<OrderElement>(children);
        for (OrderElement orderElement : children) {
            result.addAll(orderElement.getAllChildren());
        }
        return result;
    }

    public void setCriterionRequirements(
            Set<CriterionRequirement> criterionRequirements) {
        this.criterionRequirements = criterionRequirements;
    }

    @Valid
    public Set<CriterionRequirement> getCriterionRequirements() {
        return Collections.unmodifiableSet(criterionRequirements);
    }

    protected Set<CriterionRequirement> myCriterionRequirements() {
        return criterionRequirements;
    }


    /*
     * Operations to manage the criterion requirements of a orderElement
     * (remove, adding, update of the criterion requirement of the orderElement
     * such as the descendent's criterion requirement)
     */

    public void setValidCriterionRequirement(IndirectCriterionRequirement requirement,boolean valid){
        requirement.setIsValid(valid);
        criterionRequirementHandler.propagateValidCriterionRequirement(this,
                requirement.getParent(), valid);
    }

    public void removeDirectCriterionRequirement(DirectCriterionRequirement criterionRequirement){
        criterionRequirementHandler.propagateRemoveCriterionRequirement(this,
                criterionRequirement);
        removeCriterionRequirement(criterionRequirement);

    }

    protected void removeCriterionRequirement(CriterionRequirement requirement) {
        criterionRequirements.remove(requirement);
        if(requirement instanceof IndirectCriterionRequirement){
            ((IndirectCriterionRequirement)requirement).getParent().
                    getChildren().remove((IndirectCriterionRequirement)requirement);
        }
    }

    public void addDirectCriterionRequirement(
            CriterionRequirement newRequirement) {
        if (criterionRequirementHandler.canAddCriterionRequirement(this,
                newRequirement)) {
            addCriterionRequirement(newRequirement);
            criterionRequirementHandler
                    .propagateDirectCriterionRequirementAddition(this,
                            newRequirement);
        } else {
            Criterion criterion = newRequirement.getCriterion();
            throw new IllegalStateException(_(
                    " The {0} already exist into other order element",
                    criterion
                            .getName()));
        }
    }

    void addIndirectCriterionRequirement(
            IndirectCriterionRequirement criterionRequirement) {
        addCriterionRequirement(criterionRequirement);
    }

    protected void addCriterionRequirement(
            CriterionRequirement criterionRequirement) {
            criterionRequirement.setOrderElement(this);
            this.criterionRequirements.add(criterionRequirement);
    }

    public void updateCriterionRequirements() {
        updateMyCriterionRequirements();
        criterionRequirementHandler.propagateUpdateCriterionRequirements(this);
    }

    void updateMyCriterionRequirements() {
        OrderElement newParent = this.getParent();
        Set<CriterionRequirement> requirementsParent = newParent
                .getCriterionRequirements();
        Set<IndirectCriterionRequirement> currentIndirects = criterionRequirementHandler
                .getCurrentIndirectRequirements(
                        getIndirectCriterionRequirement(), requirementsParent);
        criterionRequirementHandler.transformDirectToIndirectIfNeeded(this,
                currentIndirects);
        criterionRequirementHandler.removeOldIndirects(this, currentIndirects);
        criterionRequirementHandler.addNewsIndirects(this, currentIndirects);
    }

    public boolean canAddCriterionRequirement(
            DirectCriterionRequirement newRequirement) {
        return criterionRequirementHandler.canAddCriterionRequirement(this,
                newRequirement);
    }

    protected Set<IndirectCriterionRequirement> getIndirectCriterionRequirement() {
        Set<IndirectCriterionRequirement> list = new HashSet<IndirectCriterionRequirement>();
        for (CriterionRequirement criterionRequirement : criterionRequirements) {
            if (criterionRequirement instanceof IndirectCriterionRequirement) {
                list.add((IndirectCriterionRequirement) criterionRequirement);
            }
        }
        return list;
    }

    public void applyStartConstraintIfNeededTo(Task task) {
        if (getInitDate() != null) {
            applyStartConstraintTo(task);
            return;
        }
        OrderLineGroup parent = getParent();
        if (parent != null) {
            parent.applyStartConstraintIfNeededTo(task);
        }
    }

    protected void applyStartConstraintTo(Task task) {
        task.getStartConstraint().notEarlierThan(this.getInitDate());
    }

    public Set<DirectCriterionRequirement> getDirectCriterionRequirement() {
        Set<DirectCriterionRequirement> list = new HashSet<DirectCriterionRequirement>();
        for (CriterionRequirement criterionRequirement : criterionRequirements) {
            if (criterionRequirement instanceof DirectCriterionRequirement) {
                list.add((DirectCriterionRequirement) criterionRequirement);
            }
        }
        return list;
    }

    public SchedulingState.Type getSchedulingStateType() {
        if (schedulingStateType == null) {
            schedulingStateType = Type.NO_SCHEDULED;
        }
        return schedulingStateType;
    }

    public TaskSource getTaskSource() {
        return taskSource;
    }

    public Set<TaskElement> getTaskElements() {
        if (taskSource == null) {
            return Collections.emptySet();
        }
        return Collections.singleton(taskSource.getTask());
    }

    public List<TaskSource> getTaskSourcesFromBottomToTop() {
        List<TaskSource> result = new ArrayList<TaskSource>();
        taskSourcesFromBottomToTop(result);
        return result;
    }

    private void taskSourcesFromBottomToTop(List<TaskSource> result) {
        for (OrderElement each : getChildren()) {
            each.taskSourcesFromBottomToTop(result);
        }
        if (taskSource != null) {
            result.add(taskSource);
        }
    }

    @Valid
    public Set<MaterialAssignment> getMaterialAssignments() {
        return Collections.unmodifiableSet(materialAssignments);
    }

    public void addMaterialAssignment(MaterialAssignment materialAssignment) {
        materialAssignments.add(materialAssignment);
        materialAssignment.setOrderElement(this);
    }

    public void removeMaterialAssignment(MaterialAssignment materialAssignment) {
        materialAssignments.remove(materialAssignment);
    }

    public double getTotalMaterialAssigmentUnits() {
        double result = 0;

        final Set<MaterialAssignment> materialAssigments = getMaterialAssignments();
        for (MaterialAssignment each: materialAssigments) {
            result += each.getUnits();
        }
        return result;
    }

    public BigDecimal getTotalMaterialAssigmentPrice() {
        BigDecimal result = new BigDecimal(0);

        final Set<MaterialAssignment> materialAssigments = getMaterialAssignments();
        for (MaterialAssignment each: materialAssigments) {
            result = result.add(each.getTotalPrice());
        }
        return result;
    }

    public Order getOrder() {
        if (this instanceof Order) {
            return (Order) this;
        }

        if (parent != null) {
            return parent.getOrder();
        }

        return null;
    }

    @Valid
    public Set<TaskQualityForm> getTaskQualityForms() {
        return Collections.unmodifiableSet(taskQualityForms);
    }

    public void setTaskQualityFormItems(Set<TaskQualityForm> taskQualityForms) {
        this.taskQualityForms = taskQualityForms;
    }

    public TaskQualityForm addTaskQualityForm(QualityForm qualityForm)
            throws ValidationException {
        ckeckUniqueQualityForm(qualityForm);
        TaskQualityForm taskQualityForm = TaskQualityForm.create(this,
                qualityForm);
        this.taskQualityForms.add(taskQualityForm);
        return taskQualityForm;
    }

    public void removeTaskQualityForm(TaskQualityForm taskQualityForm) {
        this.taskQualityForms.remove(taskQualityForm);
    }

    private void ckeckUniqueQualityForm(QualityForm qualityForm)
            throws ValidationException, IllegalArgumentException {
        Validate.notNull(qualityForm);
        for (TaskQualityForm taskQualityForm : getTaskQualityForms()) {
            if (qualityForm.equals(taskQualityForm.getQualityForm())) {
                throw new ValidationException(new InvalidValue(_(
                        "{0} already exists", qualityForm.getName()),
                        QualityForm.class, "name", qualityForm.getName(),
                        qualityForm));
            }
        }
    }

    @AssertTrue(message = "a label can not be assigned twice in the same branch")
    public boolean checkConstraintLabelNotRepeatedInTheSameBranch() {
        return checkConstraintLabelNotRepeatedInTheSameBranch(new HashSet<Label>());
    }

    private boolean checkConstraintLabelNotRepeatedInTheSameBranch(
            HashSet<Label> labels) {
        for (Label label : getLabels()) {
            if (containsLabel(labels, label)) {
                return false;
            }
            labels.add(label);
        }

        for (OrderElement child : getChildren()) {
            if (!child.checkConstraintLabelNotRepeatedInTheSameBranch(labels)) {
                return false;
            }
        }

        return true;
    }

    private boolean containsLabel(HashSet<Label> labels, Label label) {
        for (Label each : labels) {
            if (each.isEqualTo(label)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkAncestorsNoOtherLabelRepeated(Label newLabel) {
        for (Label label : labels) {
            if (label.equals(newLabel)) {
                return false;
            }
        }

        if (parent != null) {
            if (!((OrderElement) parent)
                    .checkAncestorsNoOtherLabelRepeated(newLabel)) {
                return false;
            }
        }
        return true;
    }

    private void removeLabelOnChildren(Label newLabel) {
        Label toRemove = null;

        for (Label label : labels) {
            if (label.equals(newLabel)) {
                toRemove = label;
                break;
            }
        }

        if (toRemove != null) {
            removeLabel(toRemove);
        }

        for (OrderElement child : getChildren()) {
            child.removeLabelOnChildren(newLabel);
        }
    }

}