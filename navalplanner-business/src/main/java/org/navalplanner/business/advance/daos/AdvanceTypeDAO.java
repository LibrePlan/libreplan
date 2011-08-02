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

package org.navalplanner.business.advance.daos;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.advance.entities.AdvanceAssignment;
import org.navalplanner.business.advance.entities.AdvanceType;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
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
        return getSession().createCriteria(AdvanceType.class).add(
                Restrictions.eq("unitName", unitName)).uniqueResult() != null;
    }

    @Override
    @Transactional(readOnly = true)
    public AdvanceType findByName(String name) {
        return (AdvanceType) getSession().createCriteria(AdvanceType.class)
                .add(Restrictions.eq("unitName", name)).uniqueResult();
    }

    @Override
    public List<AdvanceType> findActivesAdvanceTypes() {
        return getSession().createCriteria(AdvanceType.class).add(
                Restrictions.eq("active", Boolean.TRUE)).list();
    }

    @Override
    public Collection<? extends AdvanceType> getAll() {
        return list(AdvanceType.class);
    }

    @Override
    public boolean isAlreadyInUse(AdvanceType advanceType) {
        return !getSession().createCriteria(AdvanceAssignment.class).add(
                Restrictions.eq("advanceType", advanceType)).list().isEmpty();
    }

    @Override
    @Transactional(readOnly=true)
    public AdvanceType findByNameCaseInsensitive(String name)
            throws InstanceNotFoundException {
        Criteria c = getSession().createCriteria(AdvanceType.class);
        c.add(Restrictions.ilike("unitName", name, MatchMode.EXACT));
        AdvanceType result = (AdvanceType) c.uniqueResult();

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
