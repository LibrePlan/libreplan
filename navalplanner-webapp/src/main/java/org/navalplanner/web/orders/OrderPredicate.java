/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.orders;

import java.util.Date;
import java.util.List;

import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderStatusEnum;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.workreports.entities.WorkReport;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.navalplanner.web.common.components.finders.FilterPair;
import org.navalplanner.web.common.components.finders.OrderFilterEnum;
import org.zkoss.ganttz.IPredicate;

/**
 * Checks if {@link WorkReportType}, the start date and finish date from
 * {@link WorkReport} matches attributes
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class OrderPredicate implements IPredicate {

    private List<FilterPair> filters;

    private Date startDate;

    private Date finishDate;

    private Boolean includeOrderElements;

    public OrderPredicate(List<FilterPair> filters, Date startDate,
            Date finishDate, Boolean includeOrderElements) {
        this.filters = filters;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.includeOrderElements = includeOrderElements;
    }

    @Override
    public boolean accepts(Object object) {
        final Order order = (Order) object;
        return accepts(order);
    }

    private boolean accepts(Order order) {
        if (order == null) {
            return false;
        }
        if (acceptFilters(order) && acceptFiltersDates(order)) {
            return true;
        }
        return false;
    }

    private boolean acceptFilters(Order order) {
        if ((filters == null) || (filters.isEmpty())) {
            return true;
        }
        for (FilterPair filter : filters) {
            if (!acceptFilter(filter, order)) {
                return false;
            }
        }
        return true;
    }

    private boolean acceptFilter(FilterPair filter,Order order){
        switch ((OrderFilterEnum) filter.getType()) {
        case Criterion:
            return acceptCriterion(filter, order);
        case Label:
            return acceptLabel(filter, order);
        case ExternalCompany:
            return acceptExternalCompany(filter, order);
        case State:
            return acceptState(filter, order);
        case Code:
            return acceptCode(filter, order);
        case CustomerReference:
            return acceptCustomerReference(filter, order);
        }
        return false;
    }

    private boolean acceptCriterion(FilterPair filter, Order order) {
        Criterion filterCriterion = (Criterion) filter.getValue();
        if (existCriterionInOrderElement(filterCriterion, order)) {
            return true;
        }
        if (includeOrderElements) {
            for (OrderElement orderElement : order.getAllOrderElements()) {
                if (existCriterionInOrderElement(filterCriterion, orderElement)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean existCriterionInOrderElement(Criterion filterCriterion,
            OrderElement order) {
        for(CriterionRequirement criterionRequirement : order.getCriterionRequirements()){
            if(criterionRequirement.getCriterion().getId().equals(filterCriterion.getId())){
                return true;
            }
        }
        for(OrderElement child : order.getAllChildren()) {
            for(CriterionRequirement criterionRequirement : child.getCriterionRequirements()) {
                if(criterionRequirement.getCriterion().getId().equals(filterCriterion.getId())){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean acceptLabel(FilterPair filter,Order order) {
        Label filterLabel = (Label) filter.getValue();
        if (existLabelInOrderElement(filterLabel, order)) {
            return true;
        }
        if (this.includeOrderElements) {
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
        for(Label label : order.getLabels()){
            if(label.getId().equals(filterLabel.getId())){
                return true;
            }
        }
        return false;
    }

    private boolean acceptExternalCompany(FilterPair filter,Order order) {
        ExternalCompany filterCustomer = (ExternalCompany) filter.getValue();
        if ((order.getCustomer() != null)
                && (order.getCustomer().getId().equals(filterCustomer.getId()))) {
            return true;
        }
        return false;
    }

    private boolean acceptState(FilterPair filter,Order order) {
        OrderStatusEnum filterState = (OrderStatusEnum) filter.getValue();
        if ((order.getState() != null)
                && (order.getState().equals(filterState))) {
            return true;
        }
        return false;
    }

    private boolean acceptCode(FilterPair filter,Order order) {
        String filterCode = (String) filter.getValue();
        return order.getCode().equals(filterCode);
    }

    private boolean acceptCustomerReference(FilterPair filter,Order order) {
        String filterCustomerReference = (String) filter.getValue();
        return order.getCustomerReference().equals(filterCustomerReference);
    }

    protected boolean acceptFiltersDates(Order order) {
        // Check if exist work report items into interval between the start date
        // and finish date.
        return (acceptStartDate(order.getInitDate()) && (acceptFinishDate(order
                .getDeadline())));
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
