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

package org.zkoss.ganttz.timetracker;

import static org.zkoss.ganttz.i18n.I18nHelper._;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.DatesMapperOnInterval;
import org.zkoss.ganttz.IDatesMapper;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.timetracker.zoom.IDetailItemModificator;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.SeveralModificators;
import org.zkoss.ganttz.timetracker.zoom.TimeTrackerState;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.ganttz.util.LongOperationFeedback;
import org.zkoss.ganttz.util.LongOperationFeedback.ILongOperation;
import org.zkoss.ganttz.util.WeakReferencedListeners;
import org.zkoss.ganttz.util.WeakReferencedListeners.IListenerNotification;
import org.zkoss.zk.ui.Component;

public class TimeTracker {

    public interface IDetailItemFilter {

        public Collection<DetailItem> selectsFirstLevel(
                Collection<DetailItem> firstLevelDetails);

        public Collection<DetailItem> selectsSecondLevel(
                Collection<DetailItem> secondLevelDetails);

        public Interval getCurrentPaginationInterval();

        public void resetInterval();

    }

    private ZoomLevel detailLevel = ZoomLevel.DETAIL_ONE;

    private WeakReferencedListeners<IZoomLevelChangedListener> zoomListeners = WeakReferencedListeners
            .create();

    private IDatesMapper datesMapper = null;

    private Collection<DetailItem> detailsFirstLevelCached = null;

    private Collection<DetailItem> detailsSecondLevelCached = null;

    private Interval interval;

    private final IDetailItemModificator firstLevelModificator;

    private final IDetailItemModificator secondLevelModificator;

    private final Component componentOnWhichGiveFeedback;

    private boolean registeredFirstTask = false;

    private IDetailItemFilter filter = null;

    public IDetailItemFilter getFilter() {
        return filter;
    }

    public TimeTracker(Interval interval, ZoomLevel zoomLevel, Component parent) {
        this(interval, zoomLevel, SeveralModificators.empty(),
                SeveralModificators.empty(), parent);
    }

    public TimeTracker(Interval interval, Component componentOnWhichGiveFeedback) {
        this(interval, SeveralModificators.empty(),
                SeveralModificators.empty(), componentOnWhichGiveFeedback);
    }

    public TimeTracker(Interval interval,
            IDetailItemModificator firstLevelModificator,
            IDetailItemModificator secondLevelModificator,
            Component componentOnWhichGiveFeedback) {
        Validate.notNull(interval);
        Validate.notNull(firstLevelModificator);
        Validate.notNull(secondLevelModificator);
        Validate.notNull(componentOnWhichGiveFeedback);
        this.interval = interval;
        this.firstLevelModificator = firstLevelModificator;
        this.secondLevelModificator = secondLevelModificator;
        this.componentOnWhichGiveFeedback = componentOnWhichGiveFeedback;
    }

    public TimeTracker(Interval interval, ZoomLevel zoomLevel,
            IDetailItemModificator firstLevelModificator,
            IDetailItemModificator secondLevelModificator,
            Component componentOnWhichGiveFeedback) {
        this(interval, firstLevelModificator, secondLevelModificator,
                componentOnWhichGiveFeedback);
        detailLevel = zoomLevel;
    }

    public void setFilter(IDetailItemFilter filter) {
        this.filter = filter;
        datesMapper = null;
    }

    public ZoomLevel getDetailLevel() {
        return detailLevel;
    }

    public void addZoomListener(IZoomLevelChangedListener listener) {
        zoomListeners.addListener(listener);
    }

    public Collection<DetailItem> getDetailsFirstLevel() {
        if (detailsFirstLevelCached == null) {
            detailsFirstLevelCached = getTimeTrackerState()
                    .getFirstLevelDetails(interval);
        }
        return filterFirstLevel(detailsFirstLevelCached);
    }

    private Collection<DetailItem> filterFirstLevel(
            Collection<DetailItem> firstLevelDetails) {
        if (filter == null) {
            return firstLevelDetails;
        }
        return filter.selectsFirstLevel(firstLevelDetails);
    }

    public Collection<DetailItem> getDetailsSecondLevel() {
        if (detailsSecondLevelCached == null) {
            detailsSecondLevelCached = getTimeTrackerState()
                    .getSecondLevelDetails(interval);
        }
        return filterSecondLevel(detailsSecondLevelCached);
    }

    private Collection<DetailItem> filterSecondLevel(
            Collection<DetailItem> secondLevelDetails) {
        if (filter == null) {
            return secondLevelDetails;
        }
        return filter.selectsSecondLevel(secondLevelDetails);
    }

    private Interval realIntervalCached;

    public Interval getRealInterval() {
        if (realIntervalCached == null) {
            realIntervalCached = getTimeTrackerState().getRealIntervalFor(
                    interval);
        }
        return realIntervalCached;
    }

    public TimeTrackerState getTimeTrackerState() {
        return detailLevel.getTimeTrackerState(firstLevelModificator,
                secondLevelModificator);
    }

    private void fireZoomChanged() {
        zoomListeners
                .fireEvent(new IListenerNotification<IZoomLevelChangedListener>() {
                    @Override
                    public void doNotify(IZoomLevelChangedListener listener) {
                        listener.zoomLevelChanged(detailLevel);
                    }
                });
    }

    public int getHorizontalSize() {
        int horizontalSize = 0;
        for (DetailItem detailItem : getDetailsSecondLevel()) {
            horizontalSize += detailItem.getSize();
        }
        return horizontalSize;
    }

    private void clearDetailLevelDependantData() {
        datesMapper = null;
        detailsFirstLevelCached = detailsSecondLevelCached = null;
        realIntervalCached = null;
    }

    public void resetMapper() {
        datesMapper = null;
        realIntervalCached = null;
    }

    public IDatesMapper getMapper() {
        if (datesMapper == null) {
            if (filter == null) {
                datesMapper = new DatesMapperOnInterval(getHorizontalSize(),
                        getRealInterval());
            } else {
                datesMapper = new DatesMapperOnInterval(getHorizontalSize(),
                        filter.getCurrentPaginationInterval());
            }
        }
        return datesMapper;
    }

    public void zoomIncrease() {
        detailLevel = detailLevel.next();
        invalidatingChangeHappenedWithFeedback();
    }

    private void invalidatingChangeHappenedWithFeedback() {
        LongOperationFeedback.execute(componentOnWhichGiveFeedback,
                new ILongOperation() {

                    @Override
                    public void doAction() throws Exception {
                        invalidatingChangeHappened();
                    }

                    @Override
                    public String getName() {
                        return _("changing zoom");
                    }
                });
    }

    public void setZoomLevel(ZoomLevel zoomLevel) {
        detailLevel = zoomLevel;
        invalidatingChangeHappenedWithFeedback();
    }

    private void invalidatingChangeHappened() {
        clearDetailLevelDependantData();
        fireZoomChanged();
    }

    public void zoomDecrease() {
        detailLevel = detailLevel.previous();
        invalidatingChangeHappenedWithFeedback();
    }

    public void trackPosition(final Task task) {
        task
                .addFundamentalPropertiesChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        updateIntervalIfNeeded(task);
                    }
                });
        updateIntervalIfNeeded(task);
    }

    private void updateIntervalIfNeeded(Task task) {
        if (registeredFirstTask == false) {
            registeredFirstTask = true;
            interval = new Interval(startMinusTwoWeeks(task),
                    endPlusOneMonth(task));
            invalidatingChangeHappened();
        } else {
            LocalDate newStart = interval.getStart();
            LocalDate newFinish = interval.getFinish();

            boolean changed = false;
            if (interval.getStart().compareTo(startMinusTwoWeeks(task)) > 0) {
                newStart = startMinusTwoWeeks(task);
                changed = true;
            }

            if (interval.getFinish()
                    .compareTo(endPlusOneMonth(task)) < 0) {
                newFinish = endPlusOneMonth(task);
                changed = true;
            }

            if (changed) {
                interval = new Interval(newStart, newFinish);
                invalidatingChangeHappened();
            }
        }
    }

    private Date max(Date date1, Date date2) {
        if (date1 == null) {
            return date2;
        }
        if (date2 == null) {
            return date1;
        }
        return date1.compareTo(date2) > 0 ? date1 : date2;
    }

    private Date min(Date date1, Date date2) {
        if (date1 == null) {
            return date2;
        }
        if (date2 == null) {
            return date1;
        }
        return date1.compareTo(date2) <= 0 ? date1 : date2;
    }

    private LocalDate endPlusOneMonth(Task task) {
        Date taskEnd = max(task.getEndDate().toDayRoundedDate(),
                task.getDeadline());
        return new LocalDate(taskEnd).plusMonths(1);
    }

    private LocalDate startMinusTwoWeeks(Task task) {
        // the deadline could be before the start
        Date start = min(task.getBeginDate().toDayRoundedDate(),
                task.getDeadline());
        // the last consolidated value could be before the start
        if (task.getConsolidatedline() != null) {
            start = min(start, task.getConsolidatedline().toDayRoundedDate());
        }
        return new LocalDate(start).minusWeeks(2);
    }

}
