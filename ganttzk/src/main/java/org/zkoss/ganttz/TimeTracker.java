package org.zkoss.ganttz;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Date;

import org.joda.time.LocalDate;
import org.zkoss.ganttz.data.Task;
import org.zkoss.ganttz.util.Interval;
import org.zkoss.ganttz.util.WeakReferencedListeners;
import org.zkoss.ganttz.util.WeakReferencedListeners.IListenerNotification;
import org.zkoss.ganttz.util.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.util.zoom.TimeTrackerState;
import org.zkoss.ganttz.util.zoom.ZoomLevel;
import org.zkoss.ganttz.util.zoom.TimeTrackerState.DetailItem;

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

    public Collection<TimeTrackerState.DetailItem> getDetailsFirstLevel() {
        if (detailsFirstLevelCached == null) {
            detailsFirstLevelCached = getTimeTrackerState()
                    .getFirstLevelDetails(interval);
        }
        return detailsFirstLevelCached;
    }

    public Collection<TimeTrackerState.DetailItem> getDetailsSecondLevel() {
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
        for (TimeTrackerState.DetailItem item : detailsFirstLevel) {
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

    public void forceNotifyZoom() {
        fireZoomChanged();
    }

}
