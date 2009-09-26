package org.navalplanner.web.resourceload;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.LocalDate;
import org.navalplanner.business.planner.daos.IResourceAllocationDAO;
import org.navalplanner.business.planner.entities.ResourceAllocation;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
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
        new LoadTimelinesGroup(buildTimeLine(resource.getDescription(),
                sortedByStartDate),
                buildTimeLinesForEachTask(sortedByStartDate));
        return null;
    }

    private List<LoadTimeLine> buildTimeLinesForEachTask(
            List<ResourceAllocation<?>> sortedByStartDate) {
        Map<Task, List<ResourceAllocation<?>>> byTask = ResourceAllocation
                .byTask(sortedByStartDate);
        List<LoadTimeLine> secondLevel = new ArrayList<LoadTimeLine>();
        for (Entry<Task, List<ResourceAllocation<?>>> entry : byTask.entrySet()) {
            buildTimeLine(entry.getKey().getName(), entry.getValue());
        }
        return secondLevel;
    }

    private LoadTimeLine buildTimeLine(String name,
            List<ResourceAllocation<?>> sortedByStartDate) {
        return new LoadTimeLine(name, PeriodsBuilder.build(sortedByStartDate));
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

    private final List<ResourceAllocation<?>> sortedByStartDate;

    private PeriodsBuilder(List<ResourceAllocation<?>> sortedByStartDate) {
        this.sortedByStartDate = sortedByStartDate;
    }

    public static List<LoadPeriod> build(
            List<ResourceAllocation<?>> sortedByStartDate) {
        return new PeriodsBuilder(sortedByStartDate).buildPeriods();
    }

    private List<LoadPeriod> buildPeriods() {
        List<LoadPeriod> result = new ArrayList<LoadPeriod>();
        return result;
    }

}
