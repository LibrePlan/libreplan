package org.navalplanner.business.planner.entities;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Share {

    private final int hours;

    public Share(int hours) {
        this.hours = hours;
    }

    public int getHours() {
        return hours;
    }

    public Share plus(int increment) {
        return new Share(hours + increment);
    }

    @Override
    public String toString() {
        return hours + "";
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hours).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Share) {
            Share otherShare = (Share) obj;
            return new EqualsBuilder().append(hours, otherShare.hours)
                    .isEquals();
        }
        return false;
    }

}
