package org.libreplan.web.email;

import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.email.entities.EmailNotification;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.resources.entities.Resource;

import java.util.Date;
import java.util.List;

/**
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 21.10.15.
 */
public interface IEmailNotificationModel {

    void confirmSave() throws ValidationException;

    List<EmailNotification> getAll();

    void setType(EmailTemplateEnum type);
    void setUpdated(Date date);
    void setResource(Resource resource);
    void setTask(TaskElement task);
    void setProject(TaskElement project);

}
