/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 Igalia, S.L.
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

package org.libreplan.web.common;

import java.util.ArrayList;
import java.util.List;

import org.libreplan.business.common.daos.IConnectorDAO;
import org.libreplan.business.common.daos.IJobSchedulerConfigurationDAO;
import org.libreplan.business.common.entities.Connector;
import org.libreplan.business.common.entities.ConnectorException;
import org.libreplan.business.common.entities.JobClassNameEnum;
import org.libreplan.business.common.entities.JobSchedulerConfiguration;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.importers.IExportTimesheetsToTim;
import org.libreplan.importers.IImportRosterFromTim;
import org.libreplan.importers.IJiraOrderElementSynchronizer;
import org.libreplan.importers.ISchedulerManager;
import org.libreplan.importers.SynchronizationInfo;
import org.libreplan.importers.notifications.IEmailNotificationJob;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.libreplan.web.I18nHelper._;

/**
 * Model for UI operations related to {@link JobSchedulerConfiguration}.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/common/jobScheduling.zul")
public class JobSchedulerModel implements IJobSchedulerModel {

    private JobSchedulerConfiguration jobSchedulerConfiguration;

    @Autowired
    private ISchedulerManager schedulerManager;

    @Autowired
    private IJobSchedulerConfigurationDAO jobSchedulerConfigurationDAO;

    @Autowired
    private IConnectorDAO connectorDAO;

    @Autowired
    private IImportRosterFromTim importRosterFromTim;

    @Autowired
    private IExportTimesheetsToTim exportTimesheetsToTim;

    @Autowired
    private IJiraOrderElementSynchronizer jiraOrderElementSynchronizer;

    @Qualifier("sendEmailOnTaskAssignedToResource")
    @Autowired
    private IEmailNotificationJob taskAssignedToResource;

    @Qualifier("sendEmailOnMilestoneReached")
    @Autowired
    private IEmailNotificationJob milestoneReached;

    @Qualifier("sendEmailOnResourceRemovedFromTask")
    @Autowired
    private IEmailNotificationJob resourceRemovedFromTask;

    @Qualifier("sendEmailOnTaskShouldStart")
    @Autowired
    private IEmailNotificationJob taskShouldStart;

    @Qualifier("sendEmailOnTaskShouldFinish")
    @Autowired
    private IEmailNotificationJob taskShouldFinish;

    @Qualifier("sendEmailOnTimesheetDataMissing")
    @Autowired
    private IEmailNotificationJob timesheetDataMissing;

    private List<SynchronizationInfo> synchronizationInfos = new ArrayList<>();


    @Override
    @Transactional(readOnly = true)
    public List<JobSchedulerConfiguration> getJobSchedulerConfigurations() {
        return jobSchedulerConfigurationDAO.getAll();
    }

    @Override
    public String getNextFireTime(JobSchedulerConfiguration jobSchedulerConfiguration) {
        return schedulerManager.getNextFireTime(jobSchedulerConfiguration);
    }

    @Override
    public void doManual(JobSchedulerConfiguration jobSchedulerConfiguration) throws ConnectorException {
        String name = jobSchedulerConfiguration.getJobClassName().getName();

        if ( name.equals(JobClassNameEnum.IMPORT_ROSTER_FROM_TIM_JOB.getName()) ) {
            synchronizationInfos = importRosterFromTim.importRosters();
            return;
        }

        if ( name.equals(JobClassNameEnum.EXPORT_TIMESHEET_TO_TIM_JOB.getName()) ) {
            synchronizationInfos = exportTimesheetsToTim.exportTimesheets();
            return;
        }

        if ( name.equals(JobClassNameEnum.SYNC_ORDERELEMENTS_WITH_JIRA_ISSUES_JOB.getName()) ) {
            synchronizationInfos = jiraOrderElementSynchronizer.syncOrderElementsWithJiraIssues();
            return;
        }

        if ( name.equals(JobClassNameEnum.SEND_EMAIL_TASK_ASSIGNED_TO_RESOURCE.getName()) ) {
            synchronizationInfos = new ArrayList<>();
            synchronizationInfos.add(new SynchronizationInfo(_("Task assigned to resource emails job")));
            taskAssignedToResource.sendEmail();

            return;
        }

        if ( name.equals(JobClassNameEnum.SEND_EMAIL_RESOURCE_REMOVED_FROM_TASK.getName()) ) {
            synchronizationInfos = new ArrayList<>();
            synchronizationInfos.add(new SynchronizationInfo(_("Resource removed from task job")));
            resourceRemovedFromTask.sendEmail();

            return;
        }

        if ( name.equals(JobClassNameEnum.SEND_EMAIL_MILESTONE_REACHED.getName()) ) {
            synchronizationInfos = new ArrayList<>();
            synchronizationInfos.add(new SynchronizationInfo(_("Milestone reached job")));
            milestoneReached.sendEmail();

            return;
        }

        if ( name.equals(JobClassNameEnum.SEND_EMAIL_TASK_SHOULD_START.getName()) ) {
            synchronizationInfos = new ArrayList<>();
            synchronizationInfos.add(new SynchronizationInfo(_("Task should start job")));
            taskShouldStart.sendEmail();

            return;
        }

        if ( name.equals(JobClassNameEnum.SEND_EMAIL_TASK_SHOULD_FINISH.getName()) ) {
            synchronizationInfos = new ArrayList<>();
            synchronizationInfos.add(new SynchronizationInfo(_("Task should finish job")));
            taskShouldFinish.sendEmail();

            return;
        }

        if ( name.equals(JobClassNameEnum.SEND_EMAIL_TIMESHEET_DATA_MISSING.getName()) ) {
            synchronizationInfos = new ArrayList<>();
            synchronizationInfos.add(new SynchronizationInfo(_("Timesheet data missing job")));
            timesheetDataMissing.sendEmail();

            return;
        }

        throw new RuntimeException("Unknown action");
    }

    @Override
    public List<SynchronizationInfo> getSynchronizationInfos() {
        return synchronizationInfos;
    }

    @Override
    public void initCreate() {
        this.jobSchedulerConfiguration = JobSchedulerConfiguration.create();
    }

    @Override
    public void initEdit(JobSchedulerConfiguration jobSchedulerConfiguration) {
        this.jobSchedulerConfiguration = jobSchedulerConfiguration;
    }

    @Override
    public JobSchedulerConfiguration getJobSchedulerConfiguration() {
        return this.jobSchedulerConfiguration;
    }

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        jobSchedulerConfigurationDAO.save(jobSchedulerConfiguration);
    }

    @Override
    public void cancel() {
        jobSchedulerConfiguration = null;
    }

    @Override
    @Transactional
    public void remove(JobSchedulerConfiguration jobSchedulerConfiguration) {
        try {
            jobSchedulerConfigurationDAO.remove(jobSchedulerConfiguration.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Connector> getConnectors() {
        return connectorDAO.getAll();
    }

    @Override
    public boolean scheduleOrUnscheduleJobs(Connector connector) {

        List<JobSchedulerConfiguration> jobSchedulerConfigurations =
                jobSchedulerConfigurationDAO.findByConnectorName(connector.getName());

        for (JobSchedulerConfiguration jobSchedulerConfiguration : jobSchedulerConfigurations) {
            try {
                schedulerManager.scheduleOrUnscheduleJob(jobSchedulerConfiguration);
            } catch (SchedulerException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean scheduleOrUnscheduleJob() {
        try {
            schedulerManager.scheduleOrUnscheduleJob(jobSchedulerConfiguration);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to schedule job", e);
        }
        return true;
    }

    @Override
    public boolean deleteScheduledJob(JobSchedulerConfiguration jobSchedulerConfiguration) {
        try {
            schedulerManager.deleteJob(jobSchedulerConfiguration);
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to delete job", e);
        }
        return true;
    }

}
