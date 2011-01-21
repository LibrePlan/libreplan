/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.planner.entities;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.workreports.daos.IWorkReportLineDAO;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Cost calulator in terms of hours.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class HoursCostCalculator implements ICostCalculator {

    @Autowired
    private IWorkReportLineDAO workReportLineDAO;

    @Override
    public SortedMap<LocalDate, BigDecimal> getAdvanceCost(Task task) {
        return getAdvanceCost(task, null, null);
    }

    @Override
    public SortedMap<LocalDate, BigDecimal> getAdvanceCost(Task task,
            LocalDate filterStartDate, LocalDate filterEndDate) {
        DirectAdvanceAssignment advanceAssignment = task.getOrderElement()
                .getReportGlobalAdvanceAssignment();

        if (advanceAssignment == null) {
            return new TreeMap<LocalDate, BigDecimal>();
        }

        return calculateHoursPerDay(task.getHoursSpecifiedAtOrder(),
                advanceAssignment.getAdvanceMeasurements(),
                filterStartDate, filterEndDate);
    }

    private SortedMap<LocalDate, BigDecimal> calculateHoursPerDay(
            Integer totalHours,
            SortedSet<AdvanceMeasurement> advanceMeasurements,
            LocalDate filterStartDate, LocalDate filterEndDate) {
        SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();

        for (AdvanceMeasurement advanceMeasurement : advanceMeasurements) {
            LocalDate day = advanceMeasurement.getDate();
            if(((filterStartDate == null) || day.compareTo(filterStartDate) >= 0) &&
                    ((filterEndDate == null) || day.compareTo(filterEndDate) <= 0)) {
                BigDecimal cost = advanceMeasurement.getValue().setScale(2)
                        .multiply(new BigDecimal(totalHours))
                        .divide(new BigDecimal(100),
                                new MathContext(2, RoundingMode.HALF_UP));
                result.put(day, cost);
            }
        }

        return result;
    }

    @Override
    public SortedMap<LocalDate, BigDecimal> getEstimatedCost(Task task) {
        return getEstimatedCost(task, null, null);
    }

    @Override
    public SortedMap<LocalDate, BigDecimal> getEstimatedCost(Task task,
            LocalDate filterStartDate, LocalDate filterEndDate) {
        if (task.isSubcontracted()) {
            return getAdvanceCost(task);
        }

        SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();

        List<DayAssignment> dayAssignments = task.getDayAssignments();

        if (dayAssignments.isEmpty()) {
            return result;
        }

        for (DayAssignment dayAssignment : dayAssignments) {
            LocalDate day = dayAssignment.getDay();
            if(((filterStartDate == null) || day.compareTo(filterStartDate) >= 0) &&
                    ((filterEndDate == null) || day.compareTo(filterEndDate) <= 0)) {
                BigDecimal cost = new BigDecimal(dayAssignment.getHours());

                if (!result.containsKey(day)) {
                    result.put(day, BigDecimal.ZERO);
                }
                result.put(day, result.get(day).add(cost));
            }
        }

        return result;
    }

    @Override
    public SortedMap<LocalDate, BigDecimal> getWorkReportCost(Task task) {
        if (task.isSubcontracted()) {
            return getAdvanceCost(task);
        }

        SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();

        List<WorkReportLine> workReportLines = workReportLineDAO
                .findByOrderElementAndChildren(task.getOrderElement());

        if (workReportLines.isEmpty()) {
            return result;
        }

        for (WorkReportLine workReportLine : workReportLines) {
            LocalDate day = new LocalDate(workReportLine.getDate());
            BigDecimal cost = new BigDecimal(workReportLine.getNumHours());

            if (!result.containsKey(day)) {
                result.put(day, BigDecimal.ZERO);
            }
            result.put(day, result.get(day).add(cost));
        }

        return result;
    }

}
