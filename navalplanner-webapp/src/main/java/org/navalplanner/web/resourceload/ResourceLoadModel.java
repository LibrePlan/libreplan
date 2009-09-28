package org.navalplanner.web.resourceload;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
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
import org.zkoss.ganttz.data.resourceload.LoadLevel;
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
        List<Resource> allResources = resourcesDAO.list(Resource.class);
        loadTimeLines = groupsFor(allResources);
        viewInterval = new Interval(toDate(new LocalDate(2008, 6, 10)),
                toDate(new LocalDate(2011, 6, 10)));
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
                resource, allocationsSortedByStartDate));
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
        return new LoadTimeLine(name, PeriodsBuilder.build(resource,
                sortedByStartDate));
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

    private final Resource resource;

    private PeriodsBuilder(Resource resource,
            List<? extends ResourceAllocation<?>> sortedByStartDate) {
        this.resource = resource;
        this.sortedByStartDate = sortedByStartDate;
    }

    public static List<LoadPeriod> build(Resource resource,
            List<? extends ResourceAllocation<?>> sortedByStartDate) {
        return new PeriodsBuilder(resource, sortedByStartDate).buildPeriods();
    }

    private List<LoadPeriod> buildPeriods() {
        for (ResourceAllocation<?> resourceAllocation : sortedByStartDate) {
            loadPeriodsGenerators.add(new LoadPeriodGenerator(resource,
                    resourceAllocation));
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
            LoadPeriodGenerator current = iterator.next();
            if (iterator.hasNext()) {
                iterator.remove();
                LoadPeriodGenerator next = iterator.next();
                iterator.remove();
                List<LoadPeriodGenerator> generated = current.join(next);
                final LoadPeriodGenerator nextOne = generated.size() > 1 ? generated
                        .get(1) : generated.get(0);
                List<LoadPeriodGenerator> sortedByStartDate = mergeListsKeepingByStartSortOrder(
                        generated, loadPeriodsGenerators
                        .subList(iterator.nextIndex(), loadPeriodsGenerators
                                .size()));
                final int takenFromRemaining = sortedByStartDate.size()
                        - generated.size();
                removeNextElements(iterator, takenFromRemaining);
                addAtCurrentPosition(iterator, sortedByStartDate);
                rewind(iterator, nextOne);
            }
        }
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

class LoadPeriodGenerator {

    private Resource resource;
    private LocalDate start;
    private LocalDate end;

    private List<ResourceAllocation<?>> allocationsOnInterval = new ArrayList<ResourceAllocation<?>>();

    private LoadPeriodGenerator(Resource resource, LocalDate start,
            LocalDate end, List<ResourceAllocation<?>> allocationsOnInterval) {
        this.resource = resource;
        this.start = start;
        this.end = end;
        this.allocationsOnInterval = allocationsOnInterval;
    }

    LoadPeriodGenerator(Resource resource, ResourceAllocation<?> initial) {
        this.resource = resource;
        this.start = initial.getStartDate();
        this.end = initial.getEndDate();
        this.allocationsOnInterval.add(initial);
    }

    public List<LoadPeriodGenerator> join(LoadPeriodGenerator next) {
        if (!overlaps(next)) {
            return stripEmpty(this, next);
        }
        if (isIncluded(next)) {
            return stripEmpty(this.until(next.start), intersect(next), this
                    .from(next.end));
        }
        assert overlaps(next) && !isIncluded(next);
        return stripEmpty(this.until(next.start), intersect(next), next
                .from(end));
    }

    private List<LoadPeriodGenerator> stripEmpty(
            LoadPeriodGenerator... generators) {
        List<LoadPeriodGenerator> result = new ArrayList<LoadPeriodGenerator>();
        for (LoadPeriodGenerator loadPeriodGenerator : generators) {
            if (!loadPeriodGenerator.isEmpty()) {
                result.add(loadPeriodGenerator);
            }
        }
        return result;
    }

    private boolean isEmpty() {
        return start.equals(end);
    }

    private LoadPeriodGenerator intersect(LoadPeriodGenerator other) {
        return new LoadPeriodGenerator(resource, max(this.start, other.start),
                min(this.end, other.end), plusAllocations(other));
    }

    private static LocalDate max(LocalDate l1, LocalDate l2) {
        return l1.compareTo(l2) < 0 ? l2 : l1;
    }

    private static LocalDate min(LocalDate l1, LocalDate l2) {
        return l1.compareTo(l2) < 0 ? l1 : l2;
    }

    private List<ResourceAllocation<?>> plusAllocations(
            LoadPeriodGenerator other) {
        List<ResourceAllocation<?>> result = new ArrayList<ResourceAllocation<?>>();
        result.addAll(allocationsOnInterval);
        result.addAll(other.allocationsOnInterval);
        return result;
    }

    private LoadPeriodGenerator from(LocalDate newStart) {
        return new LoadPeriodGenerator(resource, newStart, end,
                allocationsOnInterval);
    }

    private LoadPeriodGenerator until(LocalDate newEnd) {
        return new LoadPeriodGenerator(resource, start, newEnd,
                allocationsOnInterval);
    }

    private boolean overlaps(LoadPeriodGenerator other) {
        return (start.compareTo(other.end) < 0 && other.start
                .compareTo(this.end) < 0);
    }

    private boolean isIncluded(LoadPeriodGenerator other) {
        return other.start.compareTo(start) >= 0
                && other.end.compareTo(end) <= 0;
    }

    public LoadPeriod build() {
        return new LoadPeriod(start, end, new LoadLevel(
                calculateLoadPercentage()));
    }

    private int calculateLoadPercentage() {
        final int totalResourceWorkHours = resource.getTotalWorkHours(start,
                end);
        int assigned = sumAssigned();
        double proportion = assigned / (double) totalResourceWorkHours;
        try {
            return new BigDecimal(proportion).scaleByPowerOfTen(2).intValue();
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private int sumAssigned() {
        int sum = 0;
        for (ResourceAllocation<?> resourceAllocation : allocationsOnInterval) {
            sum += resourceAllocation.getAssignedHours(resource, start, end);
        }
        return sum;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

}
