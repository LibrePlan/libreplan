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

package org.libreplan.importers.notifications;

import org.libreplan.business.common.entities.ConnectorProperty;
import org.libreplan.business.email.entities.EmailNotification;
import org.libreplan.business.email.entities.EmailTemplate;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.settings.entities.Language;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.email.IEmailTemplateModel;
import org.libreplan.web.planner.tabs.MultipleTabsPlannerController;
import org.libreplan.web.resources.worker.IWorkerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zul.Messagebox;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static org.libreplan.web.I18nHelper._;

/**
 * Sends E-mail to users with data that storing in notification_queue table
 * and that are treat to incoming {@link EmailNotification}.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ComposeMessage {

    @Autowired
    private IWorkerModel workerModel;

    @Autowired
    private IEmailTemplateModel emailTemplateModel;

    @Autowired
    private EmailConnectionValidator emailConnectionValidator;

    private String protocol;

    private String host;

    private String port;

    private String sender;

    private String usrnme;

    private String psswrd;

    private Properties properties;


    public boolean composeMessageForUser(EmailNotification notification) {
        // Gather data about EmailTemplate needs to be used
        Resource resource = notification.getResource();
        EmailTemplateEnum type = notification.getType();
        Locale locale;
        Worker currentWorker = getCurrentWorker(resource.getId());

        UserRole currentUserRole = getCurrentUserRole(notification.getType());

        if (currentWorker != null && currentWorker.getUser().isInRole(currentUserRole)) {
            if (currentWorker.getUser().getApplicationLanguage().equals(Language.BROWSER_LANGUAGE)) {
                locale = new Locale(System.getProperty("user.language"));
            } else {
                locale = new Locale(currentWorker.getUser().getApplicationLanguage().getLocale().getLanguage());
            }

            EmailTemplate currentEmailTemplate = findCurrentEmailTemplate(type, locale);

            // Modify text that will be composed
            String text = currentEmailTemplate.getContent();
            text = replaceKeywords(text, currentWorker, notification);

            String receiver = currentWorker.getUser().getEmail();

            setupConnectionProperties();

            final String username = usrnme;
            final String password = psswrd;

            // It is very important to use Session.getInstance() instead of Session.getDefaultInstance()
            Session mailSession = Session.getInstance(properties, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            // Send message
            try {
                MimeMessage message = new MimeMessage(mailSession);

                message.setFrom(new InternetAddress(sender));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));

                String subject = currentEmailTemplate.getSubject();
                message.setSubject(subject);

                message.setText(text);

                Transport.send(message);

                return true;

            } catch (MessagingException e) {
                throw new RuntimeException(e);
            } catch (NullPointerException e) {
                if (receiver == null) {
                    Messagebox.show(
                            _(currentWorker.getUser().getLoginName() + " - this user have not filled E-mail"),
                            _("Error"), Messagebox.OK, Messagebox.ERROR);
                }
            }
        }
        return false;
    }

    private Worker getCurrentWorker(Long resourceID){
        List<Worker> workerList = workerModel.getWorkers();

        for (Worker aWorkerList : workerList) {
            if (aWorkerList.getId().equals(resourceID)) {
                return aWorkerList;
            }
        }

        return null;
    }

    private EmailTemplate findCurrentEmailTemplate(EmailTemplateEnum templateEnum, Locale locale) {
        List<EmailTemplate> emailTemplates;
        emailTemplates = emailTemplateModel.getAll();

        for (EmailTemplate item : emailTemplates) {
            if ( item.getType().equals(templateEnum) && item.getLanguage().getLocale().equals(locale) ) {
                return item;
            }

        }

        return null;
    }

    private String replaceKeywords(String text, Worker currentWorker, EmailNotification notification) {
        String newText = text;

        if ( notification.getType().equals(EmailTemplateEnum.TEMPLATE_ENTER_DATA_IN_TIMESHEET) ) {
            // It is because there is no other data for
            // EmailNotification of TEMPLATE_ENTER_DATA_IN_TIMESHEET notification type
            newText = newText.replaceAll("\\{resource\\}", notification.getResource().getName());
        }
        else {
            newText = newText.replaceAll("\\{username\\}", currentWorker.getUser().getLoginName());
            newText = newText.replaceAll("\\{firstname\\}", currentWorker.getUser().getFirstName());
            newText = newText.replaceAll("\\{lastname\\}", currentWorker.getUser().getLastName());
            newText = newText.replaceAll("\\{project\\}", notification.getProject().getName());
            newText = newText.replaceAll("\\{resource\\}", notification.getResource().getName());
            newText = newText.replaceAll("\\{task\\}", notification.getTask().getName());
            newText = newText.replaceAll("\\{url\\}", MultipleTabsPlannerController.WELCOME_URL);
        }
        return newText;
    }

    private void setupConnectionProperties() {
        List<ConnectorProperty> emailConnectorProperties = emailConnectionValidator.getEmailConnectorProperties();

        for (int i = 0; i < emailConnectorProperties.size(); i++) {
            switch (i) {

                case 1:
                    protocol = emailConnectorProperties.get(1).getValue();
                    break;

                case 2:
                    host = emailConnectorProperties.get(2).getValue();
                    break;

                case 3:
                    port = emailConnectorProperties.get(3).getValue();
                    break;

                case 4:
                    sender = emailConnectorProperties.get(4).getValue();
                    break;

                case 5:
                    usrnme = emailConnectorProperties.get(5).getValue();
                    break;

                case 6:
                    psswrd = emailConnectorProperties.get(6).getValue();
                    break;

                default:
                    break;

            }
        }

        properties = new Properties();

        if ( "STARTTLS".equals(protocol) ) {
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.socketFactory.port", port);
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.port", port);
        }
        else if ( "SMTP".equals(protocol) ) {
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);
        }
    }

    private UserRole getCurrentUserRole(EmailTemplateEnum type) {
        switch (type) {

            case TEMPLATE_TASK_ASSIGNED_TO_RESOURCE:
                return UserRole.ROLE_EMAIL_TASK_ASSIGNED_TO_RESOURCE;

            case TEMPLATE_RESOURCE_REMOVED_FROM_TASK:
                return UserRole.ROLE_EMAIL_RESOURCE_REMOVED_FROM_TASK;

            case TEMPLATE_MILESTONE_REACHED:
                return UserRole.ROLE_EMAIL_MILESTONE_REACHED;

            case TEMPLATE_TODAY_TASK_SHOULD_START:
                return UserRole.ROLE_EMAIL_TASK_SHOULD_START;

            case TEMPLATE_TODAY_TASK_SHOULD_FINISH:
                return UserRole.ROLE_EMAIL_TASK_SHOULD_FINISH;

            case TEMPLATE_ENTER_DATA_IN_TIMESHEET:
                return UserRole.ROLE_EMAIL_TIMESHEET_DATA_MISSING;

            default:
                /* There is no other template */
                return null;
        }
    }

}
