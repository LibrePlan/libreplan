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

import java.util.List;

import org.libreplan.business.externalcompanies.daos.ICustomerComunicationDAO;
import org.libreplan.business.externalcompanies.entities.CustomerComunication;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/subcontract/customerComunication.zul")
public class CustomerComunicationModel implements ICustomerComunicationModel{

    @Autowired
    private ICustomerComunicationDAO customerComunicationDAO;

    private FilterComunicationEnum currentFilter = FilterComunicationEnum.NOT_REVIEWED;

    @Override
    @Transactional
    public void confirmSave(CustomerComunication customerComunication){
        customerComunicationDAO.save(customerComunication);
    }

    @Override
    @Transactional
    public List<CustomerComunication> getCustomerAllComunications(){
        List<CustomerComunication> list = customerComunicationDAO.getAll();
        forceLoadAssociatedData(list);
        return list;
    }

    @Override
    @Transactional
    public List<CustomerComunication> getCustomerComunicationWithoutReviewed(){
        List<CustomerComunication> list = customerComunicationDAO.getAllNotReviewed();
        forceLoadAssociatedData(list);
        return list;
    }

    private void forceLoadAssociatedData(List<CustomerComunication> customerComunicationList){
        if (customerComunicationList != null) {
            for (CustomerComunication customerComunication : customerComunicationList) {
                customerComunication.getOrder().getName();
            }
        }
    }

    @Override
    public void setCurrentFilter(FilterComunicationEnum currentFilter) {
        this.currentFilter = currentFilter;
    }

    @Override
    public FilterComunicationEnum getCurrentFilter() {
        return currentFilter;
    }
}
