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

package org.navalplanner.business.externalcompanies.daos;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hibernate DAO for {@link ExternalCompany}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ExternalCompanyDAO extends GenericDAOHibernate<ExternalCompany, Long>
        implements IExternalCompanyDAO {

    @Override
    public boolean existsByName(String name) {
        try {
            findUniqueByName(name);
            return true;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByNameInAnotherTransaction(String name) {
        return existsByName(name);
    }

    @Override
    public ExternalCompany findUniqueByName(String name)
            throws InstanceNotFoundException {
        Criteria c = getSession().createCriteria(ExternalCompany.class);
        c.add(Restrictions.eq("name", name));

        ExternalCompany found = (ExternalCompany) c.uniqueResult();
        if (found == null)
            throw new InstanceNotFoundException(name, ExternalCompany.class.getName());

        return found;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public ExternalCompany findUniqueByNameInAnotherTransaction(String name)
            throws InstanceNotFoundException {
        return findUniqueByName(name);
    }

}
