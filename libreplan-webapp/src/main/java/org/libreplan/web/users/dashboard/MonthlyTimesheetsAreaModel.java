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

package org.libreplan.web.users.dashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.entities.PersonalTimesheetsPeriodicityEnum;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate.PartialDay;
import org.libreplan.business.workreports.daos.IWorkReportDAO;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.libreplan.web.UserUtil;
import org.libreplan.web.calendars.BaseCalendarModel;
import org.libreplan.web.common.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for for "Monthly timesheets" area in the user dashboard window
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MonthlyTimesheetsAreaModel implements IMonthlyTimesheetsAreaModel {

    @Autowired
    private IWorkReportDAO workReportDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyTimesheetDTO> getMonthlyTimesheets() {
        User user = UserUtil.getUserFromSession();
        if (!user.isBound()) {
            return Collections.emptyList();
        }
        Resource resource = user.getWorker();
        BaseCalendarModel.forceLoadBaseCalendar(resource.getCalendar());

        LocalDate activationDate = getActivationDate(user.getWorker());
        LocalDate currentDate = new LocalDate();
        return getMonthlyTimesheets(user.getWorker(), activationDate,
                currentDate.plusMonths(1), getPersonalTimesheetsPeriodicity());
    }

    private List<MonthlyTimesheetDTO> getMonthlyTimesheets(Resource resource,
            LocalDate start, LocalDate end,
            PersonalTimesheetsPeriodicityEnum periodicity) {
        int items;
        switch (periodicity) {
            case WEEKLY:
                start = start.dayOfWeek().withMinimumValue();
                end = end.dayOfWeek().withMaximumValue();
                items = Weeks.weeksBetween(start, end).getWeeks();
                break;
            case TWICE_MONTHLY:
                if (start.getDayOfMonth() <= 15) {
                    start = start.dayOfMonth().withMinimumValue();
                } else {
                    start = start.dayOfMonth().withMinimumValue().plusDays(15);
                }
                if (end.getDayOfMonth() <= 15) {
                    end = end.dayOfMonth().withMinimumValue().plusDays(14);
                } else {
                    end = end.dayOfMonth().withMaximumValue();
                }
                items = Months.monthsBetween(start, end).getMonths() * 2;
                break;
            case MONTHLY:
            default:
                start = start.dayOfMonth().withMinimumValue();
                end = end.dayOfMonth().withMaximumValue();
                items = Months.monthsBetween(start, end).getMonths();
                break;
        }

        List<MonthlyTimesheetDTO> result = new ArrayList<MonthlyTimesheetDTO>();

        // In decreasing order to provide a list sorted with the more recent
        // monthly timesheets at the beginning
        for (int i = items; i >= 0; i--) {
            LocalDate date;
            switch (periodicity) {
                case WEEKLY:
                    date = start.plusWeeks(i);
                    break;
                case TWICE_MONTHLY:
                int months = (i % 2 == 0) ? (i / 2) : ((i - 1) / 2);
                    date = start.plusMonths(months);
                    if (i % 2 != 0) {
                        if (date.getDayOfMonth() <= 15) {
                            date = date.dayOfMonth().withMinimumValue()
                                    .plusDays(15);
                        } else {
                            date = date.plusMonths(1).dayOfMonth()
                                    .withMinimumValue();
                        }
                    }
                    break;
                case MONTHLY:
                default:
                    date = start.plusMonths(i);
                    break;
            }

            WorkReport workReport = getWorkReport(resource, date);

            EffortDuration hours = EffortDuration.zero();
            int tasksNumber = 0;
            if (workReport != null) {
                hours = workReport.getTotalEffortDuration();
                tasksNumber = getNumberOfOrderElementsWithTrackedTime(workReport);
            }

            result.add(new MonthlyTimesheetDTO(date, workReport,
                    getResourceCapcity(resource, date, periodicity), hours,
                    tasksNumber));
        }

        return result;
    }

    private WorkReport getWorkReport(Resource resource, LocalDate date) {
        WorkReport workReport = workReportDAO.getMonthlyTimesheetWorkReport(
                resource, date);
        forceLoad(workReport);
        return workReport;
    }

    private EffortDuration getResourceCapcity(Resource resource,
            LocalDate date, PersonalTimesheetsPeriodicityEnum periodicity) {
        LocalDate start;
        LocalDate end;
        switch (periodicity) {
            case WEEKLY:
                start = date.dayOfWeek().withMinimumValue();
                end = date.dayOfWeek().withMaximumValue();
                break;
            case TWICE_MONTHLY:
                if (date.getDayOfMonth() <= 15) {
                    start = date.dayOfMonth().withMinimumValue();
                    end = start.plusDays(14);
                } else {
                    start = date.dayOfMonth().withMinimumValue().plusDays(15);
                    end = date.dayOfMonth().withMaximumValue();
                }
                break;
            default:
            case MONTHLY:
                start = date.dayOfMonth().withMinimumValue();
                end = date.dayOfMonth().withMaximumValue();
                break;
        }

        EffortDuration capacity = EffortDuration.zero();
        for (LocalDate day = start; day.compareTo(end) <= 0; day = day
                .plusDays(1)) {
            capacity = capacity.plus(resource.getCalendar().getCapacityOn(
                    PartialDay.wholeDay(day)));
        }
        return capacity;
    }

    private void forceLoad(WorkReport workReport) {
        if (workReport != null) {
            WorkReportType workReportType = workReport.getWorkReportType();
            workReportType.getLineFields().size();
            workReportType.getWorkReportLabelTypeAssigments().size();
            workReportType.getHeadingFields().size();
        }
    }

    private LocalDate getActivationDate(Worker worker) {
        return worker.getCalendar().getFistCalendarAvailability()
                .getStartDate();
    }

    @Override
    public int getNumberOfOrderElementsWithTrackedTime(WorkReport workReport) {
        if (workReport == null) {
            return 0;
        }

        List<OrderElement> orderElements = new ArrayList<OrderElement>();
        for (WorkReportLine line : workReport.getWorkReportLines()) {
            if (!line.getEffort().isZero()) {
                OrderElement orderElement = line.getOrderElement();
                if (!Util.contains(orderElements, orderElement)) {
                    orderElements.add(orderElement);
                }
            }
        }
        return orderElements.size();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalTimesheetsPeriodicityEnum getPersonalTimesheetsPeriodicity() {
        return configurationDAO.getConfiguration()
                .getPersonalTimesheetsPeriodicity();
    }

}
