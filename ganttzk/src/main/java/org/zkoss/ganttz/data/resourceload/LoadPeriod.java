package org.zkoss.ganttz.data.resourceload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.LocalDate;

public class LoadPeriod {

    private final LocalDate start;

    private final LocalDate end;

    private final LoadLevel loadLevel;

    public LoadPeriod(LocalDate start, LocalDate end, LoadLevel loadLevel) {
        Validate.notNull(start);
        Validate.notNull(end);
        Validate.notNull(loadLevel);
        Validate.isTrue(!start.isAfter(end));
        this.start = start;
        this.end = end;
        this.loadLevel = loadLevel;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public boolean overlaps(LoadPeriod other) {
        return start.isBefore(other.end) && end.isAfter(other.start);
    }

    /**
     * @param notOverlappingPeriods
     * @return
     * @throws IllegalArgumentException
     *             if some of the LoadPeriod overlaps
     */
    public static List<LoadPeriod> sort(
            Collection<? extends LoadPeriod> notOverlappingPeriods)
            throws IllegalArgumentException {
        ArrayList<LoadPeriod> result = new ArrayList<LoadPeriod>(
                notOverlappingPeriods);
        Collections.sort(result,
                new Comparator<LoadPeriod>() {

                    @Override
                    public int compare(LoadPeriod o1, LoadPeriod o2) {
                        if (o1.overlaps(o2)) {
                            throw new IllegalArgumentException(o1
                                    + " overlaps with " + o2);
                        }
                        int comparison = compareLocalDates(o1.start, o2.end);
                        if (comparison != 0)
                            return comparison;
                        return compareLocalDates(o1.end, o2.end);
                    }
                });
        return result;
    }

    private static int compareLocalDates(LocalDate l1, LocalDate l2) {
        if (l1.isBefore(l2))
            return -1;
        if (l1.isAfter(l2))
            return 1;
        return 0;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public LoadLevel getLoadLevel() {
        return loadLevel;
    }

}
