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

package org.libreplan.web.limitingresources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.libreplan.business.planner.limiting.entities.LimitingResourceQueueElement;
import org.libreplan.business.resources.entities.LimitingResourceQueue;
import org.libreplan.web.common.Util;
import org.zkoss.ganttz.DependencyList;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.TimeTracker.IDetailItemFilter;
import org.zkoss.ganttz.timetracker.TimeTrackerComponent;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.ComponentsFinder;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.AuService;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Separator;
import org.zkoss.zul.SimpleListModel;

public class LimitingResourcesPanel extends HtmlMacroComponent {

    public interface IToolbarCommand {
        public void doAction();

        public String getLabel();

        public String getImage();
    }

    private LimitingResourcesController limitingResourcesController;

    private TimeTracker timeTracker;

    private TimeTrackerComponent timeTrackerComponent;

    private TimeTrackerComponent timeTrackerHeader;

    private LimitingResourcesLeftPane leftPane;

    private QueueListComponent queueListComponent;

    private MutableTreeModel<LimitingResourceQueue> treeModel;

    private Listbox listZoomLevels;

    private Button paginationDownButton;

    private Button paginationUpButton;

    private Listbox horizontalPagination;

    private LimitingDependencyList dependencyList = new LimitingDependencyList(
            this);

    private PaginatorFilter paginatorFilter;

    private Component insertionPointLeftPanel;
    private Component insertionPointRightPanel;
    private Component insertionPointTimetracker;

    private LocalDate previousStart;
    private Interval previousInterval;

    public void paginationDown() {
        horizontalPagination.setSelectedIndex(horizontalPagination
                .getSelectedIndex() - 1);
        goToSelectedHorizontalPage();
    }

    public void paginationUp() {
        horizontalPagination.setSelectedIndex(Math.max(1, horizontalPagination
                .getSelectedIndex() + 1));
        goToSelectedHorizontalPage();
    }

    /**
     * Returns the closest upper {@link LimitingResourcesPanel} instance going
     * all the way up from comp
     *
     * @param comp
     * @return
     */
    public static LimitingResourcesPanel getLimitingResourcesPanel(
            Component comp) {
        if (comp == null) {
            return null;
        }
        if (comp instanceof LimitingResourcesPanel) {
            return (LimitingResourcesPanel) comp;
        }
        return getLimitingResourcesPanel(comp.getParent());
    }

    public LimitingResourcesPanel(
            LimitingResourcesController limitingResourcesController,
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

        queueListComponent = new QueueListComponent(this, timeTracker,
                treeModel);

        leftPane = new LimitingResourcesLeftPane(treeModel);

        setAuService(new AuService(){
            public boolean service(AuRequest request, boolean everError){
                String command = request.getCommand();
                int zoomindex;
                int scrollLeft;

                if (command.equals("onZoomLevelChange")){
                    zoomindex=  (Integer) retrieveData(request, "zoomindex");
                    scrollLeft = (Integer) retrieveData(request, "scrollLeft");

                    setZoomLevel((ZoomLevel)((Listbox)getFellow("listZoomLevels"))
                            .getModel().getElementAt(zoomindex),
                            scrollLeft);
                    return true;
                }
                return false;
            }

            private Object retrieveData(AuRequest request, String key){
                Object value = request.getData().get(key);
                if ( value == null)
                    throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA,
                            new Object[] { key, this });

                return value;
            }
        });
    }

    public void setZoomLevel(ZoomLevel zoomLevel, int scrollLeft) {
        savePreviousData();
        getTimeTrackerComponent().updateDayScroll();
        getTimeTrackerComponent().setZoomLevel(zoomLevel, scrollLeft);
    }

    public void appendQueueElementToQueue(LimitingResourceQueueElement element) {
        queueListComponent.appendQueueElement(element);
        dependencyList.addDependenciesFor(element);
    }

    public void removeQueueElementFrom(LimitingResourceQueue queue, LimitingResourceQueueElement element) {
        queueListComponent.removeQueueElementFrom(queue, element);
        dependencyList.removeDependenciesFor(element);
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

    public ListModel getZoomLevels() {
        ZoomLevel[] selectableZoomlevels = { ZoomLevel.DETAIL_THREE,
                ZoomLevel.DETAIL_FOUR, ZoomLevel.DETAIL_FIVE,
                ZoomLevel.DETAIL_SIX };
        return new SimpleListModel(selectableZoomlevels);
    }

    public void setZoomLevel(final ZoomLevel zoomLevel) {
        savePreviousData();
        getTimeTrackerComponent().updateDayScroll();
        timeTracker.setZoomLevel(zoomLevel);
    }

    public void zoomIncrease() {
        savePreviousData();
        getTimeTrackerComponent().updateDayScroll();
        timeTracker.zoomIncrease();
    }

    public void zoomDecrease() {
        savePreviousData();
        getTimeTrackerComponent().updateDayScroll();
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
            public void onEvent(Event event) {
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

    private TimeTrackerComponent timeTrackerForLimitingResourcesPanel(
            TimeTracker timeTracker) {
        return new TimeTrackerComponent(timeTracker) {
            @Override
            protected void scrollHorizontalPercentage(int daysDisplacement) {
                response("", new AuInvoke(queueListComponent,
                        "scroll_horizontal", daysDisplacement + ""));
                moveCurrentPositionScroll();
            }

            @Override
            protected void moveCurrentPositionScroll() {
                // get the previous data.
                LocalDate previousStart = getPreviousStart();

                // get the current data
                int diffDays = getTimeTrackerComponent().getDiffDays(
                        previousStart);
                double pixelPerDay = getTimeTrackerComponent().getPixelPerDay();

                response("move_scroll", new AuInvoke(queueListComponent,
                        "move_scroll", "" + diffDays, "" + pixelPerDay));
            }

            @Override
            protected void updateCurrentDayScroll() {
                double previousPixelPerDay = getTimeTracker().getMapper()
                        .getPixelsPerDay().doubleValue();

                response("update_day_scroll", new AuInvoke(queueListComponent,
                        "update_day_scroll", "" + previousPixelPerDay));

            }
        };
    }

    @Override
    public void afterCompose() {

        super.afterCompose();

        initializeBindings();
        initializeTimetracker();

        listZoomLevels
                .setSelectedIndex(timeTracker.getDetailLevel().ordinal() - 2);

        // Insert leftPane component with limitingresources list
        insertionPointLeftPanel.appendChild(leftPane);
        leftPane.afterCompose();

        // Initialize queues
        insertionPointRightPanel.appendChild(queueListComponent);
        queueListComponent.afterCompose();

        // Initialize dependencies
        rebuildDependencies();
        dependencyList.afterCompose();

        initializePagination();
    }

    /**
     * Apparently it's necessary to append {@link DependencyList} to
     * insertionPointRightPanel again every time the list of dependencies is
     * regenerated. Otherwise tasks overflow if they don't fit in current page
     *
     */
    private void rebuildDependencies() {
        dependencyList.clear();
        for (LimitingResourceQueueElement each : getLimitingResourceQueueElements()) {
            dependencyList.addDependenciesFor(each);
        }
        insertionPointRightPanel.appendChild(dependencyList);
    }

    private Set<LimitingResourceQueueElement> getLimitingResourceQueueElements() {
        return queueListComponent.getLimitingResourceElementToQueueTaskMap()
                .keySet();
    }

    private void initializeTimetracker() {
        timeTracker.addZoomListener(new IZoomLevelChangedListener() {
            @Override
            public void zoomLevelChanged(ZoomLevel newDetailLevel) {
                reloadTimetracker();
                reloadComponent();
            }

            private void reloadTimetracker() {
                timeTracker.resetMapper();
                paginatorFilter.setInterval(timeTracker.getRealInterval());
                timeTracker.setFilter(paginatorFilter);
                paginatorFilter.populateHorizontalListbox();
                paginatorFilter.goToHorizontalPage(0);
                adjustZoomPositionScroll();
            }

        });
        timeTrackerHeader = createTimeTrackerHeader();
        timeTrackerComponent = createTimeTrackerComponent();
        insertionPointTimetracker.appendChild(timeTrackerHeader);
        insertionPointRightPanel.appendChild(timeTrackerComponent);
        timeTrackerHeader.afterCompose();
        timeTrackerComponent.afterCompose();
    }

    @SuppressWarnings("serial")
    private TimeTrackerComponent createTimeTrackerHeader() {
        return new TimeTrackerComponent(timeTracker) {

            @Override
            protected void scrollHorizontalPercentage(int pixelsDisplacement) {

            }

            @Override
            protected void moveCurrentPositionScroll() {
                // TODO Auto-generated method stub
            }

            @Override
            protected void updateCurrentDayScroll() {
                // TODO Auto-generated method stub
            }
        };
    }

    private void adjustZoomPositionScroll() {
        getTimeTrackerComponent().movePositionScroll();
    }

    @SuppressWarnings("serial")
    private TimeTrackerComponent createTimeTrackerComponent() {
        return new TimeTrackerComponent(timeTracker) {
            @Override
            protected void scrollHorizontalPercentage(int pixelsDisplacement) {
                response("", new AuInvoke(queueListComponent,
                        "adjustScrollHorizontalPosition", pixelsDisplacement
                                + ""));
            }

            @Override
            protected void moveCurrentPositionScroll() {
                // TODO Auto-generated method stub
            }

            @Override
            protected void updateCurrentDayScroll() {
                // TODO Auto-generated method stub
            }
        };
    }

    private void initializePagination() {
        paginatorFilter = new PaginatorFilter();
        paginationUpButton.setDisabled(paginatorFilter.isLastPage());
        paginatorFilter.setInterval(timeTracker.getRealInterval());
        timeTracker.setFilter(paginatorFilter);
        paginatorFilter.populateHorizontalListbox();
    }

    private void initializeBindings() {

        // Zoom and pagination
        listZoomLevels = (Listbox) getFellow("listZoomLevels");
        horizontalPagination = (Listbox) getFellow("horizontalPagination");
        paginationUpButton = (Button) getFellow("paginationUpButton");
        paginationDownButton = (Button) getFellow("paginationDownButton");

        insertionPointLeftPanel = getFellow("insertionPointLeftPanel");
        insertionPointRightPanel = getFellow("insertionPointRightPanel");
        insertionPointTimetracker = getFellow("insertionPointTimetracker");
    }

    public Map<LimitingResourceQueueElement, QueueTask> getQueueTaskMap() {
        return queueListComponent.getLimitingResourceElementToQueueTaskMap();
    }

    public void clearComponents() {
        getFellow("insertionPointLeftPanel").getChildren().clear();
        getFellow("insertionPointRightPanel").getChildren().clear();
        getFellow("insertionPointTimetracker").getChildren().clear();
    }

    public TimeTrackerComponent getTimeTrackerComponent() {
        return timeTrackerComponent;
    }

    public TimeTracker getTimeTracker() {
        return timeTrackerComponent.getTimeTracker();
    }

    public void unschedule(QueueTask task) {
        LimitingResourceQueueElement queueElement = task.getLimitingResourceQueueElement();
        LimitingResourceQueue queue = queueElement.getLimitingResourceQueue();

        limitingResourcesController.unschedule(task);
        removeQueueTask(task);
        dependencyList.removeDependenciesFor(queueElement);
        queueListComponent.removeQueueElementFrom(queue, queueElement);
    }

    private void removeQueueTask(QueueTask task) {
        task.detach();
    }

    public void moveQueueTask(QueueTask queueTask) {
        if (limitingResourcesController.moveTask(queueTask.getLimitingResourceQueueElement())) {
            removeQueueTask(queueTask);
        }
    }

    public void editResourceAllocation(QueueTask queueTask) {
        limitingResourcesController.editResourceAllocation(queueTask
                .getLimitingResourceQueueElement());
    }

    public void removeDependenciesFor(LimitingResourceQueueElement element) {
        dependencyList.removeDependenciesFor(element);
    }

    public void addDependenciesFor(LimitingResourceQueueElement element) {
        dependencyList.addDependenciesFor(element);
    }

    public void refreshQueues(Collection<LimitingResourceQueue> queues) {
        for (LimitingResourceQueue each: queues) {
            refreshQueue(each);
        }
    }

    public void refreshQueue(LimitingResourceQueue queue) {
        dependencyList.removeDependenciesFor(queue);
        queueListComponent.refreshQueue(queue);
    }

    public void goToSelectedHorizontalPage() {
        paginatorFilter.goToHorizontalPage(horizontalPagination
                .getSelectedIndex());
        reloadComponent();
    }

    public void reloadComponent() {
        refreshTimetracker();
        refreshQueueComponents();
        rebuildDependencies();
    }

    private void refreshTimetracker() {
        timeTrackerHeader.recreate();
        timeTrackerComponent.recreate();
    }

    private void refreshQueueComponents() {
        queueListComponent.invalidate();
        queueListComponent.afterCompose();
        queueListComponent.refreshQueues();
        rebuildDependencies();
    }

    private class PaginatorFilter implements IDetailItemFilter {

        private DateTime intervalStart;
        private DateTime intervalEnd;

        private DateTime paginatorStart;
        private DateTime paginatorEnd;

        @Override
        public Interval getCurrentPaginationInterval() {
            return new Interval(paginatorStart.toDate(), paginatorEnd.toDate());
        }

        private Period intervalIncrease() {
            switch (timeTracker.getDetailLevel()) {
            case DETAIL_ONE:
                return Period.years(5);
            case DETAIL_TWO:
                return Period.years(5);
            case DETAIL_THREE:
                return Period.years(2);
            case DETAIL_FOUR:
                return Period.months(12);
            case DETAIL_FIVE:
                return Period.weeks(12);
            case DETAIL_SIX:
                return Period.weeks(12);
            }
            // Default month
            return Period.years(2);
        }

        public void setInterval(Interval realInterval) {
            intervalStart = realInterval.getStart().toDateTimeAtStartOfDay();
            intervalEnd = realInterval.getFinish().toDateTimeAtStartOfDay();
            paginatorStart = intervalStart;
            paginatorEnd = intervalStart.plus(intervalIncrease());
            if ((paginatorEnd.plus(intervalIncrease()).isAfter(intervalEnd))) {
                paginatorEnd = intervalEnd;
            }
            updatePaginationButtons();
            dependencyList.recreateDependencyComponents();
        }

        @Override
        public void resetInterval() {
            setInterval(timeTracker.getRealInterval());
        }

        @Override
        public Collection<DetailItem> selectsFirstLevel(
                Collection<DetailItem> firstLevelDetails) {
            ArrayList<DetailItem> result = new ArrayList<DetailItem>();
            for (DetailItem each : firstLevelDetails) {
                if ((each.getStartDate() == null)
                        || !(each.getStartDate().isBefore(paginatorStart))
                        && (each.getStartDate().isBefore(paginatorEnd))) {
                    result.add(each);
                }
            }
            return result;
        }

        @Override
        public Collection<DetailItem> selectsSecondLevel(
                Collection<DetailItem> secondLevelDetails) {
            ArrayList<DetailItem> result = new ArrayList<DetailItem>();
            for (DetailItem each : secondLevelDetails) {
                if ((each.getStartDate() == null)
                        || !(each.getStartDate().isBefore(paginatorStart))
                        && (each.getStartDate().isBefore(paginatorEnd))) {
                    result.add(each);
                }
            }
            return result;
        }

        public void populateHorizontalListbox() {
            horizontalPagination.getItems().clear();
            DateTime intervalStart = timeTracker.getRealInterval().getStart()
                    .toDateTimeAtStartOfDay();
            if (intervalStart != null) {
                DateTime itemStart = intervalStart;
                DateTime itemEnd = intervalStart.plus(intervalIncrease());
                while (intervalEnd.isAfter(itemStart)) {
                    if (intervalEnd.isBefore(itemEnd)
                            || !intervalEnd.isAfter(itemEnd
                                    .plus(intervalIncrease()))) {
                        itemEnd = intervalEnd;
                    }
                    Listitem item = new Listitem(Util.formatDate(itemStart)
                            + " - " + Util.formatDate(itemEnd.minusDays(1)));
                    horizontalPagination.appendChild(item);
                    itemStart = itemEnd;
                    itemEnd = itemEnd.plus(intervalIncrease());
                }
            }
            horizontalPagination.setSelectedIndex(0);

            // Disable pagination if there's only one page
            int size = horizontalPagination.getItems().size();
            horizontalPagination.setDisabled(size == 1);
        }

        public void goToHorizontalPage(int interval) {
            paginatorStart = intervalStart;
            paginatorStart = timeTracker.getDetailsFirstLevel().iterator()
                    .next().getStartDate();

            for (int i = 0; i < interval; i++) {
                paginatorStart = paginatorStart.plus(intervalIncrease());
            }
            paginatorEnd = paginatorStart.plus(intervalIncrease());
            if ((paginatorEnd.plus(intervalIncrease()).isAfter(intervalEnd))) {
                paginatorEnd = paginatorEnd.plus(intervalIncrease());
            }
            timeTracker.resetMapper();
            updatePaginationButtons();
        }

        private void updatePaginationButtons() {
            paginationDownButton.setDisabled(isFirstPage());
            paginationUpButton.setDisabled(isLastPage());
        }

        public boolean isFirstPage() {
            return (horizontalPagination.getSelectedIndex() <= 0)
                    || horizontalPagination.isDisabled();
        }

        private boolean isLastPage() {
            return (horizontalPagination.getItemCount() == (horizontalPagination
                    .getSelectedIndex() + 1))
                    || horizontalPagination.isDisabled();
        }
    }

    private void savePreviousData() {
        TimeTracker timeTracker = getTimeTrackerComponent().getTimeTracker();
        this.previousStart = timeTracker.getRealInterval().getStart();
        this.previousInterval = timeTracker.getMapper().getInterval();
    }

    public LocalDate getPreviousStart() {
        return previousStart;
    }

    public Interval getPreviousInterval() {
        return previousInterval;
    }

    public String getWidgetClass() {
        return "limitingresources.LimitingResourcesPanel";
    }
}
