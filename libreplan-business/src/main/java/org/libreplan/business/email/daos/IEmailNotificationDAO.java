package org.libreplan.business.email.daos;

import org.libreplan.business.common.daos.IGenericDAO;
import org.libreplan.business.email.entities.EmailNotification;

import java.util.List;

/**
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 19.10.15.
 *
 */
public interface IEmailNotificationDAO extends IGenericDAO<EmailNotification, Long> {
    List<EmailNotification> getAll();
}
