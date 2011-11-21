/*
 * This file is part of LibrePlan
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
package org.libreplan.web.resourceload;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;

public class ResourceLoadParameters {

    private PlanningState planningState;

    private boolean filterByResources = true;

    /**
     * Contains the resources to be shown when specified manually using the
     * Bandbox
     */
    private List<Resource> resourcesToShowList = new ArrayList<Resource>();

    /**
     * Contains the criteria to be shown when specified manually using the
     * Bandbox
     */
    private List<Criterion> criteriaToShowList = new ArrayList<Criterion>();

    private LocalDate initDateFilter;

    private LocalDate endDateFilter;

    private int pageFilterPosition = 0;
    private int pageSize = 10;

    public ResourceLoadParameters(PlanningState planningState) {
        this.planningState = planningState;
    }

    public PlanningState getPlanningState() {
        return planningState;
    }

    public void setEndDateFilter(LocalDate value) {
        endDateFilter = value;
    }

    public void setInitDateFilter(LocalDate value) {
        initDateFilter = value;
    }

    public LocalDate getEndDateFilter() {
        return endDateFilter;
    }

    public LocalDate getInitDateFilter() {
        return initDateFilter;
    }

    public boolean thereIsCurrentOrder() {
        return planningState != null;
    }

    public Order getCurrentOrder() {
        Validate.isTrue(thereIsCurrentOrder());
        return planningState.getOrder();
    }

    public void setResourcesToShow(List<Resource> resourcesList) {
        this.resourcesToShowList.clear();
        this.resourcesToShowList.addAll(Resource.sortByName(resourcesList));
    }

    public void clearResourcesToShow() {
        resourcesToShowList.clear();
    }

    public void clearCriteriaToShow() {
        criteriaToShowList.clear();
    }

    public void setCriteriaToShow(List<Criterion> criteriaList) {
        criteriaToShowList.clear();
        criteriaToShowList.addAll(criteriaList);
    }

    public <T> Paginator<T> getEntities(Class<T> type,
            Callable<List<T>> allEntities, IReattacher<T> reattacher) {
        Validate.isTrue(
                type.equals(Resource.class) || type.equals(Criterion.class),
                "only " + Resource.class.getSimpleName() + " and "
                        + Criterion.class.getSimpleName() + " supported");
        if (type.equals(Resource.class)) {
            return buildPaginator(listOfType(type, resourcesToShowList),
                    allEntities, reattacher);
        } else {
            return buildPaginator(listOfType(type, criteriaToShowList),
                    allEntities, reattacher);
        }
    }

    private <T> List<T> listOfType(Class<T> klass, Collection<?> objects) {
        List<T> result = new ArrayList<T>();
        for (Object each : objects) {
            result.add(klass.cast(each));
        }
        return result;
    }

    private <T> Paginator<T> buildPaginator(List<T> selected,
            Callable<List<T>> all,
            IReattacher<T> reattacher) {
        if (selected == null || selected.isEmpty()) {
            return paginateAll(all);
        }
        List<T> reattached = reattach(selected, reattacher);
        return new Paginator<T>(reattached, pageSize, reattached);
    }

    private <T> Paginator<T> paginateAll(Callable<List<T>> allCallable) {
        List<T> allEntities = call(allCallable);
        if (pageFilterPosition == -1) {
            return new Paginator<T>(allEntities, pageSize, allEntities);
        }
        List<T> page = allEntities.subList(pageFilterPosition,
                Math.min(pageFilterPosition + pageSize, allEntities.size()));
        return new Paginator<T>(page, pageSize, allEntities);
    }

    private static <T> T call(Callable<T> all) {
        try {
            return all.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> reattach(List<T> list, IReattacher<T> reattacher) {
        List<T> result = new ArrayList<T>();
        for (T each : list) {
            result.add(reattacher.reattach(each));
        }
        return result;
    }

    public interface IReattacher<T> {
        T reattach(T entity);
    }

    public static class Paginator<T> {

        private final List<T> forCurrentPage;

        private final int pageSize;

        private final List<T> allEntities;

        private Paginator(List<T> forCurrentPage, int pageSize,
                List<T> allEntities) {
            this.forCurrentPage = forCurrentPage;
            this.pageSize = pageSize;
            this.allEntities = allEntities;
        }

        public List<T> getForCurrentPage() {
            return forCurrentPage;
        }

        public List<T> getAll() {
            return allEntities;
        }

        public int getPageSize() {
            return pageSize;
        }
    }

    public int getPageFilterPosition() {
        return pageFilterPosition;
    }

    public void setPageFilterPosition(int pageFilterPosition) {
        this.pageFilterPosition = pageFilterPosition;
    }

    public void setFilterByResources(boolean filterByResources) {
        this.filterByResources = filterByResources;
    }

    public boolean isFilterByResources() {
        return filterByResources;
    }

}
