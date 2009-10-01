/*
 * This file is part of ###PROJECT_NAME###
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

package org.zkoss.ganttz.timetracker;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Date;

import org.joda.time.LocalDate;
import org.zkoss.ganttz.DatesMapperOnInterval;
import org.zkoss.ganttz.IDatesMapper;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.TimeTrackerState;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.ganttz.util.WeakReferencedListeners;
import org.zkoss.ganttz.util.WeakReferencedListeners.IListenerNotification;

public class TimeTracker {

    private ZoomLevel detailLevel = ZoomLevel.DETAIL_ONE;

    private WeakReferencedListeners<IZoomLevelChangedListener> zoomListeners = WeakReferencedListeners
            .create();

    private IDatesMapper datesMapper = null;

    private Collection<DetailItem> detailsFirstLevelCached = null;

    private Collection<DetailItem> detailsSecondLevelCached = null;

    private Interval interval;

    public TimeTracker(Interval interval) {
        this.interval = interval;

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
        return detailsFirstLevelCached;
    }

    public Collection<DetailItem> getDetailsSecondLevel() {
        if (detailsSecondLevelCached == null) {
            detailsSecondLevelCached = getTimeTrackerState()
                    .getSecondLevelDetails(interval);
        }
        return detailsSecondLevelCached;
    }

    private Interval realIntervalCached;

    private Interval getRealInterval() {
        if (realIntervalCached == null) {
            realIntervalCached = getTimeTrackerState().getRealIntervalFor(
                    interval);
        }
        return realIntervalCached;
    }

    private TimeTrackerState getTimeTrackerState() {
        return detailLevel.getTimeTrackerState();
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
        // Code to improve. Not optimus. We have to calculate the details twice
        int result = 0;
        Collection<DetailItem> detailsFirstLevel = getDetailsFirstLevel();
        for (DetailItem item : detailsFirstLevel) {
            result += item.getSize();
        }
        return result;
    }

    private void clearDetailLevelDependantData() {
        datesMapper = null;
        detailsFirstLevelCached = detailsSecondLevelCached = null;
        realIntervalCached = null;
    }

    public IDatesMapper getMapper() {
        if (datesMapper == null) {
            datesMapper = new DatesMapperOnInterval(getHorizontalSize(),
                    getRealInterval());
        }
        return datesMapper;
    }

    public void goToNextDetailLevel() {
        detailLevel = detailLevel.next();
        invalidatingChangeHappened();
    }

    private void invalidatingChangeHappened() {
        clearDetailLevelDependantData();
        fireZoomChanged();
    }

    public void goToPreviousDetailLvel() {
        detailLevel = detailLevel.previous();
        invalidatingChangeHappened();
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
        Date newStart = interval.getStart();
        Date newFinish = interval.getFinish();
        boolean changed = false;
        if (getRealInterval().getStart().compareTo(startMinusOneYear(task)) > 0) {
            newStart = startMinusOneYear(task);
            changed = true;
        }
        if (getRealInterval().getFinish()
                .compareTo(endPlusOneYear(task)) < 0) {
            newFinish = endPlusOneYear(task);
            changed = true;
        }
        if (changed) {
            interval = new Interval(newStart, newFinish);
            invalidatingChangeHappened();
        }
    }

    private Date endPlusOneYear(Task task) {
        return new LocalDate(task.getEndDate()).plusYears(1).toDateMidnight().toDate();
    }

    private Date startMinusOneYear(Task task) {
        return new LocalDate(task.getBeginDate()).minusYears(1)
                .toDateMidnight().toDate();
    }

}
