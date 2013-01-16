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

import java.util.List;

import org.libreplan.business.common.entities.JobSchedulerConfiguration;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.JobDetailBean;

/**
 * A manager(client) that dynamically creates jobs and cron-triggers using
 * spring quartz library.
 * <p>
 * The start and destroy of the scheduler itself is managed by the Spring
 * framework. The scheduler starts automatically when the application starts and
 * destroyed when the application stops. The sole purpose of this manager is to
 * create jobs {@link JobDetailBean} and cron-triggers {@link CronTriggerBean}
 * when the scheduler is started. It links the triggers with the jobs and add
 * them to the scheduler.
 * <p>
 * It also supports the rescheduling of jobs.
 * <p>
 * The SchedulerManager reads the jobs to be scheduled and the cron-triggers to
 * fire the jobs form {@link JobSchedulerConfiguration}. Hence the
 * {@link JobSchedulerConfiguration} must exist with predefined jobs and valid
 * cron-triggers
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public interface ISchedulerManager {

    /**
     * Reads job configuration from the database and schedules the jobs
     */
    void scheduleJobs();

    /**
     * Reschedule the job.
     * <p>
     * Reads the job to be rescheduled from the specified parameter
     * {@link JobSchedulerConfiguration} and reschedule the job
     *
     * @param jobSchedulerConfiguration
     *            the job scheduler configuration
     */
    void rescheduleJob(JobSchedulerConfiguration jobSchedulerConfiguration);

    /**
     * returns the scheduler info list. This is necessary for UI
     *
     * @return
     */
    List<SchedulerInfo> getSchedulerInfos();

    /**
     * To manually execute the job specified by <code>jobName</code>
     *
     * @param jobName
     *            the name of the job to be executed
     */
    void doManual(String jobName);
}
