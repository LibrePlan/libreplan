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
import org.libreplan.business.email.entities.EmailTemplate;
import org.libreplan.business.settings.entities.Language;
import org.libreplan.business.email.entities.EmailTemplateEnum;

import java.util.List;

/**
 * Contract for {@link EmailTemplate}
 *
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 28.09.15.
 */
public interface IEmailTemplateModel {

    void confirmSave() throws ValidationException, InstanceNotFoundException;

    List<EmailTemplate> getAll();

    String initializeContent();
    String initializeSubject();

    String getContentBySelectedLanguage(int languageOrdinal, int emailTemplateTypeOrdinal);
    String getContentBySelectedTemplate(int emailTemplateTypeOrdinal, int languageOrdinal);

    String getSubjectBySelectedLanguage(int languageOrdinal, int emailTemplateTypeOrdinal);
    String getSubjectBySelectedTemplate(int emailTemplateTypeOrdinal, int languageOrdinal);

    String getContent();
    void setContent(String content);

    Language getLanguage();
    void setLanguage(Language language);

    EmailTemplateEnum getEmailTemplateEnum();
    void setEmailTemplateEnum(EmailTemplateEnum emailTemplateEnum);

    String getSubject();
    void setSubject(String subject);
}
