/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2013 St. Antoniusziekenhuis
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

package org.libreplan.business.common.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.entities.Connector;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@link Connector} entity.
 *
 * @author Miciele Ghiorghis <m.ghiorghis@antoniusziekenhuis.nl>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ConnectorDAO extends GenericDAOHibernate<Connector, Long> implements IConnectorDAO {

    @Override
    @Transactional(readOnly = true)
    public List<Connector> getAll() {
        return list(Connector.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Connector findUniqueByName(String name) {
        Criteria c = getSession().createCriteria(Connector.class).add(Restrictions.eq("name", name));

        return (Connector) c.uniqueResult();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByNameAnotherTransaction(Connector connector) {
        return existsOtherConnectorByName(connector);
    }

    private boolean existsOtherConnectorByName(Connector connector) {
        Connector found = findUniqueByName(connector.getName());

        return found != null && found != connector;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Connector findUniqueByNameAnotherTransaction(String name) {
        return findUniqueByName(name);
    }

}
