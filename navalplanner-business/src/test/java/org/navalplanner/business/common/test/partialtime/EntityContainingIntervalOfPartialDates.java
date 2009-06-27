package org.navalplanner.business.common.test.partialtime;

import org.navalplanner.business.common.partialtime.IntervalOfPartialDates;

public class EntityContainingIntervalOfPartialDates {

    private Long id;

    private Long version;

    private IntervalOfPartialDates interval;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public IntervalOfPartialDates getInterval() {
        return interval;
    }

    public void setInterval(IntervalOfPartialDates interval) {
        this.interval = interval;
    }

}
