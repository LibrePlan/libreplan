package org.libreplan.business.email.entities;

import org.libreplan.business.common.BaseEntity;

import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.resources.entities.Resource;


import java.util.Date;

/**
 * EmailNotification entity representing table: notification_queue
 *
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 19.10.15.
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
