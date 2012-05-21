/*
 * This file is part of LibrePlan
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

package org.libreplan.web.subcontract;

import java.util.List;

import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.daos.ISubcontractorCommunicationDAO;
import org.libreplan.business.planner.entities.SubcontractedTaskData;
import org.libreplan.business.planner.entities.SubcontractorCommunication;
import org.libreplan.business.planner.entities.SubcontractorCommunicationValue;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/subcontract/subcontractorCommunication.zul")
public class SubcontractorCommunicationModel implements ISubcontractorCommunicationModel{

    @Autowired
    private ISubcontractorCommunicationDAO subcontractorCommunicationDAO;

    @Autowired
    IOrderDAO orderDAO;

    private FilterCommunicationEnum currentFilter = FilterCommunicationEnum.NOT_REVIEWED;

    @Override
    @Transactional
    public void confirmSave(SubcontractorCommunication subcontractorCommunication){
        subcontractorCommunicationDAO.save(subcontractorCommunication);
    }

    @Override
    @Transactional
    public List<SubcontractorCommunication> getSubcontractorAllCommunications(){
        List<SubcontractorCommunication> list = subcontractorCommunicationDAO.getAll();
        forceLoadAssociatedData(list);
        return list;
    }

    @Override
    @Transactional
    public List<SubcontractorCommunication> getSubcontractorCommunicationWithoutReviewed(){
        List<SubcontractorCommunication> list = subcontractorCommunicationDAO.getAllNotReviewed();
        forceLoadAssociatedData(list);
        return list;
    }

    private void forceLoadAssociatedData(List<SubcontractorCommunication> subcontractorCommunicationList){
        if (subcontractorCommunicationList != null) {
            for (SubcontractorCommunication subcontractorCommunication : subcontractorCommunicationList) {
                subcontractorCommunication.getSubcontractedTaskData().getExternalCompany().getName();
                subcontractorCommunication.getSubcontractedTaskData().getTask().getName();
                subcontractorCommunication.getSubcontractedTaskData().getTask().getOrderElement().getName();
                subcontractorCommunication.getLastSubcontractorCommunicationValues().getDate();
            }
        }
    }

    private void forceLoadAssociatedDataValue(List<SubcontractorCommunicationValue> subcontractorCommunicationValueList){
        if (subcontractorCommunicationValueList != null) {
            for (SubcontractorCommunicationValue value : subcontractorCommunicationValueList) {
                value.getDate();
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getOrderCode(SubcontractedTaskData subcontractedTaskData) {
        Task task = subcontractedTaskData.getTask();
        OrderElement orderElement = orderDAO.loadOrderAvoidingProxyFor(task
                .getOrderElement());
        return orderElement.getOrder().getCode();
    }

    @Override
    @Transactional(readOnly = true)
    public String getOrderName(SubcontractedTaskData subcontractedTaskData) {
        Task task = subcontractedTaskData.getTask();
        OrderElement orderElement = orderDAO.loadOrderAvoidingProxyFor(task
                .getOrderElement());
        return orderElement.getOrder().getName();
    }

    @Override
    public void setCurrentFilter(FilterCommunicationEnum currentFilter) {
        this.currentFilter = currentFilter;
    }

    @Override
    public FilterCommunicationEnum getCurrentFilter() {
        return currentFilter;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrder(OrderElement orderElement) {
        return (orderDAO.loadOrderAvoidingProxyFor(orderElement)).getOrder();
    }
}