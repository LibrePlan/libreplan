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

package org.libreplan.importers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.libreplan.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.advance.entities.AdvanceType;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.libreplan.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.importers.jira.Issue;
import org.libreplan.importers.jira.Status;
import org.libreplan.importers.jira.TimeTracking;
import org.libreplan.importers.jira.WorkLog;
import org.libreplan.importers.jira.WorkLogItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JiraOrderElementSynchronizer implements IJiraOrderElementSynchronizer {

    private static final String CODE_PREFIX = "JIRA-";

    @Autowired
    private IConfigurationDAO configurationDAO;

    private JiraSyncInfo jiraSyncInfo;


    @Override
    @Transactional(readOnly = true)
    public List<String> getAllJiraLabels() {
        String jiraLabelUrl = configurationDAO
                .getConfigurationWithReadOnlyTransaction()
                .getJiraConfiguration().getJiraLabelUrl();

        return JiraRESTClient.getAllLables(jiraLabelUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Issue> getJiraIssues(String label) {

        String url = configurationDAO.getConfigurationWithReadOnlyTransaction()
                .getJiraConfiguration()
                .getJiraUrl();

        String username = configurationDAO
                .getConfigurationWithReadOnlyTransaction()
                .getJiraConfiguration().getJiraUserId();

        String password = configurationDAO
                .getConfigurationWithReadOnlyTransaction()
                .getJiraConfiguration().getJiraPassword();

        String path = "rest/api/latest/search";
        String query = "labels=" + label;

        List<Issue> issues = JiraRESTClient.getIssues(url, username, password,
                path, query);

        return issues;
    }

    @Override
    @Transactional(readOnly = true)
    public void syncOrderElementsWithJiraIssues(Order order, List<Issue> issues) {

        jiraSyncInfo = new JiraSyncInfo();

        for (Issue issue : issues) {
            String code = CODE_PREFIX + order.getCode() + "-" + issue.getKey();
            String name = issue.getFields().getSummary();

            syncOrderLine(order, code, name);


            syncHoursGroup(
                    (OrderLine) order.getOrderElement(code), code,
                    getEstimatedHours(issue.getFields().getTimetracking()));

            syncPorgressMeasurement(order.getOrderElement(code), issue);
        }

    }


    /**
     * Synchronize orderline
     *
     * check if orderLine is already exist for the given <code>order</code> If
     * it is, update <code>OrderLine.name</code> with the specified parameter
     * <code>name</code> (jira's name could be changed). If not, create new
     * {@link OrderLine} and add to {@link Order}
     *
     * @param order
     *            an existing order
     * @param code
     *            unique code for orderLine
     * @param name
     *            name for the orderLine to be added or updated
     */
    private void syncOrderLine(Order order, String code,
            String name) {
        OrderLine orderLine = (OrderLine) order.getOrderElement(code);
        if (orderLine == null) {
            orderLine = OrderLine.create();
            orderLine.setCode(code);
            order.add(orderLine);
        }
        orderLine.setName(name);
    }

    /**
     * Synchronize hoursgroup
     *
     * Check if hoursGroup already exist for the given <code>orderLine</code>.
     * If it is, update <code>HoursGroup.workingHours</code> with the specified
     * parameter <code>workingHours</code>. If not, create new
     * {@link HoursGroup} and add to the {@link OrderLine}
     *
     * @param orderLine
     *            an existing orderline
     * @param code
     *            unique code for hoursgroup
     * @param workingHours
     *            the working hours(jira's timetracking)
     */
    private void syncHoursGroup(OrderLine orderLine, String code,
            Integer workingHours) {
        HoursGroup hoursGroup = orderLine.getHoursGroup(code);
        if (hoursGroup == null) {
            hoursGroup = HoursGroup.create(orderLine);
            hoursGroup.setCode(code);
            orderLine.addHoursGroup(hoursGroup);
        }

        hoursGroup.setWorkingHours(workingHours);
    }

    /**
     * Synchronize progress assignment and measurement
     *
     * @param orderElement
     *            an exist orderElement
     * @param issue
     *            jira's issue to synchronize with progress assignment and
     *            measurement
     */
    private void syncPorgressMeasurement(OrderElement orderElement, Issue issue) {

        WorkLog workLog = issue.getFields().getWorklog();

        if (workLog == null) {
            jiraSyncInfo.addSyncFailedReason("No worklogs found for '"
                    + issue.getKey() + "' issue");
            return;
        }
        if (orderElement == null) {
            jiraSyncInfo.addSyncFailedReason("Order-element for '"
                    + issue.getKey() + "' issue not found");
            return;
        }

        List<WorkLogItem> workLogItems = workLog.getWorklogs();
        if (workLogItems.isEmpty()) {
            jiraSyncInfo.addSyncFailedReason("No worklog items found for '"
                    + issue.getKey() + "' issue");
            return;
        }

        Integer estimatedHours = getEstimatedHours(issue.getFields()
                .getTimetracking());

        if (estimatedHours == 0) {
            jiraSyncInfo.addSyncFailedReason("Estimated time for '"
                    + issue.getKey() + "' issue is 0");
            return;
        }

        Integer loggedHours = getLoggedHours(issue.getFields()
                .getTimetracking());

        BigDecimal percentage;

        // if status is closed, the progress percentage is 100% regardless the
        // loggedHours and estimatedHours

        if (isIssueClosed(issue.getFields().getStatus())) {
            percentage = new BigDecimal(100);
        } else {
            percentage = calculatePercentage(estimatedHours, loggedHours);
        }

        LocalDate latestWorkLogDate = LocalDate
                .fromDateFields(getTheLatestWorkLoggedDate(workLogItems));

        updateOrCreateProgressAssignmentAndMeasurement(orderElement,
                percentage, latestWorkLogDate);

    }

    /**
     * Get the estimated seconds from
     * {@link TimeTracking#getRemainingEstimateSeconds()} or
     * {@link TimeTracking#getOriginalEstimateSeconds()} and convert it to hours
     *
     * @param timeTracking
     *            where the estimated time to get from
     * @return estimatedHours
     */
    private Integer getEstimatedHours(TimeTracking timeTracking) {
        if (timeTracking == null) {
            return 0;
        }

        Integer timeestimate = timeTracking.getRemainingEstimateSeconds();
        if (timeestimate != null && timeestimate > 0) {
            return secondsToHours(timeestimate);
        }

        Integer timeoriginalestimate = timeTracking
                .getOriginalEstimateSeconds();
        if (timeoriginalestimate != null) {
            return secondsToHours(timeoriginalestimate);
        }
        return 0;
    }

    /**
     * Get the time spent in seconds from
     * {@link TimeTracking#getTimeSpentSeconds()} and convert it to hours
     *
     * @param timeTracking
     *            where the timespent to get from
     * @return timespent in hous
     */
    private Integer getLoggedHours(TimeTracking timeTracking) {
        if (timeTracking == null) {
            return 0;
        }

        Integer timespentInSec = timeTracking.getTimeSpentSeconds();
        if (timespentInSec != null && timespentInSec > 0) {
            return secondsToHours(timespentInSec);
        }

        return 0;
    }

    /**
     * Convert seconds to hours
     *
     * @param seconds
     *
     * @return hours
     */
    private Integer secondsToHours(Integer seconds) {
        return Math.round(seconds / 3600);
    }

    /**
     * updates {@link DirectAdvanceAssignment} and {@link AdvanceMeasurement} if
     * they already exist, otherwise create new one
     *
     * @param orderElement
     *            an existing orderElement
     * @param percentage
     *            percentage for advanced measurement
     * @param latestWorkLogDate
     *            date for advanced measurement
     */
    private void updateOrCreateProgressAssignmentAndMeasurement(
            OrderElement orderElement, BigDecimal percentage,
            LocalDate latestWorkLogDate) {

        AdvanceType advanceType = PredefinedAdvancedTypes.PERCENTAGE.getType();

        DirectAdvanceAssignment directAdvanceAssignment = orderElement
                .getDirectAdvanceAssignmentByType(advanceType);
        if (directAdvanceAssignment == null) {
            directAdvanceAssignment = DirectAdvanceAssignment.create(false,
                    new BigDecimal(100));
            directAdvanceAssignment.setAdvanceType(advanceType);
        }
        directAdvanceAssignment.setOrderElement(orderElement);

        AdvanceMeasurement advanceMeasurement = directAdvanceAssignment
                .getAdvanceMeasurementAtExactDate(latestWorkLogDate);
        if (advanceMeasurement == null) {
            advanceMeasurement = AdvanceMeasurement.create();
        }

        advanceMeasurement.setValue(percentage);
        advanceMeasurement.setDate(latestWorkLogDate);

        directAdvanceAssignment.addAdvanceMeasurements(advanceMeasurement);

        advanceMeasurement.setAdvanceAssignment(directAdvanceAssignment);

        if (directAdvanceAssignment.isNewObject()) {
            try {
                directAdvanceAssignment.getOrderElement().addAdvanceAssignment(
                        directAdvanceAssignment);
            } catch (DuplicateValueTrueReportGlobalAdvanceException e) {
                jiraSyncInfo
                        .addSyncFailedReason("Duplicate value ReportGlobablAdvance for '"
                                + orderElement.getCode() + "'");
            } catch (DuplicateAdvanceAssignmentForOrderElementException e) {
                jiraSyncInfo
                        .addSyncFailedReason("Duplicate value AdvanceAssignment for order element of '"
                                + orderElement.getCode() + "'");
            }
        }

    }

    /**
     * check if issue is closed
     *
     * @param status
     *            the status of the issue
     * @return true if status is Closed
     */
    private boolean isIssueClosed(Status status) {
        if (status == null) {
            return false;
        }
        return status.getName().equals("Closed");
    }

    /**
     * Calculate percentage based on <code>loggedHours</code> and
     * <code>estimatedHours</code>
     *
     * @param estimatedHours
     *            the estimated hours
     * @param loggedHours
     *            time spent so far
     *
     * @return the calculated percentage
     */
    private BigDecimal calculatePercentage(Integer estimatedHours,
            Integer loggedHours) {

        if (estimatedHours == 0) {
            return BigDecimal.ZERO;
        }

        double percentage = (loggedHours * 100)
                / (loggedHours + estimatedHours);
        return new BigDecimal(percentage);
    }

    /**
     * Loop through all <code>workLogItems</code> and get the latest date
     *
     * @param workLogItems
     *            list of workLogItems
     * @return latest date
     */
    private Date getTheLatestWorkLoggedDate(List<WorkLogItem> workLogItems) {
        List<Date> dates = new ArrayList<Date>();
        for (WorkLogItem workLogItem : workLogItems) {
            if (workLogItem.getStarted() != null) {
                dates.add(workLogItem.getStarted());
            }
        }
        return Collections.max(dates);
    }

    @Override
    public JiraSyncInfo getJiraSyncInfo() {
        return jiraSyncInfo;
    }

}
