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

import java.util.List;

import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.entities.HoursGroup;

/**
 * Contract for {@link HoursGroupDAO}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
public interface IHoursGroupDAO extends IIntegrationEntityDAO<HoursGroup> {

    boolean existsByCodeAnotherTransaction(HoursGroup hoursGroup);

    HoursGroup findUniqueByCodeAnotherTransaction(HoursGroup hoursGroup)
            throws InstanceNotFoundException;

    /**
     * Checks if there's another {@link HoursGroup} in DB which code is the same as
     * some of the ones in order.hoursGroups
     *
     * @param order
     * @return
     */
    HoursGroup findRepeatedHoursGroupCodeInDB(List<HoursGroup> hoursGroupList);
}
