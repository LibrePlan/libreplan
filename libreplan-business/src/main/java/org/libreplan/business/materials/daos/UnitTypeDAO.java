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

package org.libreplan.business.materials.daos;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.libreplan.business.common.daos.IntegrationEntityDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.materials.entities.Material;
import org.libreplan.business.materials.entities.UnitType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for {@link UnitType}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Javier Moran Rua <jmoran@igaia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class UnitTypeDAO extends IntegrationEntityDAO<UnitType> implements
        IUnitTypeDAO {

    @Override
    public List<UnitType> getAll() {
        return list(UnitType.class);
    }

    @Override
    public UnitType findByName(String measure) throws InstanceNotFoundException {
        if (StringUtils.isBlank(measure)) {
            throw new InstanceNotFoundException(null, getEntityClass()
                    .getName());
        }

        UnitType unitType = (UnitType) getSession().createCriteria(
                UnitType.class).add(
                Restrictions.eq("measure", measure)).uniqueResult();

        if (unitType == null) {
            throw new InstanceNotFoundException(measure, getEntityClass()
                    .getName());
        } else {
            return unitType;
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public UnitType findUniqueByNameInAnotherTransaction(String measure)
            throws InstanceNotFoundException {
        return findByNameCaseInsensitive(measure);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsUnitTypeByNameInAnotherTransaction(String measure) {
        try {
            findByNameCaseInsensitive(measure);
        } catch (InstanceNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional(readOnly=true)
    public UnitType findByNameCaseInsensitive(String measure)
            throws InstanceNotFoundException {
        Criteria c = getSession().createCriteria(UnitType.class);
        c.add(Restrictions.ilike("measure", measure, MatchMode.EXACT));
        UnitType result = (UnitType) c.uniqueResult();

        if (result == null) {
            throw new InstanceNotFoundException(measure,
                    getEntityClass().getName());
        }

        return result;
    }

    @Override
    @Transactional(readOnly=true)
    public boolean isUnitTypeUsedInAnyMaterial(UnitType unitType) {
        Criteria c = getSession().createCriteria(Material.class);
        return !c.add(Restrictions.eq("unitType", unitType)).list().isEmpty();
    }

}
