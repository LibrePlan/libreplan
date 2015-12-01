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

package org.libreplan.importers;

import org.libreplan.business.common.Configuration;
import org.libreplan.business.common.daos.IConnectorDAO;
import org.libreplan.business.common.entities.Connector;
import org.libreplan.business.common.entities.ConnectorProperty;
import org.libreplan.business.email.entities.EmailNotification;
import org.libreplan.business.email.entities.EmailTemplate;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.settings.entities.Language;
import org.libreplan.web.email.IEmailNotificationModel;

import org.libreplan.web.email.IEmailTemplateModel;
import org.libreplan.web.planner.tabs.MultipleTabsPlannerController;
import org.libreplan.web.resources.worker.IWorkerModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.Session;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.NoSuchProviderException;


import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Sends E-mail to users with data that storing in notification_queue table
 *
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 13.10.15.
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SendEmail implements ISendEmail {

    @Autowired
    private IEmailNotificationModel emailNotificationModel;

    @Autowired
    private IConnectorDAO connectorDAO;

    @Autowired
    private IWorkerModel workerModel;

    @Autowired
    private IEmailTemplateModel emailTemplateModel;

    private List<EmailNotification> notifications;
    private List<EmailTemplate> emailTemplates;

    @Override
    public void sendEmail() {
        if ( Configuration.isEmailSendingEnabled() == true ){
            if (validConnection() == true){
                notifications = emailNotificationModel.getAll();
                for (int i = 0; i < notifications.size(); i++) composeMessageForUser(notifications.get(i));
                deleteAllNotificationsAfterSending();
            }
        }
    }

    private void composeMessageForUser(EmailNotification notification){

        // Gather data about EmailTemplate needs to be used
        Resource resource = notification.getResource();
        EmailTemplateEnum type = notification.getType();
        Locale locale;
        Worker currentWorker = getCurrentWorker(resource.getId());

        if ( currentWorker.getUser().getApplicationLanguage().equals(Language.BROWSER_LANGUAGE) ) {
            locale = new Locale(System.getProperty("user.language"));
        } else {
            locale = new Locale(currentWorker.getUser().getApplicationLanguage().getLocale().getLanguage());
        }

        EmailTemplate currentEmailTemplate = findCurrentEmailTemplate(type, locale);


        // Modify text that will be composed
        String text = currentEmailTemplate.getContent();

        if ( type.equals(EmailTemplateEnum.TEMPLATE_TASK_ASSIGNED_TO_RESOURCE) ){
            text = text.replaceAll("\\{username\\}", currentWorker.getUser().getLoginName());
            text = text.replaceAll("\\{firstname\\}", currentWorker.getUser().getFirstName());
            text = text.replaceAll("\\{lastname\\}", currentWorker.getUser().getLastName());
            text = text.replaceAll("\\{project\\}", notification.getProject().getName());
            text = text.replaceAll("\\{resource\\}", notification.getResource().getName());
            text = text.replaceAll("\\{task\\}", notification.getTask().getName());
            text = text.replaceAll("\\{url\\}", MultipleTabsPlannerController.WELCOME_URL);
        }

        // Get/Set connection properties
        List<ConnectorProperty> emailConnectorProperties = getEmailConnectorProperties();

        String receiver = currentWorker.getUser().getEmail();
        String protocol = null;
        String host = null;
        String port = null;
        String sender = null;
        String usrnme = null;
        String psswrd = null;

        for (int i = 0; i < emailConnectorProperties.size(); i++){
            switch (i){
                case 1: {
                    protocol = emailConnectorProperties.get(1).getValue();
                    break;
                }
                case 2: {
                    host = emailConnectorProperties.get(2).getValue();
                    break;
                }
                case 3: {
                    port = emailConnectorProperties.get(3).getValue();
                    break;
                }
                case 4: {
                    sender = emailConnectorProperties.get(4).getValue();
                    break;
                }
                case 5: {
                    usrnme = emailConnectorProperties.get(5).getValue();
                    break;
                }
                case 6: {
                    psswrd = emailConnectorProperties.get(6).getValue();
                    break;
                }
            }
        }

        // Set properties of connection
        Properties properties = new Properties();

        if ( protocol.equals("STARTTLS") ) {
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.socketFactory.port", port);
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.port", port);
        }
        else if ( protocol.equals("SMTP") ) {
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);
        }

        final String username = usrnme;
        final String password = psswrd;

        /* It is very important to use Session.getInstance instead of Session.getDefaultInstance  */
        Session mailSession = Session.getInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        // Send message
            try{
                MimeMessage message = new MimeMessage(mailSession);

                message.setFrom(new InternetAddress(sender));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));

                String subject = currentEmailTemplate.getSubject();
                message.setSubject(subject);

                message.setText(text);

                Transport.send(message);



            } catch (MessagingException e){throw new RuntimeException(e);}

    }

    private void deleteAllNotificationsAfterSending(){
        emailNotificationModel.deleteAll();
    }

    private List<ConnectorProperty> getEmailConnectorProperties() {

        Connector connector = connectorDAO.findUniqueByName("E-mail");

        List<ConnectorProperty> properties = connector.getProperties();

        return properties;
    }

    private EmailTemplate findCurrentEmailTemplate(EmailTemplateEnum templateEnum, Locale locale){
        emailTemplates = emailTemplateModel.getAll();
        for (EmailTemplate item : emailTemplates)
            if ( item.getType().equals(templateEnum) && item.getLanguage().getLocale().equals(locale) )
                return item;
        return null;
    }

    private Worker getCurrentWorker(Long resourceID){
        List<Worker> workerList = workerModel.getWorkers();
        for(int i = 0; i < workerList.size(); i++)
            if ( workerList.get(i).getId().equals(resourceID) )
                return workerList.get(i);
        return null;
    }

    private boolean validConnection(){
        List<ConnectorProperty> emailConnectorProperties = getEmailConnectorProperties();

        String protocol = null;
        String host = null;
        String port = null;
        String usrnme = null;
        String psswrd = null;

        for (int i = 0; i < emailConnectorProperties.size(); i++){
            switch (i){
                case 1: {
                    protocol = emailConnectorProperties.get(1).getValue();
                    break;
                }
                case 2: {
                    host = emailConnectorProperties.get(2).getValue();
                    break;
                }
                case 3: {
                    port = emailConnectorProperties.get(3).getValue();
                    break;
                }
                case 5: {
                    usrnme = emailConnectorProperties.get(5).getValue();
                    break;
                }
                case 6: {
                    psswrd = emailConnectorProperties.get(6).getValue();
                    break;
                }
            }
        }

        // Set properties of connection
        Properties properties = new Properties();

        Transport transport = null;

        try {
            if (protocol.equals("SMTP")) {
                properties.setProperty("mail.smtp.port", port);
                properties.setProperty("mail.smtp.host", host);
                Session session = Session.getInstance(properties, null);

                transport = session.getTransport("smtp");
                if (usrnme.equals("") && psswrd.equals("")) transport.connect();
            } else if (protocol.equals("STARTTLS")) {
                properties.setProperty("mail.smtps.port", port);
                properties.setProperty("mail.smtps.host", host);
                Session session = Session.getInstance(properties, null);

                transport = session.getTransport("smtps");
                if (!usrnme.equals("") && psswrd != null) transport.connect(host, usrnme, psswrd);
            }
            if (transport.isConnected()) return true;

        } catch (NoSuchProviderException e) {}
        catch (MessagingException e) {}

        return false;
    }
}
