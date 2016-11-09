/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2015 LibrePlan
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


import org.libreplan.business.common.Configuration;
import org.libreplan.business.email.entities.EmailNotification;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.importers.notifications.ComposeMessage;
import org.libreplan.importers.notifications.EmailConnectionValidator;
import org.libreplan.importers.notifications.IEmailNotificationJob;
import org.libreplan.web.email.IEmailNotificationModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Sends E-mail to users with data that storing in notification_queue table
 * and that are treat to {@link EmailTemplateEnum#TEMPLATE_ENTER_DATA_IN_TIMESHEET}.
 * Data will be send after user will be assigned to some task.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SendEmailOnTaskAssignedToResource implements IEmailNotificationJob {

    @Autowired
    private IEmailNotificationModel emailNotificationModel;

    @Autowired
    private EmailConnectionValidator emailConnectionValidator;

    @Autowired
    private ComposeMessage composeMessage;

    @Override
    @Transactional
    public void sendEmail() {
        if ( Configuration.isEmailSendingEnabled() ) {

            if ( emailConnectionValidator.isConnectionActivated() && emailConnectionValidator.validConnection() ) {

                List<EmailNotification> notifications =
                        emailNotificationModel.getAllByType(EmailTemplateEnum.TEMPLATE_TASK_ASSIGNED_TO_RESOURCE);

                for (EmailNotification notification : notifications) {
                    if (composeMessageForUser(notification)) {
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

}
