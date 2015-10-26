package org.libreplan.web.email;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.email.daos.INotificationQueueDAO;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.email.entities.NotificationQueue;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 21.10.15.
 *
 */

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NotificationQueueModel implements INotificationQueueModel {

    @Autowired
    private INotificationQueueDAO notificationQueueDAO;

    private EmailTemplateEnum type;

    private Date updated;

    private Resource resource;

    private TaskElement task;

    private Long project;

    private NotificationQueue notificationQueue;

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        notificationQueue = new NotificationQueue();

        // + 1 because first ordinal = 0
        notificationQueue.setType(type.ordinal() + 1);
        notificationQueue.setUpdated(updated);
        notificationQueue.setResource(resource.getId());
        notificationQueue.setTask(task.getId());
        notificationQueue.setProject(project);

        notificationQueueDAO.save(notificationQueue);
    }

    @Override
    @Transactional
    public List<NotificationQueue> getAll() {
        return notificationQueueDAO.getAll();
    }

    @Override
    public void setType(EmailTemplateEnum type) {
        this.type = type;
    }

    @Override
    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public void setTask(TaskElement task) {
        this.task = task;
    }

    @Override
    public void setProject(Long project) {
        this.project = project;
    }

}
