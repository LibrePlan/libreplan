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
import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.common.components.bandboxsearch.BandboxMultipleSearch;
import org.navalplanner.web.common.components.finders.FilterPair;
import org.navalplanner.web.planner.order.BankHolidaysMarker;
import org.navalplanner.web.planner.order.IOrderPlanningGate;
import org.navalplanner.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.resourceload.IFilterChangedListener;
import org.zkoss.ganttz.resourceload.ISeeScheduledOfListener;
import org.zkoss.ganttz.resourceload.ResourcesLoadPanel;
import org.zkoss.ganttz.resourceload.ResourcesLoadPanel.IToolbarCommand;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.SeveralModificators;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;

/**
 * Controller for global resourceload view
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ResourceLoadController implements Composer {

    @Autowired
    private IResourceLoadModel resourceLoadModel;

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

    public ResourceLoadController() {
    }

    public void add(IToolbarCommand... commands) {
        Validate.noNullElements(commands);
        this.commands.addAll(Arrays.asList(commands));
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        this.parent = comp;
        reload();
    }

    public void reload() {
        // by default show the task by resources
        boolean filterByResources = true;
        timeTracker = null;
        resourcesLoadPanel = null;
        reload(filterByResources);
    }

    private void reload(boolean filterByResources) {
        try {
            this.filterHasChanged = (filterByResources != currentFilterByResources);
            this.currentFilterByResources = filterByResources;

            if (filterBy == null) {
                if(resourcesLoadPanel == null) {
                    resetAdditionalFilters();
                }
                resourceLoadModel.initGlobalView(filterByResources);
            } else {
                if(resourcesLoadPanel == null) {
                    resetAdditionalFilters();
                }
                resourceLoadModel.initGlobalView(filterBy, filterByResources);
            }
            timeTracker = buildTimeTracker();
            buildResourcesLoadPanel();

            this.parent.getChildren().clear();
            this.parent.appendChild(resourcesLoadPanel);

            resourcesLoadPanel.afterCompose();
            addCommands(resourcesLoadPanel);
        } catch (IllegalArgumentException e) {
            try {
                Messagebox
                        .show(
                                _("Some lines have not allocation periods.\nBelow it shows the load all company resources"),
                                _("Error"), Messagebox.OK, Messagebox.ERROR);
            } catch (InterruptedException o) {
                throw new RuntimeException(e);
            }
        }
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
        reload(filterByResources);
    }

    private void addCommands(ResourcesLoadPanel resourcesLoadPanel) {
        resourcesLoadPanel.add(commands.toArray(new IToolbarCommand[0]));
    }

    private TimeTracker buildTimeTracker() {
        ZoomLevel zoomLevel = (timeTracker == null) ? resourceLoadModel
                .calculateInitialZoomLevel() : timeTracker.getDetailLevel();
        return new TimeTracker(resourceLoadModel.getViewInterval(), zoomLevel,
                SeveralModificators.create(), SeveralModificators
                        .create(new BankHolidaysMarker()), parent);
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
                resourcesLoadPanel.setNameFilterDisabled(
                        !bandBox.getSelectedElements().isEmpty());
            }
            resourcesLoadPanel.init(resourceLoadModel.getLoadTimeLines(),
                    timeTracker);
        } else {
            resourcesLoadPanel = new ResourcesLoadPanel(resourceLoadModel
                    .getLoadTimeLines(), timeTracker, parent);
            if(filterBy == null) {
                addWorkersBandbox();
                addTimeFilter();
            }
            addListeners();
        }
    }

    private void addWorkersBandbox() {
        bandBox = new BandboxMultipleSearch();
        bandBox.setId("workerBandboxMultipleSearch");
        bandBox.setWidthBandbox("285px");
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
        Label label1 = new Label(_("Show load between"));
        Label label2 = new Label(_("and"));
        final Datebox initDate = new Datebox();
        initDate.setValue(resourceLoadModel.getInitDateFilter());
        initDate.addEventListener(Events.ON_CHANGE, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                resourceLoadModel.setInitDateFilter(initDate.getValue());
                reload(currentFilterByResources);
            }
        });
        final Datebox endDate = new Datebox();
        endDate.setValue(resourceLoadModel.getEndDateFilter());
        endDate.addEventListener(Events.ON_CHANGE, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                resourceLoadModel.setEndDateFilter(endDate.getValue());
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

    private void resetAdditionalFilters() {
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
                    Messagebox.show(_("The order has no scheduled elements"),
                            _("Information"), Messagebox.OK,
                            Messagebox.INFORMATION);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            try {
                Messagebox
                        .show(_("You don't have read access to this order"),
                                _("Information"), Messagebox.OK,
                                Messagebox.INFORMATION);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
}

}
