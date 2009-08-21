package org.zkoss.ganttz.data.resourceload;

import java.util.List;

import org.apache.commons.lang.Validate;

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
}
