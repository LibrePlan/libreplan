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
import org.libreplan.business.email.daos.IEmailNotificationDAO;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.email.entities.EmailNotification;

import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.resources.entities.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Model for operations related to {@link EmailNotification}.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EmailNotificationModel implements IEmailNotificationModel {

    @Autowired
    private IEmailNotificationDAO emailNotificationDAO;

    private EmailNotification emailNotification;

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        emailNotificationDAO.save(emailNotification);
    }

    @Override
    @Transactional
    public List<EmailNotification> getAll() {
        return emailNotificationDAO.getAll();
    }

    @Override
    @Transactional
    public List<EmailNotification> getAllByType(EmailTemplateEnum enumeration) {
        return emailNotificationDAO.getAllByType(enumeration);
    }

    @Override
    @Transactional
    public List<EmailNotification> getAllByProject(TaskElement taskElement) {
        return emailNotificationDAO.getAllByProject(taskElement);
    }
    @Override
    @Transactional
    public List<EmailNotification> getAllByTask(TaskElement taskElement) {
        return emailNotificationDAO.getAllByTask(taskElement);
    }

    @Override
    @Transactional
    public boolean deleteAll() {
        return emailNotificationDAO.deleteAll();
    }

    @Override
    public boolean deleteAllByType(EmailTemplateEnum enumeration) {
        return emailNotificationDAO.deleteAllByType(enumeration);
    }

    @Override
    @Transactional
    public boolean deleteById(EmailNotification notification){
        return emailNotificationDAO.deleteById(notification);
    }

    @Override
    @Transactional
    public boolean deleteByProject(TaskElement taskElement) {
        return emailNotificationDAO.deleteByProject(taskElement);
    }

    @Override
    @Transactional
    public boolean deleteByTask(TaskElement taskElement) {
        return emailNotificationDAO.deleteByTask(taskElement);
    }

    @Override
    public void setType(EmailTemplateEnum type) {
        this.emailNotification.setType(type);
    }

    @Override
    public void setUpdated(Date updated) {
        this.emailNotification.setUpdated(updated);
    }

    @Override
    public void setResource(Resource resource) {
        this.emailNotification.setResource(resource);
    }

    @Override
    public void setTask(TaskElement task) {
        this.emailNotification.setTask(task);
    }

    @Override
    public void setProject(TaskElement project) {
        this.emailNotification.setProject(project);
    }

    @Override
    public EmailNotification getEmailNotification() {
        return emailNotification;
    }

    @Override
    public void setNewObject(){
        this.emailNotification = new EmailNotification();
    }

}
