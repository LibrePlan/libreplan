package org.zkoss.ganttz.data.resourceload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.util.Interval;

public class LoadTimelinesGroup {

    public static Interval getIntervalFrom(List<LoadTimelinesGroup> timeLines) {
        LocalDate start = null;
        LocalDate end = null;
        for (LoadTimelinesGroup loadTimelinesGroup : timeLines) {
            start = min(start, loadTimelinesGroup.getStart());
            end = max(end, loadTimelinesGroup.getEnd());
        }
        return new Interval(toDate(start), toDate(end));
    }

    private static Date toDate(LocalDate localDate) {
        return localDate.toDateTimeAtStartOfDay().toDate();
    }

    private static LocalDate max(LocalDate one, LocalDate other) {
        if (one == null) {
            return other;
        }
        if (other == null) {
            return one;
        }
        return one.compareTo(other) > 0 ? one : other;
    }

    private static LocalDate min(LocalDate one, LocalDate other) {
        if (one == null) {
            return other;
        }
        if (other == null) {
            return one;
        }
        return one.compareTo(other) < 0 ? one : other;
    }

    private final LoadTimeLine principal;

    private final List<LoadTimeLine> children;

    public LoadTimelinesGroup(LoadTimeLine principal,
            List<? extends LoadTimeLine> children) {
        Validate.notNull(principal);
        Validate.notNull(children);
        this.principal = principal;
        this.children = Collections
                .unmodifiableList(new ArrayList<LoadTimeLine>(children));
    }

    public LoadTimeLine getPrincipal() {
        return principal;
    }

    public List<LoadTimeLine> getChildren() {
        return children;
    }

    private List<LoadTimeLine> getAll() {
        List<LoadTimeLine> result = new ArrayList<LoadTimeLine>();
        result.add(getPrincipal());
        result.addAll(getChildren());
        return result;
    }

    public LocalDate getStart() {
        LocalDate result = null;
        for (LoadTimeLine loadTimeLine : getAll()) {
            LocalDate start = loadTimeLine.getStart();
            result = result == null || result.compareTo(start) > 0 ? start
                    : result;
        }
        return result;
    }

    public LocalDate getEnd() {
        LocalDate result = null;
        for (LoadTimeLine loadTimeLine : getAll()) {
            LocalDate end = loadTimeLine.getEnd();
            result = result == null || result.compareTo(end) < 0 ? end
                    : result;
        }
        return result;
    }

}

