package org.navalplanner.business.planner.entities;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Share {

    private final int hours;

    public Share(int hours) {
        Validate.isTrue(hours >= 0);
        this.hours = hours;
    }

    public int getHours() {
        return hours;
    }

    public Share add(int increment) {
        Validate.isTrue(hours + increment >= 0);
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
