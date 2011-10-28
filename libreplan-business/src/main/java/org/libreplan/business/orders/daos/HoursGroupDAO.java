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

package org.libreplan.business.orders.daos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.daos.IntegrationEntityDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.entities.HoursGroup;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dao for {@link HoursGroup}
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class HoursGroupDAO extends IntegrationEntityDAO<HoursGroup>
        implements IHoursGroupDAO {

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByCodeAnotherTransaction(HoursGroup hoursGroup) {
        return existsByCode(hoursGroup);
    }

    private boolean existsByCode(HoursGroup hoursGroup) {
        try {
            HoursGroup result = findUniqueByCode(hoursGroup);
            return result != null && result != hoursGroup;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public HoursGroup findUniqueByCodeAnotherTransaction(HoursGroup hoursGroup)
            throws InstanceNotFoundException {
        return findUniqueByCode(hoursGroup);
    }

    private HoursGroup findUniqueByCode(HoursGroup hoursGroup)
            throws InstanceNotFoundException {
        Validate.notNull(hoursGroup);
        Validate.notNull(hoursGroup.getCode());

        Criteria c = getSession().createCriteria(HoursGroup.class);
        c.add(Restrictions.eq("code", hoursGroup.getCode()));

        HoursGroup result;
        try {
            result = (HoursGroup) c.uniqueResult();
        } catch (HibernateException e) {
            result = (HoursGroup) c.list().get(0);
        }

        if (result == null) {
            throw new InstanceNotFoundException(hoursGroup.getCode(),
                    HoursGroup.class.getName());
        } else {
            return result;
        }
    }

    @Override
    @Transactional(readOnly= true, propagation = Propagation.REQUIRES_NEW)
    public HoursGroup findRepeatedHoursGroupCodeInDB(List<HoursGroup> hoursGroupList) {
        final Map<String, HoursGroup> hoursGroups = createMapByCode(hoursGroupList);
        final Map<String, HoursGroup> hoursGroupsInDB = createMapByCode(getAll());

        for (String code : hoursGroups.keySet()) {
            HoursGroup hoursGroup = hoursGroups.get(code);
            HoursGroup hoursGroupInDB = hoursGroupsInDB.get(code);

            // There's an element in the DB with the same code and it's a
            // different element
            if (hoursGroupInDB != null
                    && !hoursGroupInDB.getId().equals(hoursGroup.getId())) {
                return hoursGroup;
            }
        }
        return null;
    }

    private List<HoursGroup> getAll() {
        return list(HoursGroup.class);
    }

    private Map<String, HoursGroup> createMapByCode(List<HoursGroup> hoursGroups) {
        Map<String, HoursGroup> result = new HashMap<String, HoursGroup>();
        for (HoursGroup each: hoursGroups) {
            final String code = each.getCode();
            result.put(code, each);
        }
        return result;
    }

}
