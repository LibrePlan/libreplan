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

package org.navalplanner.ws.subcontract.impl;

import static org.navalplanner.web.I18nHelper._;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.NonUniqueResultException;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.common.daos.IEntitySequenceDAO;
import org.navalplanner.business.common.entities.EntityNameEnum;
import org.navalplanner.business.common.entities.EntitySequence;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.externalcompanies.daos.IExternalCompanyDAO;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderStatusEnum;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.orders.entities.TaskSource.IOptionalPersistence;
import org.navalplanner.business.orders.entities.TaskSource.TaskSourceSynchronization;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.scenarios.daos.IScenarioDAO;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTOId;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.api.OrderDTO;
import org.navalplanner.ws.common.api.OrderElementDTO;
import org.navalplanner.ws.common.impl.ConfigurationOrderElementConverter;
import org.navalplanner.ws.common.impl.ConstraintViolationConverter;
import org.navalplanner.ws.common.impl.OrderElementConverter;
import org.navalplanner.ws.common.impl.Util;
import org.navalplanner.ws.subcontract.api.ISubcontractService;
import org.navalplanner.ws.subcontract.api.SubcontractedTaskDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private IEntitySequenceDAO entitySequenceDAO;

    @Autowired
    private IScenarioDAO scenarioDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private IAdHocTransactionService adHocTransactionService;

    static class ViolationError extends RuntimeException {
        private final List<InstanceConstraintViolationsDTO> violations;

        ViolationError(InstanceConstraintViolationsDTO... violations) {
            Validate.noNullElements(violations);
            this.violations = Arrays.asList(violations);
        }

        ViolationError(String code, String message) {
            this(createConstraintViolationFor(code, message));
        }

        public List<InstanceConstraintViolationsDTO> getViolations() {
            return violations;
        }

        public InstanceConstraintViolationsListDTO toViolationList() {
            return new InstanceConstraintViolationsListDTO(violations);
        }
    }

    @Override
    @POST
    @Consumes("application/xml")
    public InstanceConstraintViolationsListDTO subcontract(
            final SubcontractedTaskDataDTO subcontractedTaskDataDTO) {
        try {
            doSubcontract(subcontractedTaskDataDTO);
        } catch (ViolationError e) {
            return e.toViolationList();
        }
        return new InstanceConstraintViolationsListDTO();
    }

    private void doSubcontract(final SubcontractedTaskDataDTO subcontractedTask)
            throws ViolationError {

        if (StringUtils.isEmpty(subcontractedTask.externalCompanyNif)) {
            throw new ViolationError(subcontractedTask.subcontractedCode,
                    "external company ID not specified");
        }

        final ExternalCompany externalCompany = adHocTransactionService
                .runOnTransaction(new IOnTransaction<ExternalCompany>() {
                    @Override
                    public ExternalCompany execute() {
                        return findExternalCompanyFor(subcontractedTask);
                    }
                });
        if (!externalCompany.isClient()) {
            throw new ViolationError(subcontractedTask.externalCompanyNif,
                    "external company is not registered as client");
        }

        final OrderElementDTO orderElementDTO = subcontractedTask.orderElementDTO;
        if (orderElementDTO == null) {
            throw new ViolationError(subcontractedTask.subcontractedCode,
                    "task not specified");
        }
        try {
            adHocTransactionService
                    .runOnTransaction(new IOnTransaction<Void>() {

                        @Override
                        public Void execute() {
                            createOrder(subcontractedTask, externalCompany,
                                    orderElementDTO);
                            return null;
                        }
                    });
        } catch (ValidationException e) {
            InstanceConstraintViolationsDTO violation = ConstraintViolationConverter
                    .toDTO(new InstanceConstraintViolationsDTOId(Long.valueOf(1),
                            orderElementDTO.code, OrderDTO.ENTITY_TYPE), e);
            throw new ViolationError(violation);
        }
    }

    private void createOrder(
            final SubcontractedTaskDataDTO subcontractedTaskDataDTO,
            final ExternalCompany externalCompany,
            final OrderElementDTO orderElementDTO) throws ViolationError{
        Scenario current = Registry.getScenarioManager().getCurrent();
        OrderVersion version = OrderVersion.createInitialVersion(current);
        OrderElement orderElement = OrderElementConverter.toEntity(version,
                orderElementDTO,
                ConfigurationOrderElementConverter.noAdvanceMeasurements());

        Order order;
        if (orderElement instanceof Order) {
            order = (Order) orderElement;
            order.setVersionForScenario(current, version);
            order.useSchedulingDataFor(version);
        } else {
            order = wrapInOrder(current, version,
                    orderElement);
        }

        addOrderToDerivedScenarios(current, version, order);

        order.setCodeAutogenerated(true);
        String code = entitySequenceDAO.getNextEntityCode(EntityNameEnum.ORDER);
        if (code == null) {
            throw new ViolationError(subcontractedTaskDataDTO.orderElementDTO.code,
                    "unable to generate the code for the new project, please try again later");
        }

        order.setCode(code);
        generateCodes(order);

        order.setState(OrderStatusEnum.SUBCONTRACTED_PENDING_ORDER);

        if (subcontractedTaskDataDTO.workDescription != null) {
            order.setName(subcontractedTaskDataDTO.workDescription);
        }
        order.setCustomer(externalCompany);
        order.setCustomerReference(subcontractedTaskDataDTO.subcontractedCode);
        order.setWorkBudget(subcontractedTaskDataDTO.subcontractPrice);

        synchronizeWithSchedule(order,
                TaskSource.persistTaskSources(taskSourceDAO));
        order.writeSchedulingDataChanges();

        order.validate();
        orderElementDAO.save(order);
    }

    private void synchronizeWithSchedule(OrderElement orderElement,
            IOptionalPersistence persistence) {
        List<TaskSourceSynchronization> synchronizationsNeeded = orderElement
                .calculateSynchronizationsNeeded();
        for (TaskSourceSynchronization each : synchronizationsNeeded) {
            each.apply(persistence);
        }
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
        EntitySequence entitySequence;
        try {
            entitySequence = entitySequenceDAO
                    .getActiveEntitySequence(EntityNameEnum.ORDER);
            int numberOfDigits = entitySequence.getNumberOfDigits();
            order.generateOrderElementCodes(numberOfDigits);
        } catch (NonUniqueResultException e) {
            throw new ViolationError("",
                    "There are several active project sequences");
        } catch (InstanceNotFoundException e) {
            throw new ViolationError("",
                    "It does not exist any activated code sequence.");
        }
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

        order.setName(_("Project from client"));
        order.setInitDate(orderElement.getInitDate());
        order.setDeadline(orderElement.getDeadline());
        order.setCalendar(getDefaultCalendar());
        order.setExternalCode(orderElement.getCode());

        return order;
    }

    private BaseCalendar getDefaultCalendar() {
        return configurationDAO.getConfiguration().getDefaultCalendar();
    }

    private ExternalCompany findExternalCompanyFor(
            final SubcontractedTaskDataDTO subcontractedTask)
            throws ViolationError {
        try {
            return externalCompanyDAO
                    .findUniqueByNif(subcontractedTask.externalCompanyNif);
        } catch (InstanceNotFoundException e) {
            throw new ViolationError(subcontractedTask.externalCompanyNif,
                    "external company not found");
        }
    }

    private static InstanceConstraintViolationsDTO createConstraintViolationFor(
            String code, String message) {
        return InstanceConstraintViolationsDTO.create(
                Util.generateInstanceId(1, code), message);
    }

}
