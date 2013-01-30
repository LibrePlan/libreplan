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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.NonUniqueResultException;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.entities.JiraConfiguration;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.costcategories.daos.ITypeOfWorkHoursDAO;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workingday.EffortDuration;
import org.libreplan.business.workreports.daos.IWorkReportDAO;
import org.libreplan.business.workreports.daos.IWorkReportLineDAO;
import org.libreplan.business.workreports.daos.IWorkReportTypeDAO;
import org.libreplan.business.workreports.entities.PredefinedWorkReportTypes;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.libreplan.business.workreports.valueobjects.DescriptionField;
import org.libreplan.business.workreports.valueobjects.DescriptionValue;
import org.libreplan.importers.jira.Issue;
import org.libreplan.importers.jira.WorkLog;
import org.libreplan.importers.jira.WorkLogItem;
import org.libreplan.web.workreports.IWorkReportModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JiraTimesheetSynchronizer implements IJiraTimesheetSynchronizer {

    private JiraSyncInfo jiraSyncInfo;

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
    private IWorkReportLineDAO workReportLineDAO;

    @Autowired
    private IWorkReportModel workReportModel;

    @Autowired
    private ITypeOfWorkHoursDAO typeOfWorkHoursDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IAdHocTransactionService adHocTransactionService;

    @Override
    @Transactional
    public void syncJiraTimesheetWithJiraIssues(List<Issue> issues, Order order) {
        jiraSyncInfo = new JiraSyncInfo();

        workReportType = getJiraTimesheetsWorkReportType();
        typeOfWorkHours = getTypeOfWorkHours();

        workers = getWorkers();
        if (workers == null && workers.isEmpty()) {
            jiraSyncInfo.addSyncFailedReason("No workers found");
            return;
        }

        String code = order.getCode() + "-" + order.getImportedLabel();

        WorkReport workReport = updateOrCreateWorkReport(code);

        for (Issue issue : issues) {
            WorkLog worklog = issue.getFields().getWorklog();
            if (worklog == null) {
                jiraSyncInfo.addSyncFailedReason("No worklogs found for '"
                        + issue.getKey() + "'");
            } else {
                List<WorkLogItem> workLogItems = worklog.getWorklogs();
                if (workLogItems == null || workLogItems.isEmpty()) {
                    jiraSyncInfo
                            .addSyncFailedReason("No worklog items found for '"
                                    + issue.getKey() + "' issue");
                } else {

                    String codeOrderElement = JiraConfiguration.CODE_PREFIX
                            + order.getCode() + "-" + issue.getKey();

                    OrderElement orderElement = order.getOrderElement(codeOrderElement);

                    if (orderElement == null) {
                        jiraSyncInfo.addSyncFailedReason("Order element("
                                + code + ") not found");
                    } else {
                        updateOrCreateWorkReportLineAndAddToWorkReport(workReport, orderElement,
                                workLogItems);
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
     * Updates {@link WorkReport} if exist, if not creates new one
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
     * Updates {@link WorkReportLine} if exist. If not creates new one and adds
     * to <code>workReport</code>
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
            List<WorkLogItem> workLogItems) {

        for (WorkLogItem workLogItem : workLogItems) {
            WorkReportLine workReportLine;
            try {
                workReportLine = workReport
                        .getWorkReportLineByCode(orderElement.getCode() + "-"
                                + workLogItem.getId());
            } catch (InstanceNotFoundException e) {
                workReportLine = WorkReportLine.create(workReport);
                workReport.addWorkReportLine(workReportLine);
            }

            Resource resource = getWorker(workLogItem.getAuthor().getName());
            if (resource != null) {
                updateWorkReportLine(workReportLine, orderElement,
                        workLogItem, resource);
            }
        }

    }

    /**
     * Updates {@link WorkReportLine} with <code>workLogItem</code>
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
            OrderElement orderElement, WorkLogItem workLogItem,
            Resource resource) {

        String code = orderElement.getCode() + "-" + workLogItem.getId();
        int timeSpent = workLogItem.getTimeSpentSeconds().intValue();

        workReportLine.setCode(code);
        workReportLine.setDate(workLogItem.getStarted());
        workReportLine.setResource(resource);
        workReportLine.setOrderElement(orderElement);
        workReportLine.setEffort(EffortDuration.seconds(timeSpent));
        workReportLine.setTypeOfWorkHours(typeOfWorkHours);

        updateOrCreateDescriptionValuesAndAddToWorkReportLine(workReportLine,
                        workLogItem.getComment());
    }

    /**
     * Updates {@link DescriptionValue} if exist. if not creates new one and
     * adds to <code>workReportLine</code>
     *
     * @param workReportLine
     *            workReprtLinew where descriptionvalues to be added to
     * @param comment
     *            the description value
     */
    private void updateOrCreateDescriptionValuesAndAddToWorkReportLine(WorkReportLine workReportLine,
            String comment) {
        Set<DescriptionValue> descriptionValues = new HashSet<DescriptionValue>();
        for (DescriptionField descriptionField : workReportType.getLineFields()) {
            DescriptionValue descriptionValue;
            try {
                descriptionValue = workReportLine
                        .getDescriptionValueByFieldName(descriptionField
                                .getFieldName());
                descriptionValue.setValue(comment.substring(0,
                        Math.min(comment.length(), 254)));
            } catch (InstanceNotFoundException e) {
                descriptionValue = DescriptionValue.create(
                        descriptionField.getFieldName(), comment);
            }
            descriptionValues.add(descriptionValue);
        }
        workReportLine.setDescriptionValues(descriptionValues);
    }

    /**
     * Returns {@link WorkReportType} for JIRA connector
     *
     * @return WorkReportType for JIRA connector
     */
    private WorkReportType getJiraTimesheetsWorkReportType() {
        WorkReportType workReportType;
        try {
            workReportType = workReportTypeDAO
                    .findUniqueByName(PredefinedWorkReportTypes.JIRA_TIMESHEETS
                            .getName());
        } catch (NonUniqueResultException e) {
            throw new RuntimeException(e);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        return workReportType;
    }

    /**
     * Returns {@link TypeOfWorkHours} configured for JIRA connector
     *
     * @return TypeOfWorkHours for JIRA connector
     */
    private TypeOfWorkHours getTypeOfWorkHours() {
        return configurationDAO.getConfiguration().getJiraConfiguration()
                .getJiraConnectorTypeOfWorkHours();
    }

    /**
     * Searches for {@link WorkReport} for the specified parameter
     * <code>code</code>
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
     * Gets all libreplan workers
     *
     * @return list of workers
     */
    private List<Worker> getWorkers() {
        return workerDAO.findAll();
    }

    /**
     * Searches for {@link Worker} for the specified parameter <code>nif</code>
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
        jiraSyncInfo.addSyncFailedReason("Worker('" + nif + "') not found");
        return null;
    }


    @Override
    public JiraSyncInfo getJiraSyncInfo() {
        return jiraSyncInfo;
    }
}
