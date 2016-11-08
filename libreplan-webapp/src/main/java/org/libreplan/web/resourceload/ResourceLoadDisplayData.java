package org.libreplan.web.resourceload;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.Validate;
import org.joda.time.LocalDate;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.planner.entities.DayAssignment;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.web.resourceload.ResourceLoadParameters.Paginator;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.Interval;

public class ResourceLoadDisplayData {

    private final List<LoadTimeLine> timeLines;

    private final Interval viewInterval;

    private final Paginator<? extends BaseEntity> paginator;

    private final Callable<List<Resource>> resourcesConsidered;

    private final Callable<List<DayAssignment>> assignmentsConsidered;

    public ResourceLoadDisplayData(
            List<LoadTimeLine> timeLines,
            Paginator<? extends BaseEntity> paginator,
            Callable<List<Resource>> resourcesConsidered,
            Callable<List<DayAssignment>> assignmentsConsidered) {

        Validate.notNull(timeLines);
        Validate.notNull(paginator);
        Validate.notNull(resourcesConsidered);
        Validate.notNull(assignmentsConsidered);

        this.timeLines = timeLines;
        this.viewInterval = getViewIntervalFrom(timeLines);
        this.paginator = paginator;
        this.resourcesConsidered = cached(resourcesConsidered);
        this.assignmentsConsidered = cached(assignmentsConsidered);
    }

    private static <T> Callable<T> cached(Callable<T> callable) {
        return new CachedCallable<>(callable);
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

            result = callable.call();

            return result;
        }

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

}
