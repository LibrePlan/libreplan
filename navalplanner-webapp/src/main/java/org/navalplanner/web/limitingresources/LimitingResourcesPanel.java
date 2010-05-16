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

package org.navalplanner.web.limitingresources;

import static org.navalplanner.web.I18nHelper._;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.navalplanner.business.planner.entities.LimitingResourceQueueDependency;
import org.navalplanner.business.planner.entities.LimitingResourceQueueElement;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.TimeTrackerComponent;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.ComponentsFinder;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.ganttz.util.OnZKDesktopRegistry;
import org.zkoss.ganttz.util.script.IScriptsRegister;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Separator;
import org.zkoss.zul.SimpleListModel;

public class LimitingResourcesPanel extends HtmlMacroComponent {

    public interface IToolbarCommand {
        public void doAction();

        public String getLabel();

        public String getImage();
    }

    private LimitingResourcesController limitingResourcesController;

    private TimeTrackerComponent timeTrackerComponent;

    private LimitingResourcesLeftPane leftPane;

    private QueueListComponent queueListComponent;

    private MutableTreeModel<LimitingResourceQueue> treeModel;

    private TimeTracker timeTracker;

    private Listbox listZoomLevels;

    @Autowired
    IResourceDAO resourcesDAO;

    private static final String filterResources = _("Filter by resources");
    private static final String filterCriterions = _("Filter by criterions");
    private boolean filterbyResources = true;

    private LimitingDependencyList dependencyList = new LimitingDependencyList(this);

    /**
     * Returns the closest upper {@link LimitingResourcesPanel} instance going
     * all the way up from comp
     *
     * @param comp
     * @return
     */
    public static LimitingResourcesPanel getLimitingResourcesPanel(Component comp) {
        if (comp == null) {
            return null;
        }
        if (comp instanceof LimitingResourcesPanel) {
            return (LimitingResourcesPanel) comp;
        }
        return getLimitingResourcesPanel(comp.getParent());
    }

    public LimitingResourcesPanel(LimitingResourcesController limitingResourcesController,
            TimeTracker timeTracker) {
        init(limitingResourcesController, timeTracker);
    }

    public void init(LimitingResourcesController limitingResourcesController,
            TimeTracker timeTracker) {
        this.limitingResourcesController = limitingResourcesController;
        this.timeTracker = timeTracker;
        this.setVariable("limitingResourcesController",
                limitingResourcesController, true);

        treeModel = createModelForTree();
        timeTrackerComponent = timeTrackerForResourcesLoadPanel(timeTracker);
        queueListComponent = new QueueListComponent(timeTracker,
                treeModel);

        leftPane = new LimitingResourcesLeftPane(treeModel,
                queueListComponent);
        registerNeededScripts();
    }

    public void appendQueueElementToQueue(LimitingResourceQueueElement element) {
        queueListComponent.appendQueueElement(element);
    }

    private MutableTreeModel<LimitingResourceQueue> createModelForTree() {
        MutableTreeModel<LimitingResourceQueue> result = MutableTreeModel
                .create(LimitingResourceQueue.class);
        for (LimitingResourceQueue LimitingResourceQueue : getLimitingResourceQueues()) {
            result.addToRoot(LimitingResourceQueue);
        }
        return result;
    }

    private List<LimitingResourceQueue> getLimitingResourceQueues() {
        return limitingResourcesController.getLimitingResourceQueues();
    }

    public ListModel getFilters() {
        String[] filters = new String[] { filterResources, filterCriterions };
        return new SimpleListModel(filters);
    }

    public void setFilter(String filterby) {
        if (filterby.equals(filterResources)) {
            this.filterbyResources = true;
        } else {
            this.filterbyResources = false;
        }
    }

    public boolean getFilter() {
        return filterbyResources;
    }

    public ListModel getZoomLevels() {
        return new SimpleListModel(ZoomLevel.values());
    }

    public void setZoomLevel(final ZoomLevel zoomLevel) {
        timeTracker.setZoomLevel(zoomLevel);
    }

    public void zoomIncrease() {
        timeTracker.zoomIncrease();
    }

    public void zoomDecrease() {
        timeTracker.zoomDecrease();
    }

    public void add(final IToolbarCommand... commands) {
        Component toolbar = getToolbar();
        Separator separator = getSeparator();
        for (IToolbarCommand c : commands) {
            toolbar.insertBefore(asButton(c), separator);
        }
    }

    private Button asButton(final IToolbarCommand c) {
        Button result = new Button();
        result.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                c.doAction();
            }
        });
        if (!StringUtils.isEmpty(c.getImage())) {
            result.setImage(c.getImage());
            result.setTooltiptext(c.getLabel());
        } else {
            result.setLabel(c.getLabel());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Separator getSeparator() {
        List<Component> children = getToolbar().getChildren();
        Separator separator = ComponentsFinder.findComponentsOfType(
                Separator.class, children).get(0);
        return separator;
    }

    private Component getToolbar() {
        Component toolbar = getFellow("toolbar");
        return toolbar;
    }

    private void registerNeededScripts() {
        // getScriptsRegister().register(
        // ScriptsRequiredByLimitingResourcesPanel.class);
    }

    private IScriptsRegister getScriptsRegister() {
        return OnZKDesktopRegistry.getLocatorFor(IScriptsRegister.class)
                .retrieve();
    }

    private TimeTrackerComponent timeTrackerForResourcesLoadPanel(
            TimeTracker timeTracker) {
        return new TimeTrackerComponent(timeTracker) {
            @Override
            protected void scrollHorizontalPercentage(int pixelsDisplacement) {
                response("", new AuInvoke(queueListComponent,
                        "adjustScrollHorizontalPosition", pixelsDisplacement
                                + ""));
            }
        };
    }

    @Override
    public void afterCompose() {

        super.afterCompose();

        // Insert leftPane component with limitingresources list
        getFellow("insertionPointLeftPanel").appendChild(leftPane);
        leftPane.afterCompose();

        getFellow("insertionPointRightPanel").appendChild(timeTrackerComponent);
        getFellow("insertionPointRightPanel").appendChild(queueListComponent);
        queueListComponent.afterCompose();

        dependencyList = generateDependencyComponentsList();
        if (dependencyList != null) {
            dependencyList.afterCompose();
            getFellow("insertionPointRightPanel").appendChild(dependencyList);
        }

        // Insert timetracker headers
        TimeTrackerComponent timeTrackerHeader = createTimeTrackerHeader();
        getFellow("insertionPointTimetracker").appendChild(timeTrackerHeader);

        timeTrackerHeader.afterCompose();
        timeTrackerComponent.afterCompose();
        listZoomLevels = (Listbox) getFellow("listZoomLevels");
        listZoomLevels.setSelectedIndex(timeTracker.getDetailLevel().ordinal());
    }

    private LimitingDependencyList generateDependencyComponentsList() {
        final Map<LimitingResourceQueueElement, QueueTask> queueElementsMap = queueListComponent
                .getLimitingResourceElementToQueueTaskMap();

        for (LimitingResourceQueueElement queueElement : queueElementsMap
                .keySet()) {
            for (LimitingResourceQueueDependency dependency : queueElement
                    .getDependenciesAsOrigin()) {
                addDependencyComponent(dependencyList, queueElementsMap, dependency);
            }
        }
        return dependencyList;
    }

    public void addDependencyComponent(LimitingResourceQueueDependency dependency) {
        final Map<LimitingResourceQueueElement, QueueTask> queueElementsMap = queueListComponent
                .getLimitingResourceElementToQueueTaskMap();
        addDependencyComponent(dependencyList, queueElementsMap, dependency);
    }

    private void addDependencyComponent(
            LimitingDependencyList dependencyList,
            Map<LimitingResourceQueueElement, QueueTask> queueElementsMap,
            LimitingResourceQueueDependency dependency) {

        LimitingDependencyComponent component = createDependencyComponent(queueElementsMap, dependency);
        if (component != null) {
            dependencyList.addDependencyComponent(component);
        }
    }

    private LimitingDependencyComponent createDependencyComponent(
            Map<LimitingResourceQueueElement, QueueTask> queueElementsMap,
            LimitingResourceQueueDependency dependency) {

        final QueueTask origin = queueElementsMap.get(dependency
                .getHasAsOrigin());
        final QueueTask destination = queueElementsMap.get(dependency
                .getHasAsDestiny());
        return (origin != null && destination != null) ? new LimitingDependencyComponent(
                origin, destination)
                : null;
    }

    public void clearComponents() {
        getFellow("insertionPointLeftPanel").getChildren().clear();
        getFellow("insertionPointRightPanel").getChildren().clear();
        getFellow("insertionPointTimetracker").getChildren().clear();
    }

    public TimeTrackerComponent getTimeTrackerComponent() {
        return timeTrackerComponent;
    }

    private TimeTrackerComponent createTimeTrackerHeader() {
        return new TimeTrackerComponent(timeTracker) {

            @Override
            protected void scrollHorizontalPercentage(int pixelsDisplacement) {
            }
        };
    }

    public void unschedule(QueueTask task) {
        limitingResourcesController.unschedule(task);
        removeQueueTask(task);
    }

    private void removeQueueTask(QueueTask task) {
        task.detach();
        dependencyList.removeDependencyComponents(task);
    }

}
