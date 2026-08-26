/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.resources.entities.LimitingResourceQueue;
import org.libreplan.business.resources.entities.Resource;
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
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<LimitingResourceQueue> cq = cb.createQuery(LimitingResourceQueue.class);
        Root<LimitingResourceQueue> root = cq.from(LimitingResourceQueue.class);
        cq.where(cb.equal(root.get("resource"), resource));
        return getSession().createQuery(cq).uniqueResult();
    }

    @Override
    public List<LimitingResourceQueue> getAll() {
        return list(LimitingResourceQueue.class);
    }

}
