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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.externalcompanies.entities.ExternalCompany;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.daos.ISubcontractedTaskDataDAO;
import org.libreplan.business.planner.entities.SubcontractState;
import org.libreplan.business.planner.entities.SubcontractedTaskData;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.libreplan.web.subcontract.exceptions.ConnectionProblemsException;
import org.libreplan.web.subcontract.exceptions.UnrecoverableErrorServiceException;
import org.libreplan.ws.cert.NaiveTrustProvider;
import org.libreplan.ws.common.api.ConstraintViolationDTO;
import org.libreplan.ws.common.api.InstanceConstraintViolationsDTO;
import org.libreplan.ws.common.api.InstanceConstraintViolationsListDTO;
import org.libreplan.ws.common.api.OrderElementDTO;
import org.libreplan.ws.common.impl.ConfigurationOrderElementConverter;
import org.libreplan.ws.common.impl.DateConverter;
import org.libreplan.ws.common.impl.OrderElementConverter;
import org.libreplan.ws.common.impl.Util;
import org.libreplan.ws.subcontract.api.SubcontractedTaskDataDTO;
import org.libreplan.ws.subcontract.impl.SubcontractedTaskDataConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for operations related with subcontracted tasks.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Bogdan Bodnarjuk <b.bodnarjuk@libreplan-enterprise.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/subcontract/subcontractedTasks.zul")
public class SubcontractedTasksModel implements ISubcontractedTasksModel {

    private static Log LOG = LogFactory.getLog(SubcontractedTasksModel.class);

    private String CONNECTION_PROBLEM = "Problems connecting with subcontractor web service";

    @Autowired
    private ISubcontractedTaskDataDAO subcontractedTaskDataDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Override
    @Transactional(readOnly = true)
    public List<SubcontractedTaskData> getSubcontractedTasks() {
        List<SubcontractedTaskData> result = subcontractedTaskDataDAO.getAllForMasterScenario();
        for (SubcontractedTaskData subcontractedTaskData : result) {
            forceLoadExternalCompany(subcontractedTaskData);
            forceLastDeliveryDate(subcontractedTaskData);
        }

        return sortByState(result);
    }

    private void forceLastDeliveryDate(SubcontractedTaskData subcontractedTaskData) {
        subcontractedTaskData.getLastRequiredDeliverDate();
    }

    private void forceLoadExternalCompany(SubcontractedTaskData subcontractedTaskData) {
        subcontractedTaskData.getExternalCompany().getName();
    }

    private List<SubcontractedTaskData> sortByState(List<SubcontractedTaskData> tasks) {
        Collections.sort(tasks, (arg0, arg1) -> {
            if ( (arg0 == null) || (arg0.getState() == null) ) {
                return -1;
            }

            if ( (arg1 == null) || (arg1.getState() == null) ) {
                return 1;
            }

            if ( arg0.getState().equals(arg1.getState()) ) {
                return 0;
            }

            if ( arg0.getState().equals(SubcontractState.PENDING_INITIAL_SEND) ) {
                return 1;
            }

            if ( arg1.getState().equals(SubcontractState.PENDING_INITIAL_SEND) ) {
                return -1;
            }

            if( arg0.getState().equals(SubcontractState.PENDING_UPDATE_DELIVERING_DATE) ) {
                return 1;
            }

            return -1;
        });

        return tasks;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrder(SubcontractedTaskData subcontractedTaskData) {
        Task task = subcontractedTaskData.getTask();
        OrderElement orderElement = orderDAO.loadOrderAvoidingProxyFor(task.getOrderElement());

        return orderElement.getOrder();
    }

    @Override
    @Transactional
    public void sendToSubcontractor(SubcontractedTaskData subcontractedTaskData)
            throws ConnectionProblemsException, UnrecoverableErrorServiceException {

        subcontractedTaskDataDAO.save(subcontractedTaskData);

        SubcontractState currentState = subcontractedTaskData.getState();

        if ( currentState.equals(SubcontractState.PENDING_INITIAL_SEND) ) {
            subcontractedTaskData.setState(SubcontractState.FAILED_SENT);
        } else if ( currentState.equals(SubcontractState.PENDING_UPDATE_DELIVERING_DATE) ) {
            subcontractedTaskData.setState(SubcontractState.FAILED_UPDATE);
        }

        if ( !subcontractedTaskData.isSendable() ) {
            throw new RuntimeException("Subcontracted task already sent");
        }

        if ( !subcontractedTaskData.getExternalCompany().getInteractsWithApplications() ) {
            throw new RuntimeException("External company has not interaction fields filled");
        }

        makeSubcontractRequest(subcontractedTaskData,currentState);

        Date today = new Date();
        if (currentState.equals(SubcontractState.PENDING_INITIAL_SEND) ||
                currentState.equals(SubcontractState.FAILED_SENT)) {

            subcontractedTaskData.setSubcontractCommunicationDate(today);
        }

        // Update the first required deliver date
        subcontractedTaskData.updateFirstRequiredDeliverDate(today);
        subcontractedTaskData.setSubcontractCommunicationDate(today);
        subcontractedTaskData.setState(SubcontractState.SUCCESS_SENT);
    }

    private void makeSubcontractRequest(SubcontractedTaskData subcontractedTaskData,
                                        SubcontractState currentState)
            throws ConnectionProblemsException, UnrecoverableErrorServiceException {

        if ( subcontractedTaskData.getState() != null ) {

            if ( currentState.equals(SubcontractState.PENDING_INITIAL_SEND) ||
                    currentState.equals(SubcontractState.FAILED_SENT) ) {

                makeSubcontractRequestRequest_InitialSent(subcontractedTaskData);

            } else if ( currentState.equals(SubcontractState.PENDING_UPDATE_DELIVERING_DATE) ||
                    currentState.equals(SubcontractState.FAILED_UPDATE) ) {

                makeSubcontractRequestRequest_UpdateDeliverDate(subcontractedTaskData);
            }
        }
    }

    private void makeSubcontractRequestRequest_UpdateDeliverDate(SubcontractedTaskData subcontractedTaskData)
            throws ConnectionProblemsException, UnrecoverableErrorServiceException {

        UpdateDeliveringDateDTO updateDeliveringDateDTO =
                SubcontractedTaskDataConverter.toUpdateDeliveringDateDTO(subcontractedTaskData.getExternalCompany().getNif(),
                        subcontractedTaskData);

        ExternalCompany externalCompany = subcontractedTaskData.getExternalCompany();

        NaiveTrustProvider.setAlwaysTrust(true);

        try {
            WebClient client = WebClient.create(externalCompany.getAppURI());
            client.path("ws/rest/subcontracting/subcontract/update");

            Util.addAuthorizationHeader(
                    client, externalCompany.getOurCompanyLogin(), externalCompany.getOurCompanyPassword());

            InstanceConstraintViolationsListDTO instanceConstraintViolationsListDTO =
                    client.post(updateDeliveringDateDTO, InstanceConstraintViolationsListDTO.class);

            List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
                    instanceConstraintViolationsListDTO.instanceConstraintViolationsList;

            checkConstraintViolations(instanceConstraintViolationsList);

        } catch (WebApplicationException e) {
            LOG.error(CONNECTION_PROBLEM, e);

            String message = _(CONNECTION_PROBLEM);
            if ( e.getMessage() != null ) {
                message += ". " + _("Error: {0}", e.getMessage());
            }

            throw new ConnectionProblemsException(message, e);
        }
    }

    private void makeSubcontractRequestRequest_InitialSent(SubcontractedTaskData subcontractedTaskData)
            throws ConnectionProblemsException, UnrecoverableErrorServiceException {

        SubcontractedTaskDataDTO subcontractedTaskDataDTO = getSubcontractedTaskData(subcontractedTaskData);

        ExternalCompany externalCompany = subcontractedTaskData.getExternalCompany();

        NaiveTrustProvider.setAlwaysTrust(true);

        try {
            WebClient client = WebClient.create(externalCompany.getAppURI());

            client.path("ws/rest/subcontracting/subcontract/create");

            Util.addAuthorizationHeader(
                    client, externalCompany.getOurCompanyLogin(), externalCompany.getOurCompanyPassword());

            InstanceConstraintViolationsListDTO instanceConstraintViolationsListDTO =
                    client.post(subcontractedTaskDataDTO, InstanceConstraintViolationsListDTO.class);

            List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
                    instanceConstraintViolationsListDTO.instanceConstraintViolationsList;

            checkConstraintViolations(instanceConstraintViolationsList);

        } catch (WebApplicationException e) {
            LOG.error(CONNECTION_PROBLEM, e);

            String message = _(CONNECTION_PROBLEM);
            if ( e.getMessage() != null ) {
                message += ". " + _("Error: {0}", e.getMessage());
            }

            throw new ConnectionProblemsException(message, e);
        }
    }

    private void checkConstraintViolations(List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList)
            throws UnrecoverableErrorServiceException {

        if ( instanceConstraintViolationsList != null && !instanceConstraintViolationsList.isEmpty() ) {
            StringBuilder stringBuilder = new StringBuilder();

            for (ConstraintViolationDTO current :  instanceConstraintViolationsList.get(0).constraintViolations) {
                stringBuilder.append(current.toString()).append("\n");
            }

            throw new UnrecoverableErrorServiceException(stringBuilder.toString());
        }
    }

    private SubcontractedTaskDataDTO getSubcontractedTaskData(SubcontractedTaskData subcontractedTaskData) {
        return SubcontractedTaskDataConverter.toDTO(
                subcontractedTaskData.getExternalCompany().getNif(),
                subcontractedTaskData,
                getOrderElement(subcontractedTaskData));
    }

    private OrderElementDTO getOrderElement(SubcontractedTaskData subcontractedTaskData) {
        OrderElement orderElement;

        try {
            orderElement = orderElementDAO.find(subcontractedTaskData.getTask().getOrderElement().getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }

        if ( subcontractedTaskData.isNodeWithoutChildrenExported() ) {
            orderElement = orderElement.calculateOrderLineForSubcontract();
        }

        OrderElementDTO orderElementDTO =
                OrderElementConverter.toDTO(orderElement, getConfiguration(subcontractedTaskData));

        overrideDateInformationForRootNode(orderElementDTO, subcontractedTaskData.getTask());

        return orderElementDTO;
    }

    private void overrideDateInformationForRootNode(OrderElementDTO orderElementDTO, Task task) {
        orderElementDTO.initDate = DateConverter.toXMLGregorianCalendar(task.getStartDate());
        orderElementDTO.deadline = DateConverter.toXMLGregorianCalendar(task.getEndDate());
    }

    private ConfigurationOrderElementConverter getConfiguration(SubcontractedTaskData subcontractedTaskData) {
        // Never export criterions and advances to subcontract
        return ConfigurationOrderElementConverter.create(
                subcontractedTaskData.isLabelsExported(),
                subcontractedTaskData.isMaterialAssignmentsExported(),
                false,
                subcontractedTaskData.isHoursGroupsExported(),
                false);
    }

    @Override
    @Transactional(readOnly = true)
    public String exportXML(SubcontractedTaskData subcontractedTaskData) {
        SubcontractState currentState = subcontractedTaskData.getState();

        if (currentState.equals(SubcontractState.PENDING_INITIAL_SEND) ||
                currentState.equals(SubcontractState.FAILED_SENT) ) {
            return exportXML_CreateSubcontractor(subcontractedTaskData);
        } else {
            return exportXML_UpdateSubcontractor(subcontractedTaskData);
        }
    }

    public String exportXML_CreateSubcontractor(SubcontractedTaskData subcontractedTaskData){
        SubcontractedTaskDataDTO subcontractedTaskDataDTO = getSubcontractedTaskData(subcontractedTaskData);

        StringWriter xml = new StringWriter();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(SubcontractedTaskDataDTO.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(subcontractedTaskDataDTO, xml);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return xml.toString();
    }

    public String exportXML_UpdateSubcontractor(SubcontractedTaskData subcontractedTaskData){
        subcontractedTaskDataDAO.reattachUnmodifiedEntity(subcontractedTaskData);

        UpdateDeliveringDateDTO updateDeliveringDateDTO =
                SubcontractedTaskDataConverter.toUpdateDeliveringDateDTO(subcontractedTaskData.getExternalCompany().getNif(),
                        subcontractedTaskData);

        StringWriter xml = new StringWriter();

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(UpdateDeliveringDateDTO.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(updateDeliveringDateDTO, xml);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return xml.toString();
    }
}
