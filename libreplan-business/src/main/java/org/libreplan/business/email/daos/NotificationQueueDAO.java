package org.libreplan.business.email.daos;

import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.email.entities.NotificationQueue;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 19.10.15.
 */
@Repository
public class NotificationQueueDAO extends GenericDAOHibernate<NotificationQueue, Long> implements INotificationQueueDAO {

    @Override
    public List<NotificationQueue> getAll() {
        return list(NotificationQueue.class);
    }
}
