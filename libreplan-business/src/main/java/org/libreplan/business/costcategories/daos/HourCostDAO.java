/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.business.costcategories.daos;

import java.math.BigDecimal;

import org.hibernate.Query;
import org.joda.time.LocalDate;
import org.libreplan.business.common.daos.IntegrationEntityDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.costcategories.entities.HourCost;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.resources.entities.Resource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
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

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getPriceCostFromResourceDateAndType(Resource resource,
            LocalDate date, TypeOfWorkHours type) {
        String strQuery = "SELECT hc.priceCost "
                + "FROM ResourcesCostCategoryAssignment rcca, HourCost hc "
                + "WHERE rcca.costCategory = hc.category "
                + "AND rcca.resource = :resource " + "AND hc.type = :type "
                + "AND rcca.initDate <= :date "
                + "AND (rcca.endDate >= :date OR rcca.endDate IS NULL) "
                + "AND hc.initDate <= :date "
                + "AND (hc.endDate >= :date OR hc.endDate IS NULL)";

        Query query = getSession().createQuery(strQuery);
        query.setParameter("resource", resource);
        query.setParameter("date", date);
        query.setParameter("type", type);

        return (BigDecimal) query.uniqueResult();
    }

}