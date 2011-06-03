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

package org.navalplanner.ws.orders.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.navalplanner.business.common.daos.IIntegrationEntityDAO;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.api.OrderDTO;
import org.navalplanner.ws.common.impl.ConfigurationOrderElementConverter;
import org.navalplanner.ws.common.impl.GenericRESTService;
import org.navalplanner.ws.common.impl.OrderElementConverter;
import org.navalplanner.ws.common.impl.RecoverableErrorException;
import org.navalplanner.ws.orders.api.IOrderElementService;
import org.navalplanner.ws.orders.api.OrderListDTO;
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
public class OrderElementServiceREST extends
        GenericRESTService<Order, OrderDTO> implements
        IOrderElementService {

    @Autowired
    private IOrderDAO orderDAO;

    @Override
    @GET
    @Transactional(readOnly = true)
    public OrderListDTO getOrders() {
        return new OrderListDTO(findAll());
    }

    @Override
    @POST
    @Consumes("application/xml")
    public InstanceConstraintViolationsListDTO addOrders(
            OrderListDTO orderListDTO) {
        return save(orderListDTO.orderDTOs);
    }

    @Override
    protected OrderDTO toDTO(Order entity) {
        return (OrderDTO) OrderElementConverter.toDTO(entity,
                ConfigurationOrderElementConverter.all());
    }

    @Override
    protected IIntegrationEntityDAO<Order> getIntegrationEntityDAO() {
        return orderDAO;
    }

    @Override
    protected Order toEntity(OrderDTO entityDTO) throws ValidationException,
            RecoverableErrorException {
        return (Order) OrderElementConverter.toEntity(entityDTO,
                ConfigurationOrderElementConverter.all());
    }

    @Override
    protected void updateEntity(Order entity, OrderDTO entityDTO)
            throws ValidationException, RecoverableErrorException {
            OrderElementConverter.update(entity, entityDTO,
                    ConfigurationOrderElementConverter.all());
    }

}
