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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.daos.IJobSchedulerConfigurationDAO;
import org.libreplan.business.common.entities.JobSchedulerConfiguration;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.JobDetailBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SchedulerManager implements ISchedulerManager {

    private static final Log LOG = LogFactory.getLog(SchedulerManager.class);

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private IImportRosterFromTim importRosterFromTim;

    @Autowired
    private IExportTimesheetsToTim exportTimesheetsToTim;

    @Autowired
    private IJobSchedulerConfigurationDAO jobSchedulerConfigurationDAO;

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void scheduleJobs() {
        List<JobSchedulerConfiguration> jobSchedulerConfigurations = jobSchedulerConfigurationDAO
                .getAll();
        for (JobSchedulerConfiguration conf : jobSchedulerConfigurations) {
            CronTriggerBean cronTriggerBean = createCronTriggerBean(
                    conf.getTriggerGroup(), conf.getTriggerName(),
                    conf.getCronExpression());
            if (cronTriggerBean != null) {
                cronTriggerBean.setJobName(conf.getJobName());
                cronTriggerBean.setJobGroup(conf.getJobGroup());
                JobDetailBean jobDetailBean = createJobDetailBean(
                        conf.getJobName(), conf.getJobGroup(),
                        conf.getJobClassName());
                if (jobDetailBean != null) {
                    scheduleJob(jobDetailBean, cronTriggerBean);
                }
            }
        }
    }

    /**
     * Creates and returns {@link CronTriggerBean}
     *
     * @param triggerGroup
     *            the trigger group
     * @param triggerName
     *            the trigger name
     * @param cronExpression
     *            the cron expression string
     */
    private CronTriggerBean createCronTriggerBean(String triggerGroup,
            String triggerName, String cronExpression) {
        CronTriggerBean cronTriggerBean = new CronTriggerBean();
        cronTriggerBean.setGroup(triggerGroup);
        cronTriggerBean.setName(triggerName);
        try {
            cronTriggerBean
                    .setCronExpression(new CronExpression(cronExpression));
            return cronTriggerBean;
        } catch (ParseException e) {
            LOG.error("Unable to parse cron expression", e);
        }
        return null;
    }

    /**
     * Creates and returns {@link JobDetailBean}
     *
     * @param jobName
     *            the job name
     * @param jobGroup
     *            the job group
     * @param jobClassName
     *            the job classname
     */
    private JobDetailBean createJobDetailBean(String jobName, String jobGroup,
            String jobClassName) {
        JobDetailBean jobDetailBean = new JobDetailBean();

        Class jobClass = getJobClass(jobClassName);
        if (jobClass == null) {
            LOG.error("JobClass '" + jobClassName + "' not found");
            return null;
        }
        jobDetailBean.setGroup(jobGroup);
        jobDetailBean.setName(jobName);
        jobDetailBean.setJobClass(jobClass);

        Map<String, Object> jobDataAsMap = new HashMap<String, Object>();
        if (jobDetailBean.getJobClass().getSimpleName()
                .equals("ImportRosterFromTimJob")) {
            jobDataAsMap.put("importRosterFromTim", importRosterFromTim);
        } else {
            jobDataAsMap.put("exportTimesheetsToTim", exportTimesheetsToTim);
        }
        jobDetailBean.setJobDataAsMap(jobDataAsMap);
        return jobDetailBean;
    }

    /**
     * Schedules the job specified by <code>{@link JobDetailBean}</code> and
     * link it with the specified <code>{@link CronTriggerBean}</code>
     *
     * @param jobDetailBean
     *            the jobDetailBean
     * @param cronTriggerBean
     *            the cronTriggerBean
     */
    private void scheduleJob(JobDetailBean jobDetailBean,
            CronTriggerBean cronTriggerBean) {
        try {
            this.scheduler.scheduleJob(jobDetailBean, cronTriggerBean);
        } catch (SchedulerException e) {
            LOG.error("unable to schedule job", e);
        }
    }

    /**
     * returns jobClass based on <code>jobClassName</code> parameter
     *
     * @param jobClassName
     *            job className
     */
    private Class getJobClass(String jobClassName) {
        if (jobClassName.equals("ImportRosterFromTimJob")) {
            return org.libreplan.importers.ImportRosterFromTimJob.class;
        }

        if (jobClassName.equals("ExportTimesheetToTimJob")) {
            return org.libreplan.importers.ExportTimesheetToTimJob.class;
        }

        return null;
    }

    @Override
    public void rescheduleJob(JobSchedulerConfiguration jobSchedulerConfiguration) {
        CronTriggerBean cronTriggerBean = createCronTriggerBean(
                jobSchedulerConfiguration.getTriggerGroup(),
                jobSchedulerConfiguration.getTriggerName(),
                jobSchedulerConfiguration.getCronExpression());
        cronTriggerBean.setName(jobSchedulerConfiguration.getTriggerName());
        cronTriggerBean.setGroup(jobSchedulerConfiguration.getTriggerGroup());
        try {
            cronTriggerBean.setCronExpression(jobSchedulerConfiguration
                    .getCronExpression());
        } catch (ParseException e) {
            throw new RuntimeException("Invalid cron expression");
        }
        cronTriggerBean.setJobName(jobSchedulerConfiguration.getJobName());
        cronTriggerBean.setJobGroup(jobSchedulerConfiguration.getJobGroup());
        try {
            scheduler.rescheduleJob(jobSchedulerConfiguration.getTriggerName(),
                    jobSchedulerConfiguration.getTriggerGroup(),
                    cronTriggerBean);
        } catch (SchedulerException e) {
            throw new RuntimeException("Unable to reschedule the job");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchedulerInfo> getSchedulerInfos() {
        List<JobSchedulerConfiguration> jobSchedulerConfigurations = jobSchedulerConfigurationDAO
                .getAll();
        List<SchedulerInfo> results = new ArrayList<SchedulerInfo>();
        for (JobSchedulerConfiguration jobSchedulerConfiguration : jobSchedulerConfigurations) {
            SchedulerInfo schedulerInfo = new SchedulerInfo();
            schedulerInfo
                    .setJobSchedulerConfiguration(jobSchedulerConfiguration);
            try {
                CronTrigger trigger = (CronTrigger) scheduler.getTrigger(
                        jobSchedulerConfiguration.getTriggerName(),
                        jobSchedulerConfiguration.getTriggerGroup());
                if (trigger != null) {
                    schedulerInfo.setNextFireTime(trigger.getNextFireTime()
                            .toString());
                }
                results.add(schedulerInfo);
            } catch (SchedulerException e) {
                LOG.error("unable to get the trigger");
            }
        }
        return results;
    }

    @Override
    public void doManual(String jobName) {
        if (jobName.equals("Import roster from Tim")) {
            importRosterFromTim.importRosters();
            return;
        }

        if (jobName.equals("Export timesheet to Tim")) {
            exportTimesheetsToTim.exportTimesheets();
            return;
        }
    }
}
