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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.lang.StringUtils;
import org.libreplan.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.libreplan.business.advance.entities.AdvanceAssignment;
import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.libreplan.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.externalcompanies.daos.IExternalCompanyDAO;
import org.libreplan.business.externalcompanies.entities.CommunicationType;
import org.libreplan.business.externalcompanies.entities.EndDateCommunication;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.daos.ISubcontractedTaskDataDAO;
import org.libreplan.business.planner.daos.ISubcontractorCommunicationDAO;
import org.libreplan.business.planner.entities.SubcontractedTaskData;
import org.libreplan.business.planner.entities.SubcontractorCommunication;
import org.libreplan.business.planner.entities.SubcontractorCommunicationValue;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.scenarios.bootstrap.PredefinedScenarios;
import org.libreplan.business.scenarios.entities.OrderVersion;
import org.libreplan.business.scenarios.entities.Scenario;
import org.libreplan.ws.common.api.AdvanceMeasurementDTO;
import org.libreplan.ws.common.api.InstanceConstraintViolationsDTO;
import org.libreplan.ws.common.api.InstanceConstraintViolationsListDTO;
import org.libreplan.ws.common.impl.ConstraintViolationConverter;
import org.libreplan.ws.common.impl.DateConverter;
import org.libreplan.ws.common.impl.OrderElementConverter;
import org.libreplan.ws.common.impl.Util;
import org.libreplan.ws.subcontract.api.EndDateCommunicationToCustomerDTO;
import org.libreplan.ws.subcontract.api.IReportAdvancesService;
import org.libreplan.ws.subcontract.api.OrderElementWithAdvanceMeasurementsOrEndDateDTO;
import org.libreplan.ws.subcontract.api.OrderElementWithAdvanceMeasurementsOrEndDateListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * REST-based implementation of {@link IReportAdvancesService}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Path("/reportadvances/")
@Produces("application/xml")
@Service("reportAdvancesServiceREST")
public class ReportAdvancesServiceREST implements IReportAdvancesService {

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private ISubcontractedTaskDataDAO subcontractedTaskDataDAO;

    @Autowired
    private ISubcontractorCommunicationDAO subcontractorCommunicationDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IExternalCompanyDAO externalCompanyDAO;

    private InstanceConstraintViolationsListDTO getErrorMessage(String code, String message) {
        // FIXME review errors returned
        return new InstanceConstraintViolationsListDTO(
                Arrays.asList(InstanceConstraintViolationsDTO.create(
                        Util.generateInstanceId(1, code), message)));
    }

    @Override
    @POST
    @Consumes("application/xml")
    @Transactional
    public InstanceConstraintViolationsListDTO updateAdvancesOrEndDate(
            OrderElementWithAdvanceMeasurementsOrEndDateListDTO orderElementWithAdvanceMeasurementsListDTO) {

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = new ArrayList<InstanceConstraintViolationsDTO>();

        InstanceConstraintViolationsDTO instanceConstraintViolationsDTO = null;

        if (StringUtils.isEmpty(orderElementWithAdvanceMeasurementsListDTO.externalCompanyNif)) {
            return getErrorMessage("", "external company ID not specified");
        }

        ExternalCompany externalCompany;
        try {
            externalCompany = externalCompanyDAO
                    .findUniqueByNif(orderElementWithAdvanceMeasurementsListDTO.externalCompanyNif);
        } catch (InstanceNotFoundException e1) {
            return getErrorMessage(orderElementWithAdvanceMeasurementsListDTO.externalCompanyNif,
                    "external company not found");
        }

        if (!externalCompany.isSubcontractor()) {
            return getErrorMessage(orderElementWithAdvanceMeasurementsListDTO.externalCompanyNif,
                    "external company is not registered as subcontractor");
        }

        List<OrderElementWithAdvanceMeasurementsOrEndDateDTO> orderElements = orderElementWithAdvanceMeasurementsListDTO.orderElements;
        for (OrderElementWithAdvanceMeasurementsOrEndDateDTO orderElementWithAdvanceMeasurementsOrEndDateDTO : orderElements) {
            try {
                OrderElement orderElement = orderElementDAO
                        .findUniqueByCode(orderElementWithAdvanceMeasurementsOrEndDateDTO.code);

                Scenario scenarioMaster = PredefinedScenarios.MASTER.getScenario();
                Order order = orderDAO.loadOrderAvoidingProxyFor(orderElement);
                OrderVersion orderVersion = order.getScenarios().get(scenarioMaster);

                if (orderElementWithAdvanceMeasurementsOrEndDateDTO.advanceMeasurements != null
                        && !orderElementWithAdvanceMeasurementsOrEndDateDTO.advanceMeasurements
                                .isEmpty()) {
                    updateAdvances(orderVersion, orderElement,
                            orderElementWithAdvanceMeasurementsOrEndDateDTO);
                }

                if (orderElementWithAdvanceMeasurementsOrEndDateDTO.endDateCommunicationToCustomerDTO != null) {
                    updateEndDate(orderVersion, orderElement,
                            orderElementWithAdvanceMeasurementsOrEndDateDTO);
                }

            } catch (ValidationException e) {
                instanceConstraintViolationsDTO = ConstraintViolationConverter.toDTO(
                        Util.generateInstanceId(1,
                                orderElementWithAdvanceMeasurementsOrEndDateDTO.code), e
                                .getInvalidValues());
            } catch (InstanceNotFoundException e) {
                return getErrorMessage(orderElementWithAdvanceMeasurementsOrEndDateDTO.code,
                        "instance not found");
            }
        }

        if (instanceConstraintViolationsDTO != null) {
            instanceConstraintViolationsList.add(instanceConstraintViolationsDTO);
        }

        return new InstanceConstraintViolationsListDTO(instanceConstraintViolationsList);
    }

    @Transactional
    public InstanceConstraintViolationsListDTO updateAdvances(
            OrderVersion orderVersion,
            OrderElement orderElement,
            OrderElementWithAdvanceMeasurementsOrEndDateDTO orderElementWithAdvanceMeasurementsOrEndDateDTO) {
        DirectAdvanceAssignment advanceAssignmentSubcontractor = orderElement
                .getDirectAdvanceAssignmentSubcontractor();

        if (advanceAssignmentSubcontractor == null) {
            DirectAdvanceAssignment reportGlobal = orderElement.getReportGlobalAdvanceAssignment();

            advanceAssignmentSubcontractor = DirectAdvanceAssignment.create((reportGlobal == null),
                    new BigDecimal(100));
            advanceAssignmentSubcontractor.setAdvanceType(PredefinedAdvancedTypes.SUBCONTRACTOR
                    .getType());
            advanceAssignmentSubcontractor.setOrderElement(orderElement);

            try {
                orderElement.addAdvanceAssignment(advanceAssignmentSubcontractor);
            } catch (DuplicateValueTrueReportGlobalAdvanceException e) {
                // This shouldn't happen, because new advance is only
                // marked as report global if there is not other advance
                // as report global
                throw new RuntimeException(e);
            } catch (DuplicateAdvanceAssignmentForOrderElementException e) {
                return getErrorMessage(orderElementWithAdvanceMeasurementsOrEndDateDTO.code,
                        "someone in the same branch has the same advance type");
            }
        }

        for (AdvanceMeasurementDTO advanceMeasurementDTO : orderElementWithAdvanceMeasurementsOrEndDateDTO.advanceMeasurements) {
            AdvanceMeasurement advanceMeasurement = advanceAssignmentSubcontractor
                    .getAdvanceMeasurementAtExactDate(DateConverter
                            .toLocalDate(advanceMeasurementDTO.date));
            if (advanceMeasurement == null) {
                advanceAssignmentSubcontractor.addAdvanceMeasurements(OrderElementConverter
                        .toEntity(advanceMeasurementDTO));
            } else {
                advanceMeasurement.setValue(advanceMeasurementDTO.value);
            }
        }

        // set the advance assingment subcontractor like spread
        AdvanceAssignment spreadAdvance = orderElement.getReportGlobalAdvanceAssignment();
        if (spreadAdvance != null && !spreadAdvance.equals(advanceAssignmentSubcontractor)) {
            spreadAdvance.setReportGlobalAdvance(false);
            advanceAssignmentSubcontractor.setReportGlobalAdvance(true);
        }
        // update the advance percentage in its related task
        updateAdvancePercentage(orderVersion, orderElement);

        orderElement.validate();
        orderElementDAO.save(orderElement);

        /*
         * If the order element is subcontrated then create the subcontrated
         * communication for the subcontrated task data to which the order
         * element belongs.
         */
        try {
            createSubcontractorCommunicationWithNewProgress(orderElement,
                    orderElementWithAdvanceMeasurementsOrEndDateDTO.advanceMeasurements);
        } catch (InstanceNotFoundException e) {
            return getErrorMessage(orderElementWithAdvanceMeasurementsOrEndDateDTO.code,
                    "instance not found");
        }

        return null;
    }

    public void createSubcontractorCommunicationWithNewProgress(OrderElement orderElement,
            Set<AdvanceMeasurementDTO> advanceMeasurementDTOs) throws InstanceNotFoundException {
        if (orderElement != null && orderElement.getTaskSource() != null
                && orderElement.getTaskSource().getTask().isSubcontracted()) {
            Task task = (Task) orderElement.getTaskSource().getTask();
            SubcontractedTaskData subcontractedTaskData = task.getSubcontractedTaskData();
            if (subcontractedTaskData != null) {
                SubcontractorCommunication subcontractorCommunication = SubcontractorCommunication
                        .create(subcontractedTaskData, CommunicationType.PROGRESS_UPDATE,
                                new Date(), false);

                for (AdvanceMeasurementDTO advanceMeasurementDTO : advanceMeasurementDTOs) {
                    // add subcontractorCommunicationValue
                    addSubcontractorCommunicationValue(advanceMeasurementDTO,
                            subcontractorCommunication);
                }
                subcontractorCommunicationDAO.save(subcontractorCommunication);
            }
        }
    }

    @Transactional
    public InstanceConstraintViolationsListDTO updateEndDate(
            OrderVersion orderVersion,
            OrderElement orderElement,
            OrderElementWithAdvanceMeasurementsOrEndDateDTO orderElementWithAdvanceMeasurementsOrEndDateDTO) {
        try {
            orderElement.useSchedulingDataFor(orderVersion);

            if (orderElement != null && orderElement.getTaskSource() != null
                    && orderElement.getTaskSource().getTask().isSubcontracted()) {

                Task task = (Task) orderElement.getTaskSource().getTask();
                SubcontractedTaskData subcontractedTaskData = task.getSubcontractedTaskData();
                EndDateCommunicationToCustomerDTO endDateDTO = orderElementWithAdvanceMeasurementsOrEndDateDTO.endDateCommunicationToCustomerDTO;

                Date endDate = DateConverter.toDate(endDateDTO.endDate);
                Date communicationDate = DateConverter.toDate(endDateDTO.communicationDate);

                subcontractedTaskData.getEndDatesCommunicatedFromSubcontractor().add(
                        EndDateCommunication.create(new Date(), endDate,
                                communicationDate));
                subcontractedTaskDataDAO.save(subcontractedTaskData);

                createSubcontractorCommunicationWithNewEndDate(subcontractedTaskData, endDateDTO);
            }
        } catch (InstanceNotFoundException e) {
            return getErrorMessage(orderElementWithAdvanceMeasurementsOrEndDateDTO.code,
                    "instance not found");
        }
        return null;
    }

    public void createSubcontractorCommunicationWithNewEndDate(
            SubcontractedTaskData subcontractedTaskData,
            EndDateCommunicationToCustomerDTO endDateDTO) throws InstanceNotFoundException {

            if (subcontractedTaskData != null) {
                SubcontractorCommunication subcontractorCommunication = SubcontractorCommunication
                        .create(subcontractedTaskData, CommunicationType.END_DATE_UPDATE,
                                new Date(), false);
                Date dateValue = DateConverter.toDate(endDateDTO.endDate);
                SubcontractorCommunicationValue value = SubcontractorCommunicationValue.create(
                        dateValue, null);
                subcontractorCommunication.getSubcontractorCommunicationValues().add(value);
                subcontractorCommunicationDAO.save(subcontractorCommunication);
            }
    }

    private void addSubcontractorCommunicationValue(AdvanceMeasurementDTO advanceMeasurementDTO,
            SubcontractorCommunication subcontractorCommunication) {
        Date dateValue = DateConverter.toDate(advanceMeasurementDTO.date);
        SubcontractorCommunicationValue value = SubcontractorCommunicationValue.create(dateValue,
                advanceMeasurementDTO.value);
        subcontractorCommunication.getSubcontractorCommunicationValues().add(value);
    }

    private void updateAdvancePercentage(OrderVersion orderVersion, OrderElement orderElement) {
        orderElement.useSchedulingDataFor(orderVersion);
        OrderElement parent = orderElement.getParent();
        while (parent != null) {
            parent.useSchedulingDataFor(orderVersion);
            parent = parent.getParent();
        }
        orderElement.updateAdvancePercentageTaskElement();
    }
}
