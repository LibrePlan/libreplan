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
package org.libreplan.web.subcontract;

import static org.libreplan.web.I18nHelper._;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.web.subcontract.exceptions.ConnectionProblemsException;
import org.libreplan.web.subcontract.exceptions.UnrecoverableErrorServiceException;
import org.libreplan.ws.cert.NaiveTrustProvider;
import org.libreplan.ws.common.api.AdvanceMeasurementDTO;
import org.libreplan.ws.common.api.ConstraintViolationDTO;
import org.libreplan.ws.common.api.InstanceConstraintViolationsDTO;
import org.libreplan.ws.common.api.InstanceConstraintViolationsListDTO;
import org.libreplan.ws.common.impl.OrderElementConverter;
import org.libreplan.ws.common.impl.Util;
import org.libreplan.ws.subcontract.api.OrderElementWithAdvanceMeasurementsDTO;
import org.libreplan.ws.subcontract.api.OrderElementWithAdvanceMeasurementsListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for operations related with report advances.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReportAdvancesModel implements IReportAdvancesModel {

    private static Log LOG = LogFactory.getLog(ReportAdvancesModel.class);

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;


    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersWithExternalCodeInAnyOrderElement() {
        List<OrderElement> orderElements = orderElementDAO
                .findOrderElementsWithExternalCode();

        Map<Long, Order> ordersMap = new HashMap<Long, Order>();
        for (OrderElement orderElement : orderElements) {
            Order order = orderDAO.loadOrderAvoidingProxyFor(orderElement);
            if (ordersMap.get(order.getId()) == null) {
                ordersMap.put(order.getId(), order);
                forceLoadHoursGroups(order);
                forceLoadAdvanceAssignments(order);
            }
        }

        return new ArrayList<Order>(ordersMap.values());
    }

    private void forceLoadHoursGroups(OrderElement orderElement) {
        orderElement.getHoursGroups().size();
        for (OrderElement child : orderElement.getChildren()) {
            forceLoadHoursGroups(child);
        }
    }

    private void forceLoadAdvanceAssignments(Order order) {
        order.getDirectAdvanceAssignmentOfTypeSubcontractor();
        if (order.getDirectAdvanceAssignmentOfTypeSubcontractor() != null) {
            order.getDirectAdvanceAssignmentOfTypeSubcontractor()
                .getAdvanceMeasurements().size();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AdvanceMeasurement getLastAdvanceMeasurement(
            DirectAdvanceAssignment directAdvanceAssignment) {
        if (directAdvanceAssignment == null) {
            return null;
        }

        return directAdvanceAssignment.getLastAdvanceMeasurement();
    }

    @Override
    @Transactional(readOnly = true)
    public AdvanceMeasurement getLastAdvanceMeasurementReported(
            DirectAdvanceAssignment directAdvanceAssignment) {
        if (directAdvanceAssignment == null) {
            return null;
        }

        AdvanceMeasurement lastAdvanceMeasurementReported = null;
        for (AdvanceMeasurement advanceMeasurement : directAdvanceAssignment
                .getAdvanceMeasurements()) {
                if (advanceMeasurement.getCommunicationDate() != null) {
                    if (lastAdvanceMeasurementReported == null) {
                        lastAdvanceMeasurementReported = advanceMeasurement;
                    } else {
                        if (advanceMeasurement.getCommunicationDate()
                                .compareTo(
                                        lastAdvanceMeasurementReported
                                                .getCommunicationDate()) > 0) {
                            lastAdvanceMeasurementReported = advanceMeasurement;
                        }
                    }
                }
            }

        return lastAdvanceMeasurementReported;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAnyAdvanceMeasurementNotReported(
            DirectAdvanceAssignment directAdvanceAssignment) {
        if (directAdvanceAssignment == null) {
            return false;
        }

        for (AdvanceMeasurement advanceMeasurement : directAdvanceAssignment
                .getAdvanceMeasurements()) {
            if (advanceMeasurement.getCommunicationDate() == null) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = { ConnectionProblemsException.class,
            UnrecoverableErrorServiceException.class })
    public void sendAdvanceMeasurements(Order order)
            throws UnrecoverableErrorServiceException,
            ConnectionProblemsException {
        orderDAO.save(order);

        OrderElementWithAdvanceMeasurementsListDTO orderElementWithAdvanceMeasurementsListDTO = getOrderElementWithAdvanceMeasurementsListDTO(order);
        ExternalCompany externalCompany = order.getCustomer();

        NaiveTrustProvider.setAlwaysTrust(true);

        WebClient client = WebClient.create(externalCompany.getAppURI());

        client.path("ws/rest/reportadvances");

        Util.addAuthorizationHeader(client, externalCompany
                .getOurCompanyLogin(), externalCompany.getOurCompanyPassword());

        try {
            InstanceConstraintViolationsListDTO instanceConstraintViolationsListDTO = client
                    .post(orderElementWithAdvanceMeasurementsListDTO,
                            InstanceConstraintViolationsListDTO.class);

            List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList = instanceConstraintViolationsListDTO.instanceConstraintViolationsList;
            if ((instanceConstraintViolationsList != null)
                    && (!instanceConstraintViolationsList.isEmpty())) {
                String message = "";

                for (ConstraintViolationDTO constraintViolationDTO : instanceConstraintViolationsList
                        .get(0).constraintViolations) {
                    message += constraintViolationDTO.toString() + "\n";
                }

                throw new UnrecoverableErrorServiceException(message);
            }
        } catch (WebApplicationException e) {
            LOG.error("Problems connecting with client web service", e);

            String message = _("Problems connecting with client web service");
            if (e.getMessage() != null) {
                message += ". " + _("Error: {0}", e.getMessage());
            }

            throw new ConnectionProblemsException(message, e);
        }
    }

    private OrderElementWithAdvanceMeasurementsListDTO getOrderElementWithAdvanceMeasurementsListDTO(
            Order order) {
        List<OrderElementWithAdvanceMeasurementsDTO> orderElementWithAdvanceMeasurementsDTOs = new ArrayList<OrderElementWithAdvanceMeasurementsDTO>();

        DirectAdvanceAssignment directAdvanceAssignment = order
                .getDirectAdvanceAssignmentOfTypeSubcontractor();
        Set<AdvanceMeasurementDTO> advanceMeasurementDTOs = new HashSet<AdvanceMeasurementDTO>();

        for (AdvanceMeasurement advanceMeasurement : directAdvanceAssignment
                .getAdvanceMeasurements()) {
            if (advanceMeasurement.getCommunicationDate() == null) {
                AdvanceMeasurementDTO advanceMeasurementDTO = OrderElementConverter
                        .toDTO(advanceMeasurement);
                advanceMeasurement.updateCommunicationDate(new Date());
                advanceMeasurementDTOs.add(advanceMeasurementDTO);
            }
        }

        if (!advanceMeasurementDTOs.isEmpty()) {
            OrderElementWithAdvanceMeasurementsDTO orderElementWithAdvanceMeasurementsDTO = new OrderElementWithAdvanceMeasurementsDTO(
                    directAdvanceAssignment.getOrderElement().getExternalCode(),
                    advanceMeasurementDTOs);
            orderElementWithAdvanceMeasurementsDTOs
                    .add(orderElementWithAdvanceMeasurementsDTO);
        }

        return new OrderElementWithAdvanceMeasurementsListDTO(getCompanyCode(),
                orderElementWithAdvanceMeasurementsDTOs);
    }

    private String getCompanyCode() {
        return configurationDAO.getConfiguration().getCompanyCode();
    }

    @Override
    @Transactional(readOnly = true)
    public String exportXML(Order order) {
        OrderElementWithAdvanceMeasurementsListDTO orderElementWithAdvanceMeasurementsListDTO = getOrderElementWithAdvanceMeasurementsListDTO(order);

        StringWriter xml = new StringWriter();
        try {
            JAXBContext jaxbContext = JAXBContext
                    .newInstance(OrderElementWithAdvanceMeasurementsListDTO.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(orderElementWithAdvanceMeasurementsListDTO, xml);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return xml.toString();
    }

}
