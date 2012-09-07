/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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

package org.libreplan.web.reports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.reports.dtos.BudgetElementDTO;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.templates.daos.IOrderElementTemplateDAO;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Model for Budget report.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BudgetReportModel implements IBudgetReportModel {

    @Autowired
    private IOrderElementTemplateDAO orderElementTemplateDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Override
    @Transactional(readOnly = true)
    public List<BudgetElementDTO> getBudgetElementDTOs(Order order) {
        OrderElementTemplate orderElementTemplate;
        try {
            orderElementTemplate = orderElementTemplateDAO.find(order
                    .getAssociatedBudgetObject().getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }

        List<BudgetElementDTO> dtos = new ArrayList<BudgetElementDTO>();

        for (OrderElementTemplate template : orderElementTemplate
                .getAllChildren()) {
            dtos.add(new BudgetElementDTO(template));
        }

        return dtos;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrders() {
        List<Order> result = orderDAO.getOrdersByReadAuthorizationByScenario(
                SecurityUtils.getSessionUserLoginName(),
                scenarioManager.getCurrent());
        Collections.sort(result);
        return result;
    }

}
