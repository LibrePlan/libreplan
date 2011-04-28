/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.planner.allocation;

import static org.navalplanner.business.workingday.EffortDuration.hours;
import static org.navalplanner.business.workingday.EffortDuration.zero;
import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine;
import org.navalplanner.business.calendars.entities.AvailabilityTimeLine.Interval;
import org.navalplanner.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator.CapacityAvailable;
import org.navalplanner.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator.CapacityResult;
import org.navalplanner.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator.CapacityResult.IMatcher;
import org.navalplanner.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator.ResourcesPerDayIsZero;
import org.navalplanner.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator.ThereAreNoValidPeriods;
import org.navalplanner.business.calendars.entities.ThereAreHoursOnWorkHoursCalculator.ValidPeriodsDontHaveCapacity;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.navalplanner.business.planner.entities.DerivedAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.Task.ModifiedAllocation;
import org.navalplanner.business.planner.entities.allocationalgorithms.AllocationModification;
import org.navalplanner.business.planner.entities.allocationalgorithms.AllocationModification.IByType;
import org.navalplanner.business.planner.entities.allocationalgorithms.HoursModification;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.navalplanner.business.resources.daos.IResourcesSearcher;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.EffortDuration.IEffortFrom;
import org.navalplanner.business.workingday.ResourcesPerDay;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.planner.allocation.ResourceAllocationController.DerivedAllocationColumn;
import org.zkoss.zk.au.out.AuWrongValue;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.SimpleConstraint;
import org.zkoss.zul.SimpleListModel;

/**
 * The information that must be introduced to create a
 * {@link ResourceAllocation}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class AllocationRow {

    private static final ResourcesPerDay RESOURCES_PER_DAY_DEFAULT_VALUE = ResourcesPerDay
            .amount(1);

    public static final SimpleConstraint CONSTRAINT_FOR_RESOURCES_PER_DAY = new SimpleConstraint(
            SimpleConstraint.NO_EMPTY | SimpleConstraint.NO_NEGATIVE);

    private static final SimpleConstraint CONSTRAINT_FOR_HOURS_INPUT = new SimpleConstraint(
            SimpleConstraint.NO_EMPTY | SimpleConstraint.NO_NEGATIVE);

    private static final Log LOG = LogFactory.getLog(AllocationRow.class);

    public static EffortDuration sumAllConsolidatedEffort(
            Collection<? extends AllocationRow> rows) {
        return EffortDuration.sum(rows, new IEffortFrom<AllocationRow>() {

            @Override
            public EffortDuration from(AllocationRow each) {
                return each.getConsolidatedEffort();
            }
        });
    }

    public static EffortDuration sumAllTotalEffort(
            Collection<? extends AllocationRow> rows) {
        return EffortDuration.sum(rows, new IEffortFrom<AllocationRow>() {

            @Override
            public EffortDuration from(AllocationRow each) {
                return each.getTotalEffort();
            }

        });
    }

    public static EffortDuration sumAllOriginalEffort(
            Collection<? extends AllocationRow> rows) {
        return EffortDuration.sum(rows, new IEffortFrom<AllocationRow>() {
            @Override
            public EffortDuration from(AllocationRow each) {
                return each.getOriginalEffort();
            }
        });
    }

    public static EffortDuration sumAllEffortFromInputs(
            Collection<? extends AllocationRow> rows) {
        return EffortDuration.sum(rows, new IEffortFrom<AllocationRow>() {

            @Override
            public EffortDuration from(AllocationRow each) {
                return each.getEffortFromInput();
            }
        });
    }

    public static void assignHours(List<AllocationRow> rows, int[] hours) {
        int i = 0;
        for (AllocationRow each : rows) {
            each.hoursInput.setValue(hours[i++]);
        }
    }

    public static void assignResourcesPerDay(List<AllocationRow> rows,
            ResourcesPerDay[] resourcesPerDay) {
        int i = 0;
        for (AllocationRow each : rows) {
            each.setResourcesPerDayEditedValue(resourcesPerDay[i++]);
            each.clearRealResourcesPerDay();
        }
    }

    public static void loadDataFromLast(List<? extends AllocationRow> rows,
            List<? extends AllocationModification> modifications) {
        Validate.isTrue(rows.size() == modifications.size());
        Iterator<? extends AllocationModification> iterator = modifications
                .iterator();
        for (AllocationRow each : rows) {
            each.loadDataFromLast();
            each.clearRealResourcesPerDay();

            AllocationModification modification = iterator.next();
            if (!modification.satisfiesModificationRequested()) {
                each.warnObjectiveNotSatisfied(modification);
            }
        }
    }

    public static List<ResourcesPerDayModification> createAndAssociate(
            Task task, Collection<? extends AllocationRow> rows,
            Collection<? extends ResourceAllocation<?>> requestedToRemove) {
        List<ResourcesPerDayModification> result = new ArrayList<ResourcesPerDayModification>();
        for (AllocationRow each : rows) {
            ResourcesPerDayModification modification = each
                    .toResourcesPerDayModification(task, requestedToRemove);
            result.add(modification);
            each.setTemporal(modification.getBeingModified());
        }
        return result;
    }

    public static AllocationRow find(Collection<? extends AllocationRow> rows,
            ResourceAllocation<?> allocationBeingModified) {
        for (AllocationRow each : rows) {
            if (each.temporal == allocationBeingModified) {
                return each;
            }
        }
        return null;
    }

    public static List<HoursModification> createHoursModificationsAndAssociate(
            Task task, List<AllocationRow> currentRows,
            Collection<? extends ResourceAllocation<?>> requestedToRemove) {
        List<HoursModification> result = new ArrayList<HoursModification>();
        for (AllocationRow each : currentRows) {
            HoursModification hoursModification = each.toHoursModification(
                    task, requestedToRemove);
            result.add(hoursModification);
            each.setTemporal(hoursModification.getBeingModified());
        }
        return result;
    }

    public static List<ModifiedAllocation> getModifiedFrom(
            Collection<? extends AllocationRow> rows) {
        List<ModifiedAllocation> result = new ArrayList<ModifiedAllocation>();
        for (AllocationRow each : rows) {
            Validate.notNull(each.temporal);
            if (each.origin != null) {
                result.add(new ModifiedAllocation(each.origin, each.temporal));
            }
        }
        return result;
    }

    public static List<ResourceAllocation<?>> getNewFrom(
            List<AllocationRow> rows) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        for (AllocationRow each : rows) {
            Validate.notNull(each.temporal);
            if (each.origin == null) {
                result.add(each.temporal);
            }
        }
        return result;
    }

    public static List<ResourceAllocation<?>> getTemporalFrom(
            List<AllocationRow> rows) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        for (AllocationRow each : rows) {
            if (each.temporal != null) {
                result.add(each.temporal);
            }
        }
        return result;
    }

    public static List<GenericAllocationRow> getGeneric(
            Collection<? extends AllocationRow> all) {
        List<GenericAllocationRow> result = new ArrayList<GenericAllocationRow>();
        for (AllocationRow each : all) {
            if (each.isGeneric()) {
                result.add((GenericAllocationRow) each);
            }
        }
        return result;
    }

    public static List<AllocationRow> toRows(
            Collection<? extends ResourceAllocation<?>> resourceAllocations,
            IResourcesSearcher searchModel) {
        List<AllocationRow> result = new ArrayList<AllocationRow>();
        result.addAll(GenericAllocationRow.toGenericAllocations(
                resourceAllocations, searchModel));
        result.addAll(SpecificAllocationRow
                .toSpecificAllocations(resourceAllocations));
        return result;
    }

    private final ResourceAllocation<?> origin;

    private CalculatedValue currentCalculatedValue;

    private ResourceAllocation<?> temporal;

    private String name;

    private Intbox hoursInput = new Intbox();

    private final Decimalbox intendedResourcesPerDayInput = new Decimalbox();

    private ResourcesPerDay editedValue;

    private final Label realResourcesPerDay = new Label();

    private Grid derivedAllocationsGrid;

    public AllocationRow(CalculatedValue calculatedValue) {
        this.currentCalculatedValue = calculatedValue;
        this.origin = null;
        setResourcesPerDayEditedValue(RESOURCES_PER_DAY_DEFAULT_VALUE);
        initialize();
    }

    public AllocationRow(ResourceAllocation<?> origin) {
        this.origin = origin;
        this.currentCalculatedValue = origin.getTask().getCalculatedValue();
        setResourcesPerDayEditedValue(resourcesPerDayForInputFrom(origin));
        if (origin != null && !origin.areIntendedResourcesPerDaySatisfied()) {
            onDifferentRealResourcesPerDay(origin
                    .getNonConsolidatedResourcePerDay());
        }
        loadHours();
        initialize();
    }

    private static ResourcesPerDay resourcesPerDayForInputFrom(
            ResourceAllocation<?> resourceAllocation) {
        CalculatedValue calculatedValue = resourceAllocation.getTask()
                .getCalculatedValue();
        return calculatedValue == CalculatedValue.RESOURCES_PER_DAY ? resourceAllocation
                .getNonConsolidatedResourcePerDay() : resourceAllocation
                .getIntendedResourcesPerDay();
    }

    private void initializeResourcesPerDayInput() {
        intendedResourcesPerDayInput
                .setConstraint(CONSTRAINT_FOR_RESOURCES_PER_DAY);
        intendedResourcesPerDayInput.setWidth("80px");
        Util.bind(intendedResourcesPerDayInput, new Util.Getter<BigDecimal>() {

            @Override
            public BigDecimal get() {
                return getResourcesPerDayEditedValue().getAmount();
            }

        }, new Util.Setter<BigDecimal>() {

            @Override
            public void set(BigDecimal value) {
                BigDecimal amount = value == null ? new BigDecimal(0) : value;
                setResourcesPerDayEditedValue(ResourcesPerDay
                        .amount(amount));
            }
        });
    }

    private void initialize() {
        initializeResourcesPerDayInput();
        hoursInput.setWidth("80px");
        hoursInput.setConstraint(constraintForHoursInput());
        loadHours();
    }

    public abstract ResourcesPerDayModification toResourcesPerDayModification(
            Task task,
            Collection<? extends ResourceAllocation<?>> requestedToRemove);

    public abstract HoursModification toHoursModification(Task task,
            Collection<? extends ResourceAllocation<?>> requestedToRemove);

    public boolean isCreating() {
        return origin == null;
    }

    public boolean isModifying() {
        return origin != null;
    }

    public ResourceAllocation<?> getOrigin() {
        return origin;
    }

    private void onDifferentRealResourcesPerDay(
            ResourcesPerDay realResourcesPerDay) {
        this.realResourcesPerDay
                .setTooltiptext(_("It can't allocate the intended resources per day"));
        this.realResourcesPerDay.setValue(_("(achieved: {0})",
                realResourcesPerDay.getAmount().toPlainString()));
    }

    private void clearRealResourcesPerDay() {
        this.realResourcesPerDay.setValue("");
    }

    public boolean hasDerivedAllocations() {
        return ! getDerivedAllocations().isEmpty();
    }

    public List<DerivedAllocation> getDerivedAllocations() {
        if (temporal != null) {
            return new ArrayList<DerivedAllocation>(temporal
                    .getDerivedAllocations());
        } else if (origin != null) {
            return new ArrayList<DerivedAllocation>(origin
                    .getDerivedAllocations());
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

    public ResourcesPerDay getResourcesPerDayEditedValue() {
        return this.editedValue;
    }

    public ResourcesPerDay getResourcesPerDayFromInput() {
        BigDecimal value = intendedResourcesPerDayInput.getValue();
        value = value != null ? value : BigDecimal.ZERO;
        return ResourcesPerDay.amount(value);
    }

    private void setResourcesPerDayEditedValue(ResourcesPerDay resourcesPerDay) {
        this.editedValue = resourcesPerDay;
        intendedResourcesPerDayInput.setValue(getAmount(resourcesPerDay));
    }

    private BigDecimal getAmount(ResourcesPerDay resourcesPerDay) {
        return (resourcesPerDay != null) ? resourcesPerDay.getAmount()
                : new BigDecimal(0);
    }

    public void setTemporal(ResourceAllocation<?> last) {
        Validate.notNull(last);
        this.temporal = last;
    }

    public abstract boolean isGeneric();

    public boolean isEmptyResourcesPerDay() {
        return getResourcesPerDayEditedValue().isZero();
    }

    public abstract List<Resource> getAssociatedResources();

    public Intbox getHoursInput() {
        return hoursInput;
    }

    public Decimalbox getIntendedResourcesPerDayInput() {
        return intendedResourcesPerDayInput;
    }

    public Label getRealResourcesPerDay() {
        return realResourcesPerDay;
    }

    public void addListenerForInputChange(EventListener onChangeListener) {
        getHoursInput().addEventListener(Events.ON_CHANGE, onChangeListener);
        getIntendedResourcesPerDayInput().addEventListener(Events.ON_CHANGE,
                onChangeListener);
    }

    public void loadHours() {
        hoursInput.setValue(getHours());
    }

    protected EffortDuration getEffortFromInput() {
        return hoursInput.getValue() != null ? hours(hoursInput.getValue())
                : zero();
    }

    private Integer getHours() {
        if (temporal != null) {
            return temporal.getNonConsolidatedHours();
        }
        if (origin != null) {
            return origin.getNonConsolidatedHours();
        }
        return 0;
    }

    public void applyDisabledRules(CalculatedValue calculatedValue,
            boolean recommendedAllocation) {
        this.currentCalculatedValue = calculatedValue;
        hoursInput
                .setDisabled(calculatedValue != CalculatedValue.RESOURCES_PER_DAY
                        || recommendedAllocation);
        hoursInput.setConstraint(constraintForHoursInput());
        intendedResourcesPerDayInput
                .setDisabled(calculatedValue == CalculatedValue.RESOURCES_PER_DAY
                        || recommendedAllocation);
        if (intendedResourcesPerDayInput.isDisabled()) {
            clearRealResourcesPerDay();
        }
        intendedResourcesPerDayInput
                .setConstraint(constraintForResourcesPerDayInput());
    }

    private Constraint constraintForHoursInput() {
        return (hoursInput.isDisabled()) ? null : CONSTRAINT_FOR_HOURS_INPUT;
    }

    private Constraint constraintForResourcesPerDayInput() {
        return (intendedResourcesPerDayInput.isDisabled()) ? null
                : CONSTRAINT_FOR_RESOURCES_PER_DAY;
    }

    private void loadDataFromLast() {
        Clients.closeErrorBox(hoursInput);
        Clients.closeErrorBox(intendedResourcesPerDayInput);

        hoursInput.setValue(temporal.getAssignedHours());
        loadResourcesPerDayFrom(temporal);
    }

    private void warnObjectiveNotSatisfied(AllocationModification modification) {
        modification.byType(new IByType<Void>() {

            @Override
            public Void onResourcesPerDay(
                    ResourcesPerDayModification modification) {

                ResourcesPerDay realResourcesPerDay = modification
                        .getBeingModified().getNonConsolidatedResourcePerDay();
                onDifferentRealResourcesPerDay(realResourcesPerDay);

                return null;
            }

            @Override
            public Void onHours(HoursModification modification) {
                int goal = modification.getHours();
                Clients.response(new AuWrongValue(hoursInput, _(
                        "{0} hours cannot be fulfilled", goal + "")));

                return null;
            }
        });
    }

    public void addListenerForHoursInputChange(EventListener listener) {
        hoursInput.addEventListener(Events.ON_CHANGE, listener);
    }

    public void setHoursToInput(Integer hours) {
        hoursInput.setValue(hours);
    }

    public void addListenerForResourcesPerDayInputChange(
            EventListener resourcesPerDayRowInputChange) {
        intendedResourcesPerDayInput.addEventListener(Events.ON_CHANGE,
                resourcesPerDayRowInputChange);
    }

    public void reloadDerivedAllocationsGrid() {
        if (hasDerivedAllocations() && !(currentDetail instanceof Detail)) {
            replaceOld(currentDetail, createDetail());
        }
        reloadDerivedAllocationsData();
    }

    private void reloadDerivedAllocationsData() {
        if (derivedAllocationsGrid != null) {
            derivedAllocationsGrid.setModel(new SimpleListModel(
                    getDerivedAllocations()));
        }
    }

    private void replaceOld(Component oldDetail, Component newDetail) {
        Component parent = oldDetail.getParent();
        parent.insertBefore(newDetail, oldDetail);
        parent.removeChild(oldDetail);
    }

    private Component currentDetail;

    public Component createDetail() {
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
        derivedAllocationsGrid.setRowRenderer(DerivedAllocationColumn
                .createRenderer());
    }

    public boolean isSatisfied() {
        if (temporal != null) {
            return temporal.isSatisfied();
        } else if (origin != null) {
            return origin.isSatisfied();
        } else {
            return false;
        }
    }

    public EffortDuration getOriginalEffort() {
        if (temporal != null) {
            return hours(temporal.getOriginalTotalAssigment());
        }
        if (origin != null) {
            return hours(origin.getOriginalTotalAssigment());
        }
        return zero();
    }

    public EffortDuration getTotalEffort() {
        if (temporal != null) {
            return temporal.getAssignedEffort();
        }
        if (origin != null) {
            return origin.getAssignedEffort();
        }
        return zero();
    }

    public EffortDuration getConsolidatedEffort() {
        if (temporal != null) {
            return temporal.getConsolidatedEffort();
        }
        if (origin != null) {
            return origin.getConsolidatedEffort();
        }
        return zero();
    }

    public int getNonConsolidatedHours() {
        if (temporal != null) {
            return temporal.getNonConsolidatedHours();
        }
        if (origin != null) {
            return origin.getNonConsolidatedHours();
        }
        return 0;
    }

    public ResourcesPerDay getTotalResourcesPerDay() {
        if (temporal != null) {
            return temporal.calculateResourcesPerDayFromAssignments();
        }
        if (origin != null) {
            return origin.calculateResourcesPerDayFromAssignments();
        }
        return ResourcesPerDay.amount(0);
    }

    public ResourcesPerDay getConsolidatedResourcesPerDay() {
        if (temporal != null) {
            return temporal.getConsolidatedResourcePerDay();
        }
        if (origin != null) {
            return origin.getConsolidatedResourcePerDay();
        }
        return ResourcesPerDay.amount(0);
    }

    public void loadResourcesPerDay() {
        loadResourcesPerDayFrom(temporal != null ? temporal : origin);
    }

    private void loadResourcesPerDayFrom(ResourceAllocation<?> allocation) {
        if (allocation == null) {
            setResourcesPerDayEditedValue(ResourcesPerDay.amount(0));
        }
        boolean useIntention = currentCalculatedValue != CalculatedValue.RESOURCES_PER_DAY;
        setResourcesPerDayEditedValue(useIntention ? allocation
                .getIntendedResourcesPerDay() : allocation
                .getNonConsolidatedResourcePerDay());
    }

    public abstract ResourceEnum getType();

    private org.zkoss.zul.Row findRow() {
        Component current = null;
        do {
            current = hoursInput.getParent();
        } while (!(current instanceof org.zkoss.zul.Row));
        return (org.zkoss.zul.Row) current;
    }

    public void markNoCapacity(
            final ResourcesPerDayModification allocationAttempt,
            CapacityResult capacityResult) {
        final org.zkoss.zul.Row row = findRow();
        capacityResult.match(new IMatcher<Void>() {

            @Override
            public Void on(CapacityAvailable result) {
                LOG.warn("shouldn't have happened");
                return null;
            }

            @Override
            public Void on(ThereAreNoValidPeriods result) {
                List<Interval> calendarValidPeriods = result
                        .getSpecifiedCalendar()
                        .getAvailability().getValidPeriods();
                AvailabilityTimeLine otherAvailability = result
                        .getSpecifiedAdditionalAvailability();
                if (calendarValidPeriods.isEmpty()) {
                    throw new WrongValueException(row,
                            _("there are no valid periods for this calendar"));
                } else if (otherAvailability.getValidPeriods().isEmpty()) {
                    throw new WrongValueException(row, allocationAttempt
                            .getNoValidPeriodsMessage());
                } else {
                    throw new WrongValueException(row, allocationAttempt
                            .getNoValidPeriodsMessageDueToIntersectionMessage());
                }
            }

            @Override
            public Void on(ValidPeriodsDontHaveCapacity result) {
                EffortDuration sumReached = result.getSumReached();
                List<Interval> validPeriods = result.getValidPeriods();
                String firstLine = _(
                        "In the available periods {0} only {1} hours are available.",
                        validPeriods, sumReached.getHours());
                String secondLine = isGeneric() ? _("The periods available depend on the satisfaction of the criterions by the resources and their calendars.")
                        : _("The periods available depend on the resource's calendar.");
                throw new WrongValueException(hoursInput, firstLine + "\n"
                        + secondLine);
            }

            @Override
            public Void on(ResourcesPerDayIsZero result) {
                throw new WrongValueException(intendedResourcesPerDayInput,
                        _("Resources per day are zero"));
            }
        });
    }

}
