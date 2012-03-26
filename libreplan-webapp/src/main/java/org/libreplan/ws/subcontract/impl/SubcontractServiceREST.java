/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 *
 * Copyright (C) 2011 WirelessGalicia, S.L.
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

package org.libreplan.ws.subcontract.impl;

import static org.libreplan.web.I18nHelper._;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.NonUniqueResultException;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.daos.IEntitySequenceDAO;
import org.libreplan.business.common.entities.EntityNameEnum;
import org.libreplan.business.common.entities.EntitySequence;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.externalcompanies.daos.ICustomerCommunicationDAO;
import org.libreplan.business.externalcompanies.daos.IExternalCompanyDAO;
import org.libreplan.business.externalcompanies.entities.CommunicationType;
import org.libreplan.business.externalcompanies.entities.CustomerCommunication;
import org.libreplan.business.externalcompanies.entities.DeadlineCommunication;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderStatusEnum;
import org.libreplan.business.orders.entities.TaskSource;
import org.libreplan.business.orders.entities.TaskSource.IOptionalPersistence;
import org.libreplan.business.orders.entities.TaskSource.TaskSourceSynchronization;
import org.libreplan.business.planner.daos.ITaskSourceDAO;
import org.libreplan.business.scenarios.daos.IScenarioDAO;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.web.subcontract.UpdateDeliveringDateDTO;
import org.libreplan.ws.common.api.InstanceConstraintViolationsDTO;
import org.libreplan.ws.common.api.InstanceConstraintViolationsDTOId;
import org.libreplan.ws.common.api.InstanceConstraintViolationsListDTO;
import org.libreplan.ws.common.api.OrderDTO;
import org.libreplan.ws.common.api.OrderElementDTO;
import org.libreplan.ws.common.impl.ConfigurationOrderElementConverter;
import org.libreplan.ws.common.impl.ConstraintViolationConverter;
import org.libreplan.ws.common.impl.DateConverter;
import org.libreplan.ws.common.impl.OrderElementConverter;
import org.libreplan.ws.common.impl.Util;
import org.libreplan.ws.subcontract.api.ISubcontractService;
import org.libreplan.ws.subcontract.api.SubcontractedTaskDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * REST-based implementation of {@link ISubcontractService}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
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
    private ICustomerCommunicationDAO customerCommunicationDAO;

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
    @Path("update/")
    @Consumes("application/xml")
    public InstanceConstraintViolationsListDTO updateDeliveringDates(
            final UpdateDeliveringDateDTO updateDeliveringDateDTO) {
        try {
            updateSubcontract(updateDeliveringDateDTO);
        } catch (ViolationError e) {
            return e.toViolationList();
        }
        return new InstanceConstraintViolationsListDTO();
    }


    private void updateSubcontract(
            final UpdateDeliveringDateDTO updateDeliveringDateDTO) {

        if (StringUtils.isEmpty(updateDeliveringDateDTO.companyNif)) {
            throw new ViolationError(updateDeliveringDateDTO.companyNif,
                    "external company Nif not specified");
        }

        if (StringUtils.isEmpty(updateDeliveringDateDTO.externalCode)) {
            throw new ViolationError(updateDeliveringDateDTO.externalCode,
                    "external order code not specified");
        }

        if ((updateDeliveringDateDTO.deliverDate) == null) {
            throw new ViolationError(updateDeliveringDateDTO.deliverDate.toString(),
                    "deliver date not specified");
        }

        final ExternalCompany externalCompany = adHocTransactionService
                .runOnTransaction(new IOnTransaction<ExternalCompany>() {
                    @Override
                    public ExternalCompany execute() {
                        return findExternalCompanyFor(updateDeliveringDateDTO.companyNif);
                    }
                });
        if (!externalCompany.isClient()) {
            throw new ViolationError(updateDeliveringDateDTO.companyNif,
                    "external company is not registered as client");
        }
        try {
            adHocTransactionService
                    .runOnTransaction(new IOnTransaction<Void>() {

                        @Override
                        public Void execute() {
                            updateDeliveringDateInOrder(updateDeliveringDateDTO);
                            return null;
                        }
                    });
        } catch (ValidationException e) {
            InstanceConstraintViolationsDTO violation = ConstraintViolationConverter
                    .toDTO(new InstanceConstraintViolationsDTOId(Long.valueOf(1),
                            updateDeliveringDateDTO.companyNif, OrderDTO.ENTITY_TYPE), e);
            throw new ViolationError(violation);
        }
    }

    private void updateDeliveringDateInOrder(UpdateDeliveringDateDTO updateDeliveringDateDTO){
        try {
            OrderElement orderElement = orderElementDAO
                    .findByExternalCode(updateDeliveringDateDTO.externalCode);

            if((orderElement != null) && (orderElement instanceof Order)) {
                Order order = (Order)orderElement;

                Date newDeliverDate = DateConverter.toDate(updateDeliveringDateDTO.deliverDate);
                DeadlineCommunication deadlineCommunication = DeadlineCommunication
                        .create(new Date(), newDeliverDate);
                order.getDeliveringDates().add(deadlineCommunication);

                LocalDate newLocalDeliverDate = new LocalDate(newDeliverDate);
                OrderVersion orderVersion = order.getOrderVersionFor(Registry
                        .getScenarioManager().getCurrent());
                order.useSchedulingDataFor(orderVersion);
                if (order.getAssociatedTaskElement() != null) {
                    order.getAssociatedTaskElement().setDeadline(
                            newLocalDeliverDate);
                }
                createCustomerCommunication(order, CommunicationType.UPDATE_DELIVERING_DATE);
                orderElementDAO.save(order);

            } else {
                throw new ViolationError(
                        updateDeliveringDateDTO.customerReference,
                        "It do not exist any order with this reference");
            }
        } catch (InstanceNotFoundException e) {
            throw new ViolationError(updateDeliveringDateDTO.customerReference,
                    "It do not exist any order with this reference");
        }
    }

    @Override
    @POST
    @Path("create/")
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
            order.setExternalCode(order.getCode());
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

        if (subcontractedTaskDataDTO.deliverDate != null) {
            DeadlineCommunication deadlineCommunication = DeadlineCommunication
                    .create(new Date(), DateConverter
                            .toDate(subcontractedTaskDataDTO.deliverDate));
            order.getDeliveringDates().add(deadlineCommunication);
        }

        order.validate();
        orderElementDAO.save(order);

        /*
         * create the customer communication to a new subcontrating project.
         */
        if(!StringUtils.isBlank(order.getExternalCode())){
            createCustomerCommunication(order, CommunicationType.NEW_PROJECT);
        }
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
            final SubcontractedTaskDataDTO subcontractedTask){
       return findExternalCompanyFor(subcontractedTask.externalCompanyNif);
    }

    private ExternalCompany findExternalCompanyFor(
            final String externalCompanyNif)
            throws ViolationError {
        try {
            return externalCompanyDAO
                    .findUniqueByNif(externalCompanyNif);
        } catch (InstanceNotFoundException e) {
            throw new ViolationError(externalCompanyNif,
                    "external company not found");
        }
    }

    private static InstanceConstraintViolationsDTO createConstraintViolationFor(
            String code, String message) {
        return InstanceConstraintViolationsDTO.create(
                Util.generateInstanceId(1, code), message);
    }

    private void createCustomerCommunication(Order order, CommunicationType type){
        Date communicationDate = new Date();
        Date deadline = null;
        if(type.equals(CommunicationType.NEW_PROJECT)){
             deadline = order.getDeadline();
        }else{
            deadline = order.getDeliveringDates().first().getDeliverDate();
        }
        CustomerCommunication customerCommunication = CustomerCommunication
                .create(deadline, communicationDate,
                        type, order);
        customerCommunicationDAO.save(customerCommunication);
    }
}
