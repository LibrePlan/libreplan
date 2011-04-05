/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.costcategories.daos;

import java.util.Collection;

import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.IntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.costcategories.entities.HourCost;
import org.navalplanner.business.costcategories.entities.TypeOfWorkHours;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class HourCostDAO extends IntegrationEntityDAO<HourCost> implements
        IHourCostDAO {

    @Override
    public void remove(Long id) throws InstanceNotFoundException {
        try {
            find(id).getCategory().removeHourCost(find(id));
        }
        catch(InstanceNotFoundException e) {
            //it was already deleted from its parent
            //we do nothing
        }
        super.remove(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<HourCost> hoursCostsByTypeOfWorkHour(
            TypeOfWorkHours typeOfWorkHours) {
        return getSession().createCriteria(HourCost.class)
            .add(Restrictions.eq("type", typeOfWorkHours))
            .list();
    }

}