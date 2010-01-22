/*
 * This file is part of ###PROJECT_NAME###
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

package org.navalplanner.ws.orders.api;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.ws.common.api.IncompatibleTypeException;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.api.OrderDTO;
import org.navalplanner.ws.common.api.OrderElementDTO;

/**
 * Service for managing {@link OrderElement} entities.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface IOrderElementService {

    OrderElementDTO getOrderElement(String code)
            throws InstanceNotFoundException;

    InstanceConstraintViolationsListDTO addOrder(OrderDTO order);

    InstanceConstraintViolationsListDTO updateOrder(OrderDTO orderDTO)
            throws InstanceNotFoundException, IncompatibleTypeException;

}
