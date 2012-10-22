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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.reports.dtos.ProjectStatusReportDTO;
import org.libreplan.business.resources.entities.Criterion;

/**
 * Contract for Project Status report model.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public interface IProjectStatusReportModel {

    List<Order> getOrders();

    List<ProjectStatusReportDTO> getProjectStatusReportDTOs(Order order);

    BigDecimal getHoursCost(Order order);

    BigDecimal getExpensesCost(Order order);

    BigDecimal getTotalCost(Order order);

    List<Label> getAllLabels();

    void addSelectedLabel(Label label);

    void removeSelectedLabel(Label label);

    Set<Label> getSelectedLabels();

    ProjectStatusReportDTO getTotalDTO();

    List<Criterion> getAllCriteria();

    void addSelectedCriterion(Criterion criterion);

    void removeSelectedCriterion(Criterion criterion);

    Set<Criterion> getSelectedCriteria();

}
