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

package org.libreplan.business.email.daos;

import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.email.entities.EmailTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DAO for {@link EmailTemplate}
 *
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
                // language.ordinal.equals(3) - English
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
                // language.ordinal.equals(3) - English
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
