/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 Igalia, S.L.
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
package org.navalplanner.web.planner;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderStatusEnum;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.planner.entities.TaskGroup;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.web.common.components.finders.FilterPair;
import org.navalplanner.web.common.components.finders.TaskGroupFilterEnum;
import org.zkoss.ganttz.IPredicate;

/**
 * Checks if {@link TaskGroup} in company Gantt view matches with the different
 * filters.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class TaskGroupPredicate implements IPredicate {

    private List<FilterPair> filters;

    private Date startDate;

    private Date finishDate;

    private Boolean includeChildren;

    public TaskGroupPredicate(List<FilterPair> filters, Date startDate,
            Date finishDate, Boolean includeChildren) {
        this.filters = filters;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.includeChildren = includeChildren;
    }

    @Override
    public boolean accepts(Object object) {
        final TaskGroup taskGroup = (TaskGroup) object;
        return accepts(taskGroup);
    }

    private boolean accepts(TaskGroup taskGroup) {
        if (taskGroup == null) {
            return false;
        }
        if (acceptFilters(taskGroup) && acceptFiltersDates(taskGroup)) {
            return true;
        }
        return false;
    }

    private boolean acceptFilters(TaskGroup taskGroup) {
        if ((filters == null) || (filters.isEmpty())) {
            return true;
        }
        for (FilterPair filter : filters) {
            if (!acceptFilter(filter, taskGroup)) {
                return false;
            }
        }
        return true;
    }

    private boolean acceptFilter(FilterPair filter, TaskGroup taskGroup) {
        switch ((TaskGroupFilterEnum) filter.getType()) {
        case Criterion:
            return acceptCriterion(filter, taskGroup);
        case Label:
            return acceptLabel(filter, taskGroup);
        case ExternalCompany:
            return acceptExternalCompany(filter, taskGroup);
        case State:
            return acceptState(filter, taskGroup);
        case Code:
            return acceptCode(filter, taskGroup);
        case CustomerReference:
            return acceptCustomerReference(filter, taskGroup);
        }
        return false;
    }

    private boolean acceptCriterion(FilterPair filter, TaskElement taskElement) {
        Criterion filterCriterion = (Criterion) filter.getValue();
        if (existCriterionInTaskElementResourceAllocations(filterCriterion,
                taskElement)) {
            return true;
        }
        if (includeChildren) {
            for (TaskElement each : taskElement.getAllChildren()) {
                if (existCriterionInTaskElementResourceAllocations(
                        filterCriterion, each)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean existCriterionInTaskElementResourceAllocations(
            Criterion filterCriterion, TaskElement taskElement) {
        for (ResourceAllocation<?> each : taskElement
                .getAllResourceAllocations()) {
            if (acceptsCriterionInResourceAllocation(filterCriterion, each)) {
                return true;
            }
        }
        return false;
    }

    private boolean acceptsCriterionInResourceAllocation(
            Criterion filterCriterion, ResourceAllocation<?> resourceAllocation) {
        if (resourceAllocation instanceof GenericResourceAllocation) {
            Set<Criterion> criteria = ((GenericResourceAllocation) resourceAllocation)
                    .getCriterions();
            for (Criterion criterion : criteria) {
                if (criterion.getId().equals(filterCriterion.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean acceptLabel(FilterPair filter, TaskGroup taskGroup) {
        Label filterLabel = (Label) filter.getValue();
        Order order = (Order) taskGroup.getOrderElement();
        if (existLabelInOrderElement(filterLabel, order)) {
            return true;
        }
        if (this.includeChildren) {
            for (OrderElement orderElement : order.getAllOrderElements()) {
                if (existLabelInOrderElement(filterLabel, orderElement)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean existLabelInOrderElement(Label filterLabel,
            OrderElement order) {
        for (Label label : order.getLabels()) {
            if (label.getId().equals(filterLabel.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean acceptExternalCompany(FilterPair filter, TaskGroup taskGroup) {
        Order order = (Order) taskGroup.getOrderElement();
        ExternalCompany filterCustomer = (ExternalCompany) filter.getValue();
        if ((order.getCustomer() != null)
                && (order.getCustomer().getId().equals(filterCustomer.getId()))) {
            return true;
        }
        return false;
    }

    private boolean acceptState(FilterPair filter, TaskGroup taskGroup) {
        Order order = (Order) taskGroup.getOrderElement();
        OrderStatusEnum filterState = (OrderStatusEnum) filter.getValue();
        if ((order.getState() != null)
                && (order.getState().equals(filterState))) {
            return true;
        }
        return false;
    }

    private boolean acceptCode(FilterPair filter, TaskGroup taskGroup) {
        Order order = (Order) taskGroup.getOrderElement();
        String filterCode = (String) filter.getValue();
        return order.getCode().equals(filterCode);
    }

    private boolean acceptCustomerReference(FilterPair filter,
            TaskGroup taskGroup) {
        Order order = (Order) taskGroup.getOrderElement();
        String filterCustomerReference = (String) filter.getValue();
        return order.getCustomerReference().equals(filterCustomerReference);
    }

    protected boolean acceptFiltersDates(TaskGroup taskGroup) {
        // Check if exist work report items into interval between the start date
        // and finish date.
        return (acceptStartDate(taskGroup.getStartDate()) && (acceptFinishDate(taskGroup
                .getEndDate())));
    }

    protected boolean acceptStartDate(Date initDate) {
        if ((initDate == null) && (startDate == null)) {
            return true;
        }
        return isLowerToFinishDate(initDate, finishDate);
    }

    protected boolean acceptFinishDate(Date deadLine) {
        if ((deadLine == null) && (finishDate == null)) {
            return true;
        }
        return isGreaterToStartDate(deadLine, startDate);
    }

    private boolean isGreaterToStartDate(Date date, Date startDate) {
        if (startDate == null) {
            return true;
        }

        if (date != null && (date.compareTo(startDate) >= 0)) {
            return true;
        }
        return false;
    }

    private boolean isLowerToFinishDate(Date date, Date finishDate) {
        if (finishDate == null) {
            return true;
        }
        if (date != null && (date.compareTo(finishDate) <= 0)) {
            return true;
        }
        return false;
    }

}
