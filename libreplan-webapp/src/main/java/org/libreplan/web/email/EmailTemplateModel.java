package org.libreplan.web.email;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.settings.entities.Language;
import org.libreplan.business.email.daos.IEmailTemplateDAO;
import org.libreplan.business.email.entities.EmailTemplate;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 25.09.15.
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/email/email_templates.zul")
public class EmailTemplateModel implements IEmailTemplateModel {

    @Autowired
    private IEmailTemplateDAO emailTemplateDAO;

    private Language language = Language.ENGLISH_LANGUAGE;

    private EmailTemplateEnum emailTemplateEnum = EmailTemplateEnum.TEMPLATE_TASK_ASSIGNED_TO_RESOURCE;

    private String content;

    private String subject;

    private EmailTemplate emailTemplate = new EmailTemplate();

    @Override
    @Transactional
    public void confirmSave() throws InstanceNotFoundException {

        /* If current EmailTemplate entity (id) is existing in DB than it needs to update.
        *  Else current EmailTemplate entity (id) is creating and getting new values from form.
        */
        List<EmailTemplate> emailTemplates = emailTemplateDAO.getAll();
        EmailTemplate emailTemplateFromDatabase = null;

        for (int i = 0; i < emailTemplates.size(); i++) {
            if ( emailTemplate.getLanguage() == emailTemplates.get(i).getLanguage() &&
                    emailTemplate.getType() == emailTemplates.get(i).getType() ) {
                emailTemplateFromDatabase = emailTemplateDAO.find(emailTemplates.get(i).getId());
            }
        }

        if ( emailTemplateFromDatabase != null ){
            EmailTemplate temporaryEntity = emailTemplate;
            emailTemplate = emailTemplateFromDatabase;

            emailTemplate.setType(temporaryEntity.getType());
            emailTemplate.setLanguage(temporaryEntity.getLanguage());
            emailTemplate.setContent(temporaryEntity.getContent());
            emailTemplate.setSubject(temporaryEntity.getSubject());
        } else {
            EmailTemplate temporaryEntity = emailTemplate;
            emailTemplate = new EmailTemplate();

            emailTemplate.setType(temporaryEntity.getType());
            emailTemplate.setLanguage(temporaryEntity.getLanguage());
            emailTemplate.setContent(temporaryEntity.getContent());
            emailTemplate.setSubject(temporaryEntity.getSubject());
        }

        emailTemplateDAO.save(emailTemplate);
    }

    @Override
    @Transactional
    public List<EmailTemplate> getAll() {
        return emailTemplateDAO.getAll();
    }

    @Override
    public Language getLanguage() {
        return this.emailTemplate.getLanguage();
    }
    @Override
    public void setLanguage(Language language){ this.emailTemplate.setLanguage(language);}

    @Override
    public EmailTemplateEnum getEmailTemplateEnum() {
        return this.emailTemplate.getType();
    }
    @Override
    public void setEmailTemplateEnum(EmailTemplateEnum emailTemplateEnum) {
        this.emailTemplate.setType(emailTemplateEnum);
    }

    @Override
    public String getContent() {
        return this.emailTemplate.getContent();
    }
    @Override
    public void setContent(String content) {
        this.emailTemplate.setContent(content);
    }

    @Override
    public String getSubject() {
        return this.emailTemplate.getSubject();
    }
    @Override
    public void setSubject(String subject) {
        this.emailTemplate.setSubject(subject);
    }

    @Override
    @Transactional
    public String initializeContent() {
        return emailTemplateDAO.initializeContent();
    }

    @Override
    @Transactional
    public String initializeSubject() { return emailTemplateDAO.initializeSubject(); }

    @Override
    @Transactional
    public String getContentBySelectedLanguage(int languageOrdinal, int emailTemplateTypeOrdinal) {
        return emailTemplateDAO.getContentBySelectedLanguage(languageOrdinal, emailTemplateTypeOrdinal);
    }

    @Override
    @Transactional
    public String getContentBySelectedTemplate(int emailTemplateTypeOrdinal, int languageOrdinal) {
        return emailTemplateDAO.getContentBySelectedTemplate(emailTemplateTypeOrdinal, languageOrdinal);
    }
}


