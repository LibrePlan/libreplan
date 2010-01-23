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

package org.navalplanner.business.common.daos;

import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of <code>IIntegrationEntityDAO</code>. DAOs of
 * entities used in application integration may extend from this interface.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class IntegrationEntityDAO<E extends IntegrationEntity>
    extends GenericDAOHibernate<E, Long> implements IIntegrationEntityDAO<E> {

    @Override
    public boolean existsByCode(String code) {

        try {
            findByCode(code);
            return true;
        } catch (InstanceNotFoundException e) {
            return false;
        }

    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByCodeAnotherTransaction(String code) {
        return existsByCode(code);
    }

    @SuppressWarnings("unchecked")
    @Override
    public E findByCode(String code) throws InstanceNotFoundException {

        E entity = (E) getSession().createCriteria(getEntityClass()).add(
            Restrictions.eq("code", code).ignoreCase()).uniqueResult();

        if (entity == null) {
            throw new InstanceNotFoundException(
                code, getEntityClass().getName());
        } else {
            return entity;
        }

    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public E findByCodeAnotherTransaction(String code)
        throws InstanceNotFoundException {

        return findByCode(code);

    }

    @Override
    public E findExistingEntityByCode(String code) {

        try {
            return findByCode(code);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}
