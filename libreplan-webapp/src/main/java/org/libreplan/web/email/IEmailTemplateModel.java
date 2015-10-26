package org.libreplan.web.email;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.settings.entities.Language;
import org.libreplan.business.email.entities.EmailTemplateEnum;

/**
 * Model E-mail Templates
 *
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 28.09.15.
 */
public interface IEmailTemplateModel {

    void confirmSave() throws ValidationException;

    Language getLanguage();
    void setLanguage(Language language);

    EmailTemplateEnum getEmailTemplateEnum();
    void setEmailTemplateEnum(EmailTemplateEnum emailTemplateEnum);

    String getContent();
    void setContent(String content);
    String initializeContent();
    String getContentBySelectedLanguage(int languageOrdinal, int emailTemplateTypeOrdinal);
    String getContentBySelectedTemplate(int emailTemplateTypeOrdinal, int languageOrdinal);
}
