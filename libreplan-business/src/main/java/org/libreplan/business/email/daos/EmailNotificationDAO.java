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

import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.email.entities.EmailNotification;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Dao for {@link EmailNotification}
 *
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

    @Override
    public boolean deleteAll() {
        List<EmailNotification> notifications = list(EmailNotification.class);
        for (Object item : notifications){
            getSession().delete(item);
        }

        if ( list(EmailNotification.class).size() == 0 ) return true;
        return false;
    }

}
