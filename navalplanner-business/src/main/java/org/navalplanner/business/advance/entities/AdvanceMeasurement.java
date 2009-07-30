package org.navalplanner.business.advance.entities;

import java.math.BigDecimal;
import java.util.Date;

public class AdvanceMeasurement {
    private Long id;

    @SuppressWarnings("unused")
    private long version;

    private Date date;

    private BigDecimal value;

    private AdvanceAssigment advanceAssigment;

    public AdvanceMeasurement(Date date, BigDecimal value) {
        this.date = date;
        this.value = value;
        this.value.setScale(2);
    }

    public Long getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return this.date;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
        this.value.setScale(2);
    }

    public BigDecimal getValue() {
        return this.value;
    }

    public void setAdvanceAssigment(AdvanceAssigment advanceAssigment) {
        this.advanceAssigment = advanceAssigment;
    }

    public AdvanceAssigment getAdvanceAssigment() {
        return this.advanceAssigment;
    }
}
