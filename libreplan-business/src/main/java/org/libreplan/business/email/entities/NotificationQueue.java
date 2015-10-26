package org.libreplan.business.email.entities;

import org.libreplan.business.common.BaseEntity;

import java.math.BigInteger;
import java.util.Date;

/**
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 19.10.15.
 */
public class NotificationQueue extends BaseEntity {

    private Integer type;

    private Date updated;

    private Long resource;

    private Long task;

    private Long project;

    public Integer getType() {
        return type;
    }
    public void setType(Integer type) {
        this.type = type;
    }

    public Date getUpdated() {
        return updated;
    }
    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Long getResource() {
        return resource;
    }
    public void setResource(Long resource) {
        this.resource = resource;
    }

    public Long getTask() {
        return task;
    }
    public void setTask(Long task) {
        this.task = task;
    }

    public Long getProject() {
        return project;
    }
    public void setProject(Long project) {
        this.project = project;
    }
}
