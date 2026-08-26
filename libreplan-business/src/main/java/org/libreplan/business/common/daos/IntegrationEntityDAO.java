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

package org.libreplan.business.common.daos;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of <code>IIntegrationEntityDAO</code>.
 * DAOs of entities used in application integration may extend from this interface.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class IntegrationEntityDAO<E extends IntegrationEntity>
        extends GenericDAOHibernate<E, Long>
        implements IIntegrationEntityDAO<E> {

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

    @Override
    @Transactional(readOnly = true)
    public E findByCode(String code) throws InstanceNotFoundException {

        if (code == null || StringUtils.isBlank(code)) {
            throw new InstanceNotFoundException(null, getEntityClass().getName());
        }

        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(getEntityClass());
        Root<E> root = cq.from(getEntityClass());
        cq.where(cb.equal(cb.lower(root.get("code")), code.trim().toLowerCase()));
        E entity = getSession().createQuery(cq).uniqueResult();

        if (entity == null) {
            throw new InstanceNotFoundException(code, getEntityClass().getName());
        } else {
            return entity;
        }

    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public E findByCodeAnotherTransaction(String code) throws InstanceNotFoundException {
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

    @Override
    public List<E> findAll() {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(getEntityClass());
        Root<E> root = cq.from(getEntityClass());
        cq.orderBy(cb.asc(root.get("code")));
        return getSession().createQuery(cq).getResultList();
    }

}
