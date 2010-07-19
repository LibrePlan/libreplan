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

package org.navalplanner.ws.subcontract.impl;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.daos.IOrderSequenceDAO;
import org.navalplanner.business.common.entities.OrderSequence;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.externalcompanies.daos.IExternalCompanyDAO;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderStatusEnum;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.api.OrderElementDTO;
import org.navalplanner.ws.common.impl.ConfigurationOrderElementConverter;
import org.navalplanner.ws.common.impl.ConstraintViolationConverter;
import org.navalplanner.ws.common.impl.OrderElementConverter;
import org.navalplanner.ws.common.impl.Util;
import org.navalplanner.ws.subcontract.api.ISubcontractService;
import org.navalplanner.ws.subcontract.api.SubcontractedTaskDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * REST-based implementation of {@link ISubcontractService}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Path("/subcontract/")
@Produces("application/xml")
@Service("subcontractServiceREST")
public class SubcontractServiceREST implements ISubcontractService {

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IExternalCompanyDAO externalCompanyDAO;

    @Autowired
    private IOrderSequenceDAO orderSequenceDAO;

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Override
    @POST
    @Consumes("application/xml")
    @Transactional
    public InstanceConstraintViolationsListDTO subcontract(
            SubcontractedTaskDataDTO subcontractedTaskDataDTO) {

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = new ArrayList<InstanceConstraintViolationsDTO>();

        InstanceConstraintViolationsDTO instanceConstraintViolationsDTO = null;

        if (StringUtils.isEmpty(subcontractedTaskDataDTO.externalCompanyNif)) {
            return getErrorMessage(subcontractedTaskDataDTO.subcontractedCode,
                    "external company nif not specified");
        }

        ExternalCompany externalCompany;
        try {
            externalCompany = externalCompanyDAO
                    .findUniqueByNif(subcontractedTaskDataDTO.externalCompanyNif);
        } catch (InstanceNotFoundException e1) {
            return getErrorMessage(subcontractedTaskDataDTO.externalCompanyNif,
                    "external company not found");
        }

        if (!externalCompany.isClient()) {
            return getErrorMessage(subcontractedTaskDataDTO.externalCompanyNif,
                    "external company is not registered as client");
        }

        OrderElementDTO orderElementDTO = subcontractedTaskDataDTO.orderElementDTO;
        if (orderElementDTO == null) {
            return getErrorMessage(subcontractedTaskDataDTO.subcontractedCode,
                    "order element not specified");
        }

        try {
            Scenario current = Registry.getScenarioManager().getCurrent();
            OrderVersion version = OrderVersion.createInitialVersion(current);

            OrderElement orderElement = OrderElementConverter.toEntity(version,
                    orderElementDTO, ConfigurationOrderElementConverter
                            .noAdvanceMeasurements());

            Order order;
            if (orderElement instanceof Order) {
                order = (Order) orderElement;
                order.setVersionForScenario(current, version);
                order.useSchedulingDataFor(version);
            } else {
                order = wrapInOrder(current, version, orderElement);
            }

            addOrderToDerivedScenarios(current, version, order);

            order.moveCodeToExternalCode();
            order.setCodeAutogenerated(true);
            String code = orderSequenceDAO.getNextOrderCode();
            if (code == null) {
                return getErrorMessage(
                        subcontractedTaskDataDTO.orderElementDTO.code,
                        "unable to generate the code for the new order, please try again later");
            }

            order.setCode(code);
            generateCodes(order);

            order.setState(OrderStatusEnum.SUBCONTRACTED_PENDING_ORDER);

            if (subcontractedTaskDataDTO.workDescription != null) {
                order.setName(subcontractedTaskDataDTO.workDescription);
            }
            order.setCustomer(externalCompany);
            order
                    .setCustomerReference(subcontractedTaskDataDTO.subcontractedCode);
            order.setWorkBudget(subcontractedTaskDataDTO.subcontractPrice);

            order.validate();
            orderElementDAO.save(order);
        } catch (ValidationException e) {
            instanceConstraintViolationsDTO = ConstraintViolationConverter
                    .toDTO(Util.generateInstanceId(1, orderElementDTO.code), e
                            .getInvalidValues());
        }

        if (instanceConstraintViolationsDTO != null) {
            instanceConstraintViolationsList
                    .add(instanceConstraintViolationsDTO);
        }

        return new InstanceConstraintViolationsListDTO(
                instanceConstraintViolationsList);
    }

    private void addOrderToDerivedScenarios(Scenario currentScenario,
            OrderVersion orderVersion, Order order) {
        List<Scenario> derivedScenarios = scenarioDAO
                .getDerivedScenarios(currentScenario);
        for (Scenario scenario : derivedScenarios) {
            scenario.addOrder(order, orderVersion);
        }
    }

    private void generateCodes(Order order) {
        OrderSequence orderSequence = orderSequenceDAO.getActiveOrderSequence();
        int numberOfDigits = orderSequence.getNumberOfDigits();

        order.generateOrderElementCodes(numberOfDigits);
    }

    private Order wrapInOrder(Scenario current, OrderVersion version,
            OrderElement orderElement) {
        if (orderElement instanceof Order) {
            return (Order) orderElement;
        }

        Order order = Order.create();
        order.setVersionForScenario(current, version);
        order.useSchedulingDataFor(version);
        order.add(orderElement);

        order.setName(_("Order from client"));
        order.setInitDate(orderElement.getInitDate());
        order.setDeadline(orderElement.getDeadline());
        order.setCalendar(getDefaultCalendar());

        return order;
    }

    private BaseCalendar getDefaultCalendar() {
        return configurationDAO.getConfiguration().getDefaultCalendar();
    }

    private InstanceConstraintViolationsListDTO getErrorMessage(String code,
            String message) {
        // FIXME review errors returned
        return new InstanceConstraintViolationsListDTO(Arrays
                .asList(InstanceConstraintViolationsDTO.create(Util
                        .generateInstanceId(1, code), message)));
    }

}