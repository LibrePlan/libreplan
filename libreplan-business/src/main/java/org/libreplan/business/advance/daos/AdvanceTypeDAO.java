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

package org.libreplan.business.advance.daos;

import java.util.Collection;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.libreplan.business.advance.entities.AdvanceAssignment;
import org.libreplan.business.advance.entities.AdvanceType;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dao for {@link AdvanceType}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class AdvanceTypeDAO extends GenericDAOHibernate<AdvanceType, Long>
        implements IAdvanceTypeDAO {
    public boolean existsNameAdvanceType(String unitName) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<AdvanceType> cq = cb.createQuery(AdvanceType.class);
        Root<AdvanceType> root = cq.from(AdvanceType.class);
        cq.where(cb.equal(root.get("unitName"), unitName));
        return getSession().createQuery(cq).uniqueResult() != null;
    }

    @Override
    @Transactional(readOnly = true)
    public AdvanceType findByName(String name) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<AdvanceType> cq = cb.createQuery(AdvanceType.class);
        Root<AdvanceType> root = cq.from(AdvanceType.class);
        cq.where(cb.equal(root.get("unitName"), name));
        return getSession().createQuery(cq).uniqueResult();
    }

    @Override
    public List<AdvanceType> findActivesAdvanceTypes() {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<AdvanceType> cq = cb.createQuery(AdvanceType.class);
        Root<AdvanceType> root = cq.from(AdvanceType.class);
        cq.where(cb.equal(root.get("active"), Boolean.TRUE));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    public Collection<? extends AdvanceType> getAll() {
        return list(AdvanceType.class);
    }

    @Override
    public boolean isAlreadyInUse(AdvanceType advanceType) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<AdvanceAssignment> cq = cb.createQuery(AdvanceAssignment.class);
        Root<AdvanceAssignment> root = cq.from(AdvanceAssignment.class);
        cq.where(cb.equal(root.get("advanceType"), advanceType));
        return !getSession().createQuery(cq).getResultList().isEmpty();
    }

    @Override
    @Transactional(readOnly=true)
    public AdvanceType findByNameCaseInsensitive(String name)
            throws InstanceNotFoundException {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<AdvanceType> cq = cb.createQuery(AdvanceType.class);
        Root<AdvanceType> root = cq.from(AdvanceType.class);
        // Restrictions.ilike(..., MatchMode.EXACT) is a case-insensitive equals, not a
        // wildcard LIKE match.
        cq.where(cb.equal(cb.lower(root.get("unitName")), name.toLowerCase()));
        AdvanceType result = getSession().createQuery(cq).uniqueResult();

        if (result == null) {
            throw new InstanceNotFoundException(name,
                    getEntityClass().getName());
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByNameInAnotherTransaction(String name) {
        try {
            findByNameCaseInsensitive(name);
        } catch (InstanceNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public AdvanceType findUniqueByNameInAnotherTransaction(String name)
            throws InstanceNotFoundException {
        return findByNameCaseInsensitive(name);
    }
}
