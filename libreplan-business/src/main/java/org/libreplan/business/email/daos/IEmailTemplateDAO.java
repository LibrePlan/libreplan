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

import java.util.List;

/**
 * DAO interface for the <code>EmailTemplate</code> entity.
 * Contract for {@link EmailTemplateDAO}
 *
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 29.09.15.
 */
public interface IEmailTemplateDAO extends IGenericDAO<EmailTemplate, Long>{

    List<EmailTemplate> getAll();

    String initializeContent();
    String initializeSubject();

    String getContentBySelectedLanguage(int languageOrdinal, int emailTemplateTypeOrdinal);
    String getContentBySelectedTemplate(int emailTemplateTypeOrdinal, int languageOrdinal);
}
