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
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Model for operations related to {@link EmailTemplate}.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/email/email_templates.zul")
public class EmailTemplateModel implements IEmailTemplateModel {

    @Autowired
    private IEmailTemplateDAO emailTemplateDAO;

    private EmailTemplate emailTemplate = new EmailTemplate();

    @Override
    @Transactional
    public void confirmSave() {

        /*
         * If current EmailTemplate entity (id) is existing in DB than it needs to be updated.
         * Else current EmailTemplate entity (id) is creating and getting new values from form.
         */
        List<EmailTemplate> emailTemplates = emailTemplateDAO.getAll();
        EmailTemplate emailTemplateFromDatabase = null;
        boolean condition;

        for (EmailTemplate emailTemplate1 : emailTemplates) {

            condition = emailTemplate.getLanguage() == emailTemplate1.getLanguage() &&
                    emailTemplate.getType() == emailTemplate1.getType();

            if ( condition ) {
                try {
                    emailTemplateFromDatabase = emailTemplateDAO.find(emailTemplate1.getId());
                } catch (InstanceNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        if ( emailTemplateFromDatabase != null ) {
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
    public List<EmailTemplate> getAll() {
        return emailTemplateDAO.getAll();
    }

    @Override
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
    public void setContent(String content) {
        this.emailTemplate.setContent(content);
    }

    @Override
    public void setSubject(String subject) {
        this.emailTemplate.setSubject(subject);
    }

    @Override
    public String getContent(Language language, EmailTemplateEnum type) {
        EmailTemplate template = getEmailTemplateByTypeAndLanguage(type, language);
        return template != null ? template.getContent() : "";
    }

    @Override
    public String getSubject(Language language, EmailTemplateEnum type) {
        EmailTemplate template = getEmailTemplateByTypeAndLanguage(type, language);
        return template != null ? template.getSubject() : "";
    }

    @Override
    public EmailTemplate getEmailTemplateByTypeAndLanguage(EmailTemplateEnum type, Language language) {
        return emailTemplateDAO.findByTypeAndLanguage(type, language);
    }

    @Override
    public void delete() {
        emailTemplateDAO.delete(this.emailTemplate);
    }
}


