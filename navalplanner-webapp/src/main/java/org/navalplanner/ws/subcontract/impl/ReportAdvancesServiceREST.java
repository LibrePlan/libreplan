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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.advance.bootstrap.PredefinedAdvancedTypes;
import org.navalplanner.business.advance.entities.AdvanceAssignment;
import org.navalplanner.business.advance.entities.AdvanceMeasurement;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.advance.exceptions.DuplicateAdvanceAssignmentForOrderElementException;
import org.navalplanner.business.advance.exceptions.DuplicateValueTrueReportGlobalAdvanceException;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.externalcompanies.daos.IExternalCompanyDAO;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.scenarios.bootstrap.PredefinedScenarios;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.ws.common.api.AdvanceMeasurementDTO;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsDTO;
import org.navalplanner.ws.common.api.InstanceConstraintViolationsListDTO;
import org.navalplanner.ws.common.impl.ConstraintViolationConverter;
import org.navalplanner.ws.common.impl.DateConverter;
import org.navalplanner.ws.common.impl.OrderElementConverter;
import org.navalplanner.ws.common.impl.Util;
import org.navalplanner.ws.subcontract.api.IReportAdvancesService;
import org.navalplanner.ws.subcontract.api.OrderElementWithAdvanceMeasurementsDTO;
import org.navalplanner.ws.subcontract.api.OrderElementWithAdvanceMeasurementsListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * REST-based implementation of {@link IReportAdvancesService}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Path("/reportadvances/")
@Produces("application/xml")
@Service("reportAdvancesServiceREST")
public class ReportAdvancesServiceREST implements IReportAdvancesService {

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IExternalCompanyDAO externalCompanyDAO;

    private InstanceConstraintViolationsListDTO getErrorMessage(String code,
            String message) {
        // FIXME review errors returned
        return new InstanceConstraintViolationsListDTO(Arrays
                .asList(InstanceConstraintViolationsDTO.create(Util
                        .generateInstanceId(1, code), message)));
    }

    @Override
    @POST
    @Consumes("application/xml")
    @Transactional
    public InstanceConstraintViolationsListDTO updateAdvances(OrderElementWithAdvanceMeasurementsListDTO orderElementWithAdvanceMeasurementsListDTO) {

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = new ArrayList<InstanceConstraintViolationsDTO>();

        InstanceConstraintViolationsDTO instanceConstraintViolationsDTO = null;

        if (StringUtils
                .isEmpty(orderElementWithAdvanceMeasurementsListDTO.externalCompanyNif)) {
            return getErrorMessage("", "external company ID not specified");
        }

        ExternalCompany externalCompany;
        try {
            externalCompany = externalCompanyDAO
                    .findUniqueByNif(orderElementWithAdvanceMeasurementsListDTO.externalCompanyNif);
        } catch (InstanceNotFoundException e1) {
            return getErrorMessage(
                    orderElementWithAdvanceMeasurementsListDTO.externalCompanyNif,
                    "external company not found");
        }

        if (!externalCompany.isSubcontractor()) {
            return getErrorMessage(
                    orderElementWithAdvanceMeasurementsListDTO.externalCompanyNif,
                    "external company is not registered as subcontractor");
        }

        List<OrderElementWithAdvanceMeasurementsDTO> orderElements = orderElementWithAdvanceMeasurementsListDTO.orderElements;
        for (OrderElementWithAdvanceMeasurementsDTO orderElementWithAdvanceMeasurementsDTO : orderElements) {
            try {
                OrderElement orderElement = orderElementDAO
                        .findUniqueByCode(orderElementWithAdvanceMeasurementsDTO.code);

                DirectAdvanceAssignment advanceAssignmentSubcontractor = orderElement
                        .getDirectAdvanceAssignmentSubcontractor();

                if (advanceAssignmentSubcontractor == null) {
                    DirectAdvanceAssignment reportGlobal = orderElement
                            .getReportGlobalAdvanceAssignment();

                    advanceAssignmentSubcontractor = DirectAdvanceAssignment
                            .create((reportGlobal == null), new BigDecimal(100));
                    advanceAssignmentSubcontractor
                            .setAdvanceType(PredefinedAdvancedTypes.SUBCONTRACTOR
                                    .getType());
                    advanceAssignmentSubcontractor
                            .setOrderElement(orderElement);

                    try {
                        orderElement
                                .addAdvanceAssignment(advanceAssignmentSubcontractor);
                    } catch (DuplicateValueTrueReportGlobalAdvanceException e) {
                        // This shouldn't happen, because new advance is only
                        // marked as report global if there is not other advance
                        // as report global
                        throw new RuntimeException(e);
                    } catch (DuplicateAdvanceAssignmentForOrderElementException e) {
                        return getErrorMessage(
                                orderElementWithAdvanceMeasurementsDTO.code,
                                "someone in the same branch has the same advance type");
                    }
                }

                for (AdvanceMeasurementDTO advanceMeasurementDTO : orderElementWithAdvanceMeasurementsDTO.advanceMeasurements) {
                    AdvanceMeasurement advanceMeasurement = advanceAssignmentSubcontractor
                            .getAdvanceMeasurement(DateConverter
                                    .toLocalDate(advanceMeasurementDTO.date));
                    if (advanceMeasurement == null) {
                        advanceAssignmentSubcontractor
                                .addAdvanceMeasurements(OrderElementConverter
                                        .toEntity(advanceMeasurementDTO));
                    } else {
                        advanceMeasurement
                                .setValue(advanceMeasurementDTO.value);
                    }
                }

                // set the advance assingment subcontractor like spread
                AdvanceAssignment spreadAdvance = orderElement
                        .getReportGlobalAdvanceAssignment();
                if (spreadAdvance != null
                        && !spreadAdvance
                                .equals(advanceAssignmentSubcontractor)) {
                    spreadAdvance.setReportGlobalAdvance(false);
                    advanceAssignmentSubcontractor.setReportGlobalAdvance(true);
                }
                // update the advance percentage in its related task
                Scenario scenarioMaster = PredefinedScenarios.MASTER
                        .getScenario();
                Order order = orderElementDAO
                        .loadOrderAvoidingProxyFor(orderElement);
                OrderVersion orderVersion = order.getScenarios().get(
                        scenarioMaster);
                updateAdvancePercentage(orderVersion, orderElement);

                orderElement.validate();
                orderElementDAO.save(orderElement);
            } catch (ValidationException e) {
                instanceConstraintViolationsDTO = ConstraintViolationConverter
                        .toDTO(Util.generateInstanceId(1,
                                orderElementWithAdvanceMeasurementsDTO.code), e
                                .getInvalidValues());
            } catch (InstanceNotFoundException e) {
                return getErrorMessage(
                        orderElementWithAdvanceMeasurementsDTO.code,
                        "instance not found");
            }
        }

        if (instanceConstraintViolationsDTO != null) {
            instanceConstraintViolationsList
                    .add(instanceConstraintViolationsDTO);
        }

        return new InstanceConstraintViolationsListDTO(
                instanceConstraintViolationsList);
    }

    private void updateAdvancePercentage(OrderVersion orderVersion,
            OrderElement orderElement) {
        orderElement.useSchedulingDataFor(orderVersion);
        orderElement.updateAdvancePercentageTaskElement();
        OrderElement parent = orderElement.getParent();
        while (parent != null) {
            parent.useSchedulingDataFor(orderVersion);
            parent.updateAdvancePercentageTaskElement();
            parent = parent.getParent();
        }
    }
}
