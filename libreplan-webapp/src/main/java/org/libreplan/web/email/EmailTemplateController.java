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

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.settings.entities.Language;

import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.zkoss.zk.ui.Component;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;

import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import static org.libreplan.web.I18nHelper._;

/**
 * Controller for page Edit email templates.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

public class EmailTemplateController extends GenericForwardComposer<Component> {

    private IEmailTemplateModel emailTemplateModel;

    private IMessagesForUser messages;

    private Component messagesContainer;

    private Textbox contentsTextbox;

    private Textbox subjectTextbox;

    private static ListitemRenderer languagesRenderer = (item, data, i) -> {
        Language language = (Language) data;
        String displayName = language.getDisplayName();
        item.setLabel(displayName);
    };


    public EmailTemplateController() {
        if ( emailTemplateModel == null ) {
            emailTemplateModel = (IEmailTemplateModel) SpringUtil.getBean("emailTemplateModel");
        }
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setAttribute("emailTemplateController", this, true);
        messages = new MessagesForUser(messagesContainer);

        /*
         * Set default template and language for user.
         * And content and subject for that language & template.
         */
        setSelectedLanguage(Language.ENGLISH_LANGUAGE);

        getContentDataBySelectedLanguage();
        getSubjectDataBySelectedLanguage();
    }

    public boolean save() {
        try {
            setSelectedContent();
            setSelectedSubject();
            emailTemplateModel.confirmSave();
            messages.clearMessages();
            messages.showMessage(Level.INFO, _("E-mail template saved"));

            return true;
        } catch (ValidationException e) {
            messages.showInvalidValues(e);
        }

        return false;
    }

    public void cancel() throws InterruptedException {
        Executions.getCurrent().sendRedirect("../planner/index.zul");
    }

    /**
     * Used in email_templates.zul
     * Should be public!
     */
    public Language getSelectedLanguage() {
        return emailTemplateModel.getLanguage();
    }

    /**
     * Used in email_templates.zul
     * Should be public!
     */
    public void setSelectedLanguage(Language language) {
        emailTemplateModel.setLanguage(language);

        getSubjectDataBySelectedLanguage();
        getContentDataBySelectedLanguage();
    }

    /**
     * Used in email_templates.zul
     * Should be public!
     */
    public static ListitemRenderer getLanguagesRenderer() {
        return languagesRenderer;
    }

    /**
     * Used in email_templates.zul
     * Should be public!
     */
    public List<Language> getLanguages() {
        List<Language> languages = new LinkedList<>(Arrays.asList(Language.values()));
        Collections.sort(languages, (o1, o2) -> {
            if ( o1.equals(Language.BROWSER_LANGUAGE) ) {
                return -1;
            }
            if ( o2.equals(Language.BROWSER_LANGUAGE) ) {
                return 1;
            }
            return o1.getDisplayName().compareTo(o2.getDisplayName());
        });
        languages.remove(0);

        return languages;
    }

    /**
     * Used in email_templates.zul
     * Should be public!
     */
    public EmailTemplateEnum getSelectedEmailTemplateEnum() {
        return emailTemplateModel.getEmailTemplateEnum();
    }

    /**
     * Used in email_templates.zul
     * Should be public!
     */
    public void setSelectedEmailTemplateEnum(EmailTemplateEnum emailTemplateEnum) {
        emailTemplateModel.setEmailTemplateEnum(emailTemplateEnum);

        getSubjectDataBySelectedTemplate();
        getContentDataBySelectedTemplate();
    }

    /**
     * Used in email_templates.zul
     * Should be public!
     */
    public ListitemRenderer getEmailTemplateEnumRenderer() {
        return (item, data, i) -> {
            EmailTemplateEnum template = (EmailTemplateEnum) data;
            item.setLabel(_(template.getTemplateType()));
            item.setValue(template);
        };
    }

    /**
     * Used in email_templates.zul
     * Should be public!
     */
    public List<EmailTemplateEnum> getEmailTemplateEnum() {
        return Arrays.asList(EmailTemplateEnum.values());
    }

    void setSelectedContent() {
        emailTemplateModel.setContent(contentsTextbox.getValue());
    }

    void setSelectedSubject() {
        emailTemplateModel.setSubject(subjectTextbox.getValue());
    }

    private void getContentDataBySelectedLanguage() {
        contentsTextbox.setValue(emailTemplateModel.getContent(getSelectedLanguage(), getSelectedEmailTemplateEnum()));
    }
    private void getContentDataBySelectedTemplate() {
        contentsTextbox.setValue(emailTemplateModel.getContent(getSelectedLanguage(), getSelectedEmailTemplateEnum()));
    }

    private void getSubjectDataBySelectedLanguage() {
        subjectTextbox.setValue(emailTemplateModel.getSubject(getSelectedLanguage(), getSelectedEmailTemplateEnum()));
    }
    private void getSubjectDataBySelectedTemplate() {
        subjectTextbox.setValue(emailTemplateModel.getSubject(getSelectedLanguage(), getSelectedEmailTemplateEnum()));
    }

}
