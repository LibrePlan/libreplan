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

package org.navalplanner.web.planner.allocation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.planner.entities.CalculatedValue;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.ResourcesPerDay;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.Task.ModifiedAllocation;
import org.navalplanner.business.planner.entities.allocationalgorithms.ResourcesPerDayModification;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.SimpleConstraint;

/**
 * The information that must be introduced to create a
 * {@link ResourceAllocation}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public abstract class AllocationRow {

    public static List<ModifiedAllocation> getModifiedFrom(
            Collection<? extends AllocationRow> rows) {
        List<ModifiedAllocation> result = new ArrayList<ModifiedAllocation>();
        for (AllocationRow each : rows) {
            Validate.notNull(each.last);
            if (each.origin != null) {
                result.add(new ModifiedAllocation(each.origin, each.last));
            }
        }
        return result;
    }

    public static List<ResourceAllocation<?>> getNewFrom(
            List<AllocationRow> rows) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        for (AllocationRow each : rows) {
            Validate.notNull(each.last);
            if (each.origin == null) {
                result.add(each.last);
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
            Collection<? extends ResourceAllocation<?>> resourceAllocations) {
        List<AllocationRow> result = new ArrayList<AllocationRow>();
        result.addAll(GenericAllocationRow
                .toGenericAllocations(resourceAllocations));
        result.addAll(SpecificAllocationRow
                .toSpecificAllocations(resourceAllocations));
        return result;
    }

    private ResourceAllocation<?> origin;

    private ResourceAllocation<?> last;

    private String name;

    private ResourcesPerDay resourcesPerDay;

    private Intbox hoursInput = new Intbox();

    private final Decimalbox resourcesPerDayInput = new Decimalbox();

    private void initializeResourcesPerDayInput() {
        resourcesPerDayInput.setConstraint(new SimpleConstraint(
                SimpleConstraint.NO_NEGATIVE));
        Util.bind(resourcesPerDayInput, new Util.Getter<BigDecimal>() {

            @Override
            public BigDecimal get() {
                return getResourcesPerDay().getAmount();
            }

        }, new Util.Setter<BigDecimal>() {

            @Override
            public void set(BigDecimal value) {
                BigDecimal amount = value == null ? new BigDecimal(0) : value;
                resourcesPerDay = ResourcesPerDay.amount(amount);
            }
        });
    }

    public AllocationRow() {
        resourcesPerDay = ResourcesPerDay.amount(0);
        initializeResourcesPerDayInput();
        hoursInput.setDisabled(true);
        hoursInput.setConstraint(new SimpleConstraint(
                SimpleConstraint.NO_NEGATIVE));
    }

    public abstract ResourcesPerDayModification toResourcesPerDayModification(
            Task task);

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourcesPerDay getResourcesPerDay() {
        return this.resourcesPerDay;
    }

    public void setResourcesPerDay(ResourcesPerDay resourcesPerDay) {
        this.resourcesPerDay = resourcesPerDay;
        resourcesPerDayInput.setValue(this.resourcesPerDay.getAmount());
    }

    public void setLast(ResourceAllocation<?> last) {
        Validate.notNull(last);
        this.last = last;
    }

    public abstract boolean isGeneric();

    public boolean isEmptyResourcesPerDay() {
        return getResourcesPerDay().isZero();
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

    private Integer getHours() {
        if (last != null) {
            return last.getAssignedHours();
        }
        if (origin != null) {
            return origin.getAssignedHours();
        }
        return null;
    }

    public void applyDisabledRules(CalculatedValue calculatedValue) {
        hoursInput
                .setDisabled(calculatedValue != CalculatedValue.RESOURCES_PER_DAY);
        resourcesPerDayInput
                .setDisabled(calculatedValue == CalculatedValue.RESOURCES_PER_DAY);
    }
}
