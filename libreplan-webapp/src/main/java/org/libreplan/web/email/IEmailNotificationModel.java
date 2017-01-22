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

package org.libreplan.web.email;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.email.entities.EmailNotification;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.resources.entities.Resource;

import java.util.Date;
import java.util.List;

/**
 * Contract for {@link EmailNotification}.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
public interface IEmailNotificationModel {

    void confirmSave() throws ValidationException;

    List<EmailNotification> getAll();

    List<EmailNotification> getAllByType(EmailTemplateEnum enumeration);

    List<EmailNotification> getAllByProject(TaskElement taskElement);

    List<EmailNotification> getAllByTask(TaskElement taskElement);

    boolean deleteAll();

    boolean deleteAllByType(EmailTemplateEnum enumeration);

    boolean deleteById(EmailNotification notification);

    boolean deleteByProject(TaskElement taskElement);

    boolean deleteByTask(TaskElement taskElement);

    void setType(EmailTemplateEnum type);

    void setUpdated(Date date);

    void setResource(Resource resource);

    void setTask(TaskElement task);

    void setProject(TaskElement project);

    EmailNotification getEmailNotification();

    void setNewObject();

}
