/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2013 Igalia, S.L.
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

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.joda.time.LocalDate;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.planner.chart.ILoadChartData;
import org.libreplan.business.planner.chart.ResourceLoadChartData;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.resources.daos.IResourcesSearcher;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.common.FilterUtils;
import org.libreplan.web.common.components.bandboxsearch.BandboxMultipleSearch;
import org.libreplan.web.common.components.finders.FilterPair;
import org.libreplan.web.common.components.finders.ResourceAllocationFilterEnum;
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
import org.zkoss.ganttz.timetracker.zoom.SeveralModifiers;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Emitter;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Composer;
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
import org.zkoss.zul.Combobox;

/**
 * Controller for Resource Load view ( global and for order ).
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
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

    @Autowired
    private IResourcesSearcher resourcesSearcher;

    @Autowired
    private PlanningStateCreator planningStateCreator;

    private List<IToolbarCommand> commands = new ArrayList<>();

    private PlanningState filterBy;

    private org.zkoss.zk.ui.Component parent;

    private Reloader reloader = new Reloader();

    private IOrderPlanningGate planningControllerEntryPoints;

    private final Runnable onChange = new Runnable() {
        public void run() {
            reloadWithoutReset();
        }
    };

    public ResourceLoadController() {
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) {
        this.parent = comp;
    }

    public void add(IToolbarCommand... commands) {
        Validate.noNullElements(commands);
        this.commands.addAll(Arrays.asList(commands));
    }

    public void reload() {
        reloader.resetToInitialState();
        reloadWithoutReset();
    }

    public void filterBy(Order order) {
        this.filterBy = (order == null) ? null : createPlanningState(order);
    }

    private static <T> List<T> as(Class<T> klass, Collection<?> entities) {
        List<T> result = new ArrayList<>(entities.size());
        for (Object each : entities) {
            result.add(klass.cast(each));
        }

        return result;
    }

    private void reloadWithoutReset() {
        transactionService.runOnReadOnlyTransaction(reloader.reload());
    }

    PlanningState createPlanningState(final Order order) {
        return transactionService.runOnReadOnlyTransaction(new IOnTransaction<PlanningState>() {
            @Override
            public PlanningState execute() {
                return planningStateCreator.retrieveOrCreate(parent.getDesktop(), order);
            }
        });
    }

    public interface IListenerAdder {

        Object addAndReturnListener(ResourcesLoadPanel panel);
    }

    class LoadChart extends VisualizationModifier implements IListenerAdder {

        private Emitter<Timeplot> emitter = Emitter.withInitial(null);

        private volatile Chart loadChart;

        private IZoomLevelChangedListener zoomLevelListener;

        public LoadChart(Runnable onChange, PlanningState filterBy) {
            super(onChange, filterBy);
        }

        @Override
        void setup(ResourcesLoadPanel panel) {
            panel.setLoadChart(buildChart(emitter));
        }

        @Override
        public Object addAndReturnListener(ResourcesLoadPanel panel) {
            IChartVisibilityChangedListener visibilityChangedListener = fillOnChartVisibilityChange();
            panel.addChartVisibilityListener(visibilityChangedListener);

            return visibilityChangedListener;
        }

        private IChartVisibilityChangedListener fillOnChartVisibilityChange() {
            return new IChartVisibilityChangedListener() {
                @Override
                public void chartVisibilityChanged(final boolean visible) {
                    if (visible && loadChart != null) {
                        loadChart.fillChart();
                    }
                }
            };
        }

        private Tabbox buildChart(Emitter<Timeplot> timePlot) {
            Tabbox chartComponent = new Tabbox();
            chartComponent.setOrient("vertical");
            chartComponent.setHeight("200px");

            Tabs chartTabs = new Tabs();
            chartTabs.appendChild(new Tab(_("Load")));
            chartComponent.appendChild(chartTabs);
            chartTabs.setWidth("124px");

            Tabpanels chartTabpanels = new Tabpanels();
            Tabpanel loadChartPanel = new Tabpanel();

            // Avoid adding Timeplot since it has some pending issues
            CompanyPlanningModel.appendLoadChartAndLegend(loadChartPanel, timePlot);
            chartTabpanels.appendChild(loadChartPanel);
            chartComponent.appendChild(chartTabpanels);

            return chartComponent;
        }

        @Override
        void updateUI(ResourcesLoadPanel panel, ResourceLoadDisplayData generatedData) {
            TimeTracker timeTracker = panel.getTimeTracker();
            zoomLevelListener = fillOnZoomChange(panel);
            timeTracker.addZoomListener(zoomLevelListener);

            Timeplot newLoadChart = buildLoadChart(panel, generatedData, timeTracker);
            emitter.emit(newLoadChart);
        }

        private Timeplot buildLoadChart(
                ResourcesLoadPanel resourcesLoadPanel, ResourceLoadDisplayData generatedData, TimeTracker timeTracker) {

            Timeplot chartLoadTimeplot = createEmptyTimeplot();

            ResourceLoadChartFiller chartFiller = new ResourceLoadChartFiller(generatedData);
            loadChart = new Chart(chartLoadTimeplot, chartFiller, timeTracker);
            loadChart.setZoomLevel(timeTracker.getDetailLevel());
            chartFiller.initializeResources();

            if ( resourcesLoadPanel.isVisibleChart() ) {
                loadChart.fillChart();
            }

            return chartLoadTimeplot;
        }

        private IZoomLevelChangedListener fillOnZoomChange(final ResourcesLoadPanel resourcesLoadPanel) {
            return detailLevel -> {
                if ( loadChart == null ) {
                    return;
                }

                loadChart.setZoomLevel(detailLevel);

                if ( resourcesLoadPanel.isVisibleChart() ) {
                    loadChart.fillChart();
                }
                adjustZoomPositionScroll(resourcesLoadPanel);
            };
        }

        private void adjustZoomPositionScroll(ResourcesLoadPanel resourcesLoadPanel) {
            resourcesLoadPanel.getTimeTrackerComponent().movePositionScroll();
        }

        private Timeplot createEmptyTimeplot() {
            Timeplot timeplot = new Timeplot();
            timeplot.appendChild(new Plotinfo());
            return timeplot;
        }
    }

    /**
     * Some set of widgets that can change the data visualized: filtering, pagination, etc.
     */
    private abstract static class VisualizationModifier {

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

    private abstract static class DependingOnFiltering extends VisualizationModifier {

        private final FilterTypeChanger filterType;

        private boolean filteringByResource;

        DependingOnFiltering(Runnable onChange, PlanningState filterBy, FilterTypeChanger filterType) {
            super(onChange, filterBy);

            this.filterType = filterType;
            this.filteringByResource = filterType.isFilterByResources();
        }

        public boolean isFilteringByResource() {
            return filteringByResource;
        }

        @Override
        void checkDependencies() {
            if ( this.filteringByResource != filterType.isFilterByResources() ) {
                this.filteringByResource = filterType.isFilterByResources();
                filterTypeChanged();
            }
        }

        protected abstract void filterTypeChanged();

    }

    private static class FilterTypeChanger extends VisualizationModifier implements IListenerAdder {

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

        private ByDatesFilter(Runnable onChange, PlanningState filterBy, LocalDate startDate, LocalDate endDate) {
            super(onChange, filterBy);

            startDateValue = (isAppliedToOrder() || (startDate == null))
                    ? null
                    : startDate.toDateTimeAtStartOfDay().toLocalDate();

            endDateValue = (endDate == null) ? null : endDate.toDateTimeAtStartOfDay().toLocalDate();
        }

        @Override
        void setup(ResourcesLoadPanel panel) {
            if ( isAppliedToOrder() ) {
                return;
            }

            panel.setFirstOptionalFilter(buildTimeFilter());
        }

        private Hbox buildTimeFilter() {
            startBox.setValue(asDate(startDateValue));
            startBox.setWidth("100px");

            startBox.addEventListener(Events.ON_CHANGE, event -> {
                LocalDate newStart = toLocal(startBox.getValue());

                // TODO resolve deprecated
                if ( !ObjectUtils.equals(startDateValue, newStart) ) {

                    startDateValue = newStart;
                    FilterUtils.writeResourceLoadsStartDate(startDateValue);
                    notifyChange();
                }
            });

            endBox.setValue(asDate(endDateValue));
            endBox.setWidth("100px");

            endBox.addEventListener(Events.ON_CHANGE, event -> {
                LocalDate newEnd = toLocal(endBox.getValue());

                // TODO resolve deprecated
                if ( !ObjectUtils.equals(endBox, newEnd) ) {

                    endDateValue = newEnd;
                    FilterUtils.writeResourceLoadsEndDate(endDateValue);
                    notifyChange();
                }
            });

            Hbox hbox = new Hbox();
            hbox.appendChild(new Label(_("From") + ":"));
            hbox.appendChild(startBox);
            hbox.appendChild(new Label(_("To") + ":"));
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

    private static class ListenerTracker {

        private final List<Object> trackedListeners = new ArrayList<>();

        public void addListeners(ResourcesLoadPanel panel, Iterable<IListenerAdder> listeners) {
            for (IListenerAdder each : listeners) {
                Object listener = each.addAndReturnListener(panel);
                trackedListeners.add(listener);
            }
        }
    }

    private static class WorkersOrCriteriaBandbox extends DependingOnFiltering {

        private final BandboxMultipleSearch bandBox = new BandboxMultipleSearch();

        private List<Object> entitiesSelected = null;

        private final IResourcesSearcher resourcesSearcher;

        private Label label = new Label();

        private WorkersOrCriteriaBandbox(Runnable onChange,
                                         PlanningState filterBy,
                                         FilterTypeChanger filterType,
                                         IResourcesSearcher resourcesSearcher,
                                         List<FilterPair> selectedFilters) {

            super(onChange, filterBy, filterType);
            this.resourcesSearcher = resourcesSearcher;

            initBandbox();

            if ( (selectedFilters != null) && !selectedFilters.isEmpty() ) {
                for (FilterPair filterPair : selectedFilters) {
                    bandBox.addSelectedElement(filterPair);
                }
                entitiesSelected = getSelected();
            }
        }

        @Override
        void setup(ResourcesLoadPanel panel) {
            if ( isAppliedToOrder() ) {
                return;
            }
            panel.setSecondOptionalFilter(buildBandboxFilterer());
        }

        private void initBandbox() {
            bandBox.setId("workerBandboxMultipleSearch");
            bandBox.setWidthBandbox("185px");
            bandBox.setWidthListbox("450px");
            bandBox.setFinder(getFinderToUse());
            bandBox.afterCompose();

            bandBox.addEventListener(Events.ON_CHANGE, event -> {
                entitiesSelected = getSelected();
                FilterUtils.writeResourceLoadsParameters(bandBox.getSelectedElements());
                notifyChange();
            });
        }

        private Hbox buildBandboxFilterer() {
            Hbox hbox = new Hbox();
            hbox.appendChild(getLabel());
            hbox.appendChild(bandBox);
            hbox.setAlign("center");

            return hbox;
        }

        private Label getLabel() {
            updateLabelValue();
            return label;
        }

        private void updateLabelValue() {
            if ( isFilteringByResource() ) {
                label.setValue(_("Resources or criteria") + ":");
            } else {
                label.setValue(_("Criteria") + ":");
            }
        }

        private String getFinderToUse() {
            return isFilteringByResource()
                    ? "resourceMultipleFiltersFinderByResourceAndCriterion"
                    : "criterionMultipleFiltersFinder";
        }

        @Override
        protected void filterTypeChanged() {
            if ( isAppliedToOrder() ) {
                return;
            }

            entitiesSelected = null;
            bandBox.setFinder(getFinderToUse());
            updateLabelValue();
        }

        @Override
        void applyToParameters(ResourceLoadParameters parameters) {
            if ( !hasEntitiesSelected() ) {
                parameters.clearResourcesToShow();
                parameters.clearCriteriaToShow();
            } else if ( isFilteringByResource() ) {
                parameters.setResourcesToShow(calculateResourcesToShow());
            } else {
                parameters.setCriteriaToShow(as(Criterion.class, entitiesSelected));
            }
        }

        private List<Resource> calculateResourcesToShow() {
            List<Resource> resources = new ArrayList<>();
            List<Criterion> criteria = new ArrayList<>();

            for (Object each : entitiesSelected) {
                if ( each instanceof Resource ) {
                    resources.add((Resource) each);
                } else {
                    criteria.add((Criterion) each);
                }
            }

            if ( !criteria.isEmpty()) {
                resources.addAll(resourcesSearcher.searchBoth().byCriteria(criteria).execute());
            }

            return resources;
        }

        public boolean hasEntitiesSelected() {
            return entitiesSelected != null && !entitiesSelected.isEmpty();
        }

        private List<Object> getSelected() {
            List<Object> result = new ArrayList<>();

            @SuppressWarnings("unchecked")
            List<FilterPair> filterPairList = bandBox.getSelectedElements();

            for (FilterPair filterPair : filterPairList) {
                result.add(filterPair.getValue());
            }

            return result;
        }

    }

    private static class ByNamePaginator extends DependingOnFiltering implements IListenerAdder {

        private static final int ALL = -1;

        private final WorkersOrCriteriaBandbox bandbox;

        private int currentPosition;

        private List<? extends BaseEntity> allEntitiesShown = null;

        public ByNamePaginator(Runnable onChange,
                               PlanningState filterBy,
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
            IPaginationFilterChangedListener listener = newPosition -> {
                if ( currentPosition != newPosition ) {
                    currentPosition = newPosition;
                    notifyChange();
                }
            };

            panel.addPaginationFilterListener(listener);

            return listener;
        }

        @Override
        void checkDependencies() {
            super.checkDependencies();
            if ( bandbox.hasEntitiesSelected() ) {
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
            Paginator<? extends BaseEntity> paginator = generatedData.getPaginator();
            List<? extends BaseEntity> newAllEntities = paginator.getAll();

            if ( this.allEntitiesShown == null || !equivalent(this.allEntitiesShown, newAllEntities) ) {
                this.currentPosition = initialPage();
                this.allEntitiesShown = newAllEntities;

                updatePages(
                        panel.getPaginationFilterCombobox(),
                        pagesByName(this.allEntitiesShown, paginator.getPageSize()));
            }
        }

        private boolean equivalent(List<? extends BaseEntity> a, List<? extends BaseEntity> b) {
            if ( a == null || b == null ) {
                return false;
            }

            if ( a.size() != b.size() ) {
                return false;
            }

            for (int i = 0; i < a.size(); i++) {
                BaseEntity aElement = a.get(i);
                BaseEntity bElement = b.get(i);

                // TODO resolve deprecated
                if ( !ObjectUtils.equals(aElement.getId(), bElement.getId()) ) {
                    return false;
                }
            }
            return true;
        }

        private void updatePages(Combobox filterByNameCombo, List<Comboitem> pages) {
            if ( filterByNameCombo == null ) {
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

            if ( currentPosition >= 0 && currentPosition < pages.size() ) {
                filterByNameCombo.setSelectedItem(pages.get(currentPosition));
            } else if ( currentPosition == ALL ) {
                filterByNameCombo.setSelectedItem(lastItem);
            } else {
                filterByNameCombo.setSelectedIndex(0);
            }
        }

        private List<Comboitem> pagesByName(List<?> list, int pageSize) {
            if ( list.isEmpty() ) {
                return new ArrayList<>();
            }

            return list.get(0) instanceof Resource
                    ? pagesByName(as(Resource.class, list), pageSize, resource -> resource.getName())
                    : pagesByName(
                    as(Criterion.class, list),
                    pageSize,
                    criterion -> criterion.getType().getName() + ": " + criterion.getName());
        }

        interface INameExtractor<T> {
            String getNameOf(T value);
        }

        private <T> List<Comboitem> pagesByName(List<T> elements, int pageSize, INameExtractor<T> nameExtractor) {
            List<Comboitem> result = new ArrayList<>();

            for (int startPos = 0; startPos < elements.size(); startPos += pageSize) {
                int endPos = Math.min(startPos + pageSize - 1, elements.size() - 1);
                String first = nameExtractor.getNameOf(elements.get(startPos));
                String end = nameExtractor.getNameOf(elements.get(endPos));
                Comboitem item = buildPageCombo(startPos, first, end);
                result.add(item);
            }

            return result;
        }

        private Comboitem buildPageCombo(int startPosition, String first, String end) {
            Comboitem result = new Comboitem();
            result.setLabel(first.substring(0, 1) + " - " + end.substring(0, 1));
            result.setDescription(first + " - " + end);
            result.setValue(startPosition);

            return result;
        }

    }

    private final class Reloader {

        private ResourcesLoadPanel resourcesLoadPanel = null;

        private ListenerTracker listeners = new ListenerTracker();

        private TimeTracker timeTracker;

        private IZoomLevelChangedListener zoomLevelListener;

        private List<VisualizationModifier> visualizationModifiers = null;

        private List<IListenerAdder> listenersToAdd = null;

        public Reloader() {
        }

        private List<VisualizationModifier> getVisualizationModifiers() {
            if ( visualizationModifiers != null ) {
                return visualizationModifiers;
            }
            visualizationModifiers = buildVisualizationModifiers();

            return visualizationModifiers;
        }

        private List<VisualizationModifier> buildVisualizationModifiers() {
            List<VisualizationModifier> result = new ArrayList<>();
            FilterTypeChanger filterTypeChanger = new FilterTypeChanger(onChange, filterBy);
            result.add(filterTypeChanger);

            // Only by dates and bandbox filter on global resources load
            if ( filterBy == null ) {
                LocalDate startDate = FilterUtils.readResourceLoadsStartDate();
                LocalDate endDate = FilterUtils.readResourceLoadsEndDate();

                User user = resourceLoadModel.getUser();

                // Calculate filter based on user preferences
                if ( user != null ) {
                    if ( startDate == null && !FilterUtils.hasResourceLoadsStartDateChanged() ) {
                        if ( user.getResourcesLoadFilterPeriodSince() != null ) {
                            startDate = new LocalDate().minusMonths(user.getResourcesLoadFilterPeriodSince());
                        } else {
                            // Default filter start
                            startDate = new LocalDate().minusDays(1);
                        }
                    }
                    if ( (endDate == null) &&
                            !FilterUtils.hasResourceLoadsEndDateChanged() &&
                            (user.getResourcesLoadFilterPeriodTo() != null) ) {

                        endDate = new LocalDate().plusMonths(user.getResourcesLoadFilterPeriodTo());
                    }
                }

                result.add(new ByDatesFilter(onChange, filterBy, startDate, endDate));

                List<FilterPair> filterPairs = FilterUtils.readResourceLoadsBandbox();

                if (user != null && (filterPairs == null || filterPairs.isEmpty()) &&
                        user.getResourcesLoadFilterCriterion() != null) {

                    filterPairs = new ArrayList<>();
                    filterPairs.add(new FilterPair(
                            ResourceAllocationFilterEnum.Criterion,
                            user.getResourcesLoadFilterCriterion().getFinderPattern(),
                            user.getResourcesLoadFilterCriterion()));
                }

                WorkersOrCriteriaBandbox bandbox =
                        new WorkersOrCriteriaBandbox(onChange, filterBy, filterTypeChanger, resourcesSearcher, filterPairs);

                result.add(bandbox);
                result.add(new ByNamePaginator(onChange, filterBy, filterTypeChanger, bandbox));
            }

            result.add(new LoadChart(onChange, filterBy));

            return result;
        }

        private List<IListenerAdder> getListenersToAdd() {
            if ( listenersToAdd != null ) {
                return listenersToAdd;
            }

            List<IListenerAdder> result = new ArrayList<>();
            for (VisualizationModifier each : getVisualizationModifiers()) {
                if ( each instanceof IListenerAdder ) {
                    result.add((IListenerAdder) each);
                }
            }
            result.add(new GoToScheduleListener());

            listenersToAdd = result;

            return listenersToAdd;
        }

        public void resetToInitialState() {
            timeTracker = null;
            resourcesLoadPanel = null;
            listeners = new ListenerTracker();
            visualizationModifiers = null;
            listenersToAdd = null;
        }

        public IOnTransaction<Void> reload() {
            return () -> {
                reloadInTransaction();
                return null;
            };
        }

        private void reloadInTransaction() {
            for (VisualizationModifier each : getVisualizationModifiers()) {
                each.checkDependencies();
            }

            ResourceLoadParameters parameters = new ResourceLoadParameters(filterBy);

            for (VisualizationModifier each : getVisualizationModifiers()) {
                each.applyToParameters(parameters);
            }

            ResourceLoadDisplayData dataToShow = resourceLoadModel.calculateDataToDisplay(parameters);

            timeTracker = buildTimeTracker(dataToShow);

            if ( resourcesLoadPanel == null ) {
                resourcesLoadPanel = buildPanel(dataToShow);
                listeners.addListeners(resourcesLoadPanel, getListenersToAdd());
                parent.getChildren().clear();
                parent.appendChild(resourcesLoadPanel);

                for (VisualizationModifier each : getVisualizationModifiers()) {
                    each.setup(resourcesLoadPanel);
                }

            } else {
                resourcesLoadPanel.init(dataToShow.getLoadTimeLines(), timeTracker);
                listeners.addListeners(resourcesLoadPanel, getListenersToAdd());
            }

            resourcesLoadPanel.afterCompose();
            addCommands(resourcesLoadPanel);

            for (VisualizationModifier each : getVisualizationModifiers()) {
                each.updateUI(resourcesLoadPanel, dataToShow);
            }
        }

        private void addCommands(ResourcesLoadPanel resourcesLoadPanel) {
            resourcesLoadPanel.add(commands.toArray(new IToolbarCommand[commands.size()]));
        }

        private TimeTracker buildTimeTracker(ResourceLoadDisplayData dataToShow) {
            ZoomLevel zoomLevel = getZoomLevel(dataToShow);

            TimeTracker result = new TimeTracker(
                    dataToShow.getViewInterval(),
                    zoomLevel,
                    SeveralModifiers.create(),
                    SeveralModifiers.create(createBankHolidaysMarker()),
                    parent);

            setupZoomLevelListener(result);

            return result;
        }

        private BankHolidaysMarker createBankHolidaysMarker() {
            return BankHolidaysMarker.create(configurationDAO.getConfiguration().getDefaultCalendar());
        }

        private ZoomLevel getZoomLevel(ResourceLoadDisplayData dataToShow) {
            if ( filterBy != null ) {
                Order order = filterBy.getOrder();
                ZoomLevel sessionZoom = FilterUtils.readZoomLevel(order);
                if ( sessionZoom != null ) {
                    return sessionZoom;
                }
            }

            ZoomLevel sessionZoom = FilterUtils.readZoomLevelResourcesLoad();
            if ( sessionZoom != null ) {
                return sessionZoom;
            }

            return dataToShow.getInitialZoomLevel();
        }

        private void setupZoomLevelListener(TimeTracker timeTracker) {
            zoomLevelListener = getSessionZoomLevelListener();
            timeTracker.addZoomListener(zoomLevelListener);
        }

        private IZoomLevelChangedListener getSessionZoomLevelListener() {
            return detailLevel -> {
                if ( filterBy != null ) {
                    Order order = filterBy.getOrder();
                    FilterUtils.writeZoomLevel(order, detailLevel);
                } else {
                    FilterUtils.writeZoomLevelResourcesLoad(detailLevel);
                }
            };
        }

        private ResourcesLoadPanel buildPanel(ResourceLoadDisplayData dataToShow) {
            return new ResourcesLoadPanel(
                    dataToShow.getLoadTimeLines(),
                    timeTracker,
                    parent,
                    resourceLoadModel.isExpandResourceLoadViewCharts(),
                    PaginationType.EXTERNAL_PAGINATION);
        }
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
            return new ResourceLoadChartData(
                    generatedData.getDayAssignmentsConsidered(), resources, interval.getStart(), interval.getFinish());
        }

        private void initializeResources() {
            resources = generatedData.getResourcesConsidered();
        }
    }

    private class GoToScheduleListener implements IListenerAdder {

        @Override
        public Object addAndReturnListener(ResourcesLoadPanel panel) {
            ISeeScheduledOfListener listener = taskLine -> onSeeScheduleOf(taskLine);

            panel.addSeeScheduledOfListener(listener);

            return listener;
        }

        private void onSeeScheduleOf(LoadTimeLine taskLine) {

            TaskElement task = (TaskElement) taskLine.getRole().getEntity();
            Order order = resourceLoadModel.getOrderByTask(task);

            if ( resourceLoadModel.userCanRead(order, SecurityUtils.getSessionUserLoginName()) ) {
                if ( order.isScheduled() ) {
                    planningControllerEntryPoints.goToTaskResourceAllocation(order, task);
                } else {
                    Messagebox.show(
                            _("The project has no scheduled elements"), _("Information"),
                            Messagebox.OK, Messagebox.INFORMATION);
                }
            } else {
                Messagebox.show(
                        _("You don't have read access to this project"), _("Information"),
                        Messagebox.OK, Messagebox.INFORMATION);

            }
        }

    }

    public void setPlanningControllerEntryPoints(IOrderPlanningGate planningControllerEntryPoints) {
        this.planningControllerEntryPoints = planningControllerEntryPoints;
    }

    public IOrderPlanningGate getPlanningControllerEntryPoints() {
        return this.planningControllerEntryPoints;
    }

}