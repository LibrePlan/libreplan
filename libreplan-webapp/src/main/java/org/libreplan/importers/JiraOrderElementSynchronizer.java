/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

import static org.libreplan.web.I18nHelper._;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.libreplan.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.advance.entities.AdvanceType;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.libreplan.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IConnectorDAO;
import org.libreplan.business.common.entities.Connector;
import org.libreplan.business.common.entities.ConnectorException;
import org.libreplan.business.common.entities.PredefinedConnectorProperties;
import org.libreplan.business.common.entities.PredefinedConnectors;
import org.libreplan.business.orders.daos.IOrderSyncInfoDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.orders.entities.OrderSyncInfo;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.importers.jira.IssueDTO;
import org.libreplan.importers.jira.StatusDTO;
import org.libreplan.importers.jira.TimeTrackingDTO;
import org.libreplan.importers.jira.WorkLogDTO;
import org.libreplan.importers.jira.WorkLogItemDTO;
import org.libreplan.web.orders.IOrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of Synchronize order elements with jira issues
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JiraOrderElementSynchronizer implements IJiraOrderElementSynchronizer {

    private static final Log LOG = LogFactory
            .getLog(JiraOrderElementSynchronizer.class);

    private SynchronizationInfo synchronizationInfo;

    @Autowired
    private IConnectorDAO connectorDAO;

    @Autowired
    private IOrderSyncInfoDAO orderSyncInfoDAO;

    @Autowired
    private IAdHocTransactionService adHocTransactionService;

    @Autowired
    private IOrderModel orderModel;

    @Autowired
    private IJiraTimesheetSynchronizer jiraTimesheetSynchronizer;

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllJiraLabels() throws ConnectorException {
        Connector connector = getJiraConnector();
        if (connector == null) {
            throw new ConnectorException(_("JIRA connector not found"));
        }

        String jiraLabels = connector.getPropertiesAsMap().get(
                PredefinedConnectorProperties.JIRA_LABELS);

        String labels;
        try {
            new URL(jiraLabels);
            labels = JiraRESTClient.getAllLables(jiraLabels);
        } catch (MalformedURLException e) {
            labels = jiraLabels;
        }
        return Arrays.asList(StringUtils.split(labels, ","));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IssueDTO> getJiraIssues(String label) throws ConnectorException {

        Connector connector = getJiraConnector();
        if (connector == null) {
            throw new ConnectorException(_("JIRA connector not found"));
        }

        if (!connector.areConnectionValuesValid()) {
            throw new ConnectorException(
                    _("Connection values of JIRA connector are invalid"));
        }

        return getJiraIssues(label, connector);
    }

    /**
     * Gets all jira issues for the specified <code>label</code>
     *
     * @param label
     *            the search criteria
     * @param connector
     *            where to read the configuration parameters
     * @return a list of {@link IssueDTO}
     */
    private List<IssueDTO> getJiraIssues(String label, Connector connector) {
        Map<String, String> properties = connector.getPropertiesAsMap();
        String url = properties.get(PredefinedConnectorProperties.SERVER_URL);

        String username = properties
                .get(PredefinedConnectorProperties.USERNAME);

        String password = properties
                .get(PredefinedConnectorProperties.PASSWORD);

        String path = JiraRESTClient.PATH_SEARCH;
        String query = "labels=" + label;

        List<IssueDTO> issues = JiraRESTClient.getIssues(url, username, password,
                path, query);

        return issues;
    }

    @Override
    @Transactional(readOnly = true)
    public void syncOrderElementsWithJiraIssues(List<IssueDTO> issues, Order order) {

        synchronizationInfo = new SynchronizationInfo(_(
                "Synchronization order {0}", order.getName()));

        for (IssueDTO issue : issues) {
            String code = PredefinedConnectorProperties.JIRA_CODE_PREFIX
                    + order.getCode() + "-"
                    + issue.getKey();
            String name = issue.getFields().getSummary();

            OrderLine orderLine = syncOrderLine(order, code, name);
            if (orderLine == null) {
                synchronizationInfo.addFailedReason(_(
                        "Order-element for \"{0}\" issue not found",
                        issue.getKey()));
                continue;
            }

            EffortDuration loggedHours = getLoggedHours(issue.getFields()
                    .getTimetracking());
            EffortDuration estimatedHours = getEstimatedHours(issue.getFields()
                    .getTimetracking(), loggedHours);

            if (estimatedHours.isZero()) {
                synchronizationInfo.addFailedReason(_(
                                "Estimated time for \"{0}\" issue is 0",
                                issue.getKey()));
                continue;
            }

            syncHoursGroup(orderLine, code, estimatedHours.getHours());

            syncProgressMeasurement(orderLine, issue, estimatedHours,
                    loggedHours);
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
    private OrderLine syncOrderLine(Order order, String code,
            String name) {
        OrderElement orderElement = order.getOrderElement(code);
        if (orderElement != null && !orderElement.isLeaf()) {
            return null;
        }

        OrderLine orderLine = (OrderLine) orderElement;
        if (orderLine == null) {
            orderLine = OrderLine.create();
            orderLine.setCode(code);
            order.add(orderLine);
        }
        orderLine.setName(name);
        return orderLine;
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
     * @param orderLine
     *            an exist orderLine
     * @param issue
     *            jira's issue to synchronize with progress assignment and
     *            measurement
     */
    private void syncProgressMeasurement(OrderLine orderLine, IssueDTO issue,
            EffortDuration estimatedHours, EffortDuration loggedHours) {

        WorkLogDTO workLog = issue.getFields().getWorklog();

        if (workLog == null) {
            synchronizationInfo.addFailedReason(_(
                    "No worklogs found for \"{0}\" issue", issue.getKey()));
            return;
        }

        List<WorkLogItemDTO> workLogItems = workLog.getWorklogs();
        if (workLogItems.isEmpty()) {
            synchronizationInfo.addFailedReason(_(
                            "No worklog items found for \"{0}\" issue",
                            issue.getKey()));
            return;
        }

        BigDecimal percentage;

        // if status is closed, the progress percentage is 100% regardless the
        // loggedHours and estimatedHours

        if (isIssueClosed(issue.getFields().getStatus())) {
            percentage = new BigDecimal(100);
        } else {
            percentage = loggedHours.dividedByAndResultAsBigDecimal(
                    estimatedHours).multiply(new BigDecimal(100));
        }

        LocalDate latestWorkLogDate = LocalDate
                .fromDateFields(getTheLatestWorkLoggedDate(workLogItems));

        updateOrCreateProgressAssignmentAndMeasurement(orderLine,
                percentage, latestWorkLogDate);

    }

    /**
     * Get the estimated seconds from
     * {@link TimeTrackingDTO#getRemainingEstimateSeconds()} plus logged hours or
     * {@link TimeTrackingDTO#getOriginalEstimateSeconds()} and convert it to
     * {@link EffortDuration}
     *
     * @param timeTracking
     *            where the estimated time to get from
     * @param loggedHours
     *            hours already logged
     * @return estimatedHours
     */
    private EffortDuration getEstimatedHours(TimeTrackingDTO timeTracking,
            EffortDuration loggedHours) {
        if (timeTracking == null) {
            return EffortDuration.zero();
        }

        Integer timeestimate = timeTracking.getRemainingEstimateSeconds();
        if (timeestimate != null && timeestimate > 0) {
            return EffortDuration.seconds(timeestimate).plus(loggedHours);
        }

        Integer timeoriginalestimate = timeTracking
                .getOriginalEstimateSeconds();
        if (timeoriginalestimate != null) {
            return EffortDuration.seconds(timeoriginalestimate);
        }
        return EffortDuration.zero();
    }

    /**
     * Get the time spent in seconds from
     * {@link TimeTrackingDTO#getTimeSpentSeconds()} and convert it to
     * {@link EffortDuration}
     *
     * @param timeTracking
     *            where the timespent to get from
     * @return timespent in hous
     */
    private EffortDuration getLoggedHours(TimeTrackingDTO timeTracking) {
        if (timeTracking == null) {
            return EffortDuration.zero();
        }

        Integer timespentInSec = timeTracking.getTimeSpentSeconds();
        if (timespentInSec != null && timespentInSec > 0) {
            return EffortDuration.seconds(timespentInSec);
        }

        return EffortDuration.zero();
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
                    new BigDecimal(100).setScale(2));
            directAdvanceAssignment.setAdvanceType(advanceType);
            try {
                orderElement.addAdvanceAssignment(directAdvanceAssignment);
            } catch (DuplicateValueTrueReportGlobalAdvanceException e) {
                // This couldn't happen as it has just created the
                // directAdvanceAssignment with false as reportGlobalAdvance
                throw new RuntimeException(e);
            } catch (DuplicateAdvanceAssignmentForOrderElementException e) {
                // This could happen if a parent or child of the current
                // OrderElement has an advance of type PERCENTAGE
                synchronizationInfo
                        .addFailedReason(_(
                                "Duplicate value AdvanceAssignment for order element of \"{0}\"",
                                orderElement.getCode()));
                return;
            }
        }

        AdvanceMeasurement advanceMeasurement = directAdvanceAssignment
                .getAdvanceMeasurementAtExactDate(latestWorkLogDate);
        if (advanceMeasurement == null) {
            advanceMeasurement = AdvanceMeasurement.create();
            advanceMeasurement.setDate(latestWorkLogDate);
            directAdvanceAssignment.addAdvanceMeasurements(advanceMeasurement);
        }

        advanceMeasurement.setValue(percentage
                .setScale(2, RoundingMode.HALF_UP));

        DirectAdvanceAssignment spreadAdvanceAssignment = orderElement
                .getReportGlobalAdvanceAssignment();
        if (spreadAdvanceAssignment != null) {
            spreadAdvanceAssignment.setReportGlobalAdvance(false);
        }

        directAdvanceAssignment.setReportGlobalAdvance(true);
    }

    /**
     * check if issue is closed
     *
     * @param status
     *            the status of the issue
     * @return true if status is Closed
     */
    private boolean isIssueClosed(StatusDTO status) {
        if (status == null) {
            return false;
        }
        return status.getName().equals("Closed");
    }

    /**
     * Loop through all <code>workLogItems</code> and get the latest date
     *
     * @param workLogItems
     *            list of workLogItems
     * @return latest date
     */
    private Date getTheLatestWorkLoggedDate(List<WorkLogItemDTO> workLogItems) {
        List<Date> dates = new ArrayList<Date>();
        for (WorkLogItemDTO workLogItem : workLogItems) {
            if (workLogItem.getStarted() != null) {
                dates.add(workLogItem.getStarted());
            }
        }
        return Collections.max(dates);
    }

    @Override
    public SynchronizationInfo getSynchronizationInfo() {
        return synchronizationInfo;
    }

    /**
     * returns JIRA connector
     */
    private Connector getJiraConnector() {
        return connectorDAO.findUniqueByName(PredefinedConnectors.JIRA
                .getName());
    }

    @Override
    @Transactional
    public void saveSyncInfo(final String key, final Order order) {
        adHocTransactionService
                .runOnAnotherTransaction(new IOnTransaction<Void>() {
                    @Override
                    public Void execute() {
                        OrderSyncInfo orderSyncInfo = orderSyncInfoDAO
                                .findByKeyOrderAndConnectorName(key, order,
                                        PredefinedConnectors.JIRA.getName());
                        if (orderSyncInfo == null) {
                            orderSyncInfo = OrderSyncInfo.create(key, order,
                                    PredefinedConnectors.JIRA.getName());
                        }
                        orderSyncInfo.setLastSyncDate(new Date());
                        orderSyncInfoDAO.save(orderSyncInfo);
                        return null;
                    }
                });
    }


    @Override
    @Transactional(readOnly = true)
    public OrderSyncInfo getOrderLastSyncInfo(Order order) {
        return orderSyncInfoDAO.findLastSynchronizedInfoByOrderAndConnectorName(
                order, PredefinedConnectors.JIRA.getName());

    }

    @Override
    @Transactional
    public List<SynchronizationInfo> syncOrderElementsWithJiraIssues() throws ConnectorException {
        Connector connector = getJiraConnector();
        if (connector == null) {
            throw new ConnectorException(_("JIRA connector not found"));
        }
        if (!connector.areConnectionValuesValid()) {
            throw new ConnectorException(
                    _("Connection values of JIRA connector are invalid"));
        }

        List<OrderSyncInfo> orderSyncInfos = orderSyncInfoDAO
                .findByConnectorName(PredefinedConnectors.JIRA.getName());

        synchronizationInfo = new SynchronizationInfo(_("Synchronization"));

        List<SynchronizationInfo> syncInfos = new ArrayList<SynchronizationInfo>();

        if (orderSyncInfos == null || orderSyncInfos.isEmpty()) {
            LOG.warn("No items found in 'OrderSyncInfo' to synchronize with JIRA issues");
            synchronizationInfo
                    .addFailedReason(_("No items found in 'OrderSyncInfo' to synchronize with JIRA issues"));
            syncInfos.add(synchronizationInfo);
            return syncInfos;
        }

        for (OrderSyncInfo orderSyncInfo : orderSyncInfos) {
            Order order = orderSyncInfo.getOrder();
            LOG.info("Synchronizing '" + order.getName() + "'");
            synchronizationInfo = new SynchronizationInfo(_(
                    "Synchronization order {0}", order.getName()));

            List<IssueDTO> issueDTOs = getJiraIssues(orderSyncInfo.getKey(),
                    connector);
            if (issueDTOs == null || issueDTOs.isEmpty()) {
                LOG.warn("No JIRA issues found for '" + orderSyncInfo.getKey()
                        + "'");
                synchronizationInfo.addFailedReason(_(
                        "No JIRA issues found for key {0}",
                        orderSyncInfo.getKey()));
                syncInfos.add(synchronizationInfo);
                continue;
            }

            orderModel.initEdit(order, null);
            syncOrderElementsWithJiraIssues(issueDTOs, order);
            if (!synchronizationInfo.isSuccessful()) {
                syncInfos.add(synchronizationInfo);
                continue;
            }
            orderModel.save(false);

            saveSyncInfo(orderSyncInfo.getKey(), order);

            jiraTimesheetSynchronizer.syncJiraTimesheetWithJiraIssues(
                    issueDTOs, order);
            if (!synchronizationInfo.isSuccessful()) {
                syncInfos.add(synchronizationInfo);
            }
        }
        return syncInfos;
    }
}
