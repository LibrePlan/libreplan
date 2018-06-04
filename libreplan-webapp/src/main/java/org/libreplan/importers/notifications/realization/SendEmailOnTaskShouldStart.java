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

package org.libreplan.importers.notifications.realization;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.libreplan.business.common.Configuration;

import org.libreplan.business.email.entities.EmailNotification;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.importers.notifications.ComposeMessage;
import org.libreplan.importers.notifications.EmailConnectionValidator;
import org.libreplan.importers.notifications.IEmailNotificationJob;
import org.libreplan.web.email.IEmailNotificationModel;
import org.libreplan.web.resources.worker.IWorkerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zkplus.spring.SpringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Sends E-mail users with data that storing in notification_queue table
 * and that are treat to {@link EmailTemplateEnum#TEMPLATE_TODAY_TASK_SHOULD_START}.
 * Data will be send if current data equals to start date.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SendEmailOnTaskShouldStart implements IEmailNotificationJob {

    @Autowired
    private IEmailNotificationModel emailNotificationModel;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private ComposeMessage composeMessage;

    @Autowired
    private EmailConnectionValidator emailConnectionValidator;

    @Autowired
    private IWorkerDAO workerDAO;

    /**
     * Transactional here is needed because without this annotation we are getting
     * "LazyInitializationException: could not initialize proxy - no Session" error,
     * when "item.getAllResourceAllocations()" method was called.
     * Earlier this trouble was not present because in Tasks.hbm.xml for joined subclass "Task" field
     * named "resourceAllocations", which has relation "one-to-many" to "ResourceAllocation", lazy was set to "false".
     */
    @Override
    @Transactional
    public void sendEmail() {
        // Gather data
        taskShouldStart();

        if ( Configuration.isEmailSendingEnabled() ) {

            if ( emailConnectionValidator.isConnectionActivated() && emailConnectionValidator.validConnection() ) {

                List<EmailNotification> notifications =
                        emailNotificationModel.getAllByType(EmailTemplateEnum.TEMPLATE_TODAY_TASK_SHOULD_START);

                for (EmailNotification notification : notifications)
                    if ( composeMessageForUser(notification) ) {
                        deleteSingleNotification(notification);
                    }
            }
        }
    }

    @Override
    public boolean composeMessageForUser(EmailNotification notification) {
        return composeMessage.composeMessageForUser(notification);
    }

    private void deleteSingleNotification(EmailNotification notification){
        emailNotificationModel.deleteById(notification);
    }

    @Transactional
    public void taskShouldStart() {
        // Check if current date equals with item date
        DateTime currentDate = new DateTime();
        DateTimeComparator dateTimeComparator = DateTimeComparator.getDateOnlyInstance();

        List<TaskElement> tasks = taskElementDAO.getTaskElementsWithParentsWithoutMilestones();
        for (TaskElement item : tasks) {
            DateTime startDate = new DateTime(item.getStartDate());

            if ( dateTimeComparator.compare(currentDate, startDate) == 0 && item.isLeaf() ) {
                // Get all resources for current task and send them email notification
                sendEmailNotificationAboutTaskShouldStart(item);
            }
        }
    }

    private void sendEmailNotificationAboutTaskShouldStart(TaskElement item) {
        List<ResourceAllocation<?>> resourceAllocations = new ArrayList<>(item.getAllResourceAllocations());

        List<Resource> resources = new ArrayList<>();
        for (ResourceAllocation<?> allocation : resourceAllocations)
            resources.add(allocation.getAssociatedResources().get(0));

        for (Resource resourceItem : resources) {
            Worker currentWorker = workerDAO.getCurrentWorker(resourceItem.getId());

            if (currentWorker != null && (currentWorker.getUser() != null) && currentWorker.getUser().isInRole(UserRole.ROLE_EMAIL_TASK_SHOULD_START)) {
                emailNotificationModel.setNewObject();
                emailNotificationModel.setType(EmailTemplateEnum.TEMPLATE_TODAY_TASK_SHOULD_START);
                emailNotificationModel.setUpdated(new Date());
                emailNotificationModel.setResource(resourceItem);
                emailNotificationModel.setTask(item);
                emailNotificationModel.setProject(item.getTopMost());
                emailNotificationModel.confirmSave();
            }
        }
    }

}
