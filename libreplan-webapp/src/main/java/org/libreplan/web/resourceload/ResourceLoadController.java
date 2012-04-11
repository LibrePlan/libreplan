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

package org.libreplan.web.resourceload;

import static org.libreplan.web.I18nHelper._;
import static org.libreplan.web.resourceload.ResourceLoadModel.asDate;
import static org.libreplan.web.resourceload.ResourceLoadModel.toLocal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.chart.ILoadChartData;
import org.libreplan.business.planner.chart.ResourceLoadChartData;
import org.libreplan.business.planner.entities.DayAssignment;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.resources.daos.IResourcesSearcher;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.web.common.components.bandboxsearch.BandboxMultipleSearch;
import org.libreplan.web.common.components.finders.FilterPair;
import org.libreplan.web.planner.chart.Chart;
import org.libreplan.web.planner.chart.StandardLoadChartFiller;
import org.libreplan.web.planner.company.CompanyPlanningModel;
import org.libreplan.web.planner.order.BankHolidaysMarker;
import org.libreplan.web.planner.order.IOrderPlanningGate;
import org.libreplan.web.planner.order.PlanningStateCreator;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.libreplan.web.resourceload.ResourceLoadParameters.Paginator;
import org.libreplan.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkforge.timeplot.Plotinfo;
import org.zkforge.timeplot.Timeplot;
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
import org.zkoss.ganttz.util.Emitter;
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

    private PlanningState filterBy;

    private org.zkoss.zk.ui.Component parent;

    @Autowired
    private PlanningStateCreator planningStateCreator;

    private Reloader reloader = new Reloader();

    private IOrderPlanningGate planningControllerEntryPoints;

    @Autowired
    private IResourcesSearcher resourcesSearcher;

    public ResourceLoadController() {
    }

    public void add(IToolbarCommand... commands) {
        Validate.noNullElements(commands);
        this.commands.addAll(Arrays.asList(commands));
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) {
        this.parent = comp;
    }

    public void reload() {
        reloader.resetToInitialState();
        reloadWithoutReset();
    }

    private void reloadWithoutReset() {
        transactionService.runOnReadOnlyTransaction(reloader.reload());
    }

    private final Runnable onChange = new Runnable() {
        public void run() {
            reloadWithoutReset();
        }
    };

    private final class Reloader {

        private ResourcesLoadPanel resourcesLoadPanel = null;

        private ListenerTracker listeners = new ListenerTracker();

        private TimeTracker timeTracker;

        public Reloader() {
        }

        private List<VisualizationModifier> visualizationModifiers = null;

        private List<VisualizationModifier> getVisualizationModifiers() {
            if (visualizationModifiers != null) {
                return visualizationModifiers;
            }
            return visualizationModifiers = buildVisualizationModifiers();
        }

        private List<IListenerAdder> listenersToAdd = null;

        private List<IListenerAdder> getListenersToAdd() {
            if (listenersToAdd != null) {
                return listenersToAdd;
            }
            List<IListenerAdder> result = new ArrayList<IListenerAdder>();
            for (VisualizationModifier each : getVisualizationModifiers()) {
                if (each instanceof IListenerAdder) {
                    result.add((IListenerAdder) each);
                }
            }
            result.add(new GoToScheduleListener());
            return listenersToAdd = result;
        }

        public void resetToInitialState() {
            timeTracker = null;
            resourcesLoadPanel = null;
            listeners = new ListenerTracker();
            visualizationModifiers = null;
            listenersToAdd = null;
        }

        public IOnTransaction<Void> reload() {
            return new IOnTransaction<Void>() {

                @Override
                public Void execute() {
                    reloadInTransaction();
                    return null;
                }
            };
        }

        private void reloadInTransaction() {
            for (VisualizationModifier each : getVisualizationModifiers()) {
                each.checkDependencies();
            }
            ResourceLoadParameters parameters = new ResourceLoadParameters(
                    filterBy);
            for (VisualizationModifier each : getVisualizationModifiers()) {
                each.applyToParameters(parameters);
            }

            ResourceLoadDisplayData dataToShow = resourceLoadModel.calculateDataToDisplay(parameters);

            timeTracker = buildTimeTracker(dataToShow);
            if (resourcesLoadPanel == null) {
                resourcesLoadPanel = buildPanel(dataToShow);
                listeners.addListeners(resourcesLoadPanel, getListenersToAdd());
                parent.getChildren().clear();
                parent.appendChild(resourcesLoadPanel);
                for (VisualizationModifier each : getVisualizationModifiers()) {
                    each.setup(resourcesLoadPanel);
                }
            } else {
                resourcesLoadPanel.init(dataToShow.getLoadTimeLines(),
                        timeTracker);
                listeners.addListeners(resourcesLoadPanel, getListenersToAdd());
            }

            resourcesLoadPanel.afterCompose();
            addCommands(resourcesLoadPanel);

            for (VisualizationModifier each : getVisualizationModifiers()) {
                each.updateUI(resourcesLoadPanel, dataToShow);
            }
        }

        private TimeTracker buildTimeTracker(ResourceLoadDisplayData dataToShow) {
            ZoomLevel zoomLevel = dataToShow.getInitialZoomLevel();
            TimeTracker result = new TimeTracker(dataToShow.getViewInterval(),
                    zoomLevel, SeveralModificators.create(),
                    SeveralModificators.create(createBankHolidaysMarker()),
                    parent);
            return result;
        }

        private ResourcesLoadPanel buildPanel(ResourceLoadDisplayData dataToShow) {
            return new ResourcesLoadPanel(dataToShow.getLoadTimeLines(),
                    timeTracker, parent,
                    resourceLoadModel.isExpandResourceLoadViewCharts(),
                    PaginationType.EXTERNAL_PAGINATION);
        }
    }

    private List<VisualizationModifier> buildVisualizationModifiers() {
        List<VisualizationModifier> result = new ArrayList<VisualizationModifier>();
        FilterTypeChanger filterTypeChanger = new FilterTypeChanger(onChange,
                filterBy);
        result.add(filterTypeChanger);
        result.add(new ByDatesFilter(onChange, filterBy));
        WorkersOrCriteriaBandbox bandbox = new WorkersOrCriteriaBandbox(
                onChange, filterBy, filterTypeChanger, resourcesSearcher);
        result.add(bandbox);
        result.add(new ByNamePaginator(onChange, filterBy, filterTypeChanger,
                bandbox));
        result.add(new LoadChart(onChange, filterBy));
        return result;
    }

    public interface IListenerAdder {

        public Object addAndReturnListener(ResourcesLoadPanel panel);
    }

    private class GoToScheduleListener implements IListenerAdder {

        @Override
        public Object addAndReturnListener(ResourcesLoadPanel panel) {
            ISeeScheduledOfListener listener = new ISeeScheduledOfListener() {

                @Override
                public void seeScheduleOf(LoadTimeLine taskLine) {
                    onSeeScheduleOf(taskLine);
                }
            };
            panel.addSeeScheduledOfListener(listener);
            return listener;
        }

    }

    private void onSeeScheduleOf(LoadTimeLine taskLine) {

        TaskElement task = (TaskElement) taskLine.getRole().getEntity();
        Order order = resourceLoadModel.getOrderByTask(task);

        if (resourceLoadModel.userCanRead(order,
                SecurityUtils.getSessionUserLoginName())) {
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

    /**
     * Some set of widgets that can change the data visualized: filtering,
     * pagination, etc.
     */
    private static abstract class VisualizationModifier {

        private final Runnable onChange;
        private final PlanningState filterBy;

        private VisualizationModifier(Runnable onChange, PlanningState filterBy) {
            this.onChange = onChange;
            this.filterBy = filterBy;
        }

        protected final void notifyChange() {
            onChange.run();
        }

        protected boolean isAppliedToOrder() {
            return filterBy != null;
        }

        void setup(ResourcesLoadPanel panel) {
        }

        void checkDependencies() {

        }

        void applyToParameters(ResourceLoadParameters parameters) {
        }

        void updateUI(ResourcesLoadPanel panel, ResourceLoadDisplayData generatedData) {
        }
    }

    private static class FilterTypeChanger extends VisualizationModifier
            implements IListenerAdder {

        private boolean filterByResources = true;

        private FilterTypeChanger(Runnable onChange, PlanningState filterBy) {
            super(onChange, filterBy);
        }

        public boolean isFilterByResources() {
            return filterByResources;
        }

        @Override
        void applyToParameters(ResourceLoadParameters parameters) {
            parameters.setFilterByResources(filterByResources);
        }

        @Override
        public Object addAndReturnListener(ResourcesLoadPanel panel) {
            IFilterChangedListener listener = new IFilterChangedListener() {

                @Override
                public void filterChanged(boolean newValue) {
                    if (filterByResources != newValue) {
                        filterByResources = newValue;
                        notifyChange();
                    }
                }
            };
            panel.addFilterListener(listener);
            return listener;
        }
    }

    private static class ByDatesFilter extends VisualizationModifier {

        private LocalDate startDateValue;

        private LocalDate endDateValue = null;

        private final Datebox startBox = new Datebox();

        private final Datebox endBox = new Datebox();

        private ByDatesFilter(Runnable onChange, PlanningState filterBy) {
            super(onChange, filterBy);
            startDateValue = isAppliedToOrder() ? null : new LocalDate()
                    .minusDays(1);
        }

        @Override
        void setup(ResourcesLoadPanel panel) {
            if (isAppliedToOrder()) {
                return;
            }
            panel.setFirstOptionalFilter(buildTimeFilter());
        }

        private Hbox buildTimeFilter() {
            Label label1 = new Label(_("Time filter") + ":");
            Label label2 = new Label("-");
            startBox.setValue(asDate(startDateValue));
            startBox.setWidth("75px");
            startBox.addEventListener(Events.ON_CHANGE, new EventListener() {
                @Override
                public void onEvent(Event event) {
                    LocalDate newStart = toLocal(startBox.getValue());
                    if (!ObjectUtils.equals(startDateValue, newStart)) {
                        startDateValue = newStart;
                        notifyChange();
                    }
                }
            });
            endBox.setValue(asDate(endDateValue));
            endBox.setWidth("75px");
            endBox.addEventListener(Events.ON_CHANGE, new EventListener() {
                @Override
                public void onEvent(Event event) {
                    LocalDate newEnd = toLocal(endBox.getValue());
                    if (!ObjectUtils.equals(endBox, newEnd)) {
                        endDateValue = newEnd;
                        notifyChange();
                    }
                }
            });
            Hbox hbox = new Hbox();
            hbox.appendChild(label1);
            hbox.appendChild(startBox);
            hbox.appendChild(label2);
            hbox.appendChild(endBox);
            hbox.setAlign("center");
            return hbox;
        }

        @Override
        void applyToParameters(ResourceLoadParameters parameters) {
            parameters.setInitDateFilter(startDateValue);
            parameters.setEndDateFilter(endDateValue);
        }

    }

    private static abstract class DependingOnFiltering extends
            VisualizationModifier {

        private final FilterTypeChanger filterType;

        private boolean filteringByResource;

        DependingOnFiltering(Runnable onChange, PlanningState filterBy,
                FilterTypeChanger filterType) {
            super(onChange, filterBy);
            this.filterType = filterType;
            this.filteringByResource = filterType.isFilterByResources();
        }

        public boolean isFilteringByResource() {
            return filteringByResource;
        }

        @Override
        void checkDependencies() {
            if (this.filteringByResource != filterType.isFilterByResources()) {
                this.filteringByResource = filterType.isFilterByResources();
                filterTypeChanged();
            }
        }

        protected abstract void filterTypeChanged();

    }

    private static class WorkersOrCriteriaBandbox extends DependingOnFiltering {

        private final BandboxMultipleSearch bandBox = new BandboxMultipleSearch();

        private List<Object> entitiesSelected = null;

        private final IResourcesSearcher resourcesSearcher;

        private WorkersOrCriteriaBandbox(Runnable onChange,
                PlanningState filterBy, FilterTypeChanger filterType,
                IResourcesSearcher resourcesSearcher) {
            super(onChange, filterBy, filterType);
            this.resourcesSearcher = resourcesSearcher;
        }

        @Override
        void setup(ResourcesLoadPanel panel) {
            if (isAppliedToOrder()) {
                return;
            }
            panel.setSecondOptionalFilter(buildBandboxFilterer());
        }

        private Hbox buildBandboxFilterer() {
            bandBox.setId("workerBandboxMultipleSearch");
            bandBox.setWidthBandbox("185px");
            bandBox.setWidthListbox("450px");
            bandBox.setFinder(getFinderToUse());
            bandBox.afterCompose();

            Button button = new Button();
            button.setImage("/common/img/ico_filter.png");
            button.setTooltip(_("Filter by worker"));
            button.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event event) {
                    entitiesSelected = getSelected();
                    notifyChange();
                }
            });

            Hbox hbox = new Hbox();
            hbox.appendChild(bandBox);
            hbox.appendChild(button);
            hbox.setAlign("center");
            return hbox;
        }

        private String getFinderToUse() {
            if (isFilteringByResource()) {
                return "workerMultipleFiltersFinder";
            } else {
                return "criterionMultipleFiltersFinder";
            }
        }

        @Override
        protected void filterTypeChanged() {
            if (isAppliedToOrder()) {
                return;
            }
            entitiesSelected = null;
            bandBox.setFinder(getFinderToUse());
        }

        @Override
        void applyToParameters(ResourceLoadParameters parameters) {
            if (!hasEntitiesSelected()) {
                parameters.clearResourcesToShow();
                parameters.clearCriteriaToShow();
            } else if (isFilteringByResource()) {
                parameters.setResourcesToShow(calculateResourcesToShow());
            } else {
                parameters.setCriteriaToShow(as(Criterion.class,
                        entitiesSelected));
            }
        }

        private List<Resource> calculateResourcesToShow() {
            List<Resource> resources = new ArrayList<Resource>();
            List<Criterion> criteria = new ArrayList<Criterion>();

            for (Object each : entitiesSelected) {
                if (each instanceof Resource) {
                    resources.add((Resource) each);
                } else {
                    criteria.add((Criterion) each);
                }
            }

            if (!criteria.isEmpty()) {
                resources.addAll(resourcesSearcher.searchBoth()
                        .byCriteria(criteria).execute());
            }

            return resources;
        }

        public boolean hasEntitiesSelected() {
            return entitiesSelected != null && !entitiesSelected.isEmpty();
        }

        private List<Object> getSelected() {
            List<Object> result = new ArrayList<Object>();
            @SuppressWarnings("unchecked")
            List<FilterPair> filterPairList = bandBox.getSelectedElements();
            for (FilterPair filterPair : filterPairList) {
                result.add(filterPair.getValue());
            }
            return result;
        }

    }

    private static class ByNamePaginator extends DependingOnFiltering
            implements IListenerAdder {

        private static final int ALL = -1;

        private final WorkersOrCriteriaBandbox bandbox;

        private int currentPosition;

        private List<? extends BaseEntity> allEntitiesShown = null;

        public ByNamePaginator(Runnable onChange, PlanningState filterBy,
                FilterTypeChanger filterTypeChanger,
                WorkersOrCriteriaBandbox bandbox) {
            super(onChange, filterBy, filterTypeChanger);
            this.bandbox = bandbox;
            this.currentPosition = initialPage();
        }

        private int initialPage() {
            return isAppliedToOrder() ? ALL : 0;
        }

        @Override
        public Object addAndReturnListener(ResourcesLoadPanel panel) {
            IPaginationFilterChangedListener listener = new IPaginationFilterChangedListener() {
                @Override
                public void filterChanged(int newPosition) {
                    if (currentPosition != newPosition) {
                        currentPosition = newPosition;
                        notifyChange();
                    }
                }
            };
            panel.addPaginationFilterListener(listener);
            return listener;
        }

        @Override
        void checkDependencies() {
            super.checkDependencies();
            if (bandbox.hasEntitiesSelected()) {
                this.currentPosition = ALL;
            }
        }

        @Override
        protected void filterTypeChanged() {
            this.currentPosition = 0;
            this.allEntitiesShown = null;
        }

        @Override
        void applyToParameters(ResourceLoadParameters parameters) {
            parameters.setPageFilterPosition(currentPosition);
        }

        @Override
        void updateUI(ResourcesLoadPanel panel, ResourceLoadDisplayData generatedData) {
            panel.setInternalPaginationDisabled(bandbox.hasEntitiesSelected());
            Paginator<? extends BaseEntity> paginator = generatedData
                    .getPaginator();
            List<? extends BaseEntity> newAllEntities = paginator.getAll();
            if (this.allEntitiesShown == null
                    || !equivalent(this.allEntitiesShown, newAllEntities)) {
                this.currentPosition = initialPage();
                this.allEntitiesShown = newAllEntities;
                updatePages(panel.getPaginationFilterCombobox(),
                        pagesByName(this.allEntitiesShown,
                                paginator.getPageSize()));
            }
        }

        private boolean equivalent(List<? extends BaseEntity> a,
                List<? extends BaseEntity> b) {
            if (a == null || b == null) {
                return false;
            }
            if (a.size() != b.size()) {
                return false;
            }
            for (int i = 0; i < a.size(); i++) {
                BaseEntity aElement = a.get(i);
                BaseEntity bElement = b.get(i);
                if (!ObjectUtils.equals(aElement.getId(), bElement.getId())) {
                    return false;
                }
            }
            return true;
        }

        private void updatePages(Combobox filterByNameCombo,
                List<Comboitem> pages) {
            if (filterByNameCombo == null) {
                return;
            }
            filterByNameCombo.getChildren().clear();

            Comboitem lastItem = new Comboitem();
            lastItem.setLabel(_("All"));
            lastItem.setDescription(_("Show all elements"));
            lastItem.setValue(ALL);
            pages.add(lastItem);

            for (Comboitem each : pages) {
                filterByNameCombo.appendChild(each);
            }

            if (currentPosition >= 0 && currentPosition < pages.size()) {
                filterByNameCombo
                        .setSelectedItemApi(pages.get(currentPosition));
            } else if (currentPosition == ALL) {
                filterByNameCombo.setSelectedItemApi(lastItem);
            } else {
                filterByNameCombo.setSelectedIndex(0);
            }
        }

        private List<Comboitem> pagesByName(List<?> list, int pageSize) {
            if (list.isEmpty()) {
                return new ArrayList<Comboitem>();
            }
            Object first = list.get(0);
            if (first instanceof Resource) {
                return pagesByName(as(Resource.class, list), pageSize,
                        new INameExtractor<Resource>() {

                    @Override
                    public String getNameOf(Resource resource) {
                        return resource.getName();
                    }
                });
            } else {
                return pagesByName(as(Criterion.class, list), pageSize,
                        new INameExtractor<Criterion>() {

                    @Override
                    public String getNameOf(Criterion criterion) {
                        return criterion.getType().getName() + ": "
                                + criterion.getName();
                    }
                });
            }
        }

        interface INameExtractor<T> {
            public String getNameOf(T value);
        }

        private <T> List<Comboitem> pagesByName(List<T> elements,
                int pageSize,
                INameExtractor<T> nameExtractor) {
            List<Comboitem> result = new ArrayList<Comboitem>();
            for (int startPos = 0; startPos < elements.size(); startPos += pageSize) {
                int endPos = Math.min(startPos + pageSize - 1,
                        elements.size() - 1);
                String first = nameExtractor.getNameOf(elements.get(startPos));
                String end = nameExtractor.getNameOf(elements.get(endPos));
                Comboitem item = buildPageCombo(startPos, first, end);
                result.add(item);
            }
            return result;
        }

        private Comboitem buildPageCombo(int startPosition, String first,
                String end) {
            Comboitem result = new Comboitem();
            result.setLabel(first.substring(0, 1) + " - " + end.substring(0, 1));
            result.setDescription(first + " - " + end);
            result.setValue(startPosition);
            return result;
        }

    }

    private static <T> List<T> as(Class<T> klass, Collection<?> entities) {
        List<T> result = new ArrayList<T>(entities.size());
        for (Object each : entities) {
            result.add(klass.cast(each));
        }
        return result;
    }

    class LoadChart extends VisualizationModifier implements IListenerAdder {

        private Emitter<Timeplot> emitter = Emitter.withInitial(null);

        private volatile Chart loadChart;

        private IZoomLevelChangedListener zoomLevelListener;

        public LoadChart(Runnable onChange, PlanningState filterBy) {
            super(onChange, filterBy);
        }

        void setup(ResourcesLoadPanel panel) {
            panel.setLoadChart(buildChart(panel, emitter));
        }

        public Object addAndReturnListener(ResourcesLoadPanel panel) {
            IChartVisibilityChangedListener visibilityChangedListener = fillOnChartVisibilityChange();
            panel.addChartVisibilityListener(visibilityChangedListener);
            return visibilityChangedListener;
        }

        private IChartVisibilityChangedListener fillOnChartVisibilityChange() {
            IChartVisibilityChangedListener result = new IChartVisibilityChangedListener() {

                @Override
                public void chartVisibilityChanged(final boolean visible) {
                    if (visible && loadChart != null) {
                        loadChart.fillChart();
                    }
                }
            };
            return result;
        }

        private Tabbox buildChart(ResourcesLoadPanel resourcesLoadPanel,
                Emitter<Timeplot> timePlot) {
            Tabbox chartComponent = new Tabbox();
            chartComponent.setOrient("vertical");
            chartComponent.setHeight("200px");

            Tabs chartTabs = new Tabs();
            chartTabs.appendChild(new Tab(_("Load")));
            chartComponent.appendChild(chartTabs);
            chartTabs.setWidth("124px");

            Tabpanels chartTabpanels = new Tabpanels();
            Tabpanel loadChartPannel = new Tabpanel();
            // avoid adding Timeplot since it has some pending issues
            CompanyPlanningModel.appendLoadChartAndLegend(loadChartPannel,
                    timePlot);
            chartTabpanels.appendChild(loadChartPannel);
            chartComponent.appendChild(chartTabpanels);
            return chartComponent;
        }

        @Override
        void updateUI(ResourcesLoadPanel panel,
                ResourceLoadDisplayData generatedData) {
            TimeTracker timeTracker = panel.getTimeTracker();
            zoomLevelListener = fillOnZoomChange(panel);
            timeTracker.addZoomListener(zoomLevelListener);

            Timeplot newLoadChart = buildLoadChart(panel, generatedData,
                    timeTracker);
            emitter.emit(newLoadChart);
        }

        private Timeplot buildLoadChart(ResourcesLoadPanel resourcesLoadPanel,
                ResourceLoadDisplayData generatedData, TimeTracker timeTracker) {
            Timeplot chartLoadTimeplot = createEmptyTimeplot();

            ResourceLoadChartFiller chartFiller =
                    new ResourceLoadChartFiller(generatedData);
            loadChart = new Chart(chartLoadTimeplot,
                    chartFiller, timeTracker);
            loadChart.setZoomLevel(timeTracker.getDetailLevel());
            chartFiller.initializeResources();
            if (resourcesLoadPanel.isVisibleChart()) {
                loadChart.fillChart();
            }
            return chartLoadTimeplot;
        }

        private IZoomLevelChangedListener fillOnZoomChange(
                final ResourcesLoadPanel resourcesLoadPanel) {

            IZoomLevelChangedListener zoomListener = new IZoomLevelChangedListener() {

                @Override
                public void zoomLevelChanged(ZoomLevel detailLevel) {
                    if (loadChart == null) {
                        return;
                    }
                    loadChart.setZoomLevel(detailLevel);

                    if (resourcesLoadPanel.isVisibleChart()) {
                        loadChart.fillChart();
                    }
                    adjustZoomPositionScroll(resourcesLoadPanel);
                }
            };

            return zoomListener;
        }
    }

    private void adjustZoomPositionScroll(ResourcesLoadPanel resourcesLoadPanel) {
        resourcesLoadPanel.getTimeTrackerComponent().movePositionScroll();
    }

    private Timeplot createEmptyTimeplot() {
        Timeplot timeplot = new Timeplot();
        timeplot.appendChild(new Plotinfo());
        return timeplot;
    }

    private class ResourceLoadChartFiller extends StandardLoadChartFiller {

        private final ResourceLoadDisplayData generatedData;

        private List<Resource> resources;

        public ResourceLoadChartFiller(ResourceLoadDisplayData generatedData) {
            this.generatedData = generatedData;
        }

        @Override
        protected String getOptionalJavascriptCall() {
            return null;
        }

        @Override
        protected ILoadChartData getDataOn(Interval interval) {
            List<DayAssignment> assignments = generatedData
                    .getDayAssignmentsConsidered();
            return new ResourceLoadChartData(assignments,
                    resources, interval.getStart(), interval.getFinish());
        }

        private void initializeResources() {
            resources = generatedData.getResourcesConsidered();
        }


    }

    private static class ListenerTracker {
        private final List<Object> trackedListeners = new ArrayList<Object>();

        public void addListeners(ResourcesLoadPanel panel,
                Iterable<IListenerAdder> listeners) {
            for (IListenerAdder each : listeners) {
                Object listener = each.addAndReturnListener(panel);
                trackedListeners.add(listener);
            }
        }
    }

    private void addCommands(ResourcesLoadPanel resourcesLoadPanel) {
        resourcesLoadPanel.add(commands.toArray(new IToolbarCommand[commands
                .size()]));
    }

    private BankHolidaysMarker createBankHolidaysMarker() {
        BaseCalendar defaultCalendar = configurationDAO.getConfiguration()
                .getDefaultCalendar();
        return BankHolidaysMarker.create(defaultCalendar);
    }

    public void filterBy(Order order) {
        this.filterBy = order == null ? null : createPlanningState(order);
    }

    PlanningState createPlanningState(final Order order) {
        return transactionService
                .runOnReadOnlyTransaction(new IOnTransaction<PlanningState>() {

                    @Override
                    public PlanningState execute() {
                        return planningStateCreator.retrieveOrCreate(
                                parent.getDesktop(), order);
                    }
                });
    }

    public void setPlanningControllerEntryPoints(
            IOrderPlanningGate planningControllerEntryPoints) {
        this.planningControllerEntryPoints = planningControllerEntryPoints;
    }

    public IOrderPlanningGate getPlanningControllerEntryPoints() {
        return this.planningControllerEntryPoints;
    }

}
