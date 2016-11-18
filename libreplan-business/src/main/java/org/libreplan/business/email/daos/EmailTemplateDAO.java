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

import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.email.entities.EmailTemplate;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.settings.entities.Language;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * DAO for {@link EmailTemplate}
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
@Repository
public class EmailTemplateDAO extends GenericDAOHibernate<EmailTemplate, Long> implements IEmailTemplateDAO {

    @Override
    @Transactional(readOnly = true)
    public List<EmailTemplate> getAll() {
        return list(EmailTemplate.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailTemplate> findByType(EmailTemplateEnum type) {
        return getSession()
                .createCriteria(EmailTemplate.class)
                .add(Restrictions.eq("type", type))
                .list();
    }

    @Override
    @Transactional(readOnly = true)
    public EmailTemplate findByTypeAndLanguage(EmailTemplateEnum type, Language language) {
        return (EmailTemplate) getSession()
                .createCriteria(EmailTemplate.class)
                .add(Restrictions.eq("type", type))
                .add(Restrictions.eq("language", language))
                .uniqueResult();
    }

    @Override
    @Transactional
    public void delete(EmailTemplate entity) {
        try {
            remove(entity.getId());
        } catch (InstanceNotFoundException ignored) {
        }
    }
}
