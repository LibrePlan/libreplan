package org.libreplan.business.email.entities;

import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.settings.entities.Language;

/**
 * EmailTemplate entity, represents a template that LibrePlan user may use.
 *
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 29.09.15.
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
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
}
