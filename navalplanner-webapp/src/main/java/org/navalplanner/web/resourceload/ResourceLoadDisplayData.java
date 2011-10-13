package org.navalplanner.web.resourceload;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.resourceload.ResourceLoadParameters.Paginator;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;

public class ResourceLoadDisplayData {

    private static <T> Callable<T> cached(Callable<T> callable) {
        return new CachedCallable<T>(callable);
    }

    private static <T> T resolve(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class CachedCallable<T> implements Callable<T> {
        private final Callable<T> callable;

        private T result;

        public CachedCallable(Callable<T> callable) {
            Validate.notNull(callable);
            this.callable = callable;
        }

        @Override
        public T call() throws Exception {
            if (result != null) {
                return result;
            }
            return result = callable.call();
        }

    }

    private final List<LoadTimeLine> timeLines;

    private final Interval viewInterval;

    private final Paginator<? extends BaseEntity> paginator;

    private final Callable<List<Resource>> resourcesConsidered;

    private final Callable<List<DayAssignment>> assignmentsConsidered;

    private final LocalDate filterStart;

    private final LocalDate filterEnd;

    public ResourceLoadDisplayData(List<LoadTimeLine> timeLines,
            LocalDate filterStart, LocalDate filterEnd,
            Paginator<? extends BaseEntity> paginator,
            Callable<List<Resource>> resourcesConsidered,
            Callable<List<DayAssignment>> assignmentsConsidered) {
        Validate.notNull(timeLines);
        Validate.notNull(paginator);
        Validate.notNull(resourcesConsidered);
        Validate.notNull(assignmentsConsidered);
        this.timeLines = timeLines;
        this.filterStart = filterStart;
        this.filterEnd = filterEnd;
        this.viewInterval = getViewIntervalFrom(timeLines);
        this.paginator = paginator;
        this.resourcesConsidered = cached(resourcesConsidered);
        this.assignmentsConsidered = cached(assignmentsConsidered);
    }

    private static Interval getViewIntervalFrom(List<LoadTimeLine> timeLines) {
        return LoadTimeLine.getIntervalFrom(timeLines);
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

    public Paginator<? extends BaseEntity> getPaginator() {
        return paginator;
    }

    public List<Resource> getResourcesConsidered() {
        return resolve(resourcesConsidered);
    }

    public List<DayAssignment> getDayAssignmentsConsidered() {
        return resolve(assignmentsConsidered);
    }

    public LocalDate getFilterStart() {
        return filterStart;
    }

    public LocalDate getFilterEnd() {
        return filterEnd;
    }

}
