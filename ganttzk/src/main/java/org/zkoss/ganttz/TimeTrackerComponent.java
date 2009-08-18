package org.zkoss.ganttz;

import java.util.Collection;

import org.zkoss.ganttz.util.Interval;
import org.zkoss.ganttz.util.WeakReferencedListeners;
import org.zkoss.ganttz.util.WeakReferencedListeners.IListenerNotification;
import org.zkoss.ganttz.util.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.util.zoom.TimeTrackerState;
import org.zkoss.ganttz.util.zoom.ZoomLevel;
import org.zkoss.ganttz.util.zoom.TimeTrackerState.DetailItem;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.Command;
import org.zkoss.zk.au.ComponentCommand;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.HtmlMacroComponent;

/**
 * @author Javier Moran Rua <jmoran@igalia.com>
 */

public class TimeTrackerComponent extends HtmlMacroComponent {

    private static Interval getTestInterval() {
        return new Interval(TimeTrackerState.year(2009), TimeTrackerState
                .year(2012));
    }

    private WeakReferencedListeners<IZoomLevelChangedListener> zoomListeners = WeakReferencedListeners
            .create();

    private IDatesMapper datesMapper = null;

    private Collection<DetailItem> detailsFirstLevelCached = null;

    private Collection<DetailItem> detailsSecondLevelCached = null;

    private ZoomLevel detailLevel;

    public TimeTrackerComponent() {
        this.detailLevel = ZoomLevel.DETAIL_ONE;
    }

    public ZoomLevel getZoomLevel() {
        return this.detailLevel;
    }

    public void addZoomListener(IZoomLevelChangedListener listener) {
        zoomListeners.addListener(listener);
    }

    public void scrollHorizontalPercentage(int displacement) {
        response("scroll_horizontal", new AuInvoke(this.getParent(),
                "scroll_horizontal", "" + displacement));
    }

    private void fireZoomChanged(final ZoomLevel detailLevel) {
        zoomListeners
                .fireEvent(new IListenerNotification<IZoomLevelChangedListener>() {

                    @Override
                    public void doNotify(IZoomLevelChangedListener listener) {
                        listener.zoomLevelChanged(detailLevel);
                    }
                });
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

    private Command _onincreasecmd = new ComponentCommand("onIncrease", 0) {

        protected void process(AuRequest request) {
            String[] requestData = request.getData();
            int pixelsOffset = Integer.parseInt(requestData[0]);
            onIncrease(pixelsOffset);
        }

    };

    private Command _ondecreasecmd = new ComponentCommand("onDecrease", 0) {

        protected void process(AuRequest request) {
            String[] requestData = request.getData();
            int pixelsOffset = Integer.parseInt(requestData[0]);
            onDecrease(pixelsOffset);
        }

    };

    private Command[] commands = { _onincreasecmd, _ondecreasecmd };

    public Command getCommand(String cmdId) {
        for (Command command : commands) {
            if (command.getId().equals(cmdId)) {
                return command;
            }
        }
        throw new RuntimeException("not found command for " + cmdId);
    }

    public void onIncrease(int offset) {
        changeDetailLevel(getDetailLevel().next(), offset
                * getDetailLevel().getTimeTrackerState().daysPerPixel());
    }

    public void onDecrease(int offset) {
        changeDetailLevel(getDetailLevel().previous(), offset
                * getDetailLevel().getTimeTrackerState().daysPerPixel());
    }

    private void changeDetailLevel(ZoomLevel d, double days) {
        this.detailLevel = d;
        datesMapper = null;
        detailsFirstLevelCached = null;
        detailsSecondLevelCached = null;
        recreate();
        fireZoomChanged(d);

        scrollHorizontalPercentage((int) Math.floor(days
                / d.getTimeTrackerState().daysPerPixel()));
    }

    private ZoomLevel getDetailLevel() {
        return detailLevel;
    }

    public IDatesMapper getMapper() {
        if (datesMapper == null) {
            datesMapper = new DatesMapperOnInterval(getHorizontalSize(),
                    getRealInterval());
        }
        return datesMapper;
    }

}
