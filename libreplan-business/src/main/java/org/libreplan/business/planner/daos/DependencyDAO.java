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
package org.libreplan.business.planner.daos;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.planner.entities.Dependency;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for entity @{link Dedenpency}
 *
 * @author Javier Moran Rua <jmoran@igalia.com>
 *
 */

@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class DependencyDAO extends GenericDAOHibernate<Dependency, Long>
        implements IDependencyDAO {

    @Override
    @Transactional
    public void deleteUnattachedDependencies() throws InstanceNotFoundException {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<Dependency> cq = cb.createQuery(Dependency.class);
        Root<Dependency> root = cq.from(Dependency.class);
        cq.where(cb.or(cb.isNull(root.get("origin")), cb.isNull(root.get("destination"))));
        List<Dependency> results = getSession().createQuery(cq).getResultList();
        for (Dependency each : results) {
            remove(each.getId());
        }
    }

}
