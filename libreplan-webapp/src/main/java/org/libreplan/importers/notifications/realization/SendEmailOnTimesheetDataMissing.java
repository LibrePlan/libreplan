/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2016 LibrePlan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.importers.notifications.realization;


import org.joda.time.LocalDate;
import org.libreplan.business.common.Configuration;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.entities.PersonalTimesheetsPeriodicityEnum;

import org.libreplan.business.email.entities.EmailNotification;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.orders.entities.OrderElement;

import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;

import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workingday.IntraDayDate;
import org.libreplan.business.workreports.daos.IWorkReportDAO;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.libreplan.importers.notifications.ComposeMessage;
import org.libreplan.importers.notifications.EmailConnectionValidator;
import org.libreplan.importers.notifications.IEmailNotificationJob;
import org.libreplan.web.calendars.BaseCalendarModel;
import org.libreplan.web.common.Util;
import org.libreplan.web.email.IEmailNotificationModel;
import org.libreplan.web.users.IUserModel;
import org.libreplan.web.users.dashboard.PersonalTimesheetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Sends E-mail to users with data that storing in notification_queue table
 * and that are treat to {@link EmailTemplateEnum#TEMPLATE_ENTER_DATA_IN_TIMESHEET}
 * Data will be send for bound users with empty timesheet lines.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SendEmailOnTimesheetDataMissing implements IEmailNotificationJob {

    @Autowired
    private IEmailNotificationModel emailNotificationModel;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IWorkReportDAO workReportDAO;

    @Autowired
    private IUserModel userModel;

    @Autowired
    private ComposeMessage composeMessage;

    @Autowired
    private EmailConnectionValidator emailConnectionValidator;

    @Override
    @Transactional
    public void sendEmail() {
        checkTimesheet();

        if ( Configuration.isEmailSendingEnabled() ) {
            if ( emailConnectionValidator.isConnectionActivated() && emailConnectionValidator.validConnection() ) {

                List<EmailNotification> notifications =
                        emailNotificationModel.getAllByType(EmailTemplateEnum.TEMPLATE_ENTER_DATA_IN_TIMESHEET);

                for (int i = 0; i < notifications.size(); i++) {
                    if ( composeMessageForUser(notifications.get(i)) ) {
                        deleteSingleNotification(notifications.get(i));
                    }
                }
            }
        }
    }

    @Override
    public boolean composeMessageForUser(EmailNotification notification) {
        return composeMessage.composeMessageForUser(notification);
    }

    private void deleteSingleNotification(EmailNotification notification) {
        emailNotificationModel.deleteById(notification);
    }


    public void checkTimesheet() {
        List<User> list = getPersonalTimesheets();
        addRowsToNotificationTable(list);
    }

    @Transactional
    private List<User> getPersonalTimesheets() {
        List<PersonalTimesheetDTO> personalTimesheetDTO = new ArrayList<>();
        List<User> usersWithoutTimesheets = new ArrayList<>();
        List<User> users = userModel.getUsers();

        for (User user : users)
            if (user.isBound()) {
                Resource resource = user.getWorker();
                BaseCalendarModel.forceLoadBaseCalendar(resource.getCalendar());

                LocalDate activationDate = getActivationDate(user.getWorker());
                LocalDate currentDate = new LocalDate();
                personalTimesheetDTO.addAll(getPersonalTimesheets(
                        user.getWorker(),
                        activationDate,
                        currentDate.plusMonths(1),
                        getPersonalTimesheetsPeriodicity()));

                for(PersonalTimesheetDTO item : personalTimesheetDTO) {
                    WorkReport workReport = item.getWorkReport();

                    if ( item.getTasksNumber() == 0 && workReport == null )
                        if ( !usersWithoutTimesheets.contains(user) )
                            usersWithoutTimesheets.add(user);
                }


                personalTimesheetDTO.clear();
            }

        return usersWithoutTimesheets;
    }

    private void addRowsToNotificationTable(List<User> users){
        for (User user : users){
            if ( user.isInRole(UserRole.ROLE_EMAIL_TIMESHEET_DATA_MISSING) ) {
                emailNotificationModel.setNewObject();
                emailNotificationModel.setResource(user.getWorker());
                emailNotificationModel.setType(EmailTemplateEnum.TEMPLATE_ENTER_DATA_IN_TIMESHEET);
                emailNotificationModel.setUpdated(new Date());
                emailNotificationModel.confirmSave();
            }
        }
    }

    private List<PersonalTimesheetDTO> getPersonalTimesheets(Resource resource,
                                                             LocalDate start, LocalDate end,
                                                             PersonalTimesheetsPeriodicityEnum periodicity) {
        start = periodicity.getStart(start);
        end = periodicity.getEnd(end);
        int items = periodicity.getItemsBetween(start, end);

        List<PersonalTimesheetDTO> result = new ArrayList<>();

        // In decreasing order to provide a list sorted with the more recent personal timesheets at the beginning
        for (int i = items; i >= 0; i--) {
            LocalDate date = periodicity.getDateForItemFromDate(i, start);

            WorkReport workReport = getWorkReport(resource, date, periodicity);

            EffortDuration hours = EffortDuration.zero();
            int tasksNumber = 0;
            if (workReport != null) {
                hours = workReport.getTotalEffortDuration();
                tasksNumber = getNumberOfOrderElementsWithTrackedTime(workReport);
            }

            result.add(new PersonalTimesheetDTO(
                    date,
                    workReport,
                    getResourceCapacity(resource, date, periodicity),
                    hours,
                    tasksNumber));
        }

        return result;
    }
    private LocalDate getActivationDate(Worker worker) {
        return worker.getCalendar().getFistCalendarAvailability().getStartDate();
    }


    private PersonalTimesheetsPeriodicityEnum getPersonalTimesheetsPeriodicity() {
        return configurationDAO.getConfiguration().getPersonalTimesheetsPeriodicity();
    }
    private WorkReport getWorkReport(Resource resource, LocalDate date, PersonalTimesheetsPeriodicityEnum periodicity) {

        WorkReport workReport = workReportDAO.getPersonalTimesheetWorkReport(resource, date, periodicity);
        forceLoad(workReport);

        return workReport;
    }
    private void forceLoad(WorkReport workReport) {
        if (workReport != null) {
            WorkReportType workReportType = workReport.getWorkReportType();
            workReportType.getLineFields().size();
            workReportType.getWorkReportLabelTypeAssignments().size();
            workReportType.getHeadingFields().size();
        }
    }
    private int getNumberOfOrderElementsWithTrackedTime(WorkReport workReport) {
        if (workReport == null) {
            return 0;
        }

        List<OrderElement> orderElements = new ArrayList<>();
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
    private EffortDuration getResourceCapacity(Resource resource, LocalDate date,
                                              PersonalTimesheetsPeriodicityEnum periodicity) {

        LocalDate start = periodicity.getStart(date);
        LocalDate end = periodicity.getEnd(date);

        EffortDuration capacity = EffortDuration.zero();
        for (LocalDate day = start; day.compareTo(end) <= 0; day = day.plusDays(1)) {
            capacity = capacity.plus(resource.getCalendar().getCapacityOn(IntraDayDate.PartialDay.wholeDay(day)));
        }
        return capacity;
    }

}
