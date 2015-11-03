package org.libreplan.business.email.daos;

import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.email.entities.EmailTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 24.09.15.
 */
@Repository
public class EmailTemplateDAO extends GenericDAOHibernate<EmailTemplate, Long> implements IEmailTemplateDAO{

    @Override
    public List<EmailTemplate> getAll() {
        return list(EmailTemplate.class);
    }

    @Override
    public String initializeContent() {
        try{
            List<EmailTemplate> emailTemplates = list(EmailTemplate.class);
            for ( int i = 0; i < emailTemplates.size(); i++)
                if ( emailTemplates.get(i).getType().ordinal() == 0 && emailTemplates.get(i).getLanguage().ordinal() == 3)
                    return emailTemplates.get(i).getContent();
        }catch (Exception e){}

        return " ";
    }

    @Override
    public String initializeSubject() {
        try{
            List<EmailTemplate> emailTemplates = list(EmailTemplate.class);
            for ( int i = 0; i < emailTemplates.size(); i++)
                if ( emailTemplates.get(i).getType().ordinal() == 0 && emailTemplates.get(i).getLanguage().ordinal() == 3)
                    return emailTemplates.get(i).getSubject();
        }catch (Exception e){}

        return " ";
    }


    @Override
    public String getContentBySelectedLanguage(int languageOrdinal, int emailTemplateTypeOrdinal) {
        for (int i = 0; i < list(EmailTemplate.class).size(); i++)
            if (list(EmailTemplate.class).get(i).getLanguage().ordinal() == languageOrdinal &&
                    // emailTemplateTypeOrdinal + 1, because first value is 0
                    list(EmailTemplate.class).get(i).getType().ordinal() == emailTemplateTypeOrdinal + 1)
                return list(EmailTemplate.class).get(i).getContent();
        return "";
    }

    @Override
    public String getContentBySelectedTemplate(int emailTemplateTypeOrdinal, int languageOrdinal) {
        for (int i = 0; i < list(EmailTemplate.class).size(); i++)
            // emailTemplateTypeOrdinal + 1, because first value is 0
            if ( list(EmailTemplate.class).get(i).getType().ordinal() == emailTemplateTypeOrdinal + 1 &&
                    list(EmailTemplate.class).get(i).getLanguage().ordinal() == languageOrdinal )
                return list(EmailTemplate.class).get(i).getContent();
        return "";
    }

}
