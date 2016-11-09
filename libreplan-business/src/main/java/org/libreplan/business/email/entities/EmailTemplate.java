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

package org.libreplan.business.email.entities;

import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.settings.entities.Language;

/**
 * EmailTemplate entity, represents a template that LibrePlan user may use.
 * This class is intended to work as a Hibernate component.
 * It represents the E-mail template to be modified by admin and send to user.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
public class EmailTemplate extends BaseEntity {

    private EmailTemplateEnum type = EmailTemplateEnum.TEMPLATE_TASK_ASSIGNED_TO_RESOURCE;

    private Language language = Language.ENGLISH_LANGUAGE;

    private String content;

    private String subject;

    public EmailTemplateEnum getType() {
        return type;
    }

    public void setType(EmailTemplateEnum type) {
        this.type = type;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getContent() {
        return content != null ? content : "";
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSubject() {
        return subject != null ? subject : "";
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
