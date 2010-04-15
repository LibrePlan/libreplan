/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.resourceload;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.joda.time.LocalDate;
import org.navalplanner.business.orders.daos.IOrderDAO;
import org.navalplanner.business.orders.daos.IOrderElementDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.planner.daos.IResourceAllocationDAO;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.data.resourceload.LoadPeriod;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourceLoadModel implements IResourceLoadModel {

    @Autowired
    private IResourceDAO resourcesDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IOrderDAO orderDAO;

    @Autowired
    private IResourceAllocationDAO resourceAllocationDAO;

    private List<LoadTimeLine> loadTimeLines;
    private Interval viewInterval;

    private Order filterBy;

    private boolean filterByResources = true;

    @Override
    @Transactional(readOnly = true)
    public void initGlobalView(boolean filterByResources) {
        filterBy = null;
        this.filterByResources = filterByResources;
        doGlobalView();
    }

    @Override
    @Transactional(readOnly = true)
    public void initGlobalView(Order filterBy, boolean filterByResources) {
        this.filterBy = orderDAO.findExistingEntity(filterBy.getId());
        this.filterByResources = filterByResources;
        doGlobalView();
    }

    private void doGlobalView() {
        loadTimeLines = calculateLoadTimeLines();
        if (!loadTimeLines.isEmpty()) {
            viewInterval = LoadTimeLine.getIntervalFrom(loadTimeLines);
        } else {
            viewInterval = new Interval(new Date(), plusFiveYears(new Date()));
        }
    }

    private Date plusFiveYears(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, 5);
        return calendar.getTime();
    }

    private List<LoadTimeLine> calculateLoadTimeLines() {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        if (filterByResources) {
            result.addAll(groupsFor(resourcesToShow()));
        } else {
            result.addAll(groupsFor(genericAllocationsByCriterion()));
        }
        return result;
    }

    private Map<Criterion, List<GenericResourceAllocation>> genericAllocationsByCriterion() {
        if (filter()) {
            List<Criterion> criterions = new ArrayList<Criterion>();
            List<GenericResourceAllocation> generics = new ArrayList<GenericResourceAllocation>();
            List<Task> tasks = justTasks(filterBy
                    .getAllChildrenAssociatedTaskElements());
            for (Task task : tasks) {

                List<ResourceAllocation<?>> listAllocations = new ArrayList<ResourceAllocation<?>>(
                        task.getSatisfiedResourceAllocations());
                for (GenericResourceAllocation generic : (onlyGeneric(listAllocations))) {
                    criterions.addAll(generic.getCriterions());
                }
            }
            return resourceAllocationDAO
                    .findGenericAllocationsBySomeCriterion(criterions);
        } else {
            return resourceAllocationDAO.findGenericAllocationsByCriterion();
        }
    }

    private List<Resource> resourcesToShow() {
        if (filter()) {
            return resourcesForActiveTasks();
        } else {
            return allResources();
        }
    }

    private boolean filter() {
        return filterBy != null;
    }

    private List<Resource> resourcesForActiveTasks() {
        return resourcesDAO.findResourcesRelatedTo(justTasks(filterBy
                .getAllChildrenAssociatedTaskElements()));
    }

    private List<Task> justTasks(Collection<? extends TaskElement> tasks) {
        List<Task> result = new ArrayList<Task>();
        for (TaskElement taskElement : tasks) {
            if (taskElement instanceof Task) {
                result.add((Task) taskElement);
            }
        }
        return result;
    }

    private List<Resource> allResources() {
        return resourcesDAO.list(Resource.class);
    }

    /**
     * @param genericAllocationsByCriterion
     * @return
     */
    private List<LoadTimeLine> groupsFor(
            Map<Criterion, List<GenericResourceAllocation>> genericAllocationsByCriterion) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        for (Entry<Criterion, List<GenericResourceAllocation>> entry : genericAllocationsByCriterion
                .entrySet()) {
            List<GenericResourceAllocation> allocations = ResourceAllocation
                    .sortedByStartDate(entry.getValue());
            LoadTimeLine group = new LoadTimeLine(createPrincipal(entry
                    .getKey(), allocations), buildSecondLevel(entry.getKey(),
                    allocations));
            if (!group.isEmpty()) {
                result.add(group);
            }
        }
        return result;
    }

    private List<LoadTimeLine> buildSecondLevel(Criterion criterion,
            List<GenericResourceAllocation> allocations) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        Map<Order, List<ResourceAllocation<?>>> byOrder = byOrder(new ArrayList<ResourceAllocation<?>>(
                allocations));

        if (filter()) {
            // build time lines for current order
            if (byOrder.get(filterBy) != null) {
                result.addAll(buildTimeLinesForOrder(criterion, byOrder
                        .get(filterBy)));
            }
            byOrder.remove(filterBy);
            // build time lines for other orders
            LoadTimeLine lineOthersOrders = buildTimeLinesForOtherOrders(
                    criterion, byOrder);
            if (lineOthersOrders != null) {
                result.add(lineOthersOrders);
            }
        } else {
            result.addAll(buildTimeLinesGroupForOrder(criterion, byOrder));
        }
        return result;
    }

    private List<LoadTimeLine> buildTimeLinesForOrder(Criterion criterion,
            List<ResourceAllocation<?>> allocations) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        result.addAll(buildTimeLinesForEachTask(criterion, allocations));
        return result;
    }

    private LoadTimeLine buildTimeLinesForOtherOrders(Criterion criterion,
            Map<Order, List<ResourceAllocation<?>>> byOrder) {
        List<ResourceAllocation<?>> allocations = getAllSortedValues(byOrder);
        if (allocations.isEmpty()) {
            return null;
        }
        LoadTimeLine group = new LoadTimeLine(buildTimeLine(criterion,
                "Others ordes", allocations), buildTimeLinesGroupForOrder(
                criterion, byOrder));
        return group;
    }

    private List<LoadTimeLine> buildTimeLinesGroupForOrder(Criterion criterion,
            Map<Order, List<ResourceAllocation<?>>> byOrder) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        for (Order order : byOrder.keySet()) {
            result.add(new LoadTimeLine(buildTimeLine(criterion, order
                    .getName(), byOrder.get(order)), buildTimeLinesForOrder(
                    criterion, byOrder.get(order))));
        }
        return result;
    }

    private List<LoadTimeLine> buildTimeLinesForEachTask(Criterion criterion,
            List<ResourceAllocation<?>> allocations) {
        Map<Task, List<ResourceAllocation<?>>> byTask = ResourceAllocation
                .byTask(allocations);

        List<LoadTimeLine> secondLevel = new ArrayList<LoadTimeLine>();
        for (Entry<Task, List<ResourceAllocation<?>>> entry : byTask.entrySet()) {
            Task task = entry.getKey();
            Set<Criterion> criterions = task.getCriterions();
            LoadTimeLine timeLine = new LoadTimeLine(buildTimeLine(criterions,
                    task, criterion, entry.getValue()),
                    buildTimeLinesForEachResource(criterion, onlyGeneric(entry
                            .getValue())));
            if (!timeLine.isEmpty()) {
                secondLevel.add(timeLine);
            }

        }
        return secondLevel;
    }

    private List<LoadTimeLine> buildTimeLinesForEachResource(
            Criterion criterion, List<GenericResourceAllocation> allocations) {
        Map<Resource, List<GenericResourceAllocation>> byResource = GenericResourceAllocation
                .byResource(allocations);

        List<LoadTimeLine> secondLevel = new ArrayList<LoadTimeLine>();
        for (Entry<Resource, List<GenericResourceAllocation>> entry : byResource
                .entrySet()) {
            Resource resource = entry.getKey();
            List<GenericResourceAllocation> resourceAllocations = entry
                    .getValue();
            String descriptionTimeLine = getDescriptionResourceWithCriterions(resource);

            LoadTimeLine timeLine = buildTimeLine(resource,
                    descriptionTimeLine, resourceAllocations, "resource");
            if (!timeLine.isEmpty()) {
                secondLevel.add(timeLine);
            }

        }
        return secondLevel;
    }

    private String getDescriptionResourceWithCriterions(Resource resource) {
        Set<CriterionSatisfaction> criterionSatisfactions = resource
                .getCriterionSatisfactions();
        return resource.getShortDescription()
                + getCriterionSatisfactionDescription(criterionSatisfactions);
    }

    private String getCriterionSatisfactionDescription(
            Set<CriterionSatisfaction> satisfactions) {
        if (satisfactions.isEmpty()) {
            return _("");
        }
        List<Criterion> criterions = new ArrayList<Criterion>();
        for (CriterionSatisfaction satisfaction : satisfactions) {
            criterions.add(satisfaction.getCriterion());
        }
        return " :: " + getName(criterions);
    }

    private LoadTimeLine createPrincipal(Criterion criterion,
            List<GenericResourceAllocation> orderedAllocations) {
        return new LoadTimeLine(criterion.getName(), createPeriods(criterion,
                orderedAllocations), "global-generic");
    }

    private List<LoadPeriod> createPeriods(Criterion criterion,
            List<GenericResourceAllocation> value) {
        return PeriodsBuilder.build(LoadPeriodGenerator.onCriterion(criterion,
                resourcesDAO), value);
    }

    private List<LoadTimeLine> groupsFor(List<Resource> allResources) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        for (Resource resource : allResources) {
            LoadTimeLine group = buildGroup(resource);
            if (!group.isEmpty()) {
                result.add(group);
            }
        }
        return result;
    }

    private LoadTimeLine buildGroup(Resource resource) {
        List<ResourceAllocation<?>> sortedByStartDate = ResourceAllocation
                .sortedByStartDate(resourceAllocationDAO
                        .findAllocationsRelatedTo(resource));
        LoadTimeLine result = new LoadTimeLine(buildTimeLine(resource, resource
                .getShortDescription(), sortedByStartDate, "resource"),
                buildSecondLevel(resource, sortedByStartDate));
        return result;

    }

    private List<LoadTimeLine> buildSecondLevel(Resource resource,
            List<ResourceAllocation<?>> sortedByStartDate) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        Map<Order, List<ResourceAllocation<?>>> byOrder = byOrder(sortedByStartDate);

        if (filter()) {
            // build time lines for current order
            result.addAll(buildTimeLinesForOrder(resource, byOrder
                    .get(filterBy)));
            byOrder.remove(filterBy);
            // build time lines for other orders
            LoadTimeLine lineOthersOrders = buildTimeLinesForOtherOrders(
                    resource, byOrder);
            if (lineOthersOrders != null) {
                result.add(lineOthersOrders);
            }
        } else {
            result.addAll(buildTimeLinesGroupForOrder(resource, byOrder));
        }
        return result;
    }

    private LoadTimeLine buildTimeLinesForOtherOrders(Resource resource,
            Map<Order, List<ResourceAllocation<?>>> byOrder) {
        List<ResourceAllocation<?>> resourceAllocations = getAllSortedValues(byOrder);
        if (resourceAllocations.isEmpty()) {
            return null;
        }
        LoadTimeLine group = new LoadTimeLine(buildTimeLine(resource,
                _("Others ordes"), resourceAllocations, "resource"),
                buildTimeLinesGroupForOrder(resource, byOrder));
        return group;
    }

    private List<LoadTimeLine> buildTimeLinesGroupForOrder(Resource resource,
            Map<Order, List<ResourceAllocation<?>>> byOrder) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        for (Order order : byOrder.keySet()) {
            result.add(new LoadTimeLine(buildTimeLine(resource,
                    order.getName(), byOrder.get(order), "resource"),
                    buildTimeLinesForOrder(resource, byOrder.get(order))));
        }
        return result;
    }

    private List<ResourceAllocation<?>> getAllSortedValues(
            Map<Order, List<ResourceAllocation<?>>> byOrder) {
        List<ResourceAllocation<?>> resourceAllocations = new ArrayList<ResourceAllocation<?>>();
        for (List<ResourceAllocation<?>> listAllocations : byOrder.values()) {
            resourceAllocations.addAll(listAllocations);
        }
        return ResourceAllocation.sortedByStartDate(resourceAllocations);
    }

    private void initializeIfNeeded(
            Map<Order, List<ResourceAllocation<?>>> result, Order order) {
        if (!result.containsKey(order)) {
            result.put(order, new ArrayList<ResourceAllocation<?>>());
        }
    }

    @Transactional(readOnly = true)
    public Map<Order, List<ResourceAllocation<?>>> byOrder(
            Collection<ResourceAllocation<?>> allocations) {
        Map<Order, List<ResourceAllocation<?>>> result = new HashMap<Order, List<ResourceAllocation<?>>>();
        for (ResourceAllocation<?> resourceAllocation : allocations) {
            if ((resourceAllocation.isSatisfied())
                    && (resourceAllocation.getTask() != null)) {
                OrderElement orderElement = resourceAllocation.getTask()
                        .getOrderElement();
                Order order = orderElementDAO
                        .loadOrderAvoidingProxyFor(orderElement);
                initializeIfNeeded(result, order);
                result.get(order).add(resourceAllocation);
            }
        }
        return result;
    }

    private List<LoadTimeLine> buildTimeLinesForOrder(Resource resource,
            List<ResourceAllocation<?>> sortedByStartDate) {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        result.addAll(buildTimeLinesForEachTask(resource,
                onlySpecific(sortedByStartDate)));
        result.addAll(buildTimeLinesForEachCriterion(resource,
                onlyGeneric(sortedByStartDate)));
        return result;
    }

    private List<GenericResourceAllocation> onlyGeneric(
            List<ResourceAllocation<?>> sortedByStartDate) {
        return ResourceAllocation.getOfType(GenericResourceAllocation.class,
                sortedByStartDate);
    }

    private List<SpecificResourceAllocation> onlySpecific(
            List<ResourceAllocation<?>> sortedByStartDate) {
        return ResourceAllocation.getOfType(SpecificResourceAllocation.class,
                sortedByStartDate);
    }

    private List<LoadTimeLine> buildTimeLinesForEachCriterion(
            Resource resource, List<GenericResourceAllocation> sortdByStartDate) {
        Map<Set<Criterion>, List<GenericResourceAllocation>> byCriterions = GenericResourceAllocation
                .byCriterions(sortdByStartDate);

        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        for (Entry<Set<Criterion>, List<GenericResourceAllocation>> entry : byCriterions
                .entrySet()) {

            Map<Task, List<ResourceAllocation<?>>> byTask = ResourceAllocation
                    .byTask(new ArrayList<ResourceAllocation<?>>(entry
                            .getValue()));

            for (Entry<Task, List<ResourceAllocation<?>>> entryTask : byTask
                    .entrySet()) {

                Task task = entryTask.getKey();
                List<GenericResourceAllocation> resouceAllocations = onlyGeneric(entryTask
                        .getValue());
                LoadTimeLine timeLine = buildTimeLine(entry.getKey(), task,
                        resource, resouceAllocations);
                if (!timeLine.isEmpty()) {
                    result.add(timeLine);
                }

            }
        }
        return result;
    }

    private List<LoadTimeLine> buildTimeLinesForEachTask(Resource resource,
            List<SpecificResourceAllocation> sortedByStartDate) {

        List<ResourceAllocation<?>> listOnlySpecific = new ArrayList<ResourceAllocation<?>>(
                sortedByStartDate);
        Map<Task, List<ResourceAllocation<?>>> byTask = ResourceAllocation
                .byTask(listOnlySpecific);

        List<LoadTimeLine> secondLevel = new ArrayList<LoadTimeLine>();
        for (Entry<Task, List<ResourceAllocation<?>>> entry : byTask.entrySet()) {
            Task task = entry.getKey();
            LoadTimeLine timeLine = buildTimeLine(resource, task.getName(),
                    entry.getValue(), "specific");
            if (!timeLine.isEmpty()) {
                secondLevel.add(timeLine);
            }

        }
        return secondLevel;
    }

    public static String getName(Collection<? extends Criterion> criterions,
            Task task) {
        String prefix = task.getName();
        return (prefix + " :: " + getName(criterions));
    }

    public static String getName(Collection<? extends Criterion> criterions) {
        if (criterions.isEmpty()) {
            return _("[generic all workers]");
        }
        String[] names = new String[criterions.size()];
        int i = 0;
        for (Criterion criterion : criterions) {
            names[i++] = criterion.getName();
        }
        return (Arrays.toString(names));
    }

    private LoadTimeLine buildTimeLine(Resource resource, String name,
            List<? extends ResourceAllocation<?>> sortedByStartDate, String type) {
        return new LoadTimeLine(name, PeriodsBuilder.build(LoadPeriodGenerator
                .onResource(resource), sortedByStartDate), type);
    }

    private LoadTimeLine buildTimeLine(Criterion criterion, String name,
            List<ResourceAllocation<?>> allocations) {
        List<GenericResourceAllocation> generics = onlyGeneric(allocations);
        return new LoadTimeLine(name, createPeriods(criterion, generics),
                "global-generic");
    }

    private LoadTimeLine buildTimeLine(Collection<Criterion> criterions,
            Task task, Criterion criterion,
            List<ResourceAllocation<?>> allocations) {
        return buildTimeLine(criterion, getName(criterions, task), allocations);
    }

    private LoadTimeLine buildTimeLine(Collection<Criterion> criterions,
            Task task, Resource resource,
            List<GenericResourceAllocation> allocationsSortedByStartDate) {
        LoadPeriodGeneratorFactory periodGeneratorFactory = LoadPeriodGenerator
                .onResourceSatisfying(resource, criterions);
        return new LoadTimeLine(getName(criterions, task), PeriodsBuilder
                .build(periodGeneratorFactory, allocationsSortedByStartDate),
                "generic");
    }

    @Override
    public List<LoadTimeLine> getLoadTimeLines() {
        return loadTimeLines;
    }

    @Override
    public Interval getViewInterval() {
        return viewInterval;
    }

    public ZoomLevel calculateInitialZoomLevel() {
        Interval interval = getViewInterval();
        return ZoomLevel.getDefaultZoomByDates(new LocalDate(interval
                .getStart()), new LocalDate(interval.getFinish()));
    }

}

class PeriodsBuilder {

    private final List<? extends ResourceAllocation<?>> sortedByStartDate;

    private final List<LoadPeriodGenerator> loadPeriodsGenerators = new LinkedList<LoadPeriodGenerator>();

    private final LoadPeriodGeneratorFactory factory;

    private PeriodsBuilder(LoadPeriodGeneratorFactory factory,
            List<? extends ResourceAllocation<?>> sortedByStartDate) {
        this.factory = factory;
        this.sortedByStartDate = sortedByStartDate;
    }

    public static List<LoadPeriod> build(LoadPeriodGeneratorFactory factory,
            List<? extends ResourceAllocation<?>> sortedByStartDate) {
        return new PeriodsBuilder(factory, sortedByStartDate).buildPeriods();
    }

    private List<LoadPeriod> buildPeriods() {
        for (ResourceAllocation<?> resourceAllocation : sortedByStartDate) {
            loadPeriodsGenerators.add(factory.create(resourceAllocation));
        }
        joinPeriodGenerators();
        return toGenerators(loadPeriodsGenerators);
    }

    private List<LoadPeriod> toGenerators(List<LoadPeriodGenerator> generators) {
        List<LoadPeriod> result = new ArrayList<LoadPeriod>();
        for (LoadPeriodGenerator loadPeriodGenerator : generators) {
            result.add(loadPeriodGenerator.build());
        }
        return result;
    }

    private void joinPeriodGenerators() {
        ListIterator<LoadPeriodGenerator> iterator = loadPeriodsGenerators
                .listIterator();
        while (iterator.hasNext()) {
            final LoadPeriodGenerator current = findNextOneOverlapping(iterator);
            if (current != null) {
                rewind(iterator, current);
                iterator.remove();
                LoadPeriodGenerator next = iterator.next();
                iterator.remove();
                List<LoadPeriodGenerator> generated = current.join(next);
                final LoadPeriodGenerator positionToComeBack = generated.get(0);
                final List<LoadPeriodGenerator> remaining = loadPeriodsGenerators
                        .subList(iterator.nextIndex(), loadPeriodsGenerators
                                .size());
                List<LoadPeriodGenerator> generatorsSortedByStartDate = mergeListsKeepingByStartSortOrder(
                        generated, remaining);
                final int takenFromRemaining = generatorsSortedByStartDate
                        .size()
                        - generated.size();
                removeNextElements(iterator, takenFromRemaining);
                addAtCurrentPosition(iterator, generatorsSortedByStartDate);
                rewind(iterator, positionToComeBack);
            }
        }
    }

    private LoadPeriodGenerator findNextOneOverlapping(
            ListIterator<LoadPeriodGenerator> iterator) {
        while (iterator.hasNext()) {
            LoadPeriodGenerator current = iterator.next();
            if (!iterator.hasNext()) {
                return null;
            }
            LoadPeriodGenerator next = peekNext(iterator);
            if (current.overlaps(next)) {
                return current;
            }
        }
        return null;
    }

    private void addAtCurrentPosition(
            ListIterator<LoadPeriodGenerator> iterator,
            List<LoadPeriodGenerator> sortedByStartDate) {
        for (LoadPeriodGenerator l : sortedByStartDate) {
            iterator.add(l);
        }
    }

    private void removeNextElements(ListIterator<LoadPeriodGenerator> iterator,
            final int elementsNumber) {
        for (int i = 0; i < elementsNumber; i++) {
            iterator.next();
            iterator.remove();
        }
    }

    private void rewind(ListIterator<LoadPeriodGenerator> iterator,
            LoadPeriodGenerator nextOne) {
        while (peekNext(iterator) != nextOne) {
            iterator.previous();
        }
    }

    private List<LoadPeriodGenerator> mergeListsKeepingByStartSortOrder(
            List<LoadPeriodGenerator> joined,
            List<LoadPeriodGenerator> remaining) {
        List<LoadPeriodGenerator> result = new ArrayList<LoadPeriodGenerator>();
        ListIterator<LoadPeriodGenerator> joinedIterator = joined
                .listIterator();
        ListIterator<LoadPeriodGenerator> remainingIterator = remaining
                .listIterator();
        while (joinedIterator.hasNext() && remainingIterator.hasNext()) {
            LoadPeriodGenerator fromJoined = peekNext(joinedIterator);
            LoadPeriodGenerator fromRemaining = peekNext(remainingIterator);
            if (fromJoined.getStart().compareTo(fromRemaining.getStart()) <= 0) {
                result.add(fromJoined);
                joinedIterator.next();
            } else {
                result.add(fromRemaining);
                remainingIterator.next();
            }
        }
        if (joinedIterator.hasNext()) {
            result.addAll(joined.subList(joinedIterator.nextIndex(), joined
                    .size()));
        }
        return result;
    }

    private LoadPeriodGenerator peekNext(
            ListIterator<LoadPeriodGenerator> iterator) {
        if (!iterator.hasNext()) {
            return null;
        }
        LoadPeriodGenerator result = iterator.next();
        iterator.previous();
        return result;
    }

}