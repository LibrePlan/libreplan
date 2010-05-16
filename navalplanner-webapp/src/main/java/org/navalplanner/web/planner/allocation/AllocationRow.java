/*
 * This file is part of NavalPlan
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

package org.navalplanner.web.planner.allocation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.navalplanner.business.planner.entities.DerivedAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.Task.ModifiedAllocation;
import org.navalplanner.business.planner.entities.allocationalgorithms.HoursModification;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.planner.allocation.ResourceAllocationController.DerivedAllocationColumn;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
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

    public static final SimpleConstraint CONSTRAINT_FOR_RESOURCES_PER_DAY = new SimpleConstraint(
            SimpleConstraint.NO_EMPTY | SimpleConstraint.NO_NEGATIVE);

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
            each.setNonConsolidatedResourcesPerDay(resourcesPerDay[i++]);
        }
    }

    public static void loadDataFromLast(Collection<? extends AllocationRow> rows) {
        for (AllocationRow each : rows) {
            each.loadDataFromLast();
        }
    }

    public static List<ResourcesPerDayModification> createAndAssociate(
            Task task, Collection<? extends AllocationRow> rows) {
        List<ResourcesPerDayModification> result = new ArrayList<ResourcesPerDayModification>();
        for (AllocationRow each : rows) {
            ResourcesPerDayModification modification = each
                    .toResourcesPerDayModification(task);
            result.add(modification);
            each.setTemporal(modification.getBeingModified());
        }
        return result;
    }

    public static List<HoursModification> createHoursModificationsAndAssociate(
            Task task, List<AllocationRow> currentRows) {
        List<HoursModification> result = new ArrayList<HoursModification>();
        for (AllocationRow each : currentRows) {
            HoursModification hoursModification = each
                    .toHoursModification(task);
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
            Collection<? extends ResourceAllocation<?>> resourceAllocations, IResourceDAO resourceDAO) {
        List<AllocationRow> result = new ArrayList<AllocationRow>();
        result.addAll(GenericAllocationRow
                .toGenericAllocations(resourceAllocations, resourceDAO));
        result.addAll(SpecificAllocationRow
                .toSpecificAllocations(resourceAllocations));
        return result;
    }

    private ResourceAllocation<?> origin;

    private ResourceAllocation<?> temporal;

    private String name;

    private int originalHours;

    private int totalHours;

    private int consolidatedHours;

    private int nonConsolidatedHours;

    private ResourcesPerDay totalResourcesPerDay;

    private ResourcesPerDay nonConsolidatedResourcesPerDay;

    private ResourcesPerDay consolidatedResourcesPerDay;

    private Intbox hoursInput = new Intbox();

    private final Decimalbox resourcesPerDayInput = new Decimalbox();

    private Grid derivedAllocationsGrid;

    private void initializeResourcesPerDayInput() {
        resourcesPerDayInput.setConstraint(new SimpleConstraint(
                SimpleConstraint.NO_NEGATIVE));
        resourcesPerDayInput.setWidth("80px");
        Util.bind(resourcesPerDayInput, new Util.Getter<BigDecimal>() {

            @Override
            public BigDecimal get() {
                return getNonConsolidatedResourcesPerDay().getAmount();
            }

        }, new Util.Setter<BigDecimal>() {

            @Override
            public void set(BigDecimal value) {
                BigDecimal amount = value == null ? new BigDecimal(0) : value;
                setNonConsolidatedResourcesPerDay(ResourcesPerDay
                        .amount(amount));
            }
        });
    }

    public AllocationRow() {
        originalHours = 0;
        totalHours = 0;
        consolidatedHours = 0;
        nonConsolidatedHours = 0;
        consolidatedResourcesPerDay = ResourcesPerDay.amount(0);
        setNonConsolidatedResourcesPerDay(ResourcesPerDay.amount(0));
        totalResourcesPerDay = ResourcesPerDay.amount(0);
        initializeResourcesPerDayInput();
        hoursInput.setValue(0);
        hoursInput.setWidth("80px");
        hoursInput.setDisabled(true);
        hoursInput.setConstraint(new SimpleConstraint(
                SimpleConstraint.NO_NEGATIVE));
    }

    public abstract ResourcesPerDayModification toResourcesPerDayModification(
            Task task);

    public abstract HoursModification toHoursModification(Task task);

    public boolean isCreating() {
        return origin == null;
    }

    public boolean isModifying() {
        return origin != null;
    }

    public ResourceAllocation<?> getOrigin() {
        return origin;
    }

    protected void setOrigin(ResourceAllocation<?> allocation) {
        this.origin = allocation;
        loadHours();
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

    public ResourcesPerDay getNonConsolidatedResourcesPerDay() {
        return this.nonConsolidatedResourcesPerDay;
    }

    public ResourcesPerDay getResourcesPerDayFromInput() {
        BigDecimal value = resourcesPerDayInput.getValue();
        value = value != null ? value : BigDecimal.ZERO;
        return ResourcesPerDay.amount(value);
    }

    public void setNonConsolidatedResourcesPerDay(
            ResourcesPerDay resourcesPerDay) {
        this.nonConsolidatedResourcesPerDay = resourcesPerDay;
        resourcesPerDayInput.setValue(getAmount(resourcesPerDay));
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
        return getNonConsolidatedResourcesPerDay().isZero();
    }

    public abstract List<Resource> getAssociatedResources();

    public Intbox getHoursInput() {
        return hoursInput;
    }

    public Decimalbox getResourcesPerDayInput() {
        return resourcesPerDayInput;
    }

    public void addListenerForInputChange(EventListener onChangeListener) {
        getHoursInput().addEventListener(Events.ON_CHANGE, onChangeListener);
        getResourcesPerDayInput().addEventListener(Events.ON_CHANGE,
                onChangeListener);
    }

    public void loadHours() {
        hoursInput.setValue(getHours());
    }

    protected int getHoursFromInput() {
        return hoursInput.getValue() != null ? hoursInput.getValue() : 0;
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
        hoursInput
                .setDisabled(calculatedValue != CalculatedValue.RESOURCES_PER_DAY
                        || recommendedAllocation);
        hoursInput.setConstraint(constraintForHoursInput());
        resourcesPerDayInput
                .setDisabled(calculatedValue == CalculatedValue.RESOURCES_PER_DAY
                        || recommendedAllocation);
        resourcesPerDayInput.setConstraint(constraintForResourcesPerDayInput());
    }

    private Constraint constraintForHoursInput() {
        if (hoursInput.isDisabled()) {
            return null;
        }
        return new SimpleConstraint(SimpleConstraint.NO_EMPTY
                | SimpleConstraint.NO_NEGATIVE);
    }

    private Constraint constraintForResourcesPerDayInput() {
        if (resourcesPerDayInput.isDisabled()) {
            return null;
        }
        return CONSTRAINT_FOR_RESOURCES_PER_DAY;
    }

    public void loadDataFromLast() {
        hoursInput.setValue(temporal.getAssignedHours());
        resourcesPerDayInput
                .setValue(temporal.getResourcesPerDay().getAmount());
    }

    public void addListenerForHoursInputChange(EventListener listener) {
        hoursInput.addEventListener(Events.ON_CHANGE, listener);
    }

    public void setHoursToInput(Integer hours) {
        hoursInput.setValue(hours);
    }

    public void addListenerForResourcesPerDayInputChange(
            EventListener resourcesPerDayRowInputChange) {
        resourcesPerDayInput.addEventListener(Events.ON_CHANGE,
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

    public void setOriginalHours(int originalHours) {
        this.originalHours = originalHours;
    }

    public int getOriginalHours() {
        return originalHours;
    }

    public void setTotalHours(int totalHours) {
        this.totalHours = totalHours;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public void setConsolidatedHours(int consolidatedHours) {
        this.consolidatedHours = consolidatedHours;
    }

    public int getConsolidatedHours() {
        return consolidatedHours;
    }

    public void setNonConsolidatedHours(int nonConsolidatedHours) {
        this.nonConsolidatedHours = nonConsolidatedHours;
    }

    public int getNonConsolidatedHours() {
        return nonConsolidatedHours;
    }

    public void setTotalResourcesPerDay(ResourcesPerDay totalResourcesPerDay) {
        this.totalResourcesPerDay = totalResourcesPerDay;
    }

    public ResourcesPerDay getTotalResourcesPerDay() {
        return totalResourcesPerDay;
    }

    public void setConsolidatedResourcesPerDay(
            ResourcesPerDay consolidatedResourcesPerDay) {
        this.consolidatedResourcesPerDay = consolidatedResourcesPerDay;
    }

    public ResourcesPerDay getConsolidatedResourcesPerDay() {
        return consolidatedResourcesPerDay;
    }

}
