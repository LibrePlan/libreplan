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

package org.libreplan.web.resources.search;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.CalendarAvailability;
import org.libreplan.business.costcategories.entities.CostCategory;
import org.libreplan.business.costcategories.entities.ResourcesCostCategoryAssignment;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionSatisfaction;
import org.libreplan.business.resources.entities.Machine;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.web.common.components.finders.FilterPair;
import org.libreplan.web.common.components.finders.ResourceFilterEnum;
import org.zkoss.ganttz.IPredicate;

/**
 * Checks if {@link Resource} matches with this predicate.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class ResourcePredicate implements IPredicate {

    private List<FilterPair> filters;

    private LocalDate startDate;

    private LocalDate finishDate;

    private String[] personalFilters;

    private Boolean isLimitingResource;

    public ResourcePredicate(List<FilterPair> filters, String personalFilters,
            LocalDate startDate,
            LocalDate finishDate,
            Boolean isLimitingResource) {
        this.filters = filters;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.isLimitingResource = isLimitingResource;
        this.personalFilters = personalFilters.split(" ");
    }

    @Override
    public boolean accepts(Object object) {
        final Resource resource = (Resource) object;
        return accepts(resource);
    }

    private boolean accepts(Resource resource) {
        if (resource == null) {
            return false;
        }
        if (acceptFilters(resource) && acceptPersonalFilters(resource)
                && acceptFiltersDates(resource) && acceptFilterIsLimitingResource(resource)) {
            return true;
        }
        return false;
    }

    private boolean acceptFilterIsLimitingResource(Resource resource) {
        return (isLimitingResource != null) ? isLimitingResource.equals(resource.isLimitingResource()) : true;
    }

    private boolean acceptFilters(Resource resource) {
        if ((filters == null) || (filters.isEmpty())) {
            return true;
        }
        for (FilterPair filter : filters) {
            if (!acceptFilter(filter, resource)) {
                return false;
            }
        }
        return true;
    }

    private boolean acceptFilter(FilterPair filter, Resource resource) {
        switch ((ResourceFilterEnum) filter.getType()) {
        case Criterion:
            return acceptCriterion(filter, resource);
        case CostCategory:
            return acceptCostCategory(filter, resource);
        }
        return false;
    }

    private boolean acceptCriterion(FilterPair filter, Resource resource) {
        Criterion filterCriterion = (Criterion) filter.getValue();
        for (CriterionSatisfaction criterionSatisfaction : resource
                .getCriterionSatisfactions()) {
            if (criterionSatisfaction.getCriterion().getId().equals(
                    filterCriterion.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean acceptCostCategory(FilterPair filter, Resource resource) {
        CostCategory filterCostCategory = (CostCategory) filter.getValue();
        for (ResourcesCostCategoryAssignment assignedCostCategory : resource
                .getResourcesCostCategoryAssignments()) {
            if (assignedCostCategory.getCostCategory().getId().equals(
                    filterCostCategory.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean acceptPersonalFilters(Resource resource) {
        for (String filter : personalFilters) {
            filter = filter.replace(" ", "");
            filter = filter.toLowerCase();
            if (filter.isEmpty()) {
                continue;
            }

            if ((!acceptName(filter, resource))
                    && (!acceptApel(filter, resource))
                    && (!acceptNif(filter, resource))
                    && (!acceptCode(filter, resource))) {
                return false;
            }
        }
        return true;
    }

    private boolean acceptName(String filterName, Resource resource) {
        if (resource instanceof Worker) {
            return ((Worker) resource).getFirstName().toLowerCase().contains(
                    filterName);
        }
        return resource.getName().toLowerCase().contains(filterName);
    }

    private boolean acceptApel(String filterApel, Resource resource) {
        if (resource instanceof Worker) {
            return ((Worker) resource).getSurname().toLowerCase().contains(
                    filterApel);
        }
        return false;
    }

    private boolean acceptCode(String filterCode, Resource resource) {
        if (resource instanceof Machine) {
            return ((Machine) resource).getCode().toLowerCase().contains(
                    filterCode);
        }
        return false;
    }

    private boolean acceptNif(String filterNif, Resource resource) {
        if (resource instanceof Worker) {
            return ((Worker) resource).getNif().toLowerCase().contains(
                    filterNif);
        }
        return false;
    }

    private boolean acceptFiltersDates(Resource resource) {
        // Check if exist some day of the active period into interval between
        // the start date and finish date.
        if (startDate == null && finishDate == null) {
            return true;
        }
        if ((resource.getCalendar() != null)) {
            for (CalendarAvailability calendar : resource.getCalendar()
                    .getCalendarAvailabilities()) {
                if (isOverlap(calendar)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOverlap(CalendarAvailability calendar) {
        if (calendar.getEndDate() == null) {
            if (finishDate != null) {
                return (finishDate.compareTo(calendar.getStartDate()) >= 0);
            } else {
                return true;
            }
        } else {
            if (finishDate == null) {
                return (startDate.compareTo(calendar.getEndDate()) <= 0);
            }
            if (startDate == null) {
                return (finishDate.compareTo(calendar.getStartDate()) >= 0);
            }
        }
        Interval filter = getIntervalFilter();
        Interval activePeriod = getIntervalActivePeriod(calendar);
        return filter.overlaps(activePeriod);
    }

    private Interval getIntervalFilter() {
        DateTime startDateTime = null;
        if (startDate != null) {
            startDateTime = startDate.toDateTimeAtStartOfDay();
        }

        DateTime endDateTime = null;
        if (finishDate != null) {
            endDateTime = (finishDate.plusDays(1)).toDateTimeAtStartOfDay();
        }
        return new Interval(startDateTime, endDateTime);
    }

    private Interval getIntervalActivePeriod(CalendarAvailability calendar) {
        DateTime endDateTime = null;
        if (calendar.getEndDate() != null) {
            endDateTime = (calendar.getEndDate().plusDays(1))
                    .toDateTimeAtStartOfDay();
        }
        return new Interval(calendar.getStartDate().toDateTimeAtStartOfDay(),
                endDateTime);
    }

}
