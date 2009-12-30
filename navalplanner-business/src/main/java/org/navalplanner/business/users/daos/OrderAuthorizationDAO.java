/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.business.users.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.users.entities.OrderAuthorization;
import org.springframework.stereotype.Repository;

/**
 * Hibernate DAO for the {@link OrderAuthorization} entity.
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@Repository
public class OrderAuthorizationDAO extends GenericDAOHibernate<OrderAuthorization, Long>
    implements IOrderAuthorizationDAO {

    @Override
    public List<OrderAuthorization> listByOrder(Order order) {
        Criteria c = getSession().createCriteria(OrderAuthorization.class);
        c.add(Restrictions.eq("order", order));
        return c.list();
    }
}
