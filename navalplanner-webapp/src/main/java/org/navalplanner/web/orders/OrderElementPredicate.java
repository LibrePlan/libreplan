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

package org.navalplanner.web.orders;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.requirements.entities.CriterionRequirement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.web.common.components.finders.FilterPair;
import org.navalplanner.web.common.components.finders.OrderElementFilterEnum;
import org.zkoss.ganttz.IPredicate;

/**
 * Checks if {@link OrderElement} matches with the different filters.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class OrderElementPredicate implements IPredicate {

    private List<FilterPair> filters;

    private Date startDate;

    private Date finishDate;

    private String name;

    public OrderElementPredicate(List<FilterPair> filters, Date startDate,
            Date finishDate, String name) {
        this.filters = filters;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.name = name;
    }

    @Override
    public boolean accepts(Object object) {
        final OrderElement orderElement = (OrderElement) object;
        return accepts(orderElement) || accepts(orderElement.getAllChildren());
    }

    private boolean accepts(OrderElement orderElement) {
        if (orderElement == null) {
            return false;
        }
        if (acceptFilters(orderElement) && acceptFiltersDates(orderElement)
                && acceptFilterName(orderElement)) {
            return true;
        }
        return false;
    }

    private boolean accepts(List<OrderElement> orderElements) {
        for (OrderElement orderElement : orderElements) {
            if (accepts(orderElement)) {
                return true;
            }
        }
        return false;
    }

    private boolean acceptFilters(OrderElement orderElement) {
        if ((filters == null) || (filters.isEmpty())) {
            return true;
        }
        for (FilterPair filter : filters) {
            if (!acceptFilter(filter, orderElement)) {
                return false;
            }
        }
        return true;
    }

    private boolean acceptFilter(FilterPair filter, OrderElement orderElement) {
        switch ((OrderElementFilterEnum) filter.getType()) {
        case Criterion:
            return acceptCriterion(filter, orderElement);
        case Label:
            return acceptLabel(filter, orderElement);
        }
        return false;
    }

    private boolean acceptCriterion(FilterPair filter, OrderElement orderElement) {
        Criterion filterCriterion = (Criterion) filter.getValue();
        return existCriterionInOrderElementOrHoursGroups(filterCriterion,
                orderElement);
    }

    private boolean existCriterionInOrderElementOrHoursGroups(
            Criterion filterCriterion,
            OrderElement orderElement) {
        for (CriterionRequirement criterionRequirement : orderElement
                .getCriterionRequirements()) {
            if (acceptsCriterionRequrirement(filterCriterion,
                    criterionRequirement)) {
                return true;
            }
        }
        return existCriterionInHoursGroups(filterCriterion, orderElement
                .getHoursGroups());
    }

    private boolean existCriterionInHoursGroups(Criterion filterCriterion,
            List<HoursGroup> hoursGroups) {
        for (HoursGroup hoursGroup : hoursGroups) {
            for (CriterionRequirement criterionRequirement : hoursGroup
                    .getCriterionRequirements()) {
                if (acceptsCriterionRequrirement(filterCriterion,
                        criterionRequirement)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean acceptsCriterionRequrirement(Criterion filterCriterion,
            CriterionRequirement criterionRequirement) {
        return criterionRequirement.isValid()
                && criterionRequirement.getCriterion().getId().equals(
                        filterCriterion.getId());
    }

    private boolean acceptLabel(FilterPair filter, OrderElement orderElement) {
        Label filterLabel = (Label) filter.getValue();
        return existLabelInOrderElement(filterLabel, orderElement);
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

    private boolean acceptFiltersDates(OrderElement orderElement) {
        return (acceptStartDate(orderElement.getInitDate()) && (acceptFinishDate(orderElement
                .getDeadline())));
    }

    private boolean acceptStartDate(Date initDate) {
        if ((initDate == null) && (startDate == null)) {
            return true;
        }
        return isInTheRangeFilterDates(initDate);
    }

    private boolean acceptFinishDate(Date deadLine) {
        if ((deadLine == null) && (finishDate == null)) {
            return true;
        }
        return isInTheRangeFilterDates(deadLine);
    }

    private boolean isInTheRangeFilterDates(Date date) {
        // Check if date is into interval between the startdate and finish date
        return (isGreaterToStartDate(date, startDate) && isLowerToFinishDate(
                date, finishDate));
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

    private boolean acceptFilterName(OrderElement orderElement) {
        if (name == null) {
            return true;
        }
        if ((orderElement.getName() != null)
                && (StringUtils
                        .containsIgnoreCase(orderElement.getName(), name))) {
            return true;
        }
        return false;
    }

}
