package org.navalplanner.business.advance.entities;

import java.math.BigDecimal;

import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;

public class AdvanceMeasurement extends BaseEntity {

    public static AdvanceMeasurement create(LocalDate date, BigDecimal value) {
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
    private LocalDate date;

    @NotNull
    private BigDecimal value;

    @NotNull
    private AdvanceAssignment advanceAssignment;

    public AdvanceMeasurement() {
    }

    private AdvanceMeasurement(LocalDate date, BigDecimal value) {
        this.date = date;
        this.value = value;
        this.value.setScale(2,BigDecimal.ROUND_HALF_UP);
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
        if (value != null) {
            this.value.setScale(2);
        }
    }

    public BigDecimal getValue() {
        return this.value;
    }

    public void setAdvanceAssignment(AdvanceAssignment advanceAssignment) {
        this.advanceAssignment = advanceAssignment;
    }

    public AdvanceAssignment getAdvanceAssignment() {
        return this.advanceAssignment;
    }

}
