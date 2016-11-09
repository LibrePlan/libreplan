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

package org.libreplan.business.email.entities;

import org.libreplan.business.common.BaseEntity;

import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.resources.entities.Resource;

import java.util.Date;

/**
 * EmailNotification entity representing table: notification_queue.
 * This class is intended to work as a Hibernate component.
 * It represents the Email notification to be send to user.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
public class EmailNotification extends BaseEntity {

    private EmailTemplateEnum type;

    private Date updated;

    private Resource resource;

    private TaskElement task;

    private TaskElement project;


    public EmailTemplateEnum getType() {
        return type;
    }

    public void setType(EmailTemplateEnum type) {
        this.type = type;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public TaskElement getTask() {
        return task;
    }

    public void setTask(TaskElement task) {
        this.task = task;
    }

    public TaskElement getProject() {
        return project;
    }

    public void setProject(TaskElement project) {
        this.project = project;
    }
}
