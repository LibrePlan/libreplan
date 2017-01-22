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

package org.libreplan.business.email.daos;

import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.email.entities.EmailNotification;
import org.libreplan.business.email.entities.EmailTemplateEnum;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.entities.TaskElement;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

//import java.util.ArrayList;
import java.util.List;

/**
 * DAO for {@link EmailNotification}.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */
@Repository
public class EmailNotificationDAO
        extends GenericDAOHibernate<EmailNotification, Long>
        implements IEmailNotificationDAO {

    @Override
    public List<EmailNotification> getAll() {
        return list(EmailNotification.class);
    }

    @Override
    public List<EmailNotification> getAllByType(EmailTemplateEnum enumeration) {
        return getSession()
                .createCriteria(EmailNotification.class)
                .add(Restrictions.eq("type", enumeration))
                .list();
    }

    @Override
    public List<EmailNotification> getAllByProject(TaskElement taskElement) {
       return getSession()
               .createCriteria(EmailNotification.class)
               .add(Restrictions.eq("project", taskElement))
               .list();
    }

    @Override
    public List<EmailNotification> getAllByTask(TaskElement taskElement) {
        return getSession()
                .createCriteria(EmailNotification.class)
                .add(Restrictions.eq("task", taskElement))
                .list();
    }

    @Override
    public boolean deleteAll() {
        List<EmailNotification> notifications = list(EmailNotification.class);

        for (Object item : notifications) {
            getSession().delete(item);
        }

        return list(EmailNotification.class).isEmpty();
    }

    @Override
    public boolean deleteAllByType(EmailTemplateEnum enumeration) {
        List<EmailNotification> notifications = getSession()
                .createCriteria(EmailNotification.class)
                .add(Restrictions.eq("type", enumeration))
                .list();

        for (Object item : notifications){
            getSession().delete(item);
        }

        return getSession()
                .createCriteria(EmailNotification.class)
                .add(Restrictions.eq("type", enumeration.ordinal()))
                .list()
                .isEmpty();
    }

    @Override
    public boolean deleteById(EmailNotification notification) {
        getSession().delete(notification);

        return getSession()
                .createCriteria(EmailNotification.class)
                .add(Restrictions.eq("id", notification.getId()))
                .uniqueResult() == null;
    }

    @Override
    public boolean deleteByProject(TaskElement taskElement) {
        List<EmailNotification> notifications = getAllByProject(taskElement);

        for (Object item : notifications){
            getSession().delete(item);
        }

        return getAllByProject(taskElement).isEmpty();
    }

    @Override
    public boolean deleteByTask(TaskElement taskElement) {
        List<EmailNotification> notifications = getAllByTask(taskElement);

        for (Object item : notifications){
            getSession().delete(item);
        }

        return getAllByTask(taskElement).isEmpty();
    }

}
