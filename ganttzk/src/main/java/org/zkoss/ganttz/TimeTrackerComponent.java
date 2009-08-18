package org.zkoss.ganttz;

import java.util.Collection;

import org.zkoss.ganttz.util.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.util.zoom.TimeTrackerState;
import org.zkoss.ganttz.util.zoom.ZoomLevel;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.au.Command;
import org.zkoss.zk.au.ComponentCommand;
import org.zkoss.zk.au.out.AuInvoke;
import org.zkoss.zk.ui.HtmlMacroComponent;

/**
 * @author Javier Moran Rua <jmoran@igalia.com>
 */
public class TimeTrackerComponent extends HtmlMacroComponent {

    private final TimeTracker timeTracker;
    private IZoomLevelChangedListener zoomListener;

    public TimeTrackerComponent(TimeTracker timeTracker) {
        this.timeTracker = timeTracker;
        zoomListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                recreate();
            }
        };
        this.timeTracker.addZoomListener(zoomListener);
    }

    public ZoomLevel getZoomLevel() {
        return this.getTimeTracker().getDetailLevel();
    }

    public void scrollHorizontalPercentage(int displacement) {
        response("scroll_horizontal", new AuInvoke(this.getParent(),
                "scroll_horizontal", "" + displacement));
    }

    public Collection<TimeTrackerState.DetailItem> getDetailsFirstLevel() {
        return timeTracker.getDetailsFirstLevel();
    }

    public Collection<TimeTrackerState.DetailItem> getDetailsSecondLevel() {
        return timeTracker.getDetailsSecondLevel();
    }

    private TimeTrackerState getTimeTrackerState() {
        return getTimeTracker().getDetailLevel().getTimeTrackerState();
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
        double daysOffset = getDaysFor(offset);
        getTimeTracker().goToNextDetailLevel();
        changeDetailLevel(daysOffset);
    }

    public void onDecrease(int offset) {
        double daysOffset = getDaysFor(offset);
        getTimeTracker().goToPreviousDetailLvel();
        changeDetailLevel(daysOffset);
    }

    public TimeTracker getTimeTracker() {
        return timeTracker;
    }

    public int getHorizontalSize() {
        return timeTracker.getHorizontalSize();
    }

    private double getDaysFor(int offset) {
        return offset * getTimeTrackerState().daysPerPixel();
    }

    private void changeDetailLevel(double days) {
        scrollHorizontalPercentage((int) Math.floor(days
                / getTimeTrackerState().daysPerPixel()));
    }

}
