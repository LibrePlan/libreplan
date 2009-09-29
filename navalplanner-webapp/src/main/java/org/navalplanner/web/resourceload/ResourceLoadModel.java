package org.navalplanner.web.resourceload;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.joda.time.LocalDate;
import org.navalplanner.business.planner.daos.IResourceAllocationDAO;
import org.navalplanner.business.planner.entities.GenericResourceAllocation;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.SpecificResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.data.resourceload.LoadPeriod;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.data.resourceload.LoadTimelinesGroup;
import org.zkoss.ganttz.util.Interval;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourceLoadModel implements IResourceLoadModel {

    @Autowired
    private IResourceDAO resourcesDAO;

    @Autowired
    private IResourceAllocationDAO resourceAllocationDAO;

    private List<LoadTimelinesGroup> loadTimeLines;
    private Interval viewInterval;

    @Override
    @Transactional(readOnly = true)
    public void initGlobalView() {
        loadTimeLines = calculateLoadTimelinesGroups();
        viewInterval = new Interval(toDate(new LocalDate(2008, 6, 10)),
                toDate(new LocalDate(2011, 6, 10)));
    }

    private List<LoadTimelinesGroup> calculateLoadTimelinesGroups() {
        List<LoadTimelinesGroup> result = new ArrayList<LoadTimelinesGroup>();
        List<Resource> allResources = resourcesDAO.list(Resource.class);
        result.addAll(groupsFor(allResources));
        result.addAll(groupsFor(resourceAllocationDAO.findGenericAllocationsByCriterion()));
        return result;
    }

    private List<LoadTimelinesGroup> groupsFor(
            Map<Criterion, List<GenericResourceAllocation>> genericAllocationsByCriterion) {
        List<LoadTimelinesGroup> result = new ArrayList<LoadTimelinesGroup>();
        for (Entry<Criterion, List<GenericResourceAllocation>> entry : genericAllocationsByCriterion
                .entrySet()) {
            result.add(new LoadTimelinesGroup(createPrincipal(entry.getKey(),
                    entry.getValue()), new ArrayList<LoadTimeLine>()));
        }
        return result;
    }

    private LoadTimeLine createPrincipal(Criterion criterion,
            List<GenericResourceAllocation> value) {
        return new LoadTimeLine(criterion.getName(), createPeriods(criterion,
                value));
    }

    private List<LoadPeriod> createPeriods(Criterion criterion,
            List<GenericResourceAllocation> value) {
        return PeriodsBuilder.build(LoadPeriodGenerator.onCriterion(
                resourceAllocationDAO, criterion),
                value);
    }

    private List<LoadTimelinesGroup> groupsFor(List<Resource> allResources) {
        List<LoadTimelinesGroup> result = new ArrayList<LoadTimelinesGroup>();
        for (Resource resource : allResources) {
            result.add(buildGroup(resource));
        }
        return result;
    }

    private LoadTimelinesGroup buildGroup(Resource resource) {
        List<ResourceAllocation<?>> sortedByStartDate = ResourceAllocation
                .sortedByStartDate(resourceAllocationDAO
                        .findAllocationsRelatedTo(resource));
        return new LoadTimelinesGroup(buildTimeLine(resource, resource
                .getDescription(), sortedByStartDate), buildSecondLevel(
                resource, sortedByStartDate));
    }

    private List<LoadTimeLine> buildSecondLevel(Resource resource,
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
        List<GenericResourceAllocation> result = new ArrayList<GenericResourceAllocation>();
        for (ResourceAllocation<?> r : sortedByStartDate) {
            if (r instanceof GenericResourceAllocation) {
                result.add((GenericResourceAllocation) r);
            }
        }
        return result;
    }

    private List<SpecificResourceAllocation> onlySpecific(
            List<ResourceAllocation<?>> sortedByStartDate) {
        List<SpecificResourceAllocation> result = new ArrayList<SpecificResourceAllocation>();
        for (ResourceAllocation<?> r : sortedByStartDate) {
            if (r instanceof SpecificResourceAllocation) {
                result.add((SpecificResourceAllocation) r);
            }
        }
        return result;
    }

    private List<LoadTimeLine> buildTimeLinesForEachCriterion(
            Resource resource, List<GenericResourceAllocation> sortdByStartDate) {
        Map<Set<Criterion>, List<GenericResourceAllocation>> byCriterions = GenericResourceAllocation
                .byCriterions(sortdByStartDate);
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        for (Entry<Set<Criterion>, List<GenericResourceAllocation>> entry : byCriterions
                .entrySet()) {
            result.add(buildTimeLine(new ArrayList<Criterion>(entry.getKey()),
                    resource, entry.getValue()));
        }
        return result;
    }

    private List<LoadTimeLine> buildTimeLinesForEachTask(Resource resource,
            List<SpecificResourceAllocation> sortedByStartDate) {
        Map<Task, List<ResourceAllocation<?>>> byTask = ResourceAllocation
                .byTask(sortedByStartDate);
        List<LoadTimeLine> secondLevel = new ArrayList<LoadTimeLine>();
        for (Entry<Task, List<ResourceAllocation<?>>> entry : byTask.entrySet()) {
            secondLevel.add(buildTimeLine(resource, entry.getKey().getName(),
                    entry.getValue()));
        }
        return secondLevel;
    }

    private LoadTimeLine buildTimeLine(List<Criterion> criterions,
            Resource resource,
            List<GenericResourceAllocation> allocationsSortedByStartDate) {
        return new LoadTimeLine(getName(criterions), PeriodsBuilder.build(
                LoadPeriodGenerator.onResource(resource),
                allocationsSortedByStartDate));
    }

    private String getName(List<Criterion> criterions) {
        if (criterions.isEmpty()) {
            return _("generic all workers");
        }
        String[] names = new String[criterions.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = criterions.get(i).getName();
        }
        return Arrays.toString(names);
    }

    private LoadTimeLine buildTimeLine(Resource resource, String name,
            List<ResourceAllocation<?>> sortedByStartDate) {
        return new LoadTimeLine(name, PeriodsBuilder.build(LoadPeriodGenerator
                .onResource(resource), sortedByStartDate));
    }

    @Override
    public List<LoadTimelinesGroup> getLoadTimeLines() {
        return loadTimeLines;
    }

    @Override
    public Interval getViewInterval() {
        return viewInterval;
    }

    private Date toDate(LocalDate localDate) {
        return localDate.toDateTimeAtStartOfDay().toDate();
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
                List<LoadPeriodGenerator> sortedByStartDate = mergeListsKeepingByStartSortOrder(
                        generated, loadPeriodsGenerators.subList(iterator
                                .nextIndex(), loadPeriodsGenerators.size()));
                final int takenFromRemaining = sortedByStartDate.size()
                        - generated.size();
                removeNextElements(iterator, takenFromRemaining);
                addAtCurrentPosition(iterator, sortedByStartDate);
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