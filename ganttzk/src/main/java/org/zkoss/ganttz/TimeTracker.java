package org.zkoss.ganttz;

import java.util.Collection;

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

    private final Interval initialInterval;

    public TimeTracker(Interval interval) {
        this.initialInterval = interval;

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
                    .getFirstLevelDetails(initialInterval);
        }
        return detailsFirstLevelCached;
    }

    public Collection<TimeTrackerState.DetailItem> getDetailsSecondLevel() {
        if (detailsSecondLevelCached == null) {
            detailsSecondLevelCached = getTimeTrackerState()
                    .getSecondLevelDetails(initialInterval);
        }
        return detailsSecondLevelCached;
    }

    private Interval getRealInterval() {
        return getTimeTrackerState().getRealIntervalFor(initialInterval);
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
    }

    public IDatesMapper getMapper() {
        if (datesMapper == null) {
            datesMapper = new DatesMapperOnInterval(getHorizontalSize(),
                    getRealInterval());
        }
        return datesMapper;
    }

    public void goToNextDetailLevel() {
        clearDetailLevelDependantData();
        detailLevel = detailLevel.next();
        fireZoomChanged();
    }

    public void goToPreviousDetailLvel() {
        clearDetailLevelDependantData();
        datesMapper = null;
        detailLevel = detailLevel.previous();
        fireZoomChanged();
    }

}
