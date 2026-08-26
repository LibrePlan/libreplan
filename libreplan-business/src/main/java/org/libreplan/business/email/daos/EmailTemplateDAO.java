/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2015 LibrePlan
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

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
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<EmailTemplate> cq = cb.createQuery(EmailTemplate.class);
        Root<EmailTemplate> root = cq.from(EmailTemplate.class);
        cq.where(cb.equal(root.get("type"), type));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmailTemplate findByTypeAndLanguage(EmailTemplateEnum type, Language language) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<EmailTemplate> cq = cb.createQuery(EmailTemplate.class);
        Root<EmailTemplate> root = cq.from(EmailTemplate.class);
        cq.where(cb.equal(root.get("type"), type), cb.equal(root.get("language"), language));
        return getSession().createQuery(cq).uniqueResult();
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
