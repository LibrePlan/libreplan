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

import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.reports.dtos.ProjectStatusReportDTO;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Model for Project Status report.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ProjectStatusReportModel implements IProjectStatusReportModel {

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IScenarioManager scenarioManager;

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

    @Override
    @Transactional(readOnly = true)
    public List<ProjectStatusReportDTO> getProjectStatusReportDTOs(Order order) {
        orderDAO.reattach(order);

        order.useSchedulingDataFor(scenarioManager.getCurrent());

        List<ProjectStatusReportDTO> dtos = new ArrayList<ProjectStatusReportDTO>();

        for (OrderElement child : order.getAllChildren()) {
            dtos.add(new ProjectStatusReportDTO(child));
        }

        return dtos;
    }

}