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

package org.libreplan.web.email;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.settings.entities.Language;
import org.libreplan.business.email.daos.IEmailTemplateDAO;
import org.libreplan.business.email.entities.EmailTemplate;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.libreplan.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Model for operations related to {@link EmailTemplate}.
 *
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

    @Autowired
    private IUserDAO userDAO;

    private Language language = Language.ENGLISH_LANGUAGE;

    private EmailTemplateEnum emailTemplateEnum = EmailTemplateEnum.TEMPLATE_TASK_ASSIGNED_TO_RESOURCE;

    private String content;

    private String subject;

    private User user;

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
    @Transactional
    public Language getLanguage() {
        return this.emailTemplate.getLanguage();
    }
    @Override
    public void setLanguage(Language language){
        this.emailTemplate.setLanguage(language);
    }

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
    public String getContentBySelectedLanguage(int languageOrdinal, int emailTemplateTypeOrdinal) {
        return emailTemplateDAO.getContentBySelectedLanguage(languageOrdinal, emailTemplateTypeOrdinal);
    }

    @Override
    @Transactional
    public String getContentBySelectedTemplate(int emailTemplateTypeOrdinal, int languageOrdinal) {
        return emailTemplateDAO.getContentBySelectedTemplate(emailTemplateTypeOrdinal, languageOrdinal);
    }

    @Override
    @Transactional
    public String getSubjectBySelectedLanguage(int languageOrdinal, int emailTemplateTypeOrdinal) {
        return emailTemplateDAO.getSubjectBySelectedLanguage(languageOrdinal, emailTemplateTypeOrdinal);
    }

    @Override
    @Transactional
    public String getSubjectBySelectedTemplate(int emailTemplateTypeOrdinal, int languageOrdinal) {
        return emailTemplateDAO.getSubjectBySelectedTemplate(emailTemplateTypeOrdinal, languageOrdinal);
    }
}


