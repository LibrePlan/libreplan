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

package org.libreplan.ws.orders.impl;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.libreplan.business.common.daos.IEntitySequenceDAO;
import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.common.entities.EntityNameEnum;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLineGroup;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.ws.common.api.InstanceConstraintViolationsListDTO;
import org.libreplan.ws.common.api.OrderDTO;
import org.libreplan.ws.common.impl.ConfigurationOrderElementConverter;
import org.libreplan.ws.common.impl.GenericRESTService;
import org.libreplan.ws.common.impl.OrderElementConverter;
import org.libreplan.ws.common.impl.RecoverableErrorException;
import org.libreplan.ws.orders.api.IOrderElementService;
import org.libreplan.ws.orders.api.OrderListDTO;
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

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private IEntitySequenceDAO entitySequenceDAO;

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

    @Override
    @GET
    @Path("/{code}/")
    @Transactional(readOnly = true)
    public Response getOrderElement(@PathParam("code") String code) {
        return getDTOByCode(code);
    }

    @Override
    @DELETE
    @Path("/{code}/")
    @Transactional
    public Response removeOrderElement(@PathParam("code") String code) {
        try {
            OrderElement orderElement = orderElementDAO.findByCode(code);
            if (orderElement.isOrder()) {
                orderDAO.remove(orderElement.getId());
            } else {
                Order order = orderDAO.loadOrderAvoidingProxyFor(orderElement);
                order.useSchedulingDataFor(scenarioManager.getCurrent());

                orderElement = findOrderElement(order, orderElement.getId());

                OrderLineGroup parent = orderElement.getParent();
                parent.remove(orderElement);

                if (!parent.isOrder() && parent.getChildren().isEmpty()) {
                    OrderElement newElement = parent.toLeaf();
                    if (!order.isCodeAutogenerated()) {
                        newElement.setCode(UUID.randomUUID().toString());
                    }
                    parent.getParent().replace(parent, newElement);
                    if (order.isCodeAutogenerated()) {
                        order.generateOrderElementCodes(entitySequenceDAO
                                .getNumberOfDigitsCode(EntityNameEnum.ORDER));
                    }
                }

                orderDAO.save(order);
            }

            return Response.ok().build();
        } catch (InstanceNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    private OrderElement findOrderElement(OrderElement orderElement, Long id) {
        if (orderElement.getId().equals(id)) {
            return orderElement;
        }
        for (OrderElement child : orderElement.getChildren()) {
            OrderElement found = findOrderElement(child, id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

}
