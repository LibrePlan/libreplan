package org.navalplanner.business.common.test.partialtime;

import org.navalplanner.business.common.partialtime.PartialDate;

public class EntityContainingPartialDate {

    private Long id;

    private Long version;

    private PartialDate partialDate;

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

    public PartialDate getPartialDate() {
        return partialDate;
    }

    public void setPartialDate(PartialDate partialDate) {
        this.partialDate = partialDate;
    }


}
