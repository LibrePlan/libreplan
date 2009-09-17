package org.navalplanner.business.advance.entities;

import java.math.BigDecimal;
import java.util.Date;

import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;

public class AdvanceMeasurement extends BaseEntity {

    public static AdvanceMeasurement create(Date date, BigDecimal value) {
        AdvanceMeasurement advanceMeasurement = new AdvanceMeasurement(date,
                value);
        advanceMeasurement.setNewObject(true);
        return advanceMeasurement;
    }

    public static AdvanceMeasurement create() {
        AdvanceMeasurement advanceMeasurement = new AdvanceMeasurement();
        advanceMeasurement.setNewObject(true);
        return advanceMeasurement;
    }

    @NotNull
    private Date date;

    @NotNull
    private BigDecimal value;

    @NotNull
    private AdvanceAssigment advanceAssigment;

    public AdvanceMeasurement() {
    }

    private AdvanceMeasurement(Date date, BigDecimal value) {
        this.date = date;
        this.value = value;
        this.value.setScale(2,BigDecimal.ROUND_HALF_UP);
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
