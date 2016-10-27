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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.SortedMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.Date;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.planner.entities.DayAssignment.FilterType;
import org.libreplan.business.workreports.daos.IWorkReportLineDAO;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Cost calulator in terms of hours.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
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
    public SortedMap<LocalDate, BigDecimal> getAdvanceCost(Task task, LocalDate filterStartDate,
                                                           LocalDate filterEndDate) {
        DirectAdvanceAssignment advanceAssignment =
                (task.getOrderElement() != null) ? task.getOrderElement().getReportGlobalAdvanceAssignment() : null;

        if ( advanceAssignment == null ) {
            return new TreeMap<>();
        }

        return calculateHoursPerDay(
                task.getHoursSpecifiedAtOrder(),
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
            if( ((filterStartDate == null) || day.compareTo(filterStartDate) >= 0) &&
                    ((filterEndDate == null) || day.compareTo(filterEndDate) <= 0) ) {

                BigDecimal cost = advanceMeasurement.getValue().setScale(2)
                        .multiply(new BigDecimal(totalHours))
                        .divide(new BigDecimal(100), new MathContext(2, RoundingMode.HALF_UP));
                result.put(day, cost);
            }
        }

        return result;
    }

    @Override
    public SortedMap<LocalDate, BigDecimal> getEstimatedCost(Task task) {
        return getEstimatedCost(task, null, null);
    }

    /**
     * BCWS values are calculating here.
     * MAX(BCWS) equals addition of all dayAssignments.
     */
    @Override
    public SortedMap<LocalDate, BigDecimal> getEstimatedCost(Task task,
                                                             LocalDate filterStartDate,
                                                             LocalDate filterEndDate) {

        if ( task.isSubcontracted() ) {
            return getAdvanceCost(task);
        }

        SortedMap<LocalDate, BigDecimal> result = new TreeMap<LocalDate, BigDecimal>();

        List<DayAssignment> dayAssignments = task.getDayAssignments(FilterType.WITHOUT_DERIVED);
        if ( dayAssignments.isEmpty() ) {
            return result;
        }

        int additionOfAllAssignmentsMinutes = 0;

        for (DayAssignment dayAssignment : dayAssignments) {
            LocalDate day = dayAssignment.getDay();
            if( ((filterStartDate == null) || day.compareTo(filterStartDate) >= 0) &&
                    ((filterEndDate == null) || day.compareTo(filterEndDate) <= 0) ) {

                String currentTime = dayAssignment.getDuration().toFormattedString();

                SimpleDateFormat format1 = new SimpleDateFormat("hh:mm");
                SimpleDateFormat format2 = new SimpleDateFormat("hh");

                Date date = null;

                try {
                    if ( isParsableWithFormat1(currentTime) ) {
                        date = format1.parse(currentTime);
                    } else if ( isParsableWithFormat2(currentTime) ) {
                        date = format2.parse(currentTime);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                assert date != null;

                LocalTime time = new LocalTime(date.getTime());
                // Time time = new Time(date.getTime());

                BigDecimal hours = new BigDecimal(time.getHourOfDay());
                additionOfAllAssignmentsMinutes += time.getMinuteOfHour();

                if ( !result.containsKey(day) ) {
                    result.put(day, BigDecimal.ZERO);
                }

                /**
                 * On last day assignment app will check addition of minutes of all assignments.
                 * If it is between 30 and 60 - add 1 hour to the last value of result.
                 * If it is more than 60 then divide on 60 and calculate hours.
                 * E.G. 120 minutes / 60 = 2 hours.
                 */
                if ( dayAssignment.equals(dayAssignments.get(dayAssignments.size() - 1)) ){

                    if ( additionOfAllAssignmentsMinutes >= 30 && additionOfAllAssignmentsMinutes <= 60 )
                        hours = BigDecimal.valueOf(hours.intValue() + 1);

                    if ( additionOfAllAssignmentsMinutes > 60 )
                        hours = BigDecimal.valueOf(hours.intValue() + (additionOfAllAssignmentsMinutes / 60));
                }
                result.put(day, result.get(day).add(hours));
            }
        }
        return result;
    }

    private boolean isParsableWithFormat1(String input){
        boolean parsable = true;
        try {
            SimpleDateFormat format = new SimpleDateFormat("hh:mm");
            format.parse(input);
        } catch (ParseException e) {
            parsable = false;
        }
        return parsable;
    }
    private boolean isParsableWithFormat2(String input){
        boolean parsable = true;
        try {
            SimpleDateFormat format = new SimpleDateFormat("hh");
            format.parse(input);
        } catch (ParseException e) {
            parsable = false;
        }
        return parsable;
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
            BigDecimal cost = workReportLine.getEffort()
                    .toHoursAsDecimalWithScale(2);

            if (!result.containsKey(day)) {
                result.put(day, BigDecimal.ZERO);
            }
            result.put(day, result.get(day).add(cost));
        }

        return result;
    }

}
