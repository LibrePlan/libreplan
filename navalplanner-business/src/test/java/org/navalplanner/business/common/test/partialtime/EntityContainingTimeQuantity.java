package org.navalplanner.business.common.test.partialtime;

import org.navalplanner.business.common.partialtime.TimeQuantity;

public class EntityContainingTimeQuantity {

    private Long id;

    private Long version;

    private TimeQuantity timeQuantity;

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

    public TimeQuantity getTimeQuantity() {
        return timeQuantity;
    }

    public void setTimeQuantity(TimeQuantity timeQuantity) {
        this.timeQuantity = timeQuantity;
    }

}
