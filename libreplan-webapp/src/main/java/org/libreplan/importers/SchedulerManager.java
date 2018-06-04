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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.daos.IConnectorDAO;
import org.libreplan.business.common.daos.IJobSchedulerConfigurationDAO;
import org.libreplan.business.common.entities.Connector;
import org.libreplan.business.common.entities.JobClassNameEnum;
import org.libreplan.business.common.entities.JobSchedulerConfiguration;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of scheduler manager
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SchedulerManager implements ISchedulerManager {

    private static final Log LOG = LogFactory.getLog(SchedulerManager.class);

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private IJobSchedulerConfigurationDAO jobSchedulerConfigurationDAO;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private IConnectorDAO connectorDAO;


    /**
     * Suffix for trigger -group and -name
     */
    private static final String TRIGGER_SUFFIX = "-TRIGGER";

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void scheduleJobs() {
        List<JobSchedulerConfiguration> jobSchedulerConfigurations = jobSchedulerConfigurationDAO.getAll();
        for (JobSchedulerConfiguration conf : jobSchedulerConfigurations) {
            try {
                scheduleOrUnscheduleJob(conf);
            } catch (SchedulerException e) {
                LOG.error("Unable to schedule", e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void scheduleOrUnscheduleJob(JobSchedulerConfiguration jobSchedulerConfiguration) throws SchedulerException {

        if ( hasConnector(jobSchedulerConfiguration.getConnectorName()) ) {
            if ( isConnectorActivated(jobSchedulerConfiguration.getConnectorName()) ) {
                if ( jobSchedulerConfiguration.isSchedule() ) {
                    scheduleNewJob(jobSchedulerConfiguration);

                    return;
                }
            }
            deleteJob(jobSchedulerConfiguration);

            return;
        }

        if ( !jobSchedulerConfiguration.isSchedule() ) {
            deleteJob(jobSchedulerConfiguration);

            return;
        }
        scheduleNewJob(jobSchedulerConfiguration);
    }

    /**
     * Check if {@link JobSchedulerConfiguration} has a connector
     *
     * @param connectorName
     *            the connector to check for
     * @return true if connector is not null or empty
     */
    private boolean hasConnector(String connectorName) {
        return !StringUtils.isBlank(connectorName);
    }

    /**
     * Check if the specified <code>{@link Connector}</code> is activated
     *
     * @param connectorName
     *            the connector to check for activated
     * @return true if activated
     */
    private boolean isConnectorActivated(String connectorName) {
        Connector connector = connectorDAO.findUniqueByName(connectorName);

        return connector != null && connector.isActivated();
    }

    @Override
    public void deleteJob(JobSchedulerConfiguration jobSchedulerConfiguration) throws SchedulerException {
        String triggerName = jobSchedulerConfiguration.getJobName() + TRIGGER_SUFFIX;
        String triggerGroup = jobSchedulerConfiguration.getJobGroup() + TRIGGER_SUFFIX;

        CronTrigger trigger = getTriggerBean(triggerName, triggerGroup);
        if ( trigger == null ) {
            LOG.warn("Trigger not found");

            return;
        }

        if ( isJobCurrentlyExecuting(triggerName, triggerGroup) ) {
            LOG.warn("Job is currently executing...");

            return;
        }

        // deleteJob doesn't work using unscheduleJob
        this.scheduler.unscheduleJob(trigger.getKey());
    }

    /**
     * Checks if job is currently running for the specified
     * <code>triggerName</code> and <code>triggerGroup</code>
     *
     * @param triggerName
     *            the triggerName
     * @param triggerGroup
     *            the triggerGroup
     * @return true if job is currently running, otherwise false
     */
    @SuppressWarnings("unchecked")
    private boolean isJobCurrentlyExecuting(String triggerName, String triggerGroup) {
        try {
            List<JobExecutionContext> currentExecutingJobs = this.scheduler.getCurrentlyExecutingJobs();
            for (JobExecutionContext jobExecutionContext : currentExecutingJobs) {
                String name = jobExecutionContext.getTrigger().getKey().getName();
                String group = jobExecutionContext.getTrigger().getKey().getGroup();
                if ( triggerName.equals(name) && triggerGroup.equals(group) ) {
                    return true;
                }
            }
        } catch (SchedulerException e) {
            LOG.error("Unable to get currently executing jobs", e);
        }

        return false;
    }

    /**
     * Creates {@link CronTriggerFactoryBean} and {@link JobDetailFactoryBean} based on the
     * specified <code>{@link JobSchedulerConfiguration}</code>. First delete
     * job if exist and then schedule it
     *
     * @param jobSchedulerConfiguration
     *            where to reade jobs to be scheduled
     * @throws SchedulerException
     *             if unable to delete and/or schedule job
     */
    private void scheduleNewJob(JobSchedulerConfiguration jobSchedulerConfiguration) throws SchedulerException {
        CronTriggerFactoryBean cronTriggerBean = createCronTriggerBean(jobSchedulerConfiguration);
        if ( cronTriggerBean == null ) {
            return;
        }

        JobDetailFactoryBean jobDetailBean = createJobDetailBean(jobSchedulerConfiguration);
        if ( jobDetailBean == null ) {
            return;
        }

        deleteJob(jobSchedulerConfiguration);
        this.scheduler.scheduleJob(jobDetailBean.getObject(), cronTriggerBean.getObject());
    }

    /**
     * Creates {@link CronTriggerFactoryBean} from the specified
     * <code>{@link JobSchedulerConfiguration}</code>
     *
     * @param jobSchedulerConfiguration
     *            configuration to create <code>CronTriggerFactoryBean</>
     * @return the created <code>CronTriggerFactoryBean</code> or null if unable to
     *         create it
     */
    private CronTriggerFactoryBean createCronTriggerBean(JobSchedulerConfiguration jobSchedulerConfiguration) {
        final CronTriggerFactoryBean cronTriggerBean = new CronTriggerFactoryBean();
        cronTriggerBean.setName(jobSchedulerConfiguration.getJobName() + TRIGGER_SUFFIX);
        cronTriggerBean.setGroup(jobSchedulerConfiguration.getJobGroup() + TRIGGER_SUFFIX);

        try {
            cronTriggerBean.setCronExpression(
                    String.valueOf(new CronExpression(jobSchedulerConfiguration.getCronExpression())));

            cronTriggerBean.afterPropertiesSet();
            
            return cronTriggerBean;

        } catch (ParseException e) {
            LOG.error("Unable to parse cron expression", e);
        }

        return null;
    }

    /**
     * Creates {@link JobDetailFactoryBean} from the specified
     * <code>{@link JobSchedulerConfiguration}</code>
     *
     * @param jobSchedulerConfiguration
     *            configuration to create <code>JobDetailFactoryBean</>
     * @return the created <code>JobDetailFactoryBean</code> or null if unable to it
     */
    private JobDetailFactoryBean createJobDetailBean(JobSchedulerConfiguration jobSchedulerConfiguration) {
        final JobDetailFactoryBean jobDetailBean = new JobDetailFactoryBean();

        Class<?> jobClass = getJobClass(jobSchedulerConfiguration.getJobClassName());
        if ( jobClass == null ) {
            return null;
        }

        jobDetailBean.setName(jobSchedulerConfiguration.getJobName());
        jobDetailBean.setGroup(jobSchedulerConfiguration.getJobGroup());
        jobDetailBean.setJobClass(jobClass);

        Map<String, Object> jobDataAsMap = new HashMap<>();
        jobDataAsMap.put("applicationContext", applicationContext);
        jobDetailBean.setJobDataAsMap(jobDataAsMap);

        jobDetailBean.afterPropertiesSet();

        return jobDetailBean;
    }


    /**
     * returns jobClass based on <code>jobClassName</code> parameter
     *
     * @param jobClassName
     *            job className
     */
    private Class<?> getJobClass(JobClassNameEnum jobClassName) {
        try {
            return Class.forName(jobClassName.getPackageName() + "." + jobClassName.getName());
        } catch (ClassNotFoundException e) {
            LOG.error("Unable to get class object '" + jobClassName + "'", e);
        }

        return null;
    }

    @Override
    public String getNextFireTime(JobSchedulerConfiguration jobSchedulerConfiguration) {
        try {
            CronTrigger trigger = (CronTrigger)
                    this.scheduler.getTrigger(TriggerKey.triggerKey(
                            jobSchedulerConfiguration.getJobName() + TRIGGER_SUFFIX,
                            jobSchedulerConfiguration.getJobGroup() + TRIGGER_SUFFIX));

            if ( trigger != null ) {
                return trigger.getNextFireTime().toString();
            }
        } catch (SchedulerException e) {
            LOG.error("unable to get the trigger", e);
        }

        return "";
    }

    /**
     * gets the {@link CronTriggerFactoryBean} for the specified
     * <code>triggerName</code> and <code>tirggerGroup</code>
     *
     * @param triggerName
     *            the trigger name
     * @param triggerGroup
     *            the trigger group
     * @return CronTriggerFactoryBean if found, otherwise null
     */
    private CronTrigger getTriggerBean(String triggerName, String triggerGroup) {
        try {
            return (CronTrigger) this.scheduler.getTrigger(TriggerKey.triggerKey(triggerName, triggerGroup));
        } catch (SchedulerException e) {
            LOG.error("Unable to get job trigger", e);
        }

        return null;
    }

}

