package org.zkoss.ganttz.data.resourceload;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;

public class LoadTimeLine {

    private final String conceptName;
    private final List<LoadPeriod> loadPeriods;

    public LoadTimeLine(String conceptName, List<LoadPeriod> loadPeriods) {
        Validate.notEmpty(conceptName);
        Validate.notNull(loadPeriods);
        this.loadPeriods = LoadPeriod.sort(loadPeriods);
        this.conceptName = conceptName;
    }

    public List<LoadPeriod> getLoadPeriods() {
        return loadPeriods;
    }

    public String getConceptName() {
        return conceptName;
    }

    private LoadPeriod getFirst() {
        return loadPeriods.get(0);
    }

    private LoadPeriod getLast() {
        return loadPeriods.get(loadPeriods.size() - 1);
    }

    public LocalDate getStart() {
        if (loadPeriods.isEmpty()) {
            return null;
        }
        return getFirst().getStart();
    }

    public LocalDate getEnd() {
        if (loadPeriods.isEmpty()) {
            return null;
        }
        return getLast().getEnd();
    }
}
