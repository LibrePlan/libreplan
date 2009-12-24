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

package org.navalplanner.ws.orders.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.impl.ConstraintViolationConverter;
import org.navalplanner.ws.common.impl.Util;
import org.navalplanner.ws.orders.api.IOrderElementService;
import org.navalplanner.ws.orders.api.OrderDTO;
import org.navalplanner.ws.orders.api.OrderElementDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * REST-based implementation of {@link IOrderElementService}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Path("/orderelements/")
@Produces("application/xml")
@Service("orderElementServiceREST")
public class OrderElementServiceREST implements IOrderElementService {

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Override
    @GET
    @Path("/{code}")
    @Transactional(readOnly = true)
    public OrderElementDTO getOrderElement(@PathParam("code") String code)
            throws InstanceNotFoundException {
        return OrderElementConverter.toDTO(orderElementDAO
                .findUniqueByCode(code));
    }

    @Override
    @POST
    @Consumes("application/xml")
    @Transactional
    public InstanceConstraintViolationsListDTO addOrder(OrderDTO orderDTO) {

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = new ArrayList<InstanceConstraintViolationsDTO>();

        InstanceConstraintViolationsDTO instanceConstraintViolationsDTO = null;
        try {
            OrderElement orderElement = OrderElementConverter
                    .toEntity(orderDTO);
            orderElement.validate();
            orderElementDAO.save(orderElement);
        } catch (ValidationException e) {
            instanceConstraintViolationsDTO = ConstraintViolationConverter
                    .toDTO(Util.generateInstanceId(1, orderDTO.code), e
                            .getInvalidValues());
        }

        if (instanceConstraintViolationsDTO != null) {
            instanceConstraintViolationsList
                    .add(instanceConstraintViolationsDTO);
        }

        return new InstanceConstraintViolationsListDTO(
                instanceConstraintViolationsList);
    }

}
