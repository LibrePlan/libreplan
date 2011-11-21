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

package org.libreplan.web.subcontract;

import java.util.List;

import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.daos.ISubcontractorComunicationDAO;
import org.libreplan.business.planner.entities.SubcontractedTaskData;
import org.libreplan.business.planner.entities.SubcontractorComunication;
import org.libreplan.business.planner.entities.SubcontractorComunicationValue;
import org.libreplan.business.planner.entities.Task;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/subcontract/subcontractorComunication.zul")
public class SubcontractorComunicationModel implements ISubcontractorComunicationModel{

    @Autowired
    private ISubcontractorComunicationDAO subcontractorComunicationDAO;

    @Autowired
    IOrderDAO orderDAO;

    private FilterComunicationEnum currentFilter = FilterComunicationEnum.NOT_REVIEWED;

    @Override
    @Transactional
    public void confirmSave(SubcontractorComunication subcontractorComunication){
        subcontractorComunicationDAO.save(subcontractorComunication);
    }

    @Override
    @Transactional
    public List<SubcontractorComunication> getSubcontractorAllComunications(){
        List<SubcontractorComunication> list = subcontractorComunicationDAO.getAll();
        forceLoadAssociatedData(list);
        return list;
    }

    @Override
    @Transactional
    public List<SubcontractorComunication> getSubcontractorComunicationWithoutReviewed(){
        List<SubcontractorComunication> list = subcontractorComunicationDAO.getAllNotReviewed();
        forceLoadAssociatedData(list);
        return list;
    }

    private void forceLoadAssociatedData(List<SubcontractorComunication> subcontractorComunicationList){
        if (subcontractorComunicationList != null) {
            for (SubcontractorComunication subcontractorComunication : subcontractorComunicationList) {
                subcontractorComunication.getSubcontractedTaskData().getExternalCompany().getName();
                subcontractorComunication.getSubcontractedTaskData().getTask().getName();
                subcontractorComunication.getSubcontractedTaskData().getTask().getOrderElement().getName();
                subcontractorComunication.getLastSubcontratorComunicationValues().getDate();
            }
        }
    }

    private void forceLoadAssociatedDataValue(List<SubcontractorComunicationValue> subcontractorComunicationValueList){
        if (subcontractorComunicationValueList != null) {
            for (SubcontractorComunicationValue value : subcontractorComunicationValueList) {
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
    public void setCurrentFilter(FilterComunicationEnum currentFilter) {
        this.currentFilter = currentFilter;
    }

    @Override
    public FilterComunicationEnum getCurrentFilter() {
        return currentFilter;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrder(OrderElement orderElement) {
        return (orderDAO.loadOrderAvoidingProxyFor(orderElement)).getOrder();
    }
}