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
package org.libreplan.business.planner.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
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
        Criteria c = getSession().createCriteria(Dependency.class);
        c.add(Restrictions.or(Restrictions.isNull("origin"),
                Restrictions.isNull("destination")));
        List<Dependency> results = c.list();
        for (Dependency each : results) {
            remove(each.getId());
        }
    }

}
