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
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.settings.entities.Language;

import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.Component;


import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;

import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Comparator;

import static org.libreplan.web.I18nHelper._;

/**
 * Controller for page Edit email templates.
 *
 * @author Created by Vova Perebykivskiy <vova@libreplan-enterprise.com> on 25.09.2015.
 */
public class EmailTemplateController extends GenericForwardComposer{

    @Autowired
    private IUserDAO userDAO;

    private User user;

    private IEmailTemplateModel emailTemplateModel;

    private IMessagesForUser messages;

    private Component messagesContainer;

    private Textbox contentsTextbox;

    private Textbox subjectTextbox;


    public static ListitemRenderer languagesRenderer = new ListitemRenderer() {
        @Override
        public void render(Listitem item, Object data) throws Exception {
            Language language = (Language) data;
            String displayName = language.getDisplayName();
            item.setLabel(displayName);
        }
    };

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        // TODO resolve deprecated
        comp.setVariable("emailTemplateController", this, true);

        messages = new MessagesForUser(messagesContainer);

        // Set default template and language for user.
        // And content and subject for that language & template.
        setUser();
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

    public Language getSelectedLanguage() {
        return emailTemplateModel.getLanguage();
    }

    public void setSelectedLanguage(Language language) {
        emailTemplateModel.setLanguage(language);

        getSubjectDataBySelectedLanguage();
        getContentDataBySelectedLanguage();
    }

    public static ListitemRenderer getLanguagesRenderer() {
        return languagesRenderer;
    }
    public List<Language> getLanguages() {
        List<Language> languages = new LinkedList<Language>(Arrays.asList(Language.values()));
        Collections.sort(languages, new Comparator<Language>() {
            @Override
            public int compare(Language o1, Language o2) {
                if (o1.equals(Language.BROWSER_LANGUAGE)) {
                    return -1;
                }
                if (o2.equals(Language.BROWSER_LANGUAGE)) {
                    return 1;
                }
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });
        languages.remove(0);
        return languages;
    }


    public EmailTemplateEnum getSelectedEmailTemplateEnum() {
        return emailTemplateModel.getEmailTemplateEnum();
    }
    public void setSelectedEmailTemplateEnum(EmailTemplateEnum emailTemplateEnum) {
        emailTemplateModel.setEmailTemplateEnum(emailTemplateEnum);

        getSubjectDataBySelectedTemplate();
        getContentDataBySelectedTemplate();
    }

    public ListitemRenderer getEmailTemplateEnumRenderer() {
        return new ListitemRenderer() {
            @Override
            public void render(Listitem item, Object data) throws Exception {
                EmailTemplateEnum template = (EmailTemplateEnum) data;
                item.setLabel(_(template.getTemplateType()));
                item.setValue(template);
            }
        };
    }
    public List<EmailTemplateEnum> getEmailTemplateEnum() {
        return Arrays.asList(EmailTemplateEnum.values());
    }


    public void setSelectedContent() {
        emailTemplateModel.setContent(contentsTextbox.getValue());
    }

    public void setSelectedSubject() {
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

    @Transactional
    private void setUser() {
        try {
            user = userDAO.findByLoginName(SecurityUtils.getSessionUserLoginName());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
