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

package org.navalplanner.web.resourceload;

import static org.navalplanner.business.workingday.EffortDuration.min;
import static org.navalplanner.business.workingday.EffortDuration.zero;
import static org.navalplanner.web.I18nHelper._;
import static org.navalplanner.web.resourceload.ResourceLoadModel.asDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.daos.IConfigurationDAO;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.IntraDayDate.PartialDay;
import org.navalplanner.web.common.components.bandboxsearch.BandboxMultipleSearch;
import org.navalplanner.web.common.components.finders.FilterPair;
import org.navalplanner.web.planner.chart.Chart;
import org.navalplanner.web.planner.chart.ChartFiller;
import org.navalplanner.web.planner.company.CompanyPlanningModel;
import org.navalplanner.web.planner.order.BankHolidaysMarker;
import org.navalplanner.web.planner.order.IOrderPlanningGate;
import org.navalplanner.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkforge.timeplot.Plotinfo;
import org.zkforge.timeplot.Timeplot;
import org.zkforge.timeplot.geometry.TimeGeometry;
import org.zkforge.timeplot.geometry.ValueGeometry;
import org.zkoss.ganttz.IChartVisibilityChangedListener;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.resourceload.IFilterChangedListener;
import org.zkoss.ganttz.resourceload.IPaginationFilterChangedListener;
import org.zkoss.ganttz.resourceload.ISeeScheduledOfListener;
import org.zkoss.ganttz.resourceload.ResourcesLoadPanel;
import org.zkoss.ganttz.resourceload.ResourcesLoadPanel.IToolbarCommand;
import org.zkoss.ganttz.resourceload.ResourcesLoadPanel.PaginationType;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.SeveralModificators;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.api.Combobox;

/**
 * Controller for global resourceload view
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourceLoadController implements Composer {

    @Autowired
    private IResourceLoadModel resourceLoadModel;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    private List<IToolbarCommand> commands = new ArrayList<IToolbarCommand>();

    private Order filterBy;

    private org.zkoss.zk.ui.Component parent;

    private ResourcesLoadPanel resourcesLoadPanel;

    private TimeTracker timeTracker;

    private transient IFilterChangedListener filterChangedListener;

    private transient ISeeScheduledOfListener seeScheduledOfListener;

    private IOrderPlanningGate planningControllerEntryPoints;

    private BandboxMultipleSearch bandBox;

    private boolean currentFilterByResources = true;
    private boolean filterHasChanged = false;
    private boolean firstLoad = true;

    private ZoomLevel zoomLevel;

    private List<IZoomLevelChangedListener> keepAliveZoomListeners = new ArrayList<IZoomLevelChangedListener>();

    private List<IChartVisibilityChangedListener> keepAliveChartVisibilityListeners = new ArrayList<IChartVisibilityChangedListener>();

    private Chart loadChart;

    public ResourceLoadController() {
    }

    public void add(IToolbarCommand... commands) {
        Validate.noNullElements(commands);
        this.commands.addAll(Arrays.asList(commands));
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        this.parent = comp;
    }

    public void reload() {
        timeTracker = null;
        resourcesLoadPanel = null;
        firstLoad = true;
        resourceLoadModel.setPageFilterPosition(0);
        reload(true); // show filter by resources by default
    }

    private void reload(final boolean filterByResources) {
        transactionService.runOnReadOnlyTransaction(new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                reloadInTransaction(filterByResources);
                return null;
            }

            private void reloadInTransaction(boolean filterByResources) {
                filterHasChanged = (filterByResources != currentFilterByResources);
                currentFilterByResources = filterByResources;

                if (filterBy == null) {
                    if (resourcesLoadPanel == null) {
                        resetAdditionalFilters();
                    }
                    resourceLoadModel.initGlobalView(filterByResources);
                } else {
                    if (resourcesLoadPanel == null) {
                        deleteAdditionalFilters();
                    }
                    resourceLoadModel.initGlobalView(filterBy, filterByResources);
                }
                timeTracker = buildTimeTracker();
                buildResourcesLoadPanel();

                parent.getChildren().clear();
                parent.appendChild(resourcesLoadPanel);

                resourcesLoadPanel.afterCompose();
                addSchedulingScreenListeners();
                addCommands(resourcesLoadPanel);
                if(firstLoad || filterHasChanged) {
                    setupNameFilter();
                }
                firstLoad = false;
            }
        });
    }


    private void addListeners() {
        /* Listener to filter */
        filterChangedListener = new IFilterChangedListener() {

            @Override
            public void filterChanged(boolean filter) {
                onApplyFilter(filter);
            }
        };
        resourcesLoadPanel.addFilterListener(filterChangedListener);
        addNameFilterListener();
    }

    /*
     * This object is stored in an attribute to keep one reference to it, so the
     * garbage collector doesn't get rid of it. It's necessary because it is stored
     * by ResourcesLoadPanel using weak references.
     */
    private IPaginationFilterChangedListener keepAlivePaginationListener =
        new IPaginationFilterChangedListener() {
            @Override
            public void filterChanged(int initialPosition) {
                resourceLoadModel.setPageFilterPosition(initialPosition);
                reload(currentFilterByResources);
                addSchedulingScreenListeners();
            }
        };

    private void addNameFilterListener() {
        resourcesLoadPanel.addNameFilterListener(keepAlivePaginationListener);
    }

    private void addSchedulingScreenListeners() {
        /* Listener to show the scheduling screen */
        seeScheduledOfListener = new ISeeScheduledOfListener() {

            @Override
            public void seeScheduleOf(LoadTimeLine taskLine) {
                onSeeScheduleOf(taskLine);
            }
        };
        resourcesLoadPanel.addSeeScheduledOfListener(seeScheduledOfListener);
    }

    public void onApplyFilter(boolean filterByResources) {
        resourceLoadModel.setPageFilterPosition(0);
        reload(filterByResources);
    }

    private void addCommands(ResourcesLoadPanel resourcesLoadPanel) {
        resourcesLoadPanel.add(commands.toArray(new IToolbarCommand[0]));
    }

    private TimeTracker buildTimeTracker() {
        zoomLevel = (timeTracker == null) ? resourceLoadModel
                .calculateInitialZoomLevel() : timeTracker.getDetailLevel();
        return new TimeTracker(resourceLoadModel.getViewInterval(), zoomLevel,
                SeveralModificators.create(),
                SeveralModificators.create(createBankHolidaysMarker()), parent);
    }

    private BankHolidaysMarker createBankHolidaysMarker() {
        BaseCalendar defaultCalendar = configurationDAO.getConfiguration()
                .getDefaultCalendar();
        return BankHolidaysMarker.create(defaultCalendar);
    }

    private void buildResourcesLoadPanel() {
        if (resourcesLoadPanel != null) {
            if(bandBox != null) {
                //if the filter has changed, we have to clear the model and
                //the bandbox, and change its finder
                if(filterHasChanged) {
                    if(currentFilterByResources) {
                        bandBox.setFinder("workerMultipleFiltersFinder");
                        resourceLoadModel.clearCriteriaToShow();
                    }
                    else {
                        bandBox.setFinder("criterionMultipleFiltersFinder");
                        resourceLoadModel.clearResourcesToShow();
                    }
                    bandBox.clear();
                    bandBox.afterCompose();
                }

                //if the bandbox filter is active, we disable the name filter
                resourcesLoadPanel.setInternalPaginationDisabled(
                        !bandBox.getSelectedElements().isEmpty());
            }
            resourcesLoadPanel.init(resourceLoadModel.getLoadTimeLines(),
                    timeTracker);
            resourcesLoadPanel.setLoadChart(buildChart());
            if(filterHasChanged) {
                addNameFilterListener();
            }
        } else {
            resourcesLoadPanel = new ResourcesLoadPanel(resourceLoadModel
                    .getLoadTimeLines(), timeTracker, parent, resourceLoadModel
                    .isExpandResourceLoadViewCharts(), PaginationType.EXTERNAL_PAGINATION);

            if(filterBy == null) {
                addWorkersBandbox();
                addTimeFilter();
            }
            resourcesLoadPanel.setLoadChart(buildChart());
            addListeners();
        }
    }

    private void addWorkersBandbox() {
        bandBox = new BandboxMultipleSearch();
        bandBox.setId("workerBandboxMultipleSearch");
        bandBox.setWidthBandbox("185px");
        bandBox.setWidthListbox("300px");
        bandBox.setFinder("workerMultipleFiltersFinder");
        bandBox.afterCompose();

        Button button = new Button();
        button.setImage("/common/img/ico_filter.png");
        button.setTooltip(_("Filter by worker"));
        button.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                if(currentFilterByResources) {
                    filterResourcesFromBandbox();
                }
                else {
                    filterCriteriaFromBandbox();
                }
            }
        });

        Hbox hbox = new Hbox();
        hbox.appendChild(bandBox);
        hbox.appendChild(button);
        hbox.setAlign("center");

        resourcesLoadPanel.setVariable("additionalFilter2", hbox, true);
    }

    private void addTimeFilter() {
        Label label1 = new Label(_("Time filter") + ":");
        Label label2 = new Label("-");
        final Datebox initDate = new Datebox();
        initDate.setValue(asDate(resourceLoadModel.getInitDateFilter()));
        initDate.setWidth("75px");
        initDate.addEventListener(Events.ON_CHANGE, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                resourceLoadModel.setInitDateFilter(LocalDate
                        .fromDateFields(initDate.getValue()));
                reload(currentFilterByResources);
            }
        });
        final Datebox endDate = new Datebox();
        endDate.setValue(asDate(resourceLoadModel.getEndDateFilter()));
        endDate.setWidth("75px");
        endDate.addEventListener(Events.ON_CHANGE, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                resourceLoadModel.setEndDateFilter(LocalDate
                        .fromDateFields(endDate.getValue()));
                reload(currentFilterByResources);
            }
        });
        Hbox hbox = new Hbox();
        hbox.appendChild(label1);
        hbox.appendChild(initDate);
        hbox.appendChild(label2);
        hbox.appendChild(endDate);
        hbox.setAlign("center");

        resourcesLoadPanel.setVariable("additionalFilter1", hbox, true);
    }

    private void setupNameFilter() {
        Combobox filterByNameCombo = resourcesLoadPanel.getPaginationFilterCombobox();
        filterByNameCombo.getChildren().clear();
        List<Resource> resources = resourceLoadModel.getAllResourcesList();
        List<Criterion> criteria = resourceLoadModel.getAllCriteriaList();
        int size;
        if(currentFilterByResources) {
            size = resources.size();
        }
        else {
            size = criteria.size();
        }
        int pageSize = resourceLoadModel.getPageSize();

        if(size > pageSize) {
            int position = 0;
            while(position < size) {
                String firstName;
                String lastName;
                int newPosition = position + pageSize;
                if(currentFilterByResources) {
                    firstName = resources.get(position).getName();
                    if(newPosition - 1 < size) {
                        lastName = resources.get(newPosition - 1)
                        .getName();
                    }
                    else {
                        lastName = resources.get(size - 1)
                        .getName();
                    }
                }
                else {
                    Criterion criterion = criteria.get(position);
                    firstName = criterion.getType().getName() + ": " + criterion.getName();
                    if(newPosition - 1 < size) {
                        criterion = criteria.get(newPosition - 1);
                        lastName = criterion.getType().getName() + ": " + criterion.getName();
                    }
                    else {
                        criterion = criteria.get(size - 1);
                        lastName = criterion.getType().getName() + ": " + criterion.getName();
                    }
                }

                Comboitem item = new Comboitem();
                item.setLabel(firstName.substring(0, 1) + " - " + lastName.substring(0, 1));
                item.setDescription(firstName + " - " + lastName);
                item.setValue(new Integer(position));
                filterByNameCombo.appendChild(item);
                if(resourceLoadModel.getPageFilterPosition() == position) {
                    filterByNameCombo.setSelectedItemApi(item);
                }
                position = newPosition;
            }
        }

        Comboitem lastItem = new Comboitem();
        lastItem.setLabel(_("All"));
        lastItem.setDescription(_("Show all elements"));
        lastItem.setValue(new Integer(-1));
        filterByNameCombo.appendChild(lastItem);
        if(resourceLoadModel.getPageFilterPosition() == -1) {
            filterByNameCombo.setSelectedItemApi(lastItem);
        }

        if(filterByNameCombo.getSelectedIndex() == -1) {
            filterByNameCombo.setSelectedIndex(0);
        }
    }

    private void resetAdditionalFilters() {
        resourceLoadModel.setInitDateFilter(new LocalDate().minusDays(1));
        resourceLoadModel.setEndDateFilter(null);

        resourceLoadModel.setCriteriaToShow(new ArrayList<Criterion>());
        resourceLoadModel.setResourcesToShow(new ArrayList<Resource>());
    }

    private void deleteAdditionalFilters() {
        resourceLoadModel.setInitDateFilter(null);
        resourceLoadModel.setEndDateFilter(null);

        resourceLoadModel.setCriteriaToShow(new ArrayList<Criterion>());
        resourceLoadModel.setResourcesToShow(new ArrayList<Resource>());
    }

    @SuppressWarnings("unchecked")
    private void filterResourcesFromBandbox() {
        List<FilterPair> filterPairList = bandBox.getSelectedElements();
        List<Resource> workersList = new ArrayList<Resource>();
        for(FilterPair filterPair : filterPairList) {
            workersList.add((Resource)filterPair.getValue());
        }
        resourceLoadModel.setResourcesToShow(workersList);
        reload(true);
    }

    @SuppressWarnings("unchecked")
    private void filterCriteriaFromBandbox() {
        List<FilterPair> filterPairList = bandBox.getSelectedElements();
        List<Criterion> criteriaList = new ArrayList<Criterion>();
        for(FilterPair filterPair : filterPairList) {
            criteriaList.add((Criterion)filterPair.getValue());
        }
        resourceLoadModel.setCriteriaToShow(criteriaList);
        reload(false);
    }

    public void filterBy(Order order) {
        this.filterBy = order;
    }

    public void setPlanningControllerEntryPoints(
            IOrderPlanningGate planningControllerEntryPoints) {
        this.planningControllerEntryPoints = planningControllerEntryPoints;
    }

    public IOrderPlanningGate getPlanningControllerEntryPoints() {
        return this.planningControllerEntryPoints;
    }

    private void onSeeScheduleOf(LoadTimeLine taskLine) {

        TaskElement task = (TaskElement) taskLine.getRole().getEntity();
        Order order = resourceLoadModel.getOrderByTask(task);

        if (resourceLoadModel.userCanRead(order, SecurityUtils
                .getSessionUserLoginName())) {
            if (order.isScheduled()) {
                planningControllerEntryPoints.goToTaskResourceAllocation(order,
                    task);
             } else {
                try {
                    Messagebox.show(_("The project has no scheduled elements"),
                            _("Information"), Messagebox.OK,
                            Messagebox.INFORMATION);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            try {
                Messagebox
                        .show(_("You don't have read access to this project"),
                                _("Information"), Messagebox.OK,
                                Messagebox.INFORMATION);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
}


    private org.zkoss.zk.ui.Component buildChart() {
        Tabbox chartComponent = new Tabbox();
        chartComponent.setOrient("vertical");
        chartComponent.setHeight("200px");

        Tabs chartTabs = new Tabs();
        chartTabs.appendChild(new Tab(_("Load")));
        chartComponent.appendChild(chartTabs);
        chartTabs.setWidth("124px");

        Tabpanels chartTabpanels = new Tabpanels();
        Tabpanel loadChartPannel = new Tabpanel();
        CompanyPlanningModel.appendLoadChartAndLegend(loadChartPannel,
                buildLoadChart());
        chartTabpanels.appendChild(loadChartPannel);
        chartComponent.appendChild(chartTabpanels);

        return chartComponent;
    }

    private Timeplot buildLoadChart() {
        Timeplot chartLoadTimeplot = createEmptyTimeplot();

        loadChart = new Chart(chartLoadTimeplot,
                new ResourceLoadChartFiller(), timeTracker);
        loadChart.setZoomLevel(zoomLevel);
        if (resourcesLoadPanel.isVisibleChart()) {
            loadChart.fillChart();
        }
        timeTracker.addZoomListener(fillOnZoomChange(loadChart));
        resourcesLoadPanel
                .addChartVisibilityListener(fillOnChartVisibilityChange(loadChart));

        return chartLoadTimeplot;
    }

    private IZoomLevelChangedListener fillOnZoomChange(final Chart loadChart) {

        IZoomLevelChangedListener zoomListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                loadChart.setZoomLevel(detailLevel);

                if (resourcesLoadPanel.isVisibleChart()) {
                    loadChart.fillChart();
                }
            }
        };

        keepAliveZoomListeners.add(zoomListener);

        return zoomListener;
    }

    private IChartVisibilityChangedListener fillOnChartVisibilityChange(
            final Chart loadChart) {
        IChartVisibilityChangedListener chartVisibilityChangedListener = new IChartVisibilityChangedListener() {

            @Override
            public void chartVisibilityChanged(final boolean visible) {
                if (visible) {
                    loadChart.fillChart();
                }
            }
        };

        keepAliveChartVisibilityListeners.add(chartVisibilityChangedListener);
        return chartVisibilityChangedListener;
    }

    private Timeplot createEmptyTimeplot() {
        Timeplot timeplot = new Timeplot();
        timeplot.appendChild(new Plotinfo());
        return timeplot;
    }

    private class ResourceLoadChartFiller extends ChartFiller {

        @Override
        public void fillChart(Timeplot chart, Interval interval, Integer size) {
            chart.getChildren().clear();
            chart.invalidate();

            resetMinimumAndMaximumValueForChart();

            LocalDate start = interval.getStart();
            LocalDate finish = interval.getFinish();
            if ((resourceLoadModel.getInitDateFilter() != null)
                    && (resourceLoadModel.getInitDateFilter().compareTo(start) > 0)) {
                start = resourceLoadModel.getInitDateFilter();
            }
            if ((resourceLoadModel.getEndDateFilter() != null)
                    && (resourceLoadModel.getEndDateFilter().compareTo(finish) < 0)) {
                finish = resourceLoadModel.getEndDateFilter();
            }

            Plotinfo plotInfoLoad = createPlotinfoFromDurations(
                    getLoad(start, finish), interval);
            plotInfoLoad
                    .setFillColor(CompanyPlanningModel.COLOR_ASSIGNED_LOAD_GLOBAL);
            plotInfoLoad.setLineWidth(0);

            Plotinfo plotInfoMax = createPlotinfoFromDurations(
                    getCalendarMaximumAvailability(interval.getStart(),
                            interval.getFinish()), interval);
            plotInfoMax
                    .setLineColor(CompanyPlanningModel.COLOR_CAPABILITY_LINE);
            plotInfoMax.setFillColor("#FFFFFF");
            plotInfoMax.setLineWidth(2);

            Plotinfo plotInfoOverload = createPlotinfoFromDurations(
                    getOverload(start, finish), interval);
            plotInfoOverload
                    .setFillColor(CompanyPlanningModel.COLOR_OVERLOAD_GLOBAL);
            plotInfoOverload.setLineWidth(0);

            ValueGeometry valueGeometry = getValueGeometry();
            TimeGeometry timeGeometry = getTimeGeometry(interval);

            appendPlotinfo(chart, plotInfoLoad, valueGeometry, timeGeometry);
            appendPlotinfo(chart, plotInfoMax, valueGeometry, timeGeometry);
            appendPlotinfo(chart, plotInfoOverload, valueGeometry, timeGeometry);

            chart.setWidth(size + "px");
            chart.setHeight("150px");
        }

        private SortedMap<LocalDate, EffortDuration> getLoad(LocalDate start,
                LocalDate finish) {
            List<DayAssignment> dayAssignments = resourceLoadModel
                    .getDayAssignments();

            SortedMap<LocalDate, EffortDuration> result = new TreeMap<LocalDate, EffortDuration>();
            SortedMap<LocalDate, Map<Resource, EffortDuration>> dayAssignmentGrouped = groupDurationsByDayAndResource(dayAssignments);
            SortedMap<LocalDate, EffortDuration> mapDayAssignments = calculateHoursAdditionByDayWithoutOverload(dayAssignmentGrouped);
            for (Entry<LocalDate, EffortDuration> each : mapDayAssignments
                    .entrySet()) {
                LocalDate day = each.getKey();
                if (day.compareTo(start) >= 0 && day.compareTo(finish) <= 0) {
                    result.put(day, each.getValue());
                }
            }
            return result;
        }

        private SortedMap<LocalDate, EffortDuration> getOverload(
                LocalDate start, LocalDate finish) {
            List<DayAssignment> dayAssignments = resourceLoadModel
                    .getDayAssignments();

            SortedMap<LocalDate, Map<Resource, EffortDuration>> dayAssignmentGrouped = groupDurationsByDayAndResource(dayAssignments);
            SortedMap<LocalDate, EffortDuration> mapDayAssignments = calculateHoursAdditionByDayJustOverload(dayAssignmentGrouped);
            SortedMap<LocalDate, EffortDuration> mapMaxAvailability = calculateHoursAdditionByDay(
                    resourceLoadModel.getResources(), start, finish);
            for (Entry<LocalDate, EffortDuration> each : mapDayAssignments
                    .entrySet()) {
                LocalDate day = each.getKey();
                EffortDuration overload = each.getValue();
                if (day.compareTo(start) >= 0 && day.compareTo(finish) <= 0) {
                    EffortDuration maxAvailability = mapMaxAvailability
                            .get(day);
                    mapDayAssignments.put(day, overload.plus(maxAvailability));
                }
            }
            SortedMap<LocalDate, EffortDuration> result = new TreeMap<LocalDate, EffortDuration>();
            for (LocalDate day : mapDayAssignments.keySet()) {
                if (day.compareTo(start) >= 0 && day.compareTo(finish) <= 0) {
                    result.put(day, mapDayAssignments.get(day));
                }
            }
            return result;
        }

        private SortedMap<LocalDate, EffortDuration> calculateHoursAdditionByDayWithoutOverload(
                SortedMap<LocalDate, Map<Resource, EffortDuration>> dayAssignmentGrouped) {
            SortedMap<LocalDate, EffortDuration> map = new TreeMap<LocalDate, EffortDuration>();
            for (Entry<LocalDate, Map<Resource, EffortDuration>> each : dayAssignmentGrouped
                    .entrySet()) {
                LocalDate date = each.getKey();
                PartialDay day = PartialDay.wholeDay(date);
                EffortDuration result = zero();
                for (Entry<Resource, EffortDuration> resourceWithDuration : each
                        .getValue().entrySet()) {
                    Resource resource = resourceWithDuration.getKey();
                    EffortDuration assignedDuration = resourceWithDuration
                            .getValue();
                    EffortDuration resourceCapacity = resource
                            .getCalendarOrDefault().getCapacityOn(day);
                    result = result
                            .plus(min(assignedDuration, resourceCapacity));
                }
                map.put(date, result);
            }
            return groupAsNeededByZoom(map);
        }

        private SortedMap<LocalDate, EffortDuration> calculateHoursAdditionByDayJustOverload(
                SortedMap<LocalDate, Map<Resource, EffortDuration>> dayAssignmentGrouped) {
            SortedMap<LocalDate, EffortDuration> map = new TreeMap<LocalDate, EffortDuration>();
            for (Entry<LocalDate, Map<Resource, EffortDuration>> each : dayAssignmentGrouped
                    .entrySet()) {
                final LocalDate date = each.getKey();
                final PartialDay day = PartialDay.wholeDay(date);
                EffortDuration result = zero();
                for (Entry<Resource, EffortDuration> resourceWithDuration : each
                        .getValue().entrySet()) {
                    Resource resource = resourceWithDuration.getKey();
                    EffortDuration assignedDuration = resourceWithDuration
                            .getValue();
                    EffortDuration resourceCapacity = resource
                            .getCalendarOrDefault().getCapacityOn(day);
                    EffortDuration overloadIncrement = assignedDuration
                            .minus(min(resourceCapacity, assignedDuration));
                    result = result.plus(overloadIncrement);
                }
                map.put(date, result);
            }
            return groupAsNeededByZoom(map);
        }

        private SortedMap<LocalDate, EffortDuration> getCalendarMaximumAvailability(
                LocalDate start, LocalDate finish) {
            return calculateHoursAdditionByDay(
                    resourceLoadModel.getResources(), start, finish);
        }

        private SortedMap<LocalDate, EffortDuration> calculateHoursAdditionByDay(
                List<Resource> resources, LocalDate start, LocalDate finish) {
            return new EffortByDayCalculator<Entry<LocalDate, List<Resource>>>() {

                @Override
                protected LocalDate getDayFor(
                        Entry<LocalDate, List<Resource>> element) {
                    return element.getKey();
                }

                @Override
                protected EffortDuration getDurationFor(
                        Entry<LocalDate, List<Resource>> element) {
                    return sumCalendarCapacitiesForDay(element.getValue(),
                            element.getKey());
                }
            }.calculate(getResourcesByDateBetween(resources, start, finish));
        }

        private Set<Entry<LocalDate, List<Resource>>> getResourcesByDateBetween(
                List<Resource> resources, LocalDate start, LocalDate finish) {
            Map<LocalDate, List<Resource>> result = new HashMap<LocalDate, List<Resource>>();
            for (LocalDate date = start; date.compareTo(finish) <= 0; date = date
                    .plusDays(1)) {
                result.put(date, resources);
            }
            return result.entrySet();
        }

    }

}
