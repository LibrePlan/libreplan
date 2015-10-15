package org.libreplan.web.templates;

import org.libreplan.business.settings.entities.Language;
import org.libreplan.business.templates.daos.IEmailTemplateDAO;
import org.libreplan.business.templates.entities.EmailTemplate;
import org.libreplan.business.templates.entities.EmailTemplateEnum;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 25.09.15.
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/templates/email_templates.zul")
public class EmailTemplateModel implements IEmailTemplateModel {

    @Autowired
    private IEmailTemplateDAO emailTemplateDAO;

    private Language language = Language.ENGLISH_LANGUAGE;

    private EmailTemplateEnum emailTemplateEnum = EmailTemplateEnum.TEMPLATE_TASK_ASSIGNED_TO_RESOURCE;

    private String content;

    private EmailTemplate emailTemplate;

    @Override
    @Transactional
    public void confirmSave(){
        emailTemplate = new EmailTemplate();

        // + 1 because first ordinal = 0
        emailTemplate.setType(emailTemplateEnum.ordinal() + 1);
        emailTemplate.setLanguage(language.ordinal());
        emailTemplate.setContent(content);

        emailTemplateDAO.save(emailTemplate);
    }

    @Override
    public Language getLanguage() {
        return language;
    }
    @Override
    public void setLanguage(Language language){ this.language = language; }

    @Override
    public EmailTemplateEnum getEmailTemplateEnum() {
        return emailTemplateEnum;
    }
    @Override
    public void setEmailTemplateEnum(EmailTemplateEnum emailTemplateEnum) {
        this.emailTemplateEnum = emailTemplateEnum;
    }

    @Override
    public String getContent() {
        return content;
    }
    @Override
    public void setContent(String content) {
        this.content = content;
    }
    @Override
    @Transactional
    public String initializeContent() {
        return emailTemplateDAO.initializeContent();
    }

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


