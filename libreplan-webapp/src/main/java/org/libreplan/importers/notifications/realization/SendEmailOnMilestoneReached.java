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


import org.joda.time.LocalDate;
import org.libreplan.business.common.Configuration;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.email.entities.EmailNotification;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.importers.notifications.ComposeMessage;
import org.libreplan.importers.notifications.EmailConnectionValidator;
import org.libreplan.importers.notifications.IEmailNotificationJob;
import org.libreplan.web.email.IEmailNotificationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 * Sends E-mail to manager user (it writes in responsible field in project properties)
 * with data that storing in notification_queue table
 * and that are treat to {@link EmailTemplateEnum#TEMPLATE_MILESTONE_REACHED}
 * Date will be send on current date equals to deadline date of {@link org.zkoss.ganttz.data.Milestone}.
 * But it will be only send to Manager (you can assign him in project properties).
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SendEmailOnMilestoneReached implements IEmailNotificationJob {

    @Autowired
    private IEmailNotificationModel emailNotificationModel;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private ComposeMessage composeMessage;

    @Autowired
    EmailConnectionValidator emailConnectionValidator;

    /**
     * Transactional here is needed because without this annotation we are getting
     * "LazyInitializationException: could not initialize proxy - no Session" error,
     * when "item.getParent().getOrderElement().getOrder().getResponsible()" method was called.
     * Earlier this trouble was not present because in Tasks.hbm.xml for "TaskElement" class field
     * named "parent", which has relation "many-to-one" to "TaskGroup", lazy was set to "false".
     */
    @Override
    @Transactional
    public void sendEmail() {
        // Gathering data
        checkMilestoneDate();

        if ( Configuration.isEmailSendingEnabled() ) {

            if ( emailConnectionValidator.isConnectionActivated() && emailConnectionValidator.validConnection() ) {

                List<EmailNotification> notifications =
                        emailNotificationModel.getAllByType(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED);

                for (EmailNotification notification : notifications) {
                    if ( composeMessageForUser(notification) ) {
                        deleteSingleNotification(notification);
                    }
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

    private void sendEmailNotificationToManager(TaskElement item) {
        String responsible = "";
        if ( item.getTopMost().getOrderElement().getOrder().getResponsible() != null ) {
            responsible = item.getTopMost().getOrderElement().getOrder().getResponsible();
        }

        User user = null;
        try {
            user = userDAO.findByLoginName(responsible);

            boolean userHasNeededRoles =
                user.isInRole(UserRole.ROLE_SUPERUSER) || user.isInRole(UserRole.ROLE_EMAIL_MILESTONE_REACHED);

	        if ( user.getWorker() != null && userHasNeededRoles ) {
	            emailNotificationModel.setNewObject();
	            emailNotificationModel.setType(EmailTemplateEnum.TEMPLATE_MILESTONE_REACHED);
	            emailNotificationModel.setUpdated(new Date());
	            emailNotificationModel.setResource(user.getWorker());
	            emailNotificationModel.setTask(item);
	            emailNotificationModel.setProject(item.getTopMost());
	            emailNotificationModel.confirmSave();
	        }
	    } catch (InstanceNotFoundException e) {
	        // do nothing, responsible user is either blank or free text in order
	    }
    }

    public void checkMilestoneDate() {
        List<TaskElement> milestones = taskElementDAO.getTaskElementsWithMilestones();

        LocalDate date = new LocalDate();
        int currentYear = date.getYear();
        int currentMonth = date.getMonthOfYear();
        int currentDay = date.getDayOfMonth();

        for (TaskElement item : milestones) {
            if ( item.getDeadline() != null ) {

                LocalDate deadline = item.getDeadline();
                int deadlineYear = deadline.getYear();
                int deadlineMonth = deadline.getMonthOfYear();
                int deadlineDay = deadline.getDayOfMonth();

                if (currentYear == deadlineYear && currentMonth == deadlineMonth && currentDay == deadlineDay) {
                    sendEmailNotificationToManager(item);
                }

            }
        }
    }

}
