package org.libreplan.web.email;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.email.daos.EmailNotificationDAO;
import org.libreplan.business.email.daos.IEmailNotificationDAO;
import org.libreplan.business.email.entities.EmailTemplate;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.email.entities.EmailNotification;
import org.libreplan.business.planner.entities.Task;
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
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 21.10.15.
 *
 */

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EmailNotificationModel implements IEmailNotificationModel {

    @Autowired
    private IEmailNotificationDAO emailNotificationDAO;

    private EmailTemplateEnum type;

    private Date updated;

    private Resource resource;

    private TaskElement task;

    private TaskElement project;

    private EmailNotification emailNotification = new EmailNotification();

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

}
