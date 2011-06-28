/*
 * This file is part of NavalPlan
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

import java.util.Collection;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.IDatesMapper;
import org.zkoss.ganttz.timetracker.zoom.DetailItem;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.TimeTrackerState;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlMacroComponent;

/**
 * @author Javier Moran Rua <jmoran@igalia.com>
 */
public abstract class TimeTrackerComponent extends HtmlMacroComponent {

    private final TimeTracker timeTracker;
    private IZoomLevelChangedListener zoomListener;
    private final String secondLevelZul;
    private String timeTrackerElementId;
    private int scrollLeft;

    public TimeTrackerComponent(TimeTracker timeTracker) {
        this(timeTracker,
                "~./ganttz/zul/timetracker/timetrackersecondlevel.zul",
                "timetracker");
    }

    protected TimeTrackerComponent(TimeTracker timeTracker,
            String secondLevelZul, String timetrackerId) {
        this.secondLevelZul = secondLevelZul;
        this.timeTracker = timeTracker;
        zoomListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                if (isInPage()) {
                    recreate();
                    changeDetailLevel(getDaysFor(scrollLeft));
                }
            }
        };
        this.timeTracker.addZoomListener(zoomListener);
        timeTrackerElementId = timetrackerId;
    }

    private boolean isInPage() {
        return getPage() != null;
    }

    public int getHorizontalSizePixels() {
        return timeTracker.getHorizontalSize();
    }

    public String getTimeTrackerId() {
        return timeTrackerElementId;
    }

    /*
     *  fsanjurjo: I'm temporary changing the name of this method
     *  (from afterCompose to compose) to get it called after calling recreate().
     *  To understand why, please read this: http://www.zkoss.org/forum/listComment/14905
     *  Also renamed the call to its parent.
     * */
    @Override
    public void compose() {
        super.compose();
        Component fellow = getFellow("firstleveldetails");
        addSecondLevels(fellow.getParent());
    }

    private void addSecondLevels(Component parent) {
        Executions.getCurrent().createComponents(secondLevelZul, parent,
                getAttributes());
    }

    public ZoomLevel getZoomLevel() {
        return this.getTimeTracker().getDetailLevel();
    }

    protected abstract void scrollHorizontalPercentage(int daysDisplacement);

    protected abstract void moveCurrentPositionScroll();

    protected abstract void updateCurrentDayScroll();

    public Collection<DetailItem> getDetailsFirstLevel() {
        return timeTracker.getDetailsFirstLevel();
    }

    public Collection<DetailItem> getDetailsSecondLevel() {
        return timeTracker.getDetailsSecondLevel();
    }

    private TimeTrackerState getTimeTrackerState() {
        return getTimeTracker().getTimeTrackerState();
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
        scrollHorizontalPercentage((int) Math.floor(days));
    }

    public void movePositionScroll() {
        moveCurrentPositionScroll();
    }

    public void updateDayScroll() {
        updateCurrentDayScroll();
    }

    public int getDiffDays(LocalDate previousStart) {
        // get the current data
        IDatesMapper mapper = getTimeTracker().getMapper();
        LocalDate start = getTimeTracker().getRealInterval().getStart();
        return Days.daysBetween(start, previousStart).getDays();
    }

    public double getPixelPerDay() {
        return getTimeTracker().getMapper().getPixelsPerDay().doubleValue();
    }

    public boolean getWeekLevel() {
        return timeTracker.getDetailLevel() == ZoomLevel.DETAIL_FOUR;
    }

    public void setZoomLevel(ZoomLevel zoomlevel, int scrollLeft){
        this.scrollLeft = scrollLeft;
        getTimeTracker().setZoomLevel(zoomlevel);
    }

    public String getWidgetClass(){
        return getDefinition().getDefaultWidgetClass();
    }

}
