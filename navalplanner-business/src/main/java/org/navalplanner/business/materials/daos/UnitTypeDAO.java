/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.materials.daos;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.IntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.materials.entities.UnitType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * DAO for {@link UnitType}
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
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
    public boolean existsUnitTypeByName(String measure) {
        try {
            findByName(measure);
        } catch (InstanceNotFoundException e) {
            return false;
        }
        return true;
    }

}
