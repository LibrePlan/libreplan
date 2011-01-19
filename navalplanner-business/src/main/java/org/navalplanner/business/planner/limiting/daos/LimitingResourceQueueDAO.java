/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.planner.limiting.daos;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.navalplanner.business.resources.entities.Resource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@LimitingResourceQueueDAO}
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class LimitingResourceQueueDAO extends
        GenericDAOHibernate<LimitingResourceQueue, Long> implements
        ILimitingResourceQueueDAO {

    public LimitingResourceQueue findQueueByResource(Resource resource) {
        return (LimitingResourceQueue) getSession().createCriteria(
                LimitingResourceQueue.class).add(
                Restrictions.eq("resource", resource)).uniqueResult();
    }

    @Override
    public List<LimitingResourceQueue> getAll() {
        return list(LimitingResourceQueue.class);
    }

}
