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

package org.navalplanner.web.orders;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.web.common.components.finders.FilterPair;
import org.navalplanner.web.common.components.finders.TaskElementFilterEnum;
import org.zkoss.ganttz.IPredicate;

/**
 * Checks if {@link TaskElement} matches with the different filters.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class TaskElementPredicate implements IPredicate {

    private List<FilterPair> filters;

    private Date startDate;

    private Date finishDate;

    private String name;

    private boolean ignoreLabelsInheritance;

    public TaskElementPredicate(List<FilterPair> filters, Date startDate,
            Date finishDate, String name, boolean ignoreLabelsInheritance) {
        this.filters = filters;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.name = name;
        this.ignoreLabelsInheritance = ignoreLabelsInheritance;
    }

    @Override
    public boolean accepts(Object object) {
        final TaskElement taskElement = (TaskElement) object;
        return accepts(taskElement) || accepts(taskElement.getAllChildren());
    }

    private boolean accepts(TaskElement taskElement) {
        if (taskElement == null) {
            return false;
        }
        if (acceptFilters(taskElement) && acceptFiltersDates(taskElement)
                && acceptFilterName(taskElement)) {
            return true;
        }
        return false;
    }

    private boolean accepts(List<TaskElement> taskElements) {
        for (TaskElement taskElement : taskElements) {
            if (accepts(taskElement)) {
                return true;
            }
        }
        return false;
    }

    private boolean acceptFilters(TaskElement taskElement) {
        if ((filters == null) || (filters.isEmpty())) {
            return true;
        }
        for (FilterPair filter : filters) {
            if (!acceptFilter(filter, taskElement)) {
                return false;
            }
        }
        return true;
    }

    private boolean acceptFilter(FilterPair filter, TaskElement taskElement) {
        switch ((TaskElementFilterEnum) filter.getType()) {
        case Criterion:
            return acceptCriterion(filter, taskElement);
        case Label:
            return acceptLabel(filter, taskElement);
        }
        return false;
    }

    private boolean acceptCriterion(FilterPair filter, TaskElement taskElement) {
        Criterion filterCriterion = (Criterion) filter.getValue();
        return existCriterionInTaskElementResourceAllocations(filterCriterion,
                taskElement);
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

    private boolean acceptLabel(FilterPair filter, TaskElement taskElement) {
        Label filterLabel = (Label) filter.getValue();
        return existLabelInTaskElement(filterLabel, taskElement);
    }

    private boolean existLabelInTaskElement(Label filterLabel,
            TaskElement taskElement) {
        Set<Label> labels;
        if (ignoreLabelsInheritance) {
            labels = taskElement.getOrderElement().getLabels();
        } else {
            labels = taskElement.getOrderElement().getAllLabels();
        }
        for (Label label : labels) {
            if (label.getId().equals(filterLabel.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean acceptFiltersDates(TaskElement taskElement) {
        return (acceptStartDate(taskElement.getStartDate()) && (acceptFinishDate(taskElement
                .getEndDate())));
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

    private boolean acceptFilterName(TaskElement taskElement) {
        if (name == null) {
            return true;
        }
        if ((taskElement.getName() != null)
                && (StringUtils.containsIgnoreCase(taskElement.getName(), name))) {
            return true;
        }
        return false;
    }

}
