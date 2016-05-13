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

import org.libreplan.business.common.entities.JobSchedulerConfiguration;
import org.quartz.SchedulerException;

/**
 * A manager(client) that dynamically creates jobs and cron-triggers using
 * spring quartz library.
 *
 * The start and destroy of the scheduler itself is managed by the Spring
 * framework. The scheduler starts automatically when the application starts and
 * destroyed when the application stops.
 *
 * This manager (un)schedules the jobs based on the configuration
 * {@link JobSchedulerConfiguration} entity once the scheduler starts.
 *
 * <ul>
 * <li>Schedule job:create job {@link JobDetailFactoryBean} and cron-trigger
 * {@link CronTriggerFactoryBean}, associated the trigger with the job and add it to
 * the scheduler.
 * <li>
 * <li>Delete job: search the job in the scheduler and if found
 * unschedule(delete) the job</li>
 * </ul>
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 */
public interface ISchedulerManager {

    /**
     * Reads the jobs to be scheduled from the {@link JobSchedulerConfiguration}
     * and schedules the jobs based on the cron expression defined for each job
     * in the {@link JobSchedulerConfiguration}
     */
    void scheduleJobs();

    /**
     * Reads the jobs to be scheduled from the specified
     * <code>{@link JobSchedulerConfiguration}</code> and (un)schedule it
     * accordingly
     *
     * In the specified <code>{@link JobSchedulerConfiguration}</code>
     *
     * <ul>
     * <li><code>{@link JobSchedulerConfiguration#getConnectorName()}</code>
     * check if job has a connector and the connector is activated</li>
     * <li><code>{@link JobSchedulerConfiguration#isSchedule()}</code> if true
     * the job would be scheduled, if not job deleted</li>
     * </ul>
     *
     * @param jobSchedulerConfiguration
     *            configuration for job to be (un)scheduled
     * @throws SchedulerException
     *             if unable to (un)schedule
     */
    void scheduleOrUnscheduleJob(JobSchedulerConfiguration jobSchedulerConfiguration) throws SchedulerException;

    /**
     * Deletes the job from the scheduler for the specified job by
     * <code>{@link JobSchedulerConfiguration}</code>, if the job is already in
     * the scheduler
     *
     * @param jobSchedulerConfiguration
     *            configuration for job to be deleted
     * @throws SchedulerException
     *             if unable to delete
     */
    void deleteJob(JobSchedulerConfiguration jobSchedulerConfiguration) throws SchedulerException;

    /**
     * gets the next fire time for the specified job from
     * {@link JobSchedulerConfiguration} if job is already scheduled. This is
     * only neede for UI
     *
     * @param jobSchedulerConfiguration
     *            configuration to check for next fire time
     * @return next fire time or empty string
     */
    String getNextFireTime(JobSchedulerConfiguration jobSchedulerConfiguration);

}
