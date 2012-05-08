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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine.Interval;
import org.libreplan.business.hibernate.notification.PredefinedDatabaseSnapshots;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Diego Pino García <dpino@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class CompanyEarnedValueCalculator extends EarnedValueCalculator implements ICompanyEarnedValueCalculator {

    @Autowired
    private PredefinedDatabaseSnapshots databaseSnapshots;

    @Override
    @Transactional(readOnly = true)
    public SortedMap<LocalDate, BigDecimal> calculateBudgetedCostWorkScheduled(AvailabilityTimeLine.Interval interval) {
        Map<TaskElement, SortedMap<LocalDate, BigDecimal>> estimatedCostPerTask = databaseSnapshots
                .snapshotEstimatedCostPerTask();
        Collection<TaskElement> list = filterTasksByDate(
                estimatedCostPerTask.keySet(), interval);
        SortedMap<LocalDate, BigDecimal> estimatedCost = new TreeMap<LocalDate, BigDecimal>();

        for (TaskElement each : list) {
            addCost(estimatedCost, estimatedCostPerTask.get(each));
        }
        return accumulateResult(estimatedCost);
    }

    private List<TaskElement> filterTasksByDate(
            Collection<TaskElement> tasks,
            AvailabilityTimeLine.Interval interval) {
        List<TaskElement> result = new ArrayList<TaskElement>();
        for(TaskElement task : tasks) {
            if (interval.includes(task.getStartAsLocalDate())
                    || interval.includes(task.getEndAsLocalDate())) {
                result.add(task);
            }
        }
        return result;
    }

    private List<WorkReportLine> filterWorkReportLinesByDate(
            Collection<WorkReportLine> lines,
            AvailabilityTimeLine.Interval interval) {
        List<WorkReportLine> result = new ArrayList<WorkReportLine>();
        for(WorkReportLine line: lines) {
            if (interval.includes(line.getLocalDate())) {
                result.add(line);
            }
        }
        return result;
    }

    private void addCost(SortedMap<LocalDate, BigDecimal> currentCost,
            SortedMap<LocalDate, BigDecimal> additionalCost) {
        for (LocalDate day : additionalCost.keySet()) {
            if (!currentCost.containsKey(day)) {
                currentCost.put(day, BigDecimal.ZERO);
            }
            currentCost.put(day, currentCost.get(day).add(
                    additionalCost.get(day)));
        }
    }

    private SortedMap<LocalDate, BigDecimal> accumulateResult(
            SortedMap<LocalDate, BigDecimal> map) {
        SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();
        if (map.isEmpty()) {
            return result;
        }

        BigDecimal accumulatedResult = BigDecimal.ZERO;
        for (LocalDate day : map.keySet()) {
            BigDecimal value = map.get(day);
            accumulatedResult = accumulatedResult.add(value);
            result.put(day, accumulatedResult);
        }

        return result;
    }

    @Override
    public SortedMap<LocalDate, BigDecimal> calculateActualCostWorkPerformed(
            Interval interval) {
        SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();
        Collection<WorkReportLine> workReportLines = filterWorkReportLinesByDate(
                databaseSnapshots.snapshotWorkReportLines(),
                interval);

        if (workReportLines.isEmpty()) {
            return result;
        }

        for (WorkReportLine workReportLine : workReportLines) {
            LocalDate day = new LocalDate(workReportLine.getDate());
            BigDecimal cost = workReportLine.getEffort()
                    .toHoursAsDecimalWithScale(2);

            if (!result.containsKey(day)) {
                result.put(day, BigDecimal.ZERO);
            }
            result.put(day, result.get(day).add(cost));
        }
        return accumulateResult(result);
    }

    @Override
    public SortedMap<LocalDate, BigDecimal> calculateBudgetedCostWorkPerformed(
            Interval interval) {
        Map<TaskElement, SortedMap<LocalDate, BigDecimal>> advanceCostPerTask = databaseSnapshots
                .snapshotAdvanceCostPerTask();
        Collection<TaskElement> tasks = filterTasksByDate(
                advanceCostPerTask.keySet(), interval);

        SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();
        for (TaskElement each : tasks) {
            addCost(result, advanceCostPerTask.get(each));
        }
        return result;
    }

}
