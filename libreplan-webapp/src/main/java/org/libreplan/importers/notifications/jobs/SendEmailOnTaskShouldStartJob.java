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

package org.libreplan.importers.notifications.jobs;

import org.libreplan.importers.notifications.IEmailNotificationJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Sends E-mail to users with data that storing in notification_queue table and that are treat to
 * {@link org.libreplan.business.email.entities.EmailTemplateEnum#TEMPLATE_TODAY_TASK_SHOULD_START}.
 *
 * It is used in {@link org.libreplan.web.common.JobSchedulerModel}.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
@SuppressWarnings("unused")
public class SendEmailOnTaskShouldStartJob extends QuartzJobBean {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        ApplicationContext applicationContext = (ApplicationContext)
                context.getJobDetail().getJobDataMap().get("applicationContext");

        IEmailNotificationJob taskShouldStart =
                (IEmailNotificationJob) applicationContext.getBean("sendEmailOnTaskShouldStart");

        taskShouldStart.sendEmail();
    }
}
