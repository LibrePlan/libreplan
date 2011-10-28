/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.business.planner.limiting.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@LimitingResourceQueueElementDAO}
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class LimitingResourceQueueElementDAO extends
        GenericDAOHibernate<LimitingResourceQueueElement, Long> implements
        ILimitingResourceQueueElementDAO {

    @Override
    public List<LimitingResourceQueueElement> getAll() {
        return list(LimitingResourceQueueElement.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<LimitingResourceQueueElement> getAssigned() {
        Criteria criteria = getSession().createCriteria(LimitingResourceQueueElement.class);
        criteria.add(Restrictions.isNotNull("limitingResourceQueue"));
        criteria.addOrder(Order.asc("creationTimestamp"));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<LimitingResourceQueueElement> getUnassigned() {
        Criteria criteria = getSession().createCriteria(LimitingResourceQueueElement.class);
        criteria.add(Restrictions.isNull("limitingResourceQueue"));
        criteria.addOrder(Order.asc("creationTimestamp"));
        return criteria.list();
    }

}
