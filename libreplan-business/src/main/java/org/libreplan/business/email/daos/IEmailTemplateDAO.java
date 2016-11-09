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

import org.libreplan.business.common.daos.IGenericDAO;
import org.libreplan.business.email.entities.EmailTemplate;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.settings.entities.Language;

import java.util.List;

/**
 * DAO interface for the <code>EmailTemplate</code> entity.
 * Contract for {@link EmailTemplateDAO}.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
public interface IEmailTemplateDAO extends IGenericDAO<EmailTemplate, Long>{

    List<EmailTemplate> getAll();

    List<EmailTemplate> findByType(EmailTemplateEnum emailTemplateEnum);

    EmailTemplate findByTypeAndLanguage(EmailTemplateEnum emailTemplateEnum, Language language);

    void delete(EmailTemplate entity);
}
