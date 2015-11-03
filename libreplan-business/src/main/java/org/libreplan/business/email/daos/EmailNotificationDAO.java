package org.libreplan.business.email.daos;

import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.email.entities.EmailNotification;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 19.10.15.
 */
@Repository
public class EmailNotificationDAO extends GenericDAOHibernate<EmailNotification, Long> implements IEmailNotificationDAO {

    @Override
    public List<EmailNotification> getAll() {
        return list(EmailNotification.class);
    }
}
