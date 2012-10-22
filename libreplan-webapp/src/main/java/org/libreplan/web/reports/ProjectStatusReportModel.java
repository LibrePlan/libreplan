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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.libreplan.business.labels.daos.ILabelDAO;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.orders.daos.IOrderDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.planner.entities.IMoneyCostCalculator;
import org.libreplan.business.reports.dtos.ProjectStatusReportDTO;
import org.libreplan.business.resources.daos.ICriterionDAO;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.workingday.EffortDuration;
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
    private ILabelDAO labelDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    private IMoneyCostCalculator moneyCostCalculator;

    private Set<Label> selectedLabels = new HashSet<Label>();

    private Set<Criterion> selectedCriteria = new HashSet<Criterion>();

    private ProjectStatusReportDTO totalDTO;

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

        List<OrderElement> orderElements = order.getAllChildren();
        orderElements = filterBySelectedLabels(orderElements);
        orderElements = filterBySelectedCriteria(orderElements);

        for (OrderElement child : orderElements) {
            dtos.add(calculateDTO(child));
        }

        calculateTotalDTO(order, dtos);

        return dtos;
    }

    private ProjectStatusReportDTO calculateDTO(OrderElement orderElement) {
        ProjectStatusReportDTO dto = new ProjectStatusReportDTO(orderElement);
        dto.setHoursCost(moneyCostCalculator.getHoursMoneyCost(orderElement));
        dto.setExpensesCost(moneyCostCalculator
                .getExpensesMoneyCost(orderElement));
        dto.setTotalCost(moneyCostCalculator.getTotalMoneyCost(orderElement));
        return dto;
    }

    private void calculateTotalDTO(Order order,
            List<ProjectStatusReportDTO> dtos) {
        if (isNotFiltering()) {
            totalDTO = calculateDTO(order);
        } else {
            EffortDuration estimatedHours = EffortDuration.zero();
            EffortDuration plannedHours = EffortDuration.zero();
            EffortDuration imputedHours = EffortDuration.zero();

            BigDecimal budget = BigDecimal.ZERO.setScale(2);
            BigDecimal hoursCost = BigDecimal.ZERO.setScale(2);
            BigDecimal expensesCost = BigDecimal.ZERO.setScale(2);
            BigDecimal totalCost = BigDecimal.ZERO.setScale(2);

            for (ProjectStatusReportDTO dto : dtos) {
                estimatedHours = addIfNotNull(estimatedHours,
                        dto.getEstimatedHoursAsEffortDuration());
                plannedHours = addIfNotNull(plannedHours,
                        dto.getPlannedHoursAsEffortDuration());
                imputedHours = addIfNotNull(imputedHours,
                        dto.getImputedHoursAsEffortDuration());

                budget = addIfNotNull(budget, dto.getBudget());
                hoursCost = addIfNotNull(hoursCost, dto.getHoursCost());
                expensesCost = addIfNotNull(expensesCost, dto.getExpensesCost());
                totalCost = addIfNotNull(totalCost, dto.getTotalCost());
            }

            totalDTO = new ProjectStatusReportDTO(estimatedHours, plannedHours,
                    imputedHours, budget, hoursCost, expensesCost, totalCost);
        }
    }

    private boolean isNotFiltering() {
        return selectedLabels.isEmpty() && selectedCriteria.isEmpty();
    }

    private List<OrderElement> filterBySelectedLabels(
            List<OrderElement> orderElements) {
        if (selectedLabels.isEmpty()) {
            return orderElements;
        }

        List<OrderElement> result = new ArrayList<OrderElement>();
        for (OrderElement orderElement : orderElements) {
            if (orderElement.containsLabels(selectedLabels)) {
                result.add(orderElement);
            }
        }
        return result;
    }

    private List<OrderElement> filterBySelectedCriteria(
            List<OrderElement> orderElements) {
        if (selectedCriteria.isEmpty()) {
            return orderElements;
        }

        List<OrderElement> result = new ArrayList<OrderElement>();
        for (OrderElement orderElement : orderElements) {
            if (orderElement.containsCriteria(selectedCriteria)) {
                result.add(orderElement);
            }
        }
        return result;
    }

    private EffortDuration addIfNotNull(EffortDuration total,
            EffortDuration other) {
        if (other == null) {
            return total;
        }
        return total.plus(other);
    }

    private BigDecimal addIfNotNull(BigDecimal total, BigDecimal other) {
        if (other == null) {
            return total;
        }
        return total.add(other);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getHoursCost(Order order) {
        return moneyCostCalculator.getHoursMoneyCost(order);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getExpensesCost(Order order) {
        return moneyCostCalculator.getExpensesMoneyCost(order);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalCost(Order order) {
        return moneyCostCalculator.getTotalMoneyCost(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Label> getAllLabels() {
        List<Label> labels = labelDAO.findAll();
        for (Label label : labels) {
            forceLoadLabelType(label);
        }
        return labels;
    }

    private void forceLoadLabelType(Label label) {
        label.getType().getName();
    }

    @Override
    public void addSelectedLabel(Label label) {
        selectedLabels.add(label);
    }

    @Override
    public void removeSelectedLabel(Label label) {
        selectedLabels.remove(label);
    }

    @Override
    public Set<Label> getSelectedLabels() {
        return Collections.unmodifiableSet(selectedLabels);
    }

    @Override
    public ProjectStatusReportDTO getTotalDTO() {
        return totalDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Criterion> getAllCriteria() {
        List<Criterion> criteria = criterionDAO.findAll();
        for (Criterion criterion : criteria) {
            forceLoadCriterionType(criterion);
        }
        return criteria;
    }

    private void forceLoadCriterionType(Criterion criterion) {
        criterion.getType().getName();
    }

    @Override
    public void addSelectedCriterion(Criterion criterion) {
        selectedCriteria.add(criterion);
    }

    @Override
    public void removeSelectedCriterion(Criterion criterion) {
        selectedCriteria.remove(criterion);
    }

    @Override
    public Set<Criterion> getSelectedCriteria() {
        return Collections.unmodifiableSet(selectedCriteria);
    }

}