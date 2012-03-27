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

import org.joda.time.LocalDate;
import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.costcategories.entities.CostCategory;
import org.libreplan.business.costcategories.entities.HourCost;
import org.libreplan.business.costcategories.entities.ResourcesCostCategoryAssignment;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.resources.entities.Resource;

/**
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public interface IHourCostDAO extends IIntegrationEntityDAO<HourCost> {

    @Override
    public void remove(Long id) throws InstanceNotFoundException;

    /**
     * Returns the price cost of a {@link HourCost} associated with a
     * {@link Resource} in a specific {@link LocalDate} and with a concrete
     * {@link TypeOfWorkHours}<br />
     *
     * The association is done through {@link CostCategory} associated to the
     * resource in the specific date (from class
     * {@link ResourcesCostCategoryAssignment}).
     *
     * @param resource
     * @param date
     * @param type
     * @return A {@link BigDecimal} with the price cost for a {@link Resource}
     *         in a {@link LocalDate} for this {@link TypeOfWorkHours}
     */
    BigDecimal getPriceCostFromResourceDateAndType(Resource resource,
            LocalDate date, TypeOfWorkHours type);

}
