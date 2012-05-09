/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.business.planner.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.chart.ContiguousDaysLine;
import org.libreplan.business.planner.chart.ContiguousDaysLine.OnDay;
import org.libreplan.business.planner.chart.ResourceLoadChartData;
import org.libreplan.business.planner.entities.DayAssignment.FilterType;
import org.libreplan.business.resources.entities.IAssignmentsOnResourceCalculator;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.workingday.EffortDuration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class OrderResourceLoadCalculator implements IOrderResourceLoadCalculator {

    private Order order;

    private IAssignmentsOnResourceCalculator assignmentsOnResourceCalculator;

    private ContiguousDaysLine<List<DayAssignment>> orderAssignments;

    private ContiguousDaysLine<List<DayAssignment>> filteredAssignments;

    private ContiguousDaysLine<EffortDuration> maxCapacityOnResources;

    private ContiguousDaysLine<EffortDuration> orderLoad;

    private ContiguousDaysLine<EffortDuration> allLoad;

    private ContiguousDaysLine<EffortDuration> orderOverload;

    private ContiguousDaysLine<EffortDuration> allOverload;

    @Override
    public void setOrder(Order order,
            IAssignmentsOnResourceCalculator assignmentsOnResourceCalculator) {
        this.order = order;
        this.assignmentsOnResourceCalculator = assignmentsOnResourceCalculator;
        initializeValues();
    }

    private void initializeValues() {
        orderAssignments = null;
        filteredAssignments = null;
        maxCapacityOnResources = null;
        orderLoad = null;
        allLoad = null;
        orderOverload = null;
        allOverload = null;
    }

    @Override
    public ContiguousDaysLine<EffortDuration> getMaxCapacityOnResources() {
        if (maxCapacityOnResources == null) {
            maxCapacityOnResources = getOrderAssignments()
                     .transform(ResourceLoadChartData
                             .extractAvailabilityOnAssignedResources());
        }
        return maxCapacityOnResources;
    }

    private ContiguousDaysLine<List<DayAssignment>> getOrderAssignments() {
        if (orderAssignments == null) {
            List<DayAssignment> orderDayAssignments = order
                    .getDayAssignments(FilterType.WITHOUT_DERIVED);
            orderAssignments = ContiguousDaysLine.byDay(orderDayAssignments);
        }
        return orderAssignments;
    }

    @Override
    public ContiguousDaysLine<EffortDuration> getOrderLoad() {
        if (orderLoad == null) {
            orderLoad = getOrderAssignments()
                     .transform(ResourceLoadChartData.extractLoad());
        }
        return orderLoad;
    }

    @Override
    public ContiguousDaysLine<EffortDuration> getAllLoad() {
        if (allLoad == null) {
            allLoad = getFilteredAssignments()
                     .transform(ResourceLoadChartData.extractLoad());
        }
        return allLoad;
    }

    private ContiguousDaysLine<List<DayAssignment>> getFilteredAssignments() {
        if (filteredAssignments == null) {
            ContiguousDaysLine<List<DayAssignment>> allAssignments = allAssignments(getOrderAssignments());
            filteredAssignments = filterAllAssignmentsByOrderResources(
                    allAssignments, getOrderAssignments());
        }
        return filteredAssignments;
    }

    private ContiguousDaysLine<List<DayAssignment>> filterAllAssignmentsByOrderResources(
            ContiguousDaysLine<List<DayAssignment>> allAssignments,
            ContiguousDaysLine<List<DayAssignment>> orderAssignments) {
        List<DayAssignment> filteredAssignments = new ArrayList<DayAssignment>();

        Iterator<OnDay<List<DayAssignment>>> iterator = orderAssignments
                .iterator();
        while (iterator.hasNext()) {
            OnDay<List<DayAssignment>> onDay = iterator.next();
            Set<Resource> resources = getResources(onDay.getValue());
            filteredAssignments.addAll(filterAssignmentsByResource(
                    allAssignments.get(onDay.getDay()), resources));
        }
        return ContiguousDaysLine.byDay(filteredAssignments);
    }

    private List<DayAssignment> filterAssignmentsByResource(
            List<DayAssignment> list, Set<Resource> resources) {
        List<DayAssignment> result = new ArrayList<DayAssignment>();
        for (DayAssignment each : list) {
            if (resources.contains(each.getResource())) {
                result.add(each);
            }
        }
        return result;
    }

    private Set<Resource> getResources(List<DayAssignment> dayAssignments) {
        Set<Resource> resources = new HashSet<Resource>();
        for (DayAssignment each : dayAssignments) {
            resources.add(each.getResource());
        }
        return resources;
    }

    private ContiguousDaysLine<List<DayAssignment>> allAssignments(
            ContiguousDaysLine<List<DayAssignment>> orderAssignments) {
        if (orderAssignments.isNotValid()) {
            return ContiguousDaysLine.<List<DayAssignment>> invalid();
        }
        return allAssignmentsOnResourcesAt(orderAssignments.getStart(),
                orderAssignments.getEndExclusive());
    }

    private ContiguousDaysLine<List<DayAssignment>> allAssignmentsOnResourcesAt(
            LocalDate startInclusive, LocalDate endExclusive) {
        AvailabilityTimeLine.Interval interval = AvailabilityTimeLine.Interval
                .create(startInclusive, endExclusive);
        List<DayAssignment> resourcesDayAssignments = new ArrayList<DayAssignment>();
        for (Resource resource : order.getResources(FilterType.WITHOUT_DERIVED)) {
            resourcesDayAssignments.addAll(insideInterval(interval,
                    assignmentsOnResourceCalculator.getAssignments(resource)));
        }
        return ContiguousDaysLine.byDay(resourcesDayAssignments);
    }

    private List<DayAssignment> insideInterval(
            AvailabilityTimeLine.Interval interval,
            List<DayAssignment> assignments) {
        List<DayAssignment> result = new ArrayList<DayAssignment>();
        for (DayAssignment each : assignments) {
            if (interval.includes(each.getDay())) {
                result.add(each);
            }
        }
        return result;
    }

    @Override
    public ContiguousDaysLine<EffortDuration> getOrderOverload() {
        if (orderOverload == null) {
            orderOverload = getOrderAssignments()
                     .transform(ResourceLoadChartData.extractOverload());
        }
        return orderOverload;
    }

    @Override
    public ContiguousDaysLine<EffortDuration> getAllOverload() {
        if (allOverload == null) {
            allOverload = getFilteredAssignments()
                     .transform(ResourceLoadChartData.extractOverload());
        }
        return allOverload;
    }

}
