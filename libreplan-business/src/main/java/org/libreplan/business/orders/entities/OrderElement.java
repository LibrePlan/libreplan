/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.business.orders.entities;

import static org.libreplan.business.i18n.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.LocalDate;
import org.libreplan.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.libreplan.business.advance.entities.AdvanceAssignment;
import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.advance.entities.AdvanceType;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.advance.entities.IndirectAdvanceAssignment;
import org.libreplan.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.libreplan.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.common.entities.Configuration;
import org.libreplan.business.common.entities.PredefinedConnectorProperties;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.costcategories.daos.IHourCostDAO;
import org.libreplan.business.costcategories.entities.CostCategory;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.materials.entities.MaterialAssignment;
import org.libreplan.business.orders.entities.SchedulingState.Type;
import org.libreplan.business.orders.entities.TaskSource.TaskSourceSynchronization;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskPositionConstraint;
import org.libreplan.business.qualityforms.entities.QualityForm;
import org.libreplan.business.qualityforms.entities.TaskQualityForm;
import org.libreplan.business.requirements.entities.CriterionRequirement;
import org.libreplan.business.requirements.entities.DirectCriterionRequirement;
import org.libreplan.business.requirements.entities.IndirectCriterionRequirement;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.business.trees.ITreeNode;
import org.libreplan.business.util.deepcopy.DeepCopy;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.business.workreports.entities.WorkReportLine;

public abstract class OrderElement extends IntegrationEntity implements ICriterionRequirable, ITreeNode<OrderElement> {

    protected InfoComponentWithCode infoComponent = new InfoComponentWithCode();

    private Date initDate;

    private Date deadline;

    @Valid
    protected Set<DirectAdvanceAssignment> directAdvanceAssignments = new HashSet<>();

    @Valid
    protected Set<MaterialAssignment> materialAssignments = new HashSet<>();

    @Valid
    protected Set<Label> labels = new HashSet<>();

    protected Set<TaskQualityForm> taskQualityForms = new HashSet<>();

    protected Set<CriterionRequirement> criterionRequirements = new HashSet<>();

    protected OrderLineGroup parent;

    protected CriterionRequirementOrderElementHandler criterionRequirementHandler =
                    CriterionRequirementOrderElementHandler.getInstance();

    /**
     * This field is transient.
     */
    private SchedulingState schedulingState = null;

    private OrderElementTemplate template;

    private BigDecimal lastAdvanceMeasurementForSpreading = BigDecimal.ZERO;

    private Boolean dirtyLastAdvanceMeasurementForSpreading = true;

    private SumChargedEffort sumChargedEffort;

    private SumExpenses sumExpenses;

    private String externalCode;

    private Map<OrderVersion, SchedulingDataForVersion> schedulingDataForVersion = new HashMap<>();

    private SchedulingDataForVersion.Data current = null;

    public OrderElementTemplate getTemplate() {
        return template;
    }

    protected void removeVersion(OrderVersion orderVersion) {
        schedulingDataForVersion.remove(orderVersion);
        for (OrderElement each : getChildren()) {
            each.removeVersion(orderVersion);
        }
    }

    public SchedulingDataForVersion.Data getCurrentSchedulingData() {
        if ( current == null ) {
            throw new IllegalStateException(
                    "in order to use scheduling state related data " +
                            "useSchedulingDataFor(OrderVersion orderVersion) must be called first");
        }
        return current;
    }

    private void schedulingDataNowPointsTo(DeepCopy deepCopy, OrderVersion version) {
        current = getCurrentSchedulingData().pointsTo(deepCopy, version, schedulingVersionFor(version));
        for (OrderElement each : getChildren()) {
            each.schedulingDataNowPointsTo(deepCopy, version);
        }
    }

    protected void addNeededReplaces(DeepCopy deepCopy, OrderVersion newOrderVersion) {
        SchedulingDataForVersion currentVersion = getCurrentSchedulingData().getVersion();
        SchedulingDataForVersion newSchedulingVersion = schedulingVersionFor(newOrderVersion);
        deepCopy.replace(currentVersion, newSchedulingVersion);

        for (OrderElement each : getChildren()) {
            each.addNeededReplaces(deepCopy, newOrderVersion);
        }
    }

    public SchedulingState getSchedulingState() {
        if ( schedulingState == null ) {
            ensureSchedulingStateInitializedFromTop();
            initializeSchedulingState(); // Maybe this order element was added later
        }
        return schedulingState;
    }

    private void ensureSchedulingStateInitializedFromTop() {
        OrderElement current = this;
        while (current.getParent() != null) {
            current = current.getParent();
        }
        current.initializeSchedulingState();
    }

    private SchedulingState initializeSchedulingState() {
        if ( schedulingState != null ) {
            return schedulingState;
        }

        schedulingState = SchedulingState.createSchedulingState(
                getSchedulingStateType(), getChildrenStates(), getCurrentSchedulingData().onTypeChangeListener());

        return schedulingState;
    }

    private List<SchedulingState> getChildrenStates() {
        List<SchedulingState> result = new ArrayList<>();
        for (OrderElement each : getChildren()) {
            result.add(each.initializeSchedulingState());
        }

        return result;
    }

    public boolean hasSchedulingDataBeingModified() {
        return getCurrentSchedulingData().hasPendingChanges() || someSchedulingDataModified();
    }

    private boolean someSchedulingDataModified() {
        for (OrderElement each : getChildren()) {
            if (each.hasSchedulingDataBeingModified()) {
                return true;
            }
        }

        return false;
    }

    protected boolean isSchedulingDataInitialized() {
        return current != null;
    }

    public void useSchedulingDataFor(OrderVersion orderVersion) {
        useSchedulingDataFor(orderVersion, true);
    }

    public void useSchedulingDataFor(OrderVersion orderVersion, boolean recursive) {
        Validate.notNull(orderVersion);
        SchedulingDataForVersion schedulingVersion = schedulingVersionFor(orderVersion);
        if ( recursive ) {
            for (OrderElement each : getChildren()) {
                each.useSchedulingDataFor(orderVersion);
            }
        }
        current = schedulingVersion.makeAvailableFor(orderVersion);
    }

    private SchedulingDataForVersion schedulingVersionFor(OrderVersion orderVersion) {
        SchedulingDataForVersion currentSchedulingData = schedulingDataForVersion.get(orderVersion);
        if (currentSchedulingData == null) {
            currentSchedulingData = SchedulingDataForVersion.createInitialFor(this);
            schedulingDataForVersion.put(orderVersion, currentSchedulingData);
        }

        return currentSchedulingData;
    }

    public SchedulingDataForVersion getCurrentSchedulingDataForVersion() {
        return getCurrentSchedulingData().getVersion();
    }

    protected void writeSchedulingDataChanges() {
        getCurrentSchedulingData().writeSchedulingDataChanges();
        for (OrderElement each : getChildren()) {
            each.writeSchedulingDataChanges();
        }
    }

    protected void writeSchedulingDataChangesTo(DeepCopy deepCopy, OrderVersion newOrderVersion) {
        schedulingDataNowPointsTo(deepCopy, newOrderVersion);
        writeSchedulingDataChanges();
    }

    protected void removeSpuriousDayAssignments(Scenario scenario) {
        removeAtNotCurrent(scenario);
        removeAtCurrent(scenario);

        for (OrderElement each : getChildren()) {
            each.removeSpuriousDayAssignments(scenario);
        }
    }

    private void removeAtNotCurrent(Scenario scenario) {
        SchedulingDataForVersion currentDataForVersion = getCurrentSchedulingDataForVersion();
        for (Entry<OrderVersion, SchedulingDataForVersion> each : schedulingDataForVersion.entrySet()) {
            SchedulingDataForVersion dataForVersion = each.getValue();

            if (!currentDataForVersion.equals(dataForVersion)) {
                dataForVersion.removeSpuriousDayAssignments(scenario);
            }
        }
    }

    private void removeAtCurrent(Scenario scenario) {
        TaskElement associatedTaskElement = getAssociatedTaskElement();
        if (associatedTaskElement != null) {
            associatedTaskElement.removePredecessorsDayAssignmentsFor(scenario);
        }
    }

    public List<TaskSourceSynchronization> calculateSynchronizationsNeeded() {
        return calculateSynchronizationsNeeded(getCurrentSchedulingData().getVersion());
    }

    private List<TaskSourceSynchronization> calculateSynchronizationsNeeded(
            SchedulingDataForVersion schedulingDataForVersion) {

        List<TaskSourceSynchronization> result = new ArrayList<>();
        if (isSchedulingPoint()) {
            if (!wasASchedulingPoint()) {

                // This element was a container but now it's a scheduling point
                // we have to remove the TaskSource which contains a TaskGroup instead of TaskElement
                removeTaskSource(result);
            } else {
                if (hadATaskSource() && currentTaskSourceIsNotTheSame()) {

                    // This element was unscheduled and then scheduled again.
                    // Its TaskSource has been recreated but we have to remove the old one.
                    if (!getParent().currentTaskSourceIsNotTheSame()) {

                        // We only remove the TaskSource if the parent is not in the same situation.
                        // In case the parent is in the same situation, it will remove the related
                        // TaskSources in children tasks.
                        removeTaskSource(result);
                    }
                }
            }

            result.addAll(synchronizationForSchedulingPoint(schedulingDataForVersion));

        } else if (isSuperElementPartialOrCompletelyScheduled()) {
            removeUnscheduled(result);
            if (wasASchedulingPoint()) {
                removeTaskSource(result);
            } else {
                if (hadATaskSource() && currentTaskSourceIsNotTheSame()) {

                    // All the children of this element were unscheduled and then scheduled again,
                    // its TaskSource has been recreated but we have to remove the old one.
                    if (getParent() == null || !getParent().currentTaskSourceIsNotTheSame()) {

                        // If it's a container node inside another container we could have the
                        // same problem than in the case of leaf tasks.
                        result.add(taskSourceRemoval());
                    }
                }
            }
            result.add(synchronizationForSuperelement(schedulingDataForVersion));
        } else if (schedulingState.isNoScheduled()) {
            removeTaskSource(result);
        }
        return result;
    }

    private TaskSourceSynchronization synchronizationForSuperelement(SchedulingDataForVersion schedulingState) {
        List<TaskSourceSynchronization> childrenSynchronizations = childrenSynchronizations();
        if (thereIsNoTaskSource()) {
            getCurrentSchedulingData().requestedCreationOf(TaskSource.createForGroup(schedulingState));

            return TaskSource.mustAddGroup(getTaskSource(), childrenSynchronizations);
        } else {
            return getTaskSource().modifyGroup(childrenSynchronizations);
        }
    }

    private boolean wasASchedulingPoint() {
        TaskSource currentTaskSource = getTaskSource();
        // Check if the existing TaskSource is inconsistent with the current scheduling state
        if (currentTaskSource != null && currentTaskSource.getTask() != null &&
                currentTaskSource.getTask().isLeaf() &&
                getSchedulingStateType() != Type.SCHEDULING_POINT) {

            return true;
        }

        // Check if the scheduling state has changed WRT the DB
        return SchedulingState.Type.SCHEDULING_POINT == getCurrentVersionOnDB().getSchedulingStateType();
    }

    protected boolean currentTaskSourceIsNotTheSame() {
        return getOnDBTaskSource() != getTaskSource();
    }

    protected boolean hadATaskSource() {
        return getOnDBTaskSource() != null;
    }

    private List<TaskSourceSynchronization> childrenSynchronizations() {
        List<TaskSourceSynchronization> childrenOfGroup = new ArrayList<>();
        for (OrderElement orderElement : getSomewhatScheduledOrderElements()) {
            childrenOfGroup.addAll(orderElement.calculateSynchronizationsNeeded());
        }
        return childrenOfGroup;
    }

    private void removeUnscheduled(List<TaskSourceSynchronization> result) {
        for (OrderElement orderElement : getNoScheduledOrderElements()) {
            orderElement.removeTaskSource(result);
        }
    }

    private List<TaskSourceSynchronization> synchronizationForSchedulingPoint(
            SchedulingDataForVersion schedulingState) {

        if (thereIsNoTaskSource()) {
            getCurrentSchedulingData().requestedCreationOf(TaskSource.create(schedulingState, getHoursGroups()));

            return Collections.singletonList(TaskSource.mustAdd(getTaskSource()));
        } else if (getTaskSource().getTask().isLeaf()) {
            return Collections.singletonList(getTaskSource().withCurrentHoursGroup(getHoursGroups()));
        } else {
            return synchronizationsForFromPartiallyScheduledToSchedulingPoint(schedulingState);
        }
    }

    private List<TaskSourceSynchronization> synchronizationsForFromPartiallyScheduledToSchedulingPoint(
            SchedulingDataForVersion schedulingState) {

        List<TaskSourceSynchronization> result = new ArrayList<>();
        for (TaskSource each : getTaskSourcesFromBottomToTop()) {
            OrderElement orderElement = each.getOrderElement();
            result.add(orderElement.taskSourceRemoval());
        }

        TaskSource newTaskSource = TaskSource.create(schedulingState, getHoursGroups());
        getCurrentSchedulingData().requestedCreationOf(newTaskSource);
        result.add(TaskSource.mustAdd(newTaskSource));

        return result;
    }

    private boolean thereIsNoTaskSource() {
        return getTaskSource() == null;
    }

    private List<OrderElement> getSomewhatScheduledOrderElements() {
        List<OrderElement> result = new ArrayList<>();
        for (OrderElement orderElement : getChildren()) {
            if (orderElement.getSchedulingStateType().isSomewhatScheduled()) {
                result.add(orderElement);
            }
        }
        return result;
    }

    private List<OrderElement> getNoScheduledOrderElements() {
        List<OrderElement> result = new ArrayList<>();
        for (OrderElement orderElement : getChildren()) {
            if (orderElement.getSchedulingState().isNoScheduled()) {
                result.add(orderElement);
            }
        }
        return result;
    }

    private void removeTaskSource(List<TaskSourceSynchronization> result) {
        removeChildrenTaskSource(result);
        if (getOnDBTaskSource() != null) {
            result.add(taskSourceRemoval());
        } else {
            TaskSource taskSource = getTaskSource();
            if (taskSource != null) {
                taskSource.getTask().detach();
                getCurrentSchedulingData().taskSourceRemovalRequested();
            }
        }
    }

    private TaskSource getOnDBTaskSource() {
        return getCurrentVersionOnDB().getTaskSource();
    }

    SchedulingDataForVersion getCurrentVersionOnDB() {
        return schedulingDataForVersion.get(getCurrentSchedulingData().getOriginOrderVersion());
    }

    private TaskSourceSynchronization taskSourceRemoval() {
        Validate.notNull(getOnDBTaskSource());
        TaskSourceSynchronization result = TaskSource.mustRemove(getOnDBTaskSource());
        getCurrentSchedulingData().taskSourceRemovalRequested();

        return result;
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

    public void initializeType(SchedulingState.Type type) {
        if (!isNewObject()) {
            throw new IllegalStateException();
        }
        getCurrentSchedulingData().initializeType(type);
        schedulingState = null;
    }

    public void initializeTemplate(OrderElementTemplate template) {
        if (!isNewObject()) {
            throw new IllegalStateException();
        }

        if (this.template != null) {
            throw new IllegalStateException("already initialized");
        }

        this.template = template;
    }

    public boolean isSchedulingPoint() {
        return getSchedulingState().getType() == Type.SCHEDULING_POINT;
    }

    public OrderLineGroup getParent() {
        return parent;
    }

    public TaskElement getAssociatedTaskElement() {
        return getTaskSource() == null ? null : getTaskSource().getTask();
    }

    protected void setParent(OrderLineGroup parent) {
        this.parent = parent;
    }

    public abstract Integer getWorkHours();

    public abstract List<HoursGroup> getHoursGroups();

    @NotEmpty(message = "name not specified")
    public String getName() {
        return getInfoComponent().getName();
    }

    public void setName(String name) {
        if ( name != null && name.length() > 255 ) {
            name = name.substring(0, 255);
        }

        this.getInfoComponent().setName(name);
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public void setDescription(String description) {
        this.getInfoComponent().setDescription(description);
    }

    public String getDescription() {
        return getInfoComponent().getDescription();
    }

    public abstract OrderLine toLeaf();

    public abstract OrderLineGroup toContainer();

    public boolean isScheduled() {
        return getTaskSource() != null;
    }

    public boolean checkAtLeastOneHoursGroup() {
        return !getHoursGroups().isEmpty();
    }

    public boolean isFormatCodeValid(String code) {
        return !code.contains("_") && !"".equals(code);
    }

    public void setCode(String code) {
        this.getInfoComponent().setCode(code);
    }


    public String getCode() {
        return getInfoComponent().getCode();
    }

    public abstract OrderElementTemplate createTemplate();

    public abstract DirectAdvanceAssignment getReportGlobalAdvanceAssignment();

    public abstract void removeReportGlobalAdvanceAssignment();

    public abstract DirectAdvanceAssignment getAdvanceAssignmentByType(AdvanceType type);

    public DirectAdvanceAssignment getDirectAdvanceAssignmentByType(AdvanceType advanceType) {
        if (advanceType != null) {
            for (DirectAdvanceAssignment directAdvanceAssignment : getDirectAdvanceAssignments()) {
                if (directAdvanceAssignment.getAdvanceType().getId().equals(advanceType.getId())) {
                    return directAdvanceAssignment;
                }
            }
        }

        return null;
    }

    public Set<DirectAdvanceAssignment> getDirectAdvanceAssignments() {
        return Collections.unmodifiableSet(directAdvanceAssignments);
    }

    protected abstract Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignments();

    public abstract Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignments(AdvanceType advanceType);

    public abstract Set<IndirectAdvanceAssignment> getAllIndirectAdvanceAssignments(AdvanceType advanceType);

    protected abstract Set<DirectAdvanceAssignment> getAllDirectAdvanceAssignmentsReportGlobal();

    public void removeAdvanceAssignment(AdvanceAssignment advanceAssignment) {
        if (directAdvanceAssignments.contains(advanceAssignment)) {
            directAdvanceAssignments.remove(advanceAssignment);
            if (this.getParent() != null) {
                this.getParent().removeIndirectAdvanceAssignment(advanceAssignment.getAdvanceType());
                removeChildrenAdvanceInParents(this.getParent());
            }
            markAsDirtyLastAdvanceMeasurementForSpreading();
            updateSpreadAdvance();
        }
    }

    public Set<Label> getLabels() {
        return Collections.unmodifiableSet(labels);
    }

    public Set<Label> getAllLabels() {
        Set<Label> allLabels = new HashSet<>();
        allLabels.addAll(this.labels);
        if (parent != null) {
            allLabels.addAll(parent.getAllLabels());
        }

        return allLabels;
    }

    public void setLabels(Set<Label> labels) {
        this.labels = labels;
    }

    public void addLabel(Label label) {
        Validate.notNull(label);

        if (!checkAncestorsNoOtherLabelRepeated(label)) {
            throw new IllegalArgumentException("An ancestor has the same label assigned, " +
                    "so this element is already inheriting this label");
        }

        removeLabelOnChildren(label);

        labels.add(label);
    }

    protected void updateLabels() {
        if (parent != null) {
            Set<Label> toRemove = new HashSet<>();
            for (Label each : labels) {
                if (!parent.checkAncestorsNoOtherLabelRepeated(each)) {
                    toRemove.add(each);
                }
            }
            labels.removeAll(toRemove);
        }

        for (OrderElement each : getChildren()) {
            each.updateLabels();
        }
    }

    public void removeLabel(Label label) {
        labels.remove(label);
    }

    /**
     * Validate if the advanceAssignment can be added to the order element.
     * The list of advanceAssignments must be attached.
     *
     * @param newAdvanceAssignment
     *            must be attached
     * @throws DuplicateValueTrueReportGlobalAdvanceException
     * @throws DuplicateAdvanceAssignmentForOrderElementException
     */
    public void addAdvanceAssignment(DirectAdvanceAssignment newAdvanceAssignment)
            throws DuplicateValueTrueReportGlobalAdvanceException, DuplicateAdvanceAssignmentForOrderElementException {

        checkNoOtherGlobalAdvanceAssignment(newAdvanceAssignment);
        checkAncestorsNoOtherAssignmentWithSameAdvanceType(this, newAdvanceAssignment);
        checkChildrenNoOtherAssignmentWithSameAdvanceType(this, newAdvanceAssignment);

        if ( getReportGlobalAdvanceAssignment() == null ) {
            newAdvanceAssignment.setReportGlobalAdvance(true);
        }

        newAdvanceAssignment.setOrderElement(this);
        this.directAdvanceAssignments.add(newAdvanceAssignment);

        if ( this.getParent() != null ) {
            addChildrenAdvanceInParents(this.getParent());
            this.getParent()
                    .addIndirectAdvanceAssignment(newAdvanceAssignment.createIndirectAdvanceFor(this.getParent()));
        }
    }

    public void addChildrenAdvanceInParents(OrderLineGroup parent) {
        if ( (parent != null) && (!parent.existChildrenAdvance()) ) {
            parent.addChildrenAdvanceOrderLineGroup();
            addChildrenAdvanceInParents(parent.getParent());
        }

    }

    public void removeChildrenAdvanceInParents(OrderLineGroup parent) {
        if ( (parent != null) && (parent.existChildrenAdvance() ) && (!itsChildrenHasAdvances(parent))) {
            parent.removeChildrenAdvanceOrderLineGroup();
            removeChildrenAdvanceInParents(parent.getParent());
        }
    }

    private boolean itsChildrenHasAdvances(OrderElement orderElement) {
        for (OrderElement child : orderElement.getChildren()) {

            if ( (!child.getIndirectAdvanceAssignments().isEmpty()) ||
                    (!child.getDirectAdvanceAssignments().isEmpty()) ) {

                return true;
            }
            if ( itsChildrenHasAdvances(child) ) {
                return true;
            }
        }
        return false;
    }

    protected void checkNoOtherGlobalAdvanceAssignment(DirectAdvanceAssignment newAdvanceAssignment)
            throws DuplicateValueTrueReportGlobalAdvanceException {

        if ( !newAdvanceAssignment.getReportGlobalAdvance() ) {
            return;
        }

        for (DirectAdvanceAssignment directAdvanceAssignment : directAdvanceAssignments) {
            if ( directAdvanceAssignment.getReportGlobalAdvance() ) {
                throw new DuplicateValueTrueReportGlobalAdvanceException(
                        _("Cannot spread two progress in the same task"), this, OrderElement.class);
            }
        }
    }

    /**
     * It checks there are no {@link DirectAdvanceAssignment} with the same type in {@link OrderElement} and ancestors.
     *
     * @param orderElement
     * @param newAdvanceAssignment
     * @throws DuplicateAdvanceAssignmentForOrderElementException
     */
    public void checkAncestorsNoOtherAssignmentWithSameAdvanceType(
            OrderElement orderElement, DirectAdvanceAssignment newAdvanceAssignment)
            throws DuplicateAdvanceAssignmentForOrderElementException {

        for (DirectAdvanceAssignment directAdvanceAssignment : orderElement.getDirectAdvanceAssignments()) {

            if ( AdvanceType.equivalentInDB(
                    directAdvanceAssignment.getAdvanceType(), newAdvanceAssignment.getAdvanceType()) ) {

                throw new DuplicateAdvanceAssignmentForOrderElementException(
                        _("Duplicate Progress Assignment For Task"), this, OrderElement.class);
            }
        }
        if (orderElement.getParent() != null) {
            checkAncestorsNoOtherAssignmentWithSameAdvanceType(orderElement.getParent(), newAdvanceAssignment);
        }
    }

    /**
     * It checks there are no {@link AdvanceAssignment} with the same type in orderElement and its children.
     *
     * @param orderElement
     * @param newAdvanceAssignment
     * @throws DuplicateAdvanceAssignmentForOrderElementException
     */
    protected void checkChildrenNoOtherAssignmentWithSameAdvanceType(
            OrderElement orderElement, DirectAdvanceAssignment newAdvanceAssignment)
            throws DuplicateAdvanceAssignmentForOrderElementException {

        if (orderElement.existsDirectAdvanceAssignmentWithTheSameType(newAdvanceAssignment.getAdvanceType())) {
            throw new DuplicateAdvanceAssignmentForOrderElementException(
                    _("Duplicate Progress Assignment For Task"),
                    this,
                    OrderElement.class);
        }
        if (!orderElement.getChildren().isEmpty()) {
            for (OrderElement child : orderElement.getChildren()) {
                checkChildrenNoOtherAssignmentWithSameAdvanceType(child, newAdvanceAssignment);
            }
        }
    }

    public boolean existsDirectAdvanceAssignmentWithTheSameType(AdvanceType type) {
        for (DirectAdvanceAssignment directAdvanceAssignment : directAdvanceAssignments) {
            if (AdvanceType.equivalentInDB(directAdvanceAssignment.getAdvanceType(), type)) {
                return true;
            }
        }
        return false;
    }

    public BigDecimal getAdvancePercentage() {
        if ((dirtyLastAdvanceMeasurementForSpreading == null) || dirtyLastAdvanceMeasurementForSpreading) {
            lastAdvanceMeasurementForSpreading = getAdvancePercentage(null);
            dirtyLastAdvanceMeasurementForSpreading = false;
        }

        return lastAdvanceMeasurementForSpreading;
    }

    public abstract BigDecimal getAdvancePercentage(LocalDate date);

    public abstract Set<IndirectAdvanceAssignment> getIndirectAdvanceAssignments();

    public abstract DirectAdvanceAssignment calculateFakeDirectAdvanceAssignment(
            IndirectAdvanceAssignment indirectAdvanceAssignment);

    public abstract BigDecimal getAdvancePercentageChildren();

    public List<OrderElement> getAllChildren() {
        List<OrderElement> children = getChildren();
        List<OrderElement> result = new ArrayList<>();

        for (OrderElement orderElement : children) {
            result.add(orderElement);
            result.addAll(orderElement.getAllChildren());
        }
        return result;
    }

    public void setCriterionRequirements(Set<CriterionRequirement> criterionRequirements) {
        this.criterionRequirements = criterionRequirements;
    }

    @Valid
    @Override
    public Set<CriterionRequirement> getCriterionRequirements() {
        return Collections.unmodifiableSet(criterionRequirements);
    }

    /**
     * Operations to manage the criterion requirements of a orderElement
     * (remove, adding, update of the criterion requirement of the orderElement such as the descendant's criterion requirement)
     */

    public void setValidCriterionRequirement(IndirectCriterionRequirement requirement,boolean valid) {
        requirement.setValid(valid);
        criterionRequirementHandler.propagateValidCriterionRequirement(this, requirement.getParent(), valid);
    }

    public void removeDirectCriterionRequirement(DirectCriterionRequirement criterionRequirement) {
        criterionRequirementHandler.propagateRemoveCriterionRequirement(this, criterionRequirement);
        removeCriterionRequirement(criterionRequirement);
    }

    @Override
    public void removeCriterionRequirement(CriterionRequirement requirement) {
        criterionRequirements.remove(requirement);
        if (requirement instanceof IndirectCriterionRequirement) {
            ((IndirectCriterionRequirement)requirement).getParent().getChildren().remove(requirement);
        }
    }

    @Override
    public void addCriterionRequirement(CriterionRequirement criterionRequirement) {
        criterionRequirementHandler.addCriterionRequirement(this, criterionRequirement);
    }

    public void addDirectCriterionRequirement(CriterionRequirement criterionRequirement) {
        criterionRequirementHandler.addDirectCriterionRequirement(this, criterionRequirement);
    }

    public void addIndirectCriterionRequirement(IndirectCriterionRequirement criterionRequirement) {
        criterionRequirementHandler.addIndirectCriterionRequirement(this, criterionRequirement);
    }

    protected void basicAddCriterionRequirement(CriterionRequirement criterionRequirement) {
        criterionRequirement.setOrderElement(this);
        this.criterionRequirements.add(criterionRequirement);
    }

    public void updateCriterionRequirements() {
        criterionRequirementHandler.updateMyCriterionRequirements(this);
        criterionRequirementHandler.propagateUpdateCriterionRequirements(this);
    }

    public boolean canAddCriterionRequirement(DirectCriterionRequirement newRequirement) {
        return criterionRequirementHandler.canAddCriterionRequirement(this, newRequirement);
    }

    public Set<IndirectCriterionRequirement> getIndirectCriterionRequirement() {
        return criterionRequirementHandler.getIndirectCriterionRequirement(criterionRequirements);
    }

    public void updatePositionConstraintOf(Task task) {
        applyConstraintsInOrderElementParents(task);
    }

    public void applyInitialPositionConstraintTo(Task task) {
        boolean applied = applyConstraintsInOrderElementParents(task);
        if (applied) {
            return;
        }
        if (getOrder().isScheduleBackwards()) {
            task.getPositionConstraint().asLateAsPossible();
        } else {
            task.getPositionConstraint().asSoonAsPossible();
        }
    }

    /**
     * Searches for init date or end constraints on order element and order element parents.
     *
     * @param task
     * @return <code>true</code> if a constraint have been applied, otherwise
     *         <code>false</code>
     */
    private boolean applyConstraintsInOrderElementParents(Task task) {
        boolean scheduleBackwards = getOrder().isScheduleBackwards();
        OrderElement current = this;

        while (current != null) {
            boolean applied = current.applyConstraintBasedOnInitOrEndDate(task, scheduleBackwards);
            if (applied) {
                return true;
            }
            current = current.getParent();
        }

        return false;
    }

    protected boolean applyConstraintBasedOnInitOrEndDate(Task task, boolean scheduleBackwards) {
        TaskPositionConstraint constraint = task.getPositionConstraint();

        if (getInitDate() != null && (getDeadline() == null || !scheduleBackwards)) {
            constraint.notEarlierThan(IntraDayDate.startOfDay(LocalDate.fromDateFields(this.getInitDate())));
            return true;
        }

        return false;
    }

    public Set<DirectCriterionRequirement> getDirectCriterionRequirement() {
        return criterionRequirementHandler.getDirectCriterionRequirement(criterionRequirements);
    }

    public SchedulingState.Type getSchedulingStateType() {
        return getCurrentSchedulingData().getSchedulingStateType();
    }

    public TaskSource getTaskSource() {
        return getCurrentSchedulingData().getTaskSource();
    }

    public TaskElement getTaskElement() {
        TaskSource taskSource = getTaskSource();
        return taskSource == null ? null : taskSource.getTask();
    }

    public Set<TaskElement> getTaskElements() {
        return getTaskSource() == null
                ? Collections.emptySet()
                : Collections.singleton(getTaskSource().getTask());
    }

    public List<TaskSource> getTaskSourcesFromBottomToTop() {
        List<TaskSource> result = new ArrayList<>();
        taskSourcesFromBottomToTop(result);
        return result;
    }

    public List<TaskSource> getAllScenariosTaskSourcesFromBottomToTop() {
        List<TaskSource> result = new ArrayList<>();
        allScenariosTaskSourcesFromBottomToTop(result);
        return result;
    }

    public List<SchedulingDataForVersion> getSchedulingDataForVersionFromBottomToTop() {
        List<SchedulingDataForVersion> result = new ArrayList<>();
        schedulingDataForVersionFromBottomToTop(result);
        return result;
    }

    private void schedulingDataForVersionFromBottomToTop(List<SchedulingDataForVersion> result) {
        for (OrderElement each : getChildren()) {
            each.schedulingDataForVersionFromBottomToTop(result);
        }
        result.addAll(schedulingDataForVersion.values());
    }

    private void taskSourcesFromBottomToTop(List<TaskSource> result) {
        for (OrderElement each : getChildren()) {
            each.taskSourcesFromBottomToTop(result);
        }

        if ( getTaskSource() != null ) {
            result.add(getTaskSource());
        }
    }

    private void allScenariosTaskSourcesFromBottomToTop(List<TaskSource> result) {
        for (OrderElement each : getChildren()) {
            each.allScenariosTaskSourcesFromBottomToTop(result);
        }

        for (Entry<OrderVersion, SchedulingDataForVersion> each : schedulingDataForVersion.entrySet()) {
            TaskSource taskSource = each.getValue().getTaskSource();
            if (taskSource != null) {
                result.add(taskSource);
            }
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

    public BigDecimal getTotalMaterialAssignmentUnits() {
        BigDecimal result = BigDecimal.ZERO;

        final Set<MaterialAssignment> materialAssignments = getMaterialAssignments();

        for (MaterialAssignment each: materialAssignments) {
            result = result.add(each.getUnits());
        }
        return result;
    }

    public BigDecimal getTotalMaterialAssignmentPrice() {
        BigDecimal result = new BigDecimal(0);

        final Set<MaterialAssignment> materialAssignments = getMaterialAssignments();

        for (MaterialAssignment each: materialAssignments) {
            result = result.add(each.getTotalPrice());
        }
        return result;
    }

    public Order getOrder() {
        return parent == null ? null : parent.getOrder();
    }

    @Valid
    public Set<TaskQualityForm> getTaskQualityForms() {
        return Collections.unmodifiableSet(taskQualityForms);
    }

    public Set<QualityForm> getQualityForms() {
        Set<QualityForm> result = new HashSet<>();
        for (TaskQualityForm each : taskQualityForms) {
            result.add(each.getQualityForm());
        }
        return result;
    }

    public void setTaskQualityFormItems(Set<TaskQualityForm> taskQualityForms) {
        this.taskQualityForms = taskQualityForms;
    }

    public TaskQualityForm addTaskQualityForm(QualityForm qualityForm) throws ValidationException {
        checkUniqueQualityForm(qualityForm);
        TaskQualityForm taskQualityForm = TaskQualityForm.create(this, qualityForm);
        this.taskQualityForms.add(taskQualityForm);

        return taskQualityForm;
    }

    public void removeTaskQualityForm(TaskQualityForm taskQualityForm) {
        this.taskQualityForms.remove(taskQualityForm);
    }

    private void checkUniqueQualityForm(QualityForm qualityForm) throws ValidationException, IllegalArgumentException {
        Validate.notNull(qualityForm);
        for (TaskQualityForm taskQualityForm : getTaskQualityForms()) {

            if ( qualityForm.equals(taskQualityForm.getQualityForm()) ) {

                throw new ValidationException(ValidationException.invalidValue(
                        _("Quality form already exists"),
                        "name",
                        qualityForm.getName(),
                        qualityForm));
            }
        }
    }

    @Override
    public boolean isUniqueCodeConstraint() {
        // The automatic checking of this constraint is avoided because it uses the wrong code property
        return true;
    }

    @AssertTrue(message = "code is already used in another project")
    public boolean isCodeRepeatedInAnotherOrderConstraint() {
        return StringUtils.isBlank(getCode()) ||
                !Registry.getOrderElementDAO().existsByCodeInAnotherOrderAnotherTransaction(this);
    }

    @AssertTrue(message = "a label can not be assigned twice in the same branch")
    public boolean isLabelNotRepeatedInTheSameBranchConstraint() {
        return checkConstraintLabelNotRepeatedInTheSameBranch(new HashSet<>());
    }

    private boolean checkConstraintLabelNotRepeatedInTheSameBranch(HashSet<Label> parentLabels) {
        HashSet<Label> withThisLabels = new HashSet<>(parentLabels);
        for (Label label : getLabels()) {
            if (containsLabel(withThisLabels, label)) {
                return false;
            }
            withThisLabels.add(label);
        }
        for (OrderElement child : getChildren()) {
            if (!child.checkConstraintLabelNotRepeatedInTheSameBranch(withThisLabels)) {
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

    public boolean checkAncestorsNoOtherLabelRepeated(Label newLabel) {
        for (Label label : labels) {
            if (label.isEqualTo(newLabel)) {
                return false;
            }
        }

        return !(parent != null && !parent.checkAncestorsNoOtherLabelRepeated(newLabel));
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

    public boolean containsOrderElement(String code) {
        for (OrderElement child : getChildren()) {
            if (child.getCode().equals(code)) {
                return true;
            }
        }

        return false;
    }

    public OrderElement getOrderElement(String code) {
        if (code == null) {
            return null;
        }

        for (OrderElement child : getChildren()) {
            if (child.getCode().equals(code)) {
                return child;
            }
        }

        return null;
    }

    public boolean containsLabel(String code) {
        for (Label label : getLabels()) {
            if (label.getCode().equals(code)) {
                return true;
            }
        }

        return false;
    }

    public boolean containsLabels(Set<Label> labels) {
        Integer matches = 0;
        for (Label label : labels) {
            if (containsLabel(label.getCode())) {
                matches++;
            }
        }
        return matches == labels.size();
    }

    public boolean containsCriterion(String code) {
        for (CriterionRequirement criterionRequirement : getDirectCriterionRequirement()) {
            if (criterionRequirement.getCriterion().getCode().equals(code)) {
                return true;
            }
        }

        return false;
    }

    public boolean containsCriteria(Set<Criterion> criteria) {
        Integer matches = 0;
        for (Criterion criterion : criteria) {
            if (containsCriterion(criterion.getCode())) {
                matches++;
            }
        }
        return matches == criteria.size();
    }

    public boolean containsMaterialAssignment(String materialCode) {
        for (MaterialAssignment materialAssignment : getMaterialAssignments()) {
            if (materialAssignment.getMaterial().getCode().equals(materialCode)) {
                return true;
            }
        }

        return false;
    }

    public MaterialAssignment getMaterialAssignment(String materialCode) {
        for (MaterialAssignment materialAssignment : getMaterialAssignments()) {
            if (materialAssignment.getMaterial().getCode().equals(materialCode)) {
                return materialAssignment;
            }
        }

        return null;
    }

    public DirectAdvanceAssignment getDirectAdvanceAssignmentSubcontractor() {
        for (DirectAdvanceAssignment directAdvanceAssignment : directAdvanceAssignments) {

            if (directAdvanceAssignment.getAdvanceType().getUnitName()
                    .equals(PredefinedAdvancedTypes.SUBCONTRACTOR.getTypeName())) {

                return directAdvanceAssignment;
            }
        }

        return null;
    }

    public DirectAdvanceAssignment addSubcontractorAdvanceAssignment()
            throws DuplicateValueTrueReportGlobalAdvanceException, DuplicateAdvanceAssignmentForOrderElementException {

        boolean reportGlobalAdvance = false;
        if (getReportGlobalAdvanceAssignment() == null) {
            reportGlobalAdvance = true;
        }

        DirectAdvanceAssignment directAdvanceAssignment =
                DirectAdvanceAssignment.create(reportGlobalAdvance, new BigDecimal(100));

        directAdvanceAssignment.setAdvanceType(PredefinedAdvancedTypes.SUBCONTRACTOR.getType());

        addAdvanceAssignment(directAdvanceAssignment);

        return directAdvanceAssignment;
    }

    public InfoComponentWithCode getInfoComponent() {
        if (infoComponent == null) {
            infoComponent = new InfoComponentWithCode();
        }

        return infoComponent;
    }

    @Override
    public OrderElement getThis() {
        return this;
    }

    public void setExternalCode(String externalCode) {
        this.externalCode = externalCode;
    }

    public String getExternalCode() {
        return externalCode;
    }

    public abstract OrderLine calculateOrderLineForSubcontract();

    public Set<MaterialAssignment> getAllMaterialAssignments() {
        Set<MaterialAssignment> result = new HashSet<>();

        result.addAll(getMaterialAssignments());

        for (OrderElement orderElement : getChildren()) {
            result.addAll(orderElement.getAllMaterialAssignments());
        }

        return result;
    }

    /**
     * Calculate if the tasks of the planification point has finished.
     */

    public boolean isFinishPlanificationPointTask() {
        // Look up into the order elements tree
        TaskElement task = lookToUpAssignedTask();
        if (task != null) {
            return task.getOrderElement().isFinishedAdvance();
        }

        // Look down into the order elements tree
        List<TaskElement> listTask = lookToDownAssignedTask();
        if (!listTask.isEmpty()) {
            for (TaskElement taskElement : listTask) {
                if (!taskElement.getOrderElement().isFinishedAdvance()) {
                    return false;
                }
            }
        }

        // Not exist assigned task
        return (Registry.getOrderDAO().loadOrderAvoidingProxyFor(this)).isFinishedAdvance();
    }

    private TaskElement lookToUpAssignedTask() {
        OrderElement current = this;
        while (current != null) {
            if (isSchedulingPoint()) {
                return getAssociatedTaskElement();
            }
            current = current.getParent();
        }
        return null;
    }

    private List<TaskElement> lookToDownAssignedTask() {
        List<TaskElement> resultTask = new ArrayList<>();
        for (OrderElement child : getAllChildren()) {
            if (child.isSchedulingPoint()) {
                TaskElement task = child.getAssociatedTaskElement();
                if (task != null) {
                    resultTask.add(task);
                }
            }
        }

        return resultTask;
    }

    public boolean isFinishedAdvance() {
        BigDecimal measuredProgress = getAdvancePercentage();
        measuredProgress = measuredProgress.setScale(0, BigDecimal.ROUND_UP).multiply(new BigDecimal(100));

        return measuredProgress.compareTo(new BigDecimal(100)) == 0;
    }

    @Override
    protected IIntegrationEntityDAO<OrderElement> getIntegrationEntityDAO() {
        return Registry.getOrderElementDAO();
    }

    public void markAsDirtyLastAdvanceMeasurementForSpreading() {
        if (parent != null) {
            parent.markAsDirtyLastAdvanceMeasurementForSpreading();
        }

        dirtyLastAdvanceMeasurementForSpreading = true;
    }

    public void setSumChargedEffort(SumChargedEffort sumChargedHours) {
        this.sumChargedEffort = sumChargedHours;
    }

    public SumChargedEffort getSumChargedEffort() {
        return sumChargedEffort;
    }

    public void updateAdvancePercentageTaskElement() {
        BigDecimal advancePercentage = this.getAdvancePercentage();
        if (this.getTaskSource() != null && this.getTaskSource().getTask() != null) {
            this.getTaskSource().getTask().setAdvancePercentage(advancePercentage);
        }

        if (parent != null) {
            parent.updateAdvancePercentageTaskElement();
        }
    }

    public void setCodeAutogenerated(Boolean codeAutogenerated) {
        if ( getOrder().equals(this) ) {
            super.setCodeAutogenerated(codeAutogenerated);
        }
    }

    public Boolean isCodeAutogenerated() {
        if ( getOrder().equals(this) ) {
            return super.isCodeAutogenerated();
        }
        return (getOrder() != null) ? getOrder().isCodeAutogenerated() : false;
    }

    @AssertTrue(message = "a quality form cannot be assigned twice to the same task")
    public boolean isUniqueQualityFormConstraint() {
        Set<QualityForm> qualityForms = new HashSet<>();
        for (TaskQualityForm each : taskQualityForms) {
            QualityForm qualityForm = each.getQualityForm();

            if ( qualityForms.contains(qualityForm) ) {
                return false;
            }

            qualityForms.add(qualityForm);
        }
        return true;
    }

    public void removeDirectAdvancesInList(Set<DirectAdvanceAssignment> directAdvanceAssignments) {
        for (DirectAdvanceAssignment each : directAdvanceAssignments) {
            removeAdvanceAssignment(getAdvanceAssignmentByType(each.getAdvanceType()));
        }

        for (OrderElement each : getChildren()) {
            each.removeDirectAdvancesInList(directAdvanceAssignments);
        }
    }

    protected Set<DirectAdvanceAssignment> getDirectAdvanceAssignmentsAndAllInAncest() {
        Set<DirectAdvanceAssignment> result = new HashSet<>();

        result.addAll(directAdvanceAssignments);

        if ( getParent() != null ) {
            result.addAll(getParent().getDirectAdvanceAssignmentsAndAllInAncest());
        }

        return result;
    }

    protected void updateSpreadAdvance() {
        if ( getReportGlobalAdvanceAssignment() == null ) {
            // Set PERCENTAGE type as spread if any
            String type = PredefinedAdvancedTypes.PERCENTAGE.getTypeName();
            for (DirectAdvanceAssignment each : directAdvanceAssignments) {

                if ( each.getAdvanceType() != null &&
                        each.getAdvanceType().getType() != null &&
                        each.getAdvanceType().getType().equals(type) ) {

                    each.setReportGlobalAdvance(true);

                    return;
                }
            }

            // Otherwise, set first advance assignment
            if ( !directAdvanceAssignments.isEmpty() ) {
                directAdvanceAssignments.iterator().next().setReportGlobalAdvance(true);

                return;
            }
        }
    }

    public List<OrderVersion> getOrderVersions() {
        return new ArrayList<>(schedulingDataForVersion.keySet());
    }

    @Override
    public String toString() {
        return super.toString() + " :: " + getName();
    }

    public List<WorkReportLine> getWorkReportLines(boolean sortedByDate) {
        return Registry.getWorkReportLineDAO().findByOrderElementAndChildren(this, sortedByDate);
    }

    /**
     * Gets workReportLines of this order-element between the specified
     * <code>startDate</code> and <code>endDate</code>.
     *
     * @param startDate
     *            the startDate
     * @param endDate
     *            the endDate
     * @param sortedByDate
     * @return list of workReportLines
     */
    public List<WorkReportLine> getWorkReportLines(Date startDate, Date endDate, boolean sortedByDate) {
        return Registry
                .getWorkReportLineDAO()
                .findByOrderElementAndChildrenFilteredByDate(this, startDate, endDate, sortedByDate);
    }

    /**
     * Checks if it has nay consolidated advance, if not checks if any parent has it.
     */
    public boolean hasAnyConsolidatedAdvance() {
        for (DirectAdvanceAssignment each : directAdvanceAssignments) {
            if (each.hasAnyConsolidationValue()) {
                return true;
            }
        }

        for (IndirectAdvanceAssignment each : getIndirectAdvanceAssignments()) {
            if (each.hasAnyConsolidationValue()) {
                return true;
            }
        }

        if (parent != null) {
            return parent.hasAnyConsolidatedAdvance();
        }

        return false;
    }

    public abstract BigDecimal getBudget();

    public void setSumExpenses(SumExpenses sumExpenses) {
        this.sumExpenses = sumExpenses;
    }

    public SumExpenses getSumExpenses() {
        return this.sumExpenses;
    }

    public boolean isOrder() {
        return false;
    }

    public boolean hasTimesheetsReportingHours() {
        return sumChargedEffort != null && sumChargedEffort.getFirstTimesheetDate() != null;
    }

    public boolean isFinishedTimesheets() {
        return sumChargedEffort == null ? false : sumChargedEffort.isFinishedTimesheets();
    }

    @Override
    public boolean isUpdatedFromTimesheets() {
        TaskElement taskElement = getTaskElement();

        return taskElement == null ? false : taskElement.isUpdatedFromTimesheets();
    }

    public Date getFirstTimesheetDate() {
        return sumChargedEffort == null ? null : sumChargedEffort.getFirstTimesheetDate();
    }

    public Date getLastTimesheetDate() {
        return sumChargedEffort == null ? null : sumChargedEffort.getLastTimesheetDate();
    }

    public void detachFromParent() {
        parent = null;
    }

    public AdvanceMeasurement getLastAdvanceMeasurement() {
        DirectAdvanceAssignment advanceAssignment = getReportGlobalAdvanceAssignment();
        return advanceAssignment == null ? null : advanceAssignment.getLastAdvanceMeasurement();
    }

    public String getEffortAsString() {
        SumChargedEffort sumChargedEffort = getSumChargedEffort();

        EffortDuration effort = sumChargedEffort != null
                ? sumChargedEffort.getTotalChargedEffort()
                : EffortDuration.zero();

        return effort.toFormattedString();
    }

    public boolean isJiraIssue() {
        String code = getCode();
        return code == null
                ? false
                : code.startsWith(PredefinedConnectorProperties.JIRA_CODE_PREFIX);
    }

    public boolean isConvertedToContainer() {
        return false;
    }

    public BigDecimal getTotalBudget() {
        return getBudget().add(getResourcesBudget());
    }

    public BigDecimal getSubstractedBudget() {
        return getBudget().subtract(getResourcesBudget());
    }

    public BigDecimal getResourcesBudget() {
        return Registry.getTransactionService().runOnReadOnlyTransaction(
                () -> calculateBudgetFromCriteriaAndCostCategories());
    }

    public BigDecimal calculateBudgetFromCriteriaAndCostCategories() {
        BigDecimal totalBudget = new BigDecimal(0);

        Configuration configuration = Registry.getConfigurationDAO().getConfiguration();
        TypeOfWorkHours typeofWorkHours = configuration.getBudgetDefaultTypeOfWorkHours();

        if (!configuration.isEnabledAutomaticBudget() || (configuration.getBudgetDefaultTypeOfWorkHours() == null)) {
            return totalBudget;
        }

        BigDecimal costPerHour = new BigDecimal(0);
        BigDecimal hours;

        for (HoursGroup hoursGroup : getHoursGroups()) {
            hours = new BigDecimal(hoursGroup.getWorkingHours());

            for (CriterionRequirement crit : hoursGroup.getCriterionRequirements()) {
                CostCategory costcat = crit.getCriterion().getCostCategory();

                if (costcat != null) {

                    IHourCostDAO hourCostDAO = Registry.getHourCostDAO();
                    costPerHour = hourCostDAO.getPriceCostFromCriterionAndType(costcat, typeofWorkHours);

                }
                totalBudget = totalBudget.add(costPerHour.multiply(hours));
            }
            if (hoursGroup.getCriterionRequirements().size() > 1) {
                totalBudget = totalBudget.divide(new BigDecimal(hoursGroup.getCriterionRequirements().size()));
            }
        }

        return totalBudget;
    }

    /**
     * Returns with margin calculated hours for this orderElement.
     */
    public EffortDuration getWithMarginCalculatedHours() {
        return calculateWorkHoursWithMargin();
    }

    /**
     * Calculates the work hours with the margin {@link Order#getHoursMargin()} for this orderElement.
     *
     * @return calculated work hours
     */
    private EffortDuration calculateWorkHoursWithMargin() {
        BigDecimal margin =
                this.getOrder().getHoursMargin() != null
                        ? new BigDecimal(this.getOrder().getHoursMargin()).setScale(2)
                        : BigDecimal.ZERO;

        BigDecimal hundred = new BigDecimal(100);

        BigDecimal estimatedHours = new BigDecimal(getWorkHours()).setScale(2);

        BigDecimal marginHours = estimatedHours.multiply(margin).divide(hundred, 2, BigDecimal.ROUND_HALF_EVEN);

        BigDecimal result = estimatedHours.add(marginHours);

        return EffortDuration.fromHoursAsBigDecimal(result);
    }

    /**
     * Returns with margin calculated budget for this orderElement.
     */
    public BigDecimal getWithMarginCalculatedBudget() {
        return calculateBudgetWithMargin();
    }

    /**
     * Calculates the budget with the margin {@link Order#getBudgetMargin()} for this orderElement.
     *
     * @return calculated budget
     */
    private BigDecimal calculateBudgetWithMargin() {
        BigDecimal margin =
                this.getOrder().getBudgetMargin() != null
                        ? new BigDecimal(this.getOrder().getBudgetMargin())
                        : BigDecimal.ZERO;

        BigDecimal hundred = new BigDecimal(100);

        BigDecimal budget = getBudget();
        BigDecimal marginBudget = budget.multiply(margin).divide(hundred, 2, BigDecimal.ROUND_HALF_EVEN);

        return budget.add(marginBudget);
    }

}
