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
        start = periodicity.getStart(start);
        end = periodicity.getEnd(end);
        int items = periodicity.getItemsBetween(start, end);

        List<MonthlyTimesheetDTO> result = new ArrayList<MonthlyTimesheetDTO>();

        // In decreasing order to provide a list sorted with the more recent
        // monthly timesheets at the beginning
        for (int i = items; i >= 0; i--) {
            LocalDate date = periodicity.getDateForItemFromDate(i, start);

            WorkReport workReport = getWorkReport(resource, date, periodicity);

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

    private WorkReport getWorkReport(Resource resource, LocalDate date,
            PersonalTimesheetsPeriodicityEnum periodicity) {
        WorkReport workReport = workReportDAO.getMonthlyTimesheetWorkReport(
                resource, date, periodicity);
        forceLoad(workReport);
        return workReport;
    }

    private EffortDuration getResourceCapcity(Resource resource,
            LocalDate date, PersonalTimesheetsPeriodicityEnum periodicity) {
        LocalDate start = periodicity.getStart(date);
        LocalDate end = periodicity.getEnd(date);

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
