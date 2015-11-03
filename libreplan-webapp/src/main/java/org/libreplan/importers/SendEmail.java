package org.libreplan.importers;


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

// TODO not importing all packages
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

// TODO not importing all packages
import java.util.*;

/**
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
        /*
        // TODO
        1. check all added classes to identity (Licenses, annotations, comments, ...)
        2. the mvn compile flags -Ddefault.passwordsControl=false -Ddefault.exampleUsersDisabled=false are used to compile the demo version of LibrePlan.
           There must also be a flag like "sendingEmail" that can be set to false to make sure that the demo edition will not become the worlds biggest spammer.
        */

        notifications = emailNotificationModel.getAll();

        for (int i = 0; i < notifications.size(); i++) composeMessageForUser(notifications.get(i));

        deleteAllNotificationsAfterSending();
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
        //List<String> predefinedCommandsForTemplateTaskAssignedToResource;
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
            if (emailConnectorProperties.get(i).getValue() != null)
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
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.socketFactory.port", "465");
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.port", "465");
        }
        else if ( protocol.equals("SMTP") ) {
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);
        }

        final String username = usrnme;
        final String password = psswrd;

        Session mailSession = Session.getDefaultInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        // Send message
        try{
            MimeMessage message = new MimeMessage(mailSession);
            // TODO check from field
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));

            String subject = currentEmailTemplate.getSubject();
            message.setSubject(subject);

            message.setText(text);

            // TODO delete me
            mailSession.setDebug(true);

            Transport.send(message);

        }catch (MessagingException e){throw new RuntimeException(e);}

    }
    private void deleteAllNotificationsAfterSending(){

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
}
