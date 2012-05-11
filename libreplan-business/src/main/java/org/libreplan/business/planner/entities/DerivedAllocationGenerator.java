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
package org.libreplan.business.planner.entities;

import static org.libreplan.business.workingday.EffortDuration.seconds;
import static org.libreplan.business.workingday.EffortDuration.zero;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.libreplan.business.planner.entities.EffortDistributor.ResourceWithAssignedDuration;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Machine;
import org.libreplan.business.resources.entities.MachineWorkerAssignment;
import org.libreplan.business.resources.entities.MachineWorkersConfigurationUnit;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DerivedAllocationGenerator {

    public interface IWorkerFinder {
        Collection<Worker> findWorkersMatching(
                Collection<? extends Criterion> requiredCriterions);
    }

    public static DerivedAllocation generate(ResourceAllocation<?> derivedFrom,
            IWorkerFinder finder,
            MachineWorkersConfigurationUnit configurationUnit,
            List<? extends DayAssignment> dayAssignments) {
        Validate.notNull(derivedFrom);
        Validate.notNull(finder);
        Validate.notNull(configurationUnit);
        Validate.noNullElements(dayAssignments);
        DerivedAllocation result = DerivedAllocation.create(derivedFrom,
                configurationUnit);
        List<Resource> foundResources = findResources(finder, configurationUnit);
        final Machine machine = configurationUnit.getMachine();
        BigDecimal alpha = configurationUnit.getAlpha();
        result.resetAssignmentsTo(createAssignments(result, alpha,
                foundResources, onlyFor(machine, dayAssignments)));
        return result;
    }

    private static List<DayAssignment> onlyFor(Machine machine,
            List<? extends DayAssignment> dayAssignments) {
        List<DayAssignment> result = new ArrayList<DayAssignment>();
        for (DayAssignment each : dayAssignments) {
            if (each.isAssignedTo(machine)) {
                result.add(each);
            }
        }
        return result;
    }

    private static List<Resource> findResources(IWorkerFinder finder,
            MachineWorkersConfigurationUnit configurationUnit) {
        Set<Resource> result = getResourcesFromAssignments(configurationUnit);
        result.addAll(finder.findWorkersMatching(configurationUnit
                .getRequiredCriterions()));
        return new ArrayList<Resource>(result);
    }

    private static Set<Resource> getResourcesFromAssignments(
            MachineWorkersConfigurationUnit configurationUnit) {
        Set<Resource> result = new HashSet<Resource>();
        for (MachineWorkerAssignment each : configurationUnit
                .getWorkerAssignments()) {
            result.add(each.getWorker());
        }
        return result;
    }

    private static List<DerivedDayAssignment> createAssignments(
            DerivedAllocation parent, BigDecimal alpha,
            List<Resource> resourcesFound,
            List<? extends DayAssignment> dayAssignments) {
        List<DerivedDayAssignment> result = new ArrayList<DerivedDayAssignment>();
        EffortDistributor distributor = new EffortDistributor(resourcesFound,
                AssignedEffortForResource.discount(Collections.singletonList(parent)));
        for (DayAssignment each : dayAssignments) {
            int durationInSeconds = alpha.multiply(
                    new BigDecimal(each.getDuration().getSeconds())).intValue();
            LocalDate day = each.getDay();
            List<ResourceWithAssignedDuration> distributeForDay = distributor
                    .distributeForDay(PartialDay.wholeDay(day),
                            seconds(durationInSeconds));
            result.addAll(asDerived(parent, day, distributeForDay));
        }
        return result;
    }

    private static List<DerivedDayAssignment> asDerived(
            DerivedAllocation parent, LocalDate day,
            List<ResourceWithAssignedDuration> distributeForDay) {
        List<DerivedDayAssignment> result = new ArrayList<DerivedDayAssignment>();
        for (ResourceWithAssignedDuration each : distributeForDay) {
            if (each.duration.compareTo(zero()) > 0) {
                result.add(DerivedDayAssignment.create(day, each.duration,
                        each.resource, parent));
            }
        }
        return result;
    }

}
