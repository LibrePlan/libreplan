package org.navalplanner.web.resourceload;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;

public class ResourceLoadDisplayData {

    private final List<LoadTimeLine> timeLines;

    private final Interval viewInterval;

    public ResourceLoadDisplayData(List<LoadTimeLine> timeLines) {
        this.timeLines = timeLines;
        this.viewInterval = getViewIntervalFrom(timeLines);
    }

    private static Interval getViewIntervalFrom(List<LoadTimeLine> timeLines) {
        if (timeLines.isEmpty()) {
            return new Interval(new Date(), plusFiveYears(new Date()));
        }
        return LoadTimeLine.getIntervalFrom(timeLines);
    }

    private static Date plusFiveYears(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, 5);
        return calendar.getTime();
    }

    public List<LoadTimeLine> getLoadTimeLines() {
        return timeLines;
    }

    public Interval getViewInterval() {
        return viewInterval;
    }

    public ZoomLevel getInitialZoomLevel() {
        Interval interval = getViewInterval();
        return ZoomLevel.getDefaultZoomByDates(
                new LocalDate(interval.getStart()),
                new LocalDate(interval.getFinish()));
    }

}
