package org.navalplanner.business.partialtime;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class IntervalOfPartialDates {

    private final PartialDate start;

    private final PartialDate end;

    public IntervalOfPartialDates(PartialDate start, PartialDate end) {
        if (!start.getGranularity().equals(end.getGranularity()))
            throw new IllegalArgumentException(
                    "the from and the to must have the same granularity");
        if (!start.before(end)) {
            throw new IllegalArgumentException(
                    "the start must be before the end");
        }
        this.start = start;
        this.end = end;
    }

    public PartialDate getStart() {
        return this.start;
    }

    public PartialDate getEnd() {
        return this.end;
    }

    public TimeQuantity getDuration() {
        return end.quantityFrom(start);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof IntervalOfPartialDates) {
            IntervalOfPartialDates other = (IntervalOfPartialDates) obj;
            return new EqualsBuilder().append(this.start, other.start).append(
                    this.end, other.end).isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(start).append(end).toHashCode();
    }
}