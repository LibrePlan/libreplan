package org.libreplan.business.email.daos;

import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.email.entities.EmailTemplate;
import org.springframework.stereotype.Repository;

/**
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 24.09.15.
 */
@Repository
public class EmailTemplateDAO extends GenericDAOHibernate<EmailTemplate, Long> implements IEmailTemplateDAO{

    @Override
    public String initializeContent() {
        for ( int i = 0; i < list(EmailTemplate.class).size(); i++)
            if ( list(EmailTemplate.class).get(i).getType() == 1 && list(EmailTemplate.class).get(i).getLanguage() == 3)
                return list(EmailTemplate.class).get(i).getContent();
        return " ";
    }

    @Override
    public String getContentBySelectedLanguage(int languageOrdinal, int emailTemplateTypeOrdinal) {
        for (int i = 0; i < list(EmailTemplate.class).size(); i++)
            if (list(EmailTemplate.class).get(i).getLanguage() == languageOrdinal &&
                    // emailTemplateTypeOrdinal + 1, because first value is 0
                    list(EmailTemplate.class).get(i).getType() == emailTemplateTypeOrdinal + 1)
                return list(EmailTemplate.class).get(i).getContent();
        return "";
    }

    @Override
    public String getContentBySelectedTemplate(int emailTemplateTypeOrdinal, int languageOrdinal) {
        for (int i = 0; i < list(EmailTemplate.class).size(); i++)
            // emailTemplateTypeOrdinal + 1, because first value is 0
            if ( list(EmailTemplate.class).get(i).getType() == emailTemplateTypeOrdinal + 1 &&
                    list(EmailTemplate.class).get(i).getLanguage() == languageOrdinal )
                return list(EmailTemplate.class).get(i).getContent();
        return "";
    }

}
