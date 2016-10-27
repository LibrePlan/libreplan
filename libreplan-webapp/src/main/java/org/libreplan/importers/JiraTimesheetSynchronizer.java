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

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.NonUniqueResultException;
import org.libreplan.business.common.daos.IConnectorDAO;
import org.libreplan.business.common.entities.Connector;
import org.libreplan.business.common.entities.ConnectorException;
import org.libreplan.business.common.entities.PredefinedConnectorProperties;
import org.libreplan.business.common.entities.PredefinedConnectors;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.costcategories.daos.ITypeOfWorkHoursDAO;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.orders.daos.IOrderSyncInfoDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderSyncInfo;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workreports.daos.IWorkReportDAO;
import org.libreplan.business.workreports.daos.IWorkReportTypeDAO;
import org.libreplan.business.workreports.entities.PredefinedWorkReportTypes;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.libreplan.business.workreports.valueobjects.DescriptionField;
import org.libreplan.business.workreports.valueobjects.DescriptionValue;
import org.libreplan.importers.jira.IssueDTO;
import org.libreplan.importers.jira.WorkLogDTO;
import org.libreplan.importers.jira.WorkLogItemDTO;
import org.libreplan.web.workreports.IWorkReportModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of Synchronize timesheets with jira issues.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JiraTimesheetSynchronizer implements IJiraTimesheetSynchronizer {

    private SynchronizationInfo synchronizationInfo;

    private List<Worker> workers;

    private WorkReportType workReportType;

    private TypeOfWorkHours typeOfWorkHours;

    @Autowired
    private IWorkerDAO workerDAO;

    @Autowired
    private IWorkReportTypeDAO workReportTypeDAO;

    @Autowired
    private IWorkReportDAO workReportDAO;

    @Autowired
    private IWorkReportModel workReportModel;

    @Autowired
    private ITypeOfWorkHoursDAO typeOfWorkHoursDAO;

    @Autowired
    private IConnectorDAO connectorDAO;

    @Autowired
    private IOrderSyncInfoDAO orderSyncInfoDAO;

    @Override
    @Transactional
    public void syncJiraTimesheetWithJiraIssues(List<IssueDTO> issues, Order order) throws ConnectorException {
        synchronizationInfo = new SynchronizationInfo(_("Synchronization"));

        workReportType = getJiraTimesheetsWorkReportType();
        typeOfWorkHours = getTypeOfWorkHours();

        workers = getWorkers();
        if (workers == null && workers.isEmpty()) {
            synchronizationInfo.addFailedReason(_("No workers found"));
            return;
        }

        OrderSyncInfo orderSyncInfo = orderSyncInfoDAO
                .findLastSynchronizedInfoByOrderAndConnectorName(order, PredefinedConnectors.JIRA.getName());

        if (orderSyncInfo == null) {
            synchronizationInfo.addFailedReason(
                    _("Order \"{0}\" not found. Order probalbly not synchronized", order.getName()));
            return;
        }
        if (StringUtils.isBlank(orderSyncInfo.getKey())) {
            synchronizationInfo.addFailedReason(_("Key for Order \"{0}\" is empty", order.getName()));
            return;
        }

        String code = order.getCode() + "-" + orderSyncInfo.getKey();

        WorkReport workReport = updateOrCreateWorkReport(code);

        for (IssueDTO issue : issues) {
            WorkLogDTO workLog = issue.getFields().getWorklog();
            if (workLog == null) {
                synchronizationInfo.addFailedReason(_("No worklogs found for \"{0}\" key", issue.getKey()));
            } else {
                List<WorkLogItemDTO> workLogItems = workLog.getWorklogs();
                if (workLogItems == null || workLogItems.isEmpty()) {
                    synchronizationInfo.addFailedReason(_("No worklog items found for \"{0}\" issue", issue.getKey()));
                } else {

                    String codeOrderElement =
                            PredefinedConnectorProperties.JIRA_CODE_PREFIX + order.getCode() + "-" + issue.getKey();

                    OrderElement orderElement = order.getOrderElement(codeOrderElement);

                    if (orderElement == null) {
                        synchronizationInfo.addFailedReason(_("Order element \"{0}\" not found", code));
                    } else {
                        updateOrCreateWorkReportLineAndAddToWorkReport(workReport, orderElement, workLogItems);
                    }
                }
            }
        }

        saveWorkReportIfNotEmpty();
    }

    private void saveWorkReportIfNotEmpty() {
        if (workReportModel.getWorkReport().getWorkReportLines().size() > 0) {
            workReportModel.confirmSave();
        }
    }

    /**
     * Updates {@link WorkReport} if exist, if not creates new one.
     *
     * @param code
     *            search criteria for workReport
     * @return the workReport
     */
    private WorkReport updateOrCreateWorkReport(String code) {
        WorkReport workReport = findWorkReport(code);
        if (workReport == null) {
            workReportModel.initCreate(workReportType);
            workReport = workReportModel.getWorkReport();
            workReport.setCode(code);
        } else {
            workReportModel.initEdit(workReport);
        }
        workReportModel.setCodeAutogenerated(false);

        return workReport;
    }

    /**
     * Updates {@link WorkReportLine} if exist. If not creates new one and adds to <code>workReport</code>.
     *
     * @param workReport
     *            an existing or new created workReport
     * @param orderElement
     *            the orderElement
     * @param workLogItems
     *            jira's workLog items to be added to workReportLine
     */
    private void updateOrCreateWorkReportLineAndAddToWorkReport(WorkReport workReport,
                                                                OrderElement orderElement,
                                                                List<WorkLogItemDTO> workLogItems) {

        for (WorkLogItemDTO workLogItem : workLogItems) {
            Resource resource = getWorker(workLogItem.getAuthor().getName());
            if (resource == null) {
                continue;
            }

            WorkReportLine workReportLine;
            String code = orderElement.getCode() + "-" + workLogItem.getId();

            try {
                workReportLine = workReport.getWorkReportLineByCode(code);
            } catch (InstanceNotFoundException e) {
                workReportLine = WorkReportLine.create(workReport);
                workReport.addWorkReportLine(workReportLine);
                workReportLine.setCode(code);
            }

            updateWorkReportLine(workReportLine, orderElement, workLogItem, resource);
        }

    }

    /**
     * Updates {@link WorkReportLine} with <code>workLogItem</code>.
     *
     * @param workReportLine
     *            workReportLine to be updated
     * @param orderElement
     *            the orderElement
     * @param workLogItem
     *            workLogItem to update the workReportLine
     * @param resource
     *            the resource
     */
    private void updateWorkReportLine(WorkReportLine workReportLine,
                                      OrderElement orderElement,
                                      WorkLogItemDTO workLogItem,
                                      Resource resource) {

        int timeSpent = workLogItem.getTimeSpentSeconds();

        workReportLine.setDate(workLogItem.getStarted());
        workReportLine.setResource(resource);
        workReportLine.setOrderElement(orderElement);
        workReportLine.setEffort(EffortDuration.seconds(timeSpent));
        workReportLine.setTypeOfWorkHours(typeOfWorkHours);

        updateOrCreateDescriptionValuesAndAddToWorkReportLine(workReportLine, workLogItem.getComment());
    }

    /**
     * Updates {@link DescriptionValue} if exist. if not creates new one and adds to <code>workReportLine</code>.
     *
     * @param workReportLine
     *            workReprtLinew where descriptionvalues to be added to
     * @param comment
     *            the description value
     */
    private void updateOrCreateDescriptionValuesAndAddToWorkReportLine(WorkReportLine workReportLine, String comment) {
        DescriptionField descriptionField = workReportType.getLineFields().iterator().next();

        Integer maxLength = descriptionField.getLength();
        if (comment.length() > maxLength) {
            comment = comment.substring(0, maxLength - 1);
        }

        Set<DescriptionValue> descriptionValues = workReportLine.getDescriptionValues();
        if (descriptionValues.isEmpty()) {
            descriptionValues.add(DescriptionValue.create(descriptionField.getFieldName(), comment));
        } else {
            descriptionValues.iterator().next().setValue(comment);
        }

        workReportLine.setDescriptionValues(descriptionValues);
    }

    /**
     * Returns {@link WorkReportType} for JIRA connector.
     *
     * @return WorkReportType for JIRA connector
     */
    private WorkReportType getJiraTimesheetsWorkReportType() {
        WorkReportType workReportType;
        try {
            workReportType = workReportTypeDAO.findUniqueByName(PredefinedWorkReportTypes.JIRA_TIMESHEETS.getName());
        } catch (NonUniqueResultException | InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        return workReportType;
    }

    /**
     * Returns {@link TypeOfWorkHours} configured for JIRA connector.
     *
     * @return TypeOfWorkHours for JIRA connector
     * @throws ConnectorException
     */
    private TypeOfWorkHours getTypeOfWorkHours() throws ConnectorException {
        Connector connector = connectorDAO.findUniqueByName(PredefinedConnectors.JIRA.getName());
        if (connector == null) {
            throw new ConnectorException(_("JIRA connector not found"));
        }

        TypeOfWorkHours typeOfWorkHours;
        String name = connector.getPropertiesAsMap().get(PredefinedConnectorProperties.JIRA_HOURS_TYPE);

        if (StringUtils.isBlank(name)) {
            throw new ConnectorException(_("Hours type should not be empty to synchronine timesheets"));
        }

        try {
            typeOfWorkHours = typeOfWorkHoursDAO.findUniqueByName(name);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }

        return typeOfWorkHours;
    }

    /**
     * Searches for {@link WorkReport} for the specified parameter <code>code</code>.
     *
     * @param code
     *            unique code
     * @return workReportType if found, null otherwise
     */
    private WorkReport findWorkReport(String code) {
        try {
            return workReportDAO.findByCode(code);
        } catch (InstanceNotFoundException e) {
        }
        return null;
    }


    /**
     * Gets all LibrePlan workers.
     *
     * @return list of workers
     */
    private List<Worker> getWorkers() {
        return workerDAO.findAll();
    }

    /**
     * Searches for {@link Worker} for the specified parameter <code>nif</code>.
     *
     * @param nif
     *            unique id
     * @return worker if found, null otherwise
     */
    private Worker getWorker(String nif) {
        for (Worker worker : workers) {
            if (worker.getNif().equals(nif)) {
                return worker;
            }
        }
        synchronizationInfo.addFailedReason(_("Worker \"{0}\" not found", nif));
        return null;
    }


    @Override
    public SynchronizationInfo getSynchronizationInfo() {
        return synchronizationInfo;
    }
}
