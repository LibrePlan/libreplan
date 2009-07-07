/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zkoss.ganttz;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.zkoss.ganttz.util.Interval;
import org.zkoss.ganttz.util.zoom.TimeTrackerState;
import org.zkoss.ganttz.util.zoom.ZoomLevel;
import org.zkoss.ganttz.util.zoom.ZoomLevelChangedListener;
import org.zkoss.ganttz.util.zoom.TimeTrackerState.DetailItem;
import org.zkoss.zk.ui.HtmlMacroComponent;

/**
 * @author Javier Moran Rua <jmoran@igalia.com>
 */

public class TimeTracker extends HtmlMacroComponent {

    private static Interval getTestInterval() {
        return new Interval(TimeTrackerState.year(2009), TimeTrackerState
                .year(2012));
    }

//    private AbstractComponent fakeRow;

    private List<WeakReference<ZoomLevelChangedListener>> zoomListeners = new LinkedList<WeakReference<ZoomLevelChangedListener>>();

    private DatesMapper datesMapper = null;

    private Collection<DetailItem> detailsFirstLevelCached = null;

    private Collection<DetailItem> detailsSecondLevelCached = null;

    private ZoomLevel detailLevel;

    public TimeTracker() {
        this.detailLevel = ZoomLevel.DETAIL_ONE;
    }

    public void addZoomListener(ZoomLevelChangedListener listener) {
        zoomListeners
                .add(new WeakReference<ZoomLevelChangedListener>(listener));
    }

    private void fireZoomChanged(ZoomLevel detailLevel) {
        ListIterator<WeakReference<ZoomLevelChangedListener>> listIterator = zoomListeners
                .listIterator();
        while (listIterator.hasNext()) {
            ZoomLevelChangedListener listener = listIterator.next().get();
            if (listener == null) {
                listIterator.remove();
            } else {
                listener.zoomLevelChanged(detailLevel);
            }
        }
    }

    public Collection<TimeTrackerState.DetailItem> getDetailsFirstLevel() {
        if (detailsFirstLevelCached == null) {
            detailsFirstLevelCached = getTimeTrackerState()
                    .getFirstLevelDetails(getTestInterval());
        }
        return detailsFirstLevelCached;
    }

    public Interval getRealInterval() {
        return getTimeTrackerState().getRealIntervalFor(getTestInterval());
    }

    public Collection<TimeTrackerState.DetailItem> getDetailsSecondLevel()
            throws Exception {
        if (detailsSecondLevelCached == null) {
            detailsSecondLevelCached = getTimeTrackerState()
                    .getSecondLevelDetails(getTestInterval());
        }
        return detailsSecondLevelCached;
    }

    private TimeTrackerState getTimeTrackerState() {
        return getDetailLevel().getTimeTrackerState();
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

    public void onIncrease() {
        changeDetailLevel(getDetailLevel().next());
    }

    private void changeDetailLevel(ZoomLevel d) {
        this.detailLevel = d;
        datesMapper = null;
        detailsFirstLevelCached = null;
        detailsSecondLevelCached = null;
        recreate();
        fireZoomChanged(d);
    }

    public void onDecrease() {
        changeDetailLevel(getDetailLevel().previous());
    }

    private ZoomLevel getDetailLevel() {
        return detailLevel;
    }

    public DatesMapper getMapper() {
        if (datesMapper == null) {
            datesMapper = new DatesMapperOnInterval(getHorizontalSize(),
                    getRealInterval());
        }
        return datesMapper;
    }

}
