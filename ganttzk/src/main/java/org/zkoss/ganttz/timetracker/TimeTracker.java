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

package org.zkoss.ganttz.timetracker;

import static org.zkoss.ganttz.i18n.I18nHelper._;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang3.Validate;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.DatesMapperOnInterval;
import org.zkoss.ganttz.IDatesMapper;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.timetracker.zoom.IDetailItemModifier;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.SeveralModifiers;
import org.zkoss.ganttz.timetracker.zoom.TimeTrackerState;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.ganttz.util.LongOperationFeedback;
import org.zkoss.ganttz.util.WeakReferencedListeners;
import org.zkoss.ganttz.util.LongOperationFeedback.ILongOperation;
import org.zkoss.zk.ui.Component;

public class TimeTracker {

    public interface IDetailItemFilter {

        Collection<DetailItem> selectsFirstLevel(Collection<DetailItem> firstLevelDetails);

        Collection<DetailItem> selectsSecondLevel(Collection<DetailItem> secondLevelDetails);

        Interval getCurrentPaginationInterval();

        void resetInterval();

    }

    private ZoomLevel detailLevel = ZoomLevel.DETAIL_ONE;

    private WeakReferencedListeners<IZoomLevelChangedListener> zoomListeners = WeakReferencedListeners.create();

    private IDatesMapper datesMapper = null;

    private Collection<DetailItem> detailsFirstLevelCached = null;

    private Collection<DetailItem> detailsSecondLevelCached = null;

    private Interval interval;

    private final IDetailItemModifier firstLevelModifier;

    private final IDetailItemModifier secondLevelModifier;

    private final Component componentOnWhichGiveFeedback;

    private boolean registeredFirstTask = false;

    private IDetailItemFilter filter = null;

    private Interval realIntervalCached;

    public TimeTracker(Interval interval, ZoomLevel zoomLevel, Component parent) {
        this(interval, zoomLevel, SeveralModifiers.empty(), SeveralModifiers.empty(), parent);
    }

    public TimeTracker(Interval interval, Component componentOnWhichGiveFeedback) {
        this(interval, SeveralModifiers.empty(), SeveralModifiers.empty(), componentOnWhichGiveFeedback);
    }

    public TimeTracker(
            Interval interval,
            IDetailItemModifier firstLevelModifier,
            IDetailItemModifier secondLevelModifier,
            Component componentOnWhichGiveFeedback) {

        Validate.notNull(interval);
        Validate.notNull(firstLevelModifier);
        Validate.notNull(secondLevelModifier);
        Validate.notNull(componentOnWhichGiveFeedback);

        this.interval = interval;
        this.firstLevelModifier = firstLevelModifier;
        this.secondLevelModifier = secondLevelModifier;
        this.componentOnWhichGiveFeedback = componentOnWhichGiveFeedback;
    }

    public TimeTracker(
            Interval interval,
            ZoomLevel zoomLevel,
            IDetailItemModifier firstLevelModifier,
            IDetailItemModifier secondLevelModifier,
            Component componentOnWhichGiveFeedback) {

        this(interval, firstLevelModifier, secondLevelModifier, componentOnWhichGiveFeedback);
        detailLevel = zoomLevel;
    }

    public IDetailItemFilter getFilter() {
        return filter;
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
        if ( detailsFirstLevelCached == null ) {
            detailsFirstLevelCached = getTimeTrackerState().getFirstLevelDetails(interval);
        }

        return filterFirstLevel(detailsFirstLevelCached);
    }

    private Collection<DetailItem> filterFirstLevel(Collection<DetailItem> firstLevelDetails) {
        return filter == null ? firstLevelDetails : filter.selectsFirstLevel(firstLevelDetails);
    }

    public Collection<DetailItem> getDetailsSecondLevel() {
        if ( detailsSecondLevelCached == null ) {
            detailsSecondLevelCached = getTimeTrackerState().getSecondLevelDetails(interval);
        }

        return filterSecondLevel(detailsSecondLevelCached);
    }

    private Collection<DetailItem> filterSecondLevel(Collection<DetailItem> secondLevelDetails) {
        return filter == null ? secondLevelDetails : filter.selectsSecondLevel(secondLevelDetails);
    }

    public Interval getRealInterval() {
        if ( realIntervalCached == null ) {
            realIntervalCached = getTimeTrackerState().getRealIntervalFor(interval);
        }

        return realIntervalCached;
    }

    public TimeTrackerState getTimeTrackerState() {
        return detailLevel.getTimeTrackerState(firstLevelModifier, secondLevelModifier);
    }

    private void fireZoomChanged() {
        /* Do not replace it with lambda */
        zoomListeners.fireEvent(new WeakReferencedListeners.IListenerNotification<IZoomLevelChangedListener>() {
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
        if ( datesMapper == null ) {
            if ( filter == null ) {
                datesMapper = new DatesMapperOnInterval(getHorizontalSize(), getRealInterval());
            } else {
                datesMapper = new DatesMapperOnInterval(getHorizontalSize(), filter.getCurrentPaginationInterval());
            }
        }

        return datesMapper;
    }

    public void zoomIncrease() {
        detailLevel = detailLevel.next();
        invalidatingChangeHappenedWithFeedback();
    }

    private void invalidatingChangeHappenedWithFeedback() {
        LongOperationFeedback.execute(componentOnWhichGiveFeedback, new ILongOperation() {
            @Override
            public void doAction() {
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
        task.addFundamentalPropertiesChangeListener(evt -> updateIntervalIfNeeded(task));
        updateIntervalIfNeeded(task);
    }

    private void updateIntervalIfNeeded(Task task) {
        if ( !registeredFirstTask ) {
            registeredFirstTask = true;
            interval = new Interval(startMinusTwoWeeks(task), endPlusOneMonth(task));
            invalidatingChangeHappened();
        } else {
            LocalDate newStart = interval.getStart();
            LocalDate newFinish = interval.getFinish();

            boolean changed = false;

            if ( interval.getStart().compareTo(startMinusTwoWeeks(task) ) > 0) {
                newStart = startMinusTwoWeeks(task);
                changed = true;
            }

            if ( interval.getFinish().compareTo(endPlusOneMonth(task)) < 0 ) {
                newFinish = endPlusOneMonth(task);
                changed = true;
            }

            if ( changed ) {
                interval = new Interval(newStart, newFinish);
                invalidatingChangeHappened();
            }
        }
    }

    private Date max(Date date1, Date date2) {
        if ( date1 == null ) {
            return date2;
        }
        if ( date2 == null ) {
            return date1;
        }

        return date1.compareTo(date2) > 0 ? date1 : date2;
    }

    private Date min(Date date1, Date date2) {
        if ( date1 == null ) {
            return date2;
        }

        if ( date2 == null ) {
            return date1;
        }

        return date1.compareTo(date2) <= 0 ? date1 : date2;
    }

    private LocalDate endPlusOneMonth(Task task) {
        return new LocalDate(max(task.getEndDate().toDayRoundedDate(), task.getDeadline())).plusMonths(1);
    }

    private LocalDate startMinusTwoWeeks(Task task) {
        // The deadline could be before the start
        Date start = min(task.getBeginDate().toDayRoundedDate(), task.getDeadline());

        // The last consolidated value could be before the start
        if ( task.getConsolidatedline() != null ) {
            start = min(start, task.getConsolidatedline().toDayRoundedDate());
        }

        return new LocalDate(start).minusWeeks(2);
    }

}
