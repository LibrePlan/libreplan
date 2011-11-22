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

import org.libreplan.business.externalcompanies.daos.ICustomerCommunicationDAO;
import org.libreplan.business.externalcompanies.entities.CustomerCommunication;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/subcontract/customerCommunication.zul")
public class CustomerCommunicationModel implements ICustomerCommunicationModel{

    @Autowired
    private ICustomerCommunicationDAO customerCommunicationDAO;

    private FilterCommunicationEnum currentFilter = FilterCommunicationEnum.NOT_REVIEWED;

    @Override
    @Transactional
    public void confirmSave(CustomerCommunication customerCommunication){
        customerCommunicationDAO.save(customerCommunication);
    }

    @Override
    @Transactional
    public List<CustomerCommunication> getCustomerAllCommunications(){
        List<CustomerCommunication> list = customerCommunicationDAO.getAll();
        forceLoadAssociatedData(list);
        return list;
    }

    @Override
    @Transactional
    public List<CustomerCommunication> getCustomerCommunicationWithoutReviewed(){
        List<CustomerCommunication> list = customerCommunicationDAO.getAllNotReviewed();
        forceLoadAssociatedData(list);
        return list;
    }

    private void forceLoadAssociatedData(List<CustomerCommunication> customerCommunicationList){
        if (customerCommunicationList != null) {
            for (CustomerCommunication customerCommunication : customerCommunicationList) {
                customerCommunication.getOrder().getName();
            }
        }
    }

    @Override
    public void setCurrentFilter(FilterCommunicationEnum currentFilter) {
        this.currentFilter = currentFilter;
    }

    @Override
    public FilterCommunicationEnum getCurrentFilter() {
        return currentFilter;
    }
}
