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

package org.libreplan.business.resources.daos;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.libreplan.business.planner.daos.IDayAssignmentDAO;
import org.libreplan.business.planner.entities.DayAssignment;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class designed to be used as a singleton spring bean implementing the
 * {@link IResourceLoadRatiosCalculator} interface.
 *
 * @author Javier Moran Rua <jmoran@igalia.com>
 */

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ResourceLoadRatiosCalculator implements IResourceLoadRatiosCalculator {

    @Autowired
    private IDayAssignmentDAO dayAssignmentDAO;

    @Autowired
    private IResourceDAO resourceDAO;

    private static class LoadRatiosDataType implements IResourceLoadRatiosCalculator.ILoadRatiosDataType {
        private EffortDuration load;

        private EffortDuration overload;

        private EffortDuration capacity;

        public LoadRatiosDataType(EffortDuration load, EffortDuration overload, EffortDuration capacity) {
            this.load = load;
            this.overload = overload;
            this.capacity = capacity;
        }

        @Override
        public EffortDuration getLoad() {
            return this.load;
        }

        @Override
        public EffortDuration getOverload() {
            return this.overload;
        }

        @Override
        public EffortDuration getCapacity() {
            return this.capacity;
        }

        @Override
        public BigDecimal getOvertimeRatio() {
            BigDecimal result;
            if (this.load.isZero() && this.overload.isZero()) {
                result = BigDecimal.ZERO;
            } else {
                result = this.overload.dividedByAndResultAsBigDecimal(this.load.plus(this.capacity));
            }

            return result.setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        @Override
        public BigDecimal getAvailiabilityRatio() {
            BigDecimal result;

            if (this.capacity.isZero()) {
                result = BigDecimal.ZERO;
            } else {

                result = BigDecimal.ONE.subtract(this.load.dividedByAndResultAsBigDecimal(this.capacity));

                if (result.compareTo(BigDecimal.ZERO) < 0) {
                    result = BigDecimal.ZERO;
                }
            }
            return result.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ILoadRatiosDataType calculateLoadRatios(final Resource resource,
                                                   final LocalDate startDate,
                                                   final LocalDate endDate,
                                                   final Scenario scenario) {
        resourceDAO.reattach(resource);

        EffortDuration
                totalLoad = EffortDuration.zero(),
                totalOverload = EffortDuration.zero(),
                totalCapacity;

        Set<Map.Entry<LocalDate, EffortDuration>> efforts =
                getAllEffortPerDateFor(scenario, startDate, endDate, resource).entrySet();

        for (Map.Entry<LocalDate, EffortDuration> each : efforts) {
            totalLoad = totalLoad.plus(each.getValue());
            totalOverload = addOverload(totalOverload, resource, each.getValue(), each.getKey());
        }

        totalCapacity = calculateTotalCapacity(resource, startDate, endDate);

        return new LoadRatiosDataType(totalLoad, totalOverload, totalCapacity);
    }

    private Map<LocalDate, EffortDuration> getAllEffortPerDateFor(
            Scenario scenario, LocalDate startDate, LocalDate endDate, Resource resource) {

        HashMap<LocalDate, EffortDuration> result;
        result = new HashMap<>();

        List<DayAssignment> l = dayAssignmentDAO.getAllFor(scenario, startDate, endDate, resource);

        EffortDuration newValue;

        for (DayAssignment each : l) {
            if (result.containsKey(each.getDay())) {
                newValue = result.get(each.getDay()).plus(each.getDuration());
            } else {
                newValue = each.getDuration();
            }
            result.put(each.getDay(), newValue);
        }
        return result;
    }

    private EffortDuration calculateTotalCapacity(Resource resource, LocalDate startDate, LocalDate endDate) {
        return resource.getCalendar().getWorkableDuration(startDate, endDate);
    }

    private EffortDuration addOverload(
            EffortDuration currentOverload, Resource resource, EffortDuration loadAtDate, LocalDate date) {

        EffortDuration result;
        EffortDuration capacityAtDay = getCapacityAtDate(resource, date);

        if (capacityAtDay.compareTo(loadAtDate) < 0) {
            result = currentOverload.plus(loadAtDate.minus(capacityAtDay));
        } else {
            result = currentOverload;
        }

        return result;
    }

    private EffortDuration getCapacityAtDate(Resource resource, LocalDate date) {
        return resource.getCalendar().getCapacityOn(PartialDay.wholeDay(date));
    }
}