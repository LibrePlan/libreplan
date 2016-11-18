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

package org.libreplan.business.orders.daos;

import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderFile;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DAO for {@link OrderFile}.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

@Repository
public class OrderFileDAO extends GenericDAOHibernate<OrderFile, Long> implements IOrderFileDAO {

    @Override
    public List<OrderFile> getAll() {
        return list(OrderFile.class);
    }

    @Override
    public void delete(OrderFile file) {
        try {
            remove(file.getId());
        } catch (InstanceNotFoundException ignored) {
        }
    }

    @Override
    public List<OrderFile> findByParent(OrderElement parent) {
        return getSession()
                .createCriteria(OrderFile.class)
                .add(Restrictions.eq("parent", parent))
                .list();
    }
}
