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

package org.libreplan.web.planner.allocation;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine.Interval;
import org.libreplan.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator.CapacityAvailable;
import org.libreplan.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator.CapacityResult;
import org.libreplan.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator.CapacityResult.IMatcher;
import org.libreplan.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator.ResourcesPerDayIsZero;
import org.libreplan.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator.ThereAreNoValidPeriods;
import org.libreplan.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator.ValidPeriodsDontHaveCapacity;
import org.libreplan.business.planner.entities.AssignedEffortForResource;
import org.libreplan.business.planner.entities.AssignedEffortForResource.IAssignedEffortForResource;
import org.libreplan.business.planner.entities.AssignedEffortForResource.WithTheLoadOf;
import org.libreplan.business.planner.entities.AssignmentFunction.AssignmentFunctionName;
import org.libreplan.business.planner.entities.AssignmentFunction;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.GenericResourceAllocation;
import org.libreplan.business.planner.entities.CalculatedValue;
import org.libreplan.business.planner.entities.DerivedAllocation;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.Task.ModifiedAllocation;
import org.libreplan.business.planner.entities.allocationalgorithms.AllocationModification;
import org.libreplan.business.planner.entities.allocationalgorithms.AllocationModification.IByType;
import org.libreplan.business.planner.entities.allocationalgorithms.EffortModification;
import org.libreplan.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.libreplan.business.resources.daos.IResourcesSearcher;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.ResourceEnum;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.EffortDuration.IEffortFrom;
import org.libreplan.business.workingday.ResourcesPerDay;
import org.libreplan.web.common.EffortDurationBox;
import org.libreplan.web.common.LenientDecimalBox;
import org.libreplan.web.common.Util;
import org.libreplan.web.planner.allocation.ResourceAllocationController.DerivedAllocationColumn;
import org.zkoss.zk.au.out.AuWrongValue;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.SimpleConstraint;
import org.zkoss.zul.SimpleListModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.libreplan.java.zk.components.customdetailrowcomponent.Detail;

import static org.libreplan.business.workingday.EffortDuration.zero;
import static org.libreplan.web.I18nHelper._;

/**
 * It connects the GUI widgets of the allocation row in the GUI with the
 * {@link AllocationRow#transientAllocationBeingModified}.
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class AllocationRow {

    private static final ResourcesPerDay RESOURCES_PER_DAY_DEFAULT_VALUE = ResourcesPerDay.amount(1);

    static final SimpleConstraint CONSTRAINT_FOR_RESOURCES_PER_DAY =
            new SimpleConstraint(SimpleConstraint.NO_EMPTY | SimpleConstraint.NO_NEGATIVE);

    private static final SimpleConstraint CONSTRAINT_FOR_HOURS_INPUT =
            new SimpleConstraint(SimpleConstraint.NO_EMPTY | SimpleConstraint.NO_NEGATIVE);

    private static final Log LOG = LogFactory.getLog(AllocationRow.class);

    /**
     * The original allocation.
     * If <code>null</code> this {@link AllocationRow} represents a new allocation.
     * Otherwise, it's the modification of an existing allocation.
     */
    private final ResourceAllocation<?> original;

    private CalculatedValue currentCalculatedValue;

    /**
     * The allocation that is being modified, it's not the one stored in the
     * database, only a copy used to receive the ongoing changes being done in the allocation form.
     * Later, the modifications are retrieved via
     * {@link AllocationRow#getModificationsDone(Collection)} and the changes
     * can be merged into the {@link AllocationRow#original} allocation.
     *
     * @see AllocationResult
     * @see AllocationRow#getModificationsDone(Collection)
     */
    private ResourceAllocation<?> transientAllocationBeingModified;

    private String name;

    private EffortDurationBox effortInput = new EffortDurationBox();

    private final Decimalbox intendedResourcesPerDayInput = new LenientDecimalBox();

    private ResourcesPerDay editedValue;

    private final Label realResourcesPerDay = new Label();

    private Grid derivedAllocationsGrid;

    private Listbox assignmentFunctionListbox;

    private Component currentDetail;

    public AllocationRow(CalculatedValue calculatedValue) {
        this.currentCalculatedValue = calculatedValue;
        this.original = null;
        setResourcesPerDayEditedValue(RESOURCES_PER_DAY_DEFAULT_VALUE);
        initialize();
    }

    public AllocationRow(ResourceAllocation<?> origin) {
        this.original = origin;
        this.currentCalculatedValue = origin.getTask().getCalculatedValue();
        setResourcesPerDayEditedValue(resourcesPerDayForInputFrom(origin));

        if (!origin.areIntendedResourcesPerDaySatisfied()) {
            onDifferentRealResourcesPerDay(origin.getNonConsolidatedResourcePerDay());
        }

        loadEffort();
        initialize();
    }

    static EffortDuration sumAllConsolidatedEffort(Collection<? extends AllocationRow> rows) {
        return EffortDuration.sum(rows, new IEffortFrom<AllocationRow>() {
            @Override
            public EffortDuration from(AllocationRow each) {
                return each.getConsolidatedEffort();
            }
        });
    }

    static EffortDuration sumAllTotalEffort(Collection<? extends AllocationRow> rows) {
        return EffortDuration.sum(rows, new IEffortFrom<AllocationRow>() {
            @Override
            public EffortDuration from(AllocationRow each) {
                return each.getTotalEffort();
            }
        });
    }

    static EffortDuration sumAllOriginalEffort(Collection<? extends AllocationRow> rows) {
        return EffortDuration.sum(rows, new IEffortFrom<AllocationRow>() {
            @Override
            public EffortDuration from(AllocationRow each) {
                return each.getOriginalEffort();
            }
        });
    }

    static EffortDuration sumAllEffortFromInputs(Collection<? extends AllocationRow> rows) {
        return EffortDuration.sum(rows, new IEffortFrom<AllocationRow>() {
            @Override
            public EffortDuration from(AllocationRow each) {
                return each.getEffortFromInput();
            }
        });
    }

    static void assignEfforts(List<AllocationRow> rows, EffortDuration[] efforts) {
        int i = 0;
        for (AllocationRow each : rows) {
            each.effortInput.setValue(efforts[i++]);
        }
    }

    static void unknownResourcesPerDay(List<AllocationRow> rows) {
        for (AllocationRow each : rows) {
            each.setUnknownResourcesPerDay();
        }
    }

    static void assignResourcesPerDay(List<AllocationRow> rows, ResourcesPerDay[] resourcesPerDay) {
        int i = 0;
        for (AllocationRow each : rows) {
            each.setResourcesPerDayEditedValue(resourcesPerDay[i++]);
            each.clearRealResourcesPerDay();
        }
    }

    static void updateUIWithModificationsDone(List<? extends AllocationRow> rows,
                                              List<? extends AllocationModification> modifications) {

        Validate.isTrue(rows.size() == modifications.size());
        Iterator<? extends AllocationModification> iterator = modifications.iterator();

        for (AllocationRow each : rows) {
            each.updateUIWithModificationsDone();
            each.clearRealResourcesPerDay();

            AllocationModification modification = iterator.next();
            if (!modification.satisfiesModificationRequested()) {
                each.warnObjectiveNotSatisfied(modification);
            }
        }
    }

    static List<ResourcesPerDayModification> createAndAssociate(
            Task task,
            Collection<? extends AllocationRow> rows,
            Collection<? extends ResourceAllocation<?>> requestedToRemove) {

        List<ResourcesPerDayModification> result = new ArrayList<>();
        for (AllocationRow each : rows) {
            ResourcesPerDayModification modification = each.toResourcesPerDayModification(task);
            result.add(modification);
            each.associateAllocationToModify(modification.getBeingModified());
        }
        setCustomAssignedEffortForResource(rows, requestedToRemove);

        return result;
    }

    private static void setCustomAssignedEffortForResource(
            Collection<? extends AllocationRow> rows,
            Collection<? extends ResourceAllocation<?>> requestedToRemove) {

        List<ResourceAllocation<?>> allocationsToDiscount = getToDiscount(rows);
        allocationsToDiscount.addAll(requestedToRemove);

        final IAssignedEffortForResource effortForResource =
                AssignedEffortForResource.effortDiscounting(allocationsToDiscount);

        List<ResourceAllocation<?>> beingModified = AllocationRow.getBeingModified(rows);
        final WithTheLoadOf withTheLoadOf = AssignedEffortForResource.withTheLoadOf(beingModified);

        List<GenericResourceAllocation> allocations =
                ResourceAllocation.getOfType(GenericResourceAllocation.class, beingModified);

        for (GenericResourceAllocation each : allocations) {

            IAssignedEffortForResource custom =
                    AssignedEffortForResource.sum(withTheLoadOf.withoutConsidering(each), effortForResource);

            each.setAssignedEffortForResource(custom);
        }
    }

    private static List<ResourceAllocation<?>> getToDiscount(Collection<? extends AllocationRow> rows) {
        List<ResourceAllocation<?>> result = new ArrayList<>();
        for (AllocationRow each : rows) {
            if (each.getOrigin() != null) {
                result.add(each.getOrigin());
            }
        }

        return result;
    }

    public static AllocationRow find(Collection<? extends AllocationRow> rows,
                                     ResourceAllocation<?> allocationBeingModified) {

        for (AllocationRow each : rows) {
            if (each.transientAllocationBeingModified == allocationBeingModified) {
                return each;
            }
        }

        return null;
    }

    static List<EffortModification> createHoursModificationsAndAssociate(
            Task task,
            List<AllocationRow> currentRows,
            Collection<? extends ResourceAllocation<?>> requestedToRemove) {

        List<EffortModification> result = new ArrayList<>();
        for (AllocationRow each : currentRows) {
            EffortModification hoursModification = each.toHoursModification(task);
            result.add(hoursModification);
            each.associateAllocationToModify(hoursModification.getBeingModified());
        }
        setCustomAssignedEffortForResource(currentRows, requestedToRemove);

        return result;
    }

    static List<ModifiedAllocation> getModificationsDone(Collection<? extends AllocationRow> rows) {
        List<ModifiedAllocation> result = new ArrayList<>();
        for (AllocationRow each : rows) {
            Validate.notNull(each.transientAllocationBeingModified);
            if (each.original != null) {
                result.add(new ModifiedAllocation(each.original, each.transientAllocationBeingModified));
            }
        }

        return result;
    }

    static List<ResourceAllocation<?>> getNewFrom(List<AllocationRow> rows) {
        List<ResourceAllocation<?>> result = new ArrayList<>();
        for (AllocationRow each : rows) {
            Validate.notNull(each.transientAllocationBeingModified);
            if (each.original == null) {
                result.add(each.transientAllocationBeingModified);
            }
        }

        return result;
    }

    static List<ResourceAllocation<?>> getBeingModified(Collection<? extends AllocationRow> rows) {
        List<ResourceAllocation<?>> result = new ArrayList<>();
        for (AllocationRow each : rows) {
            if (each.transientAllocationBeingModified != null) {
                result.add(each.transientAllocationBeingModified);
            }
        }

        return result;
    }

    static List<GenericAllocationRow> getGeneric(Collection<? extends AllocationRow> all) {
        List<GenericAllocationRow> result = new ArrayList<>();

        for (AllocationRow each : all) {
            if (each.isGeneric()) {
                result.add((GenericAllocationRow) each);
            }
        }

        return result;
    }

    static List<AllocationRow> toRows(
            Collection<? extends ResourceAllocation<?>> resourceAllocations, IResourcesSearcher searchModel) {

        List<AllocationRow> result = new ArrayList<>();
        result.addAll(GenericAllocationRow.toGenericAllocations(resourceAllocations, searchModel));
        result.addAll(SpecificAllocationRow.toSpecificAllocations(resourceAllocations));

        return result;
    }

    private static ResourcesPerDay resourcesPerDayForInputFrom(ResourceAllocation<?> resourceAllocation) {
        CalculatedValue calculatedValue = resourceAllocation.getTask().getCalculatedValue();

        return calculatedValue == CalculatedValue.RESOURCES_PER_DAY
                ? resourceAllocation.getNonConsolidatedResourcePerDay()
                : resourceAllocation.getIntendedResourcesPerDay();
    }

    private void initializeResourcesPerDayInput() {
        intendedResourcesPerDayInput.setConstraint(CONSTRAINT_FOR_RESOURCES_PER_DAY);
        intendedResourcesPerDayInput.setSclass("assigned-resources-input");

        Util.bind(
                intendedResourcesPerDayInput,
                () -> getResourcesPerDayEditedValue().getAmount(),
                value -> {
                    BigDecimal amount = value == null ? new BigDecimal(0) : value;
                    setResourcesPerDayEditedValue(ResourcesPerDay.amount(amount));
                });
    }

    private void initialize() {
        initializeResourcesPerDayInput();
        effortInput.setSclass("assigned-hours-input");
        effortInput.setConstraint(constraintForHoursInput());
        loadEffort();

        assignmentFunctionListbox = new Listbox();
        assignmentFunctionListbox.setMold("select");
        assignmentFunctionListbox.setStyle("font-size: 10px");

        updateAssignmentFunctionListbox();
    }

    private void updateAssignmentFunctionListbox() {
        initializeAndAppendFlatFunction();

        AssignmentFunction function = getAssignmentFunction();
        if (function != null) {
            Listitem listitem = new Listitem(_(function.getName()));
            listitem.setDisabled(true);
            assignmentFunctionListbox.appendChild(listitem);
            assignmentFunctionListbox.setSelectedItem(listitem);
        }
    }

    private void initializeAndAppendFlatFunction() {
        Listitem listitem = new Listitem(_(AssignmentFunctionName.FLAT.toString()));
        assignmentFunctionListbox.getChildren().clear();
        assignmentFunctionListbox.appendChild(listitem);
        assignmentFunctionListbox.setSelectedItem(listitem);
    }

    public abstract ResourcesPerDayModification toResourcesPerDayModification(Task task);

    public abstract EffortModification toHoursModification(Task task);

    public boolean isCreating() {
        return original == null;
    }

    boolean isModifying() {
        return original != null;
    }

    public ResourceAllocation<?> getOrigin() {
        return original;
    }

    private void onDifferentRealResourcesPerDay(ResourcesPerDay realResourcesPerDay) {
        this.realResourcesPerDay.setSclass("assigned-resources-label");

        this.realResourcesPerDay.setTooltiptext(_(
                "Only {0} resources per day were achieved for current allocation",
                realResourcesPerDay.getAmount().toPlainString()));

        this.realResourcesPerDay.setValue(realResourcesPerDay.getAmount().toPlainString());
    }

    private void clearRealResourcesPerDay() {
        this.realResourcesPerDay.setValue("");
    }

    private boolean hasDerivedAllocations() {
        return ! getDerivedAllocations().isEmpty();
    }

    private List<DerivedAllocation> getDerivedAllocations() {
        if (transientAllocationBeingModified != null) {
            return new ArrayList<>(transientAllocationBeingModified.getDerivedAllocations());
        } else if (original != null) {
            return new ArrayList<>(original.getDerivedAllocations());
        } else {
            return Collections.emptyList();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    ResourcesPerDay getResourcesPerDayEditedValue() {
        return this.editedValue;
    }

    ResourcesPerDay getResourcesPerDayFromInput() {
        BigDecimal value = intendedResourcesPerDayInput.getValue();
        value = value != null ? value : BigDecimal.ZERO;

        return ResourcesPerDay.amount(value);
    }

    private void setResourcesPerDayEditedValue(ResourcesPerDay resourcesPerDay) {
        this.editedValue = resourcesPerDay;
        intendedResourcesPerDayInput.setValue(getAmount(resourcesPerDay));
    }

    private void setUnknownResourcesPerDay() {
        this.editedValue = null;
        this.intendedResourcesPerDayInput.setValue((BigDecimal) null);
        clearRealResourcesPerDay();
    }

    private BigDecimal getAmount(ResourcesPerDay resourcesPerDay) {
        return (resourcesPerDay != null) ? resourcesPerDay.getAmount() : new BigDecimal(0);
    }

    private void associateAllocationToModify(ResourceAllocation<?> allocationToModify) {
        Validate.notNull(allocationToModify);
        this.transientAllocationBeingModified = allocationToModify;
    }

    public abstract boolean isGeneric();

    boolean isEmptyResourcesPerDay() {
        return getResourcesPerDayEditedValue() == null || getResourcesPerDayEditedValue().isZero();
    }

    public abstract List<Resource> getAssociatedResources();

    EffortDurationBox getEffortInput() {
        return effortInput;
    }

    Decimalbox getIntendedResourcesPerDayInput() {
        return intendedResourcesPerDayInput;
    }

    Label getRealResourcesPerDay() {
        return realResourcesPerDay;
    }

    void addListenerForInputChange(EventListener onChangeListener) {
        getEffortInput().addEventListener(Events.ON_CHANGE, onChangeListener);
        getIntendedResourcesPerDayInput().addEventListener(Events.ON_CHANGE, onChangeListener);
    }

    void loadEffort() {
        effortInput.setValue(getEffort());
    }

    void loadAssignmentFunctionName() {
        updateAssignmentFunctionListbox();
    }

    EffortDuration getEffortFromInput() {
        return effortInput.getValue() != null ? effortInput.getEffortDurationValue() : zero();
    }

    private EffortDuration getEffort() {
        if (transientAllocationBeingModified != null) {
            return transientAllocationBeingModified.getNonConsolidatedEffort();
        }
        if (original != null) {
            return original.getNonConsolidatedEffort();
        }

        return zero();
    }

    void applyDisabledRules(CalculatedValue calculatedValue, boolean recommendedAllocation, boolean isAnyManual) {
        this.currentCalculatedValue = calculatedValue;

        effortInput.setDisabled(
                calculatedValue != CalculatedValue.RESOURCES_PER_DAY || recommendedAllocation || isAnyManual);

        effortInput.setConstraint(constraintForHoursInput());

        intendedResourcesPerDayInput.setDisabled(
                calculatedValue == CalculatedValue.RESOURCES_PER_DAY || recommendedAllocation || isAnyManual);

        if (intendedResourcesPerDayInput.isDisabled()) {
            clearRealResourcesPerDay();
        }
        intendedResourcesPerDayInput.setConstraint(constraintForResourcesPerDayInput());
    }

    private AssignmentFunction getAssignmentFunction() {
        if (transientAllocationBeingModified != null) {
            return transientAllocationBeingModified.getAssignmentFunction();
        }
        if (original != null) {
            return original.getAssignmentFunction();
        }

        return null;
    }

    boolean isAssignmentFunctionNotFlat() {
        return getAssignmentFunction() != null;
    }

    boolean isAssignmentFunctionManual() {
        AssignmentFunction assignmentFunction = getAssignmentFunction();

        return (assignmentFunction != null) && assignmentFunction.isManual();
    }

    private Constraint constraintForHoursInput() {
        return effortInput.isDisabled() ? null : CONSTRAINT_FOR_HOURS_INPUT;
    }

    private Constraint constraintForResourcesPerDayInput() {
        return intendedResourcesPerDayInput.isDisabled() ? null : CONSTRAINT_FOR_RESOURCES_PER_DAY;
    }

    private void updateUIWithModificationsDone() {
        Clients.clearWrongValue(effortInput);
        Clients.clearWrongValue(intendedResourcesPerDayInput);

        effortInput.setValue(transientAllocationBeingModified.getAssignedEffort());
        loadResourcesPerDayFrom(transientAllocationBeingModified);
    }

    private void warnObjectiveNotSatisfied(AllocationModification modification) {
        modification.byType(new IByType<Void>() {

            @Override
            public Void onResourcesPerDay(ResourcesPerDayModification modification) {

                ResourcesPerDay realResourcesPerDay =
                        modification.getBeingModified().getNonConsolidatedResourcePerDay();

                onDifferentRealResourcesPerDay(realResourcesPerDay);

                return null;
            }

            @Override
            public Void onHours(EffortModification modification) {
                EffortDuration goal = modification.getEffort();
                Clients.response(new AuWrongValue(effortInput, _("{0} cannot be fulfilled", goal.toFormattedString())));

                return null;
            }
        });
    }

    void addListenerForHoursInputChange(EventListener listener) {
        effortInput.addEventListener(Events.ON_CHANGE, listener);
    }

    void setEffortToInput(EffortDuration effort) {
        effortInput.setValue(effort);
    }

    void addListenerForResourcesPerDayInputChange(EventListener resourcesPerDayRowInputChange) {
        intendedResourcesPerDayInput.addEventListener(Events.ON_CHANGE, resourcesPerDayRowInputChange);
    }

    void reloadDerivedAllocationsGrid() {
        if (hasDerivedAllocations() && !(currentDetail instanceof Detail)) {
            replaceOld(currentDetail, createDetail());
        }

        reloadDerivedAllocationsData();
    }

    private void reloadDerivedAllocationsData() {
        if (derivedAllocationsGrid != null) {
            derivedAllocationsGrid.setModel(new SimpleListModel<>(getDerivedAllocations()));
        }
    }

    private void replaceOld(Component oldDetail, Component newDetail) {
        Component parent = oldDetail.getParent();
        parent.insertBefore(newDetail, oldDetail);
        parent.removeChild(oldDetail);
    }

    Component createDetail() {
        if (!hasDerivedAllocations()) {
            return currentDetail = new Label();
        }

        Detail result = new Detail();
        createDerivedAllocationsGrid();
        result.appendChild(derivedAllocationsGrid);
        reloadDerivedAllocationsData();

        return currentDetail = result;
    }

    private void createDerivedAllocationsGrid() {
        if (derivedAllocationsGrid != null) {
            return;
        }

        derivedAllocationsGrid = new Grid();
        DerivedAllocationColumn.appendColumnsTo(derivedAllocationsGrid);
        derivedAllocationsGrid.setRowRenderer(DerivedAllocationColumn.createRenderer());
    }

    public boolean isSatisfied() {
        return transientAllocationBeingModified != null
                ? transientAllocationBeingModified.isSatisfied()
                : original != null && original.isSatisfied();
    }

    EffortDuration getOriginalEffort() {
        if (transientAllocationBeingModified != null) {
            return transientAllocationBeingModified.getIntendedTotalAssignment();
        }

        if (original != null) {
            return original.getIntendedTotalAssignment();
        }

        return zero();
    }

    EffortDuration getTotalEffort() {
        if (transientAllocationBeingModified != null) {
            return transientAllocationBeingModified.getAssignedEffort();
        }

        if (original != null) {
            return original.getAssignedEffort();
        }

        return zero();
    }

    EffortDuration getConsolidatedEffort() {
        if (transientAllocationBeingModified != null) {
            return transientAllocationBeingModified.getConsolidatedEffort();
        }

        if (original != null) {
            return original.getConsolidatedEffort();
        }

        return zero();
    }

    public int getNonConsolidatedHours() {
        if (transientAllocationBeingModified != null) {
            return transientAllocationBeingModified.getNonConsolidatedHours();
        }

        if (original != null) {
            return original.getNonConsolidatedHours();
        }

        return 0;
    }

    ResourcesPerDay getTotalResourcesPerDay() {
        if (transientAllocationBeingModified != null) {
            return transientAllocationBeingModified.calculateResourcesPerDayFromAssignments();
        }

        if (original != null) {
            return original.calculateResourcesPerDayFromAssignments();
        }

        return ResourcesPerDay.amount(0);
    }

    ResourcesPerDay getConsolidatedResourcesPerDay() {
        if (transientAllocationBeingModified != null) {
            return transientAllocationBeingModified.getConsolidatedResourcePerDay();
        }

        if (original != null) {
            return original.getConsolidatedResourcePerDay();
        }

        return ResourcesPerDay.amount(0);
    }

    void loadResourcesPerDay() {
        loadResourcesPerDayFrom(transientAllocationBeingModified != null ? transientAllocationBeingModified : original);
    }

    private void loadResourcesPerDayFrom(ResourceAllocation<?> allocation) {
        setResourcesPerDayEditedValue(extractEditedValueFrom(allocation));
    }

    private ResourcesPerDay extractEditedValueFrom(ResourceAllocation<?> allocation) {
        if (allocation == null) {
            return ResourcesPerDay.amount(0);
        }

        return currentCalculatedValue != CalculatedValue.RESOURCES_PER_DAY
                ? allocation.getIntendedResourcesPerDay()
                : allocation.getNonConsolidatedResourcePerDay();
    }

    public abstract ResourceEnum getType();

    private org.zkoss.zul.Row findRow() {
        Component current;
        do {
            current = effortInput.getParent();
        } while (!(current instanceof org.zkoss.zul.Row));

        return (org.zkoss.zul.Row) current;
    }

    void markNoCapacity(final ResourcesPerDayModification allocationAttempt, CapacityResult capacityResult) {
        final org.zkoss.zul.Row row = findRow();

        capacityResult.match(new IMatcher<Void>() {

            @Override
            public Void on(CapacityAvailable result) {
                LOG.warn("shouldn't have happened");
                return null;
            }

            @Override
            public Void on(ThereAreNoValidPeriods result) {
                List<Interval> calendarValidPeriods = result.getSpecifiedCalendar().getAvailability().getValidPeriods();
                AvailabilityTimeLine otherAvailability = result.getSpecifiedAdditionalAvailability();

                if (calendarValidPeriods.isEmpty()) {
                    throw new WrongValueException(row, _("there are no valid periods for this calendar"));
                } else if (otherAvailability.getValidPeriods().isEmpty()) {
                    throw new WrongValueException(row, allocationAttempt.getNoValidPeriodsMessage());
                } else {
                    throw new WrongValueException(
                            row, allocationAttempt.getNoValidPeriodsMessageDueToIntersectionMessage());
                }
            }

            @Override
            public Void on(ValidPeriodsDontHaveCapacity result) {
                EffortDuration sumReached = result.getSumReached();
                List<Interval> validPeriods = result.getValidPeriods();

                String firstLine = _(
                        "In the available periods {0} only {1} hours are available.",
                        validPeriods,
                        sumReached.getHours());

                String secondLine = isGeneric()
                        ? _("Periods available depend on the satisfaction of " +
                        "the criteria of resources and their calendars.")

                        : _("Periods available depend on resources' calendar.");

                throw new WrongValueException(effortInput, firstLine + "\n" + secondLine);
            }

            @Override
            public Void on(ResourcesPerDayIsZero result) {
                throw new WrongValueException(intendedResourcesPerDayInput, _("Resources per day are zero"));
            }
        });
    }

    Listbox getAssignmentFunctionListbox() {
        return assignmentFunctionListbox;
    }

    void resetAssignmentFunction() {
        if (transientAllocationBeingModified == null) {
            associateAllocationToModify(original);
        }
    }

}
