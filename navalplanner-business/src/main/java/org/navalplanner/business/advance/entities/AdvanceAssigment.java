package org.navalplanner.business.advance.entities;

import java.math.BigDecimal;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.orders.entities.OrderElement;

public class AdvanceAssigment extends BaseEntity {

    public enum Type { DIRECT, 	CALCULATED };

    public static AdvanceAssigment create() {
        AdvanceAssigment advanceAssigment = new AdvanceAssigment();
        advanceAssigment.setNewObject(true);
        return advanceAssigment;
    }

    public static AdvanceAssigment create(boolean reportGlobalAdvance, BigDecimal maxValue) {
        AdvanceAssigment advanceAssigment = new AdvanceAssigment(reportGlobalAdvance, maxValue);
        advanceAssigment.setNewObject(true);
        return advanceAssigment;
    }

    private boolean reportGlobalAdvance;

    @NotNull
    private BigDecimal maxValue;

    private Type type;

    private OrderElement orderElement;

    private AdvanceType advanceType;

    private SortedSet<AdvanceMeasurement> advanceMeasurements =
            new TreeSet<AdvanceMeasurement>(new AdvanceMeasurementComparator());

    public AdvanceAssigment() {
        this.reportGlobalAdvance = false;
    }

    private AdvanceAssigment(boolean reportGlobalAdvance,BigDecimal maxValue) {
        this.reportGlobalAdvance = reportGlobalAdvance;
        this.maxValue = maxValue;
        this.maxValue.setScale(2,BigDecimal.ROUND_HALF_UP);
    }

    public void setReportGlobalAdvance(boolean reportGlobalAdvance) {
        this.reportGlobalAdvance = reportGlobalAdvance;
    }

    public boolean getReportGlobalAdvance() {
        return this.reportGlobalAdvance;
    }

    public BigDecimal getMaxValue() {
        return this.maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
        this.maxValue.setScale(2);
    }

    public void setOrderElement(OrderElement orderElement) {
        this.orderElement = orderElement;
    }

    public OrderElement getOrderElement() {
        return this.orderElement;
    }

    public void setAdvanceType(AdvanceType advanceType) {
        this.advanceType = advanceType;
    }

    public AdvanceType getAdvanceType() {
        return this.advanceType;
    }

    public SortedSet<AdvanceMeasurement> getAdvanceMeasurements() {
        return this.advanceMeasurements;
    }

    public void setAdvanceMeasurements(
            SortedSet<AdvanceMeasurement> advanceMeasurements) {
        this.advanceMeasurements = advanceMeasurements;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public AdvanceMeasurement getLastAdvanceMeasurement() {
        if (advanceMeasurements.isEmpty()) {
            return null;
        }

        AdvanceMeasurement last = advanceMeasurements.first();
        for (AdvanceMeasurement advanceMeasurement : advanceMeasurements) {
            LocalDate date = advanceMeasurement.getDate();
            if (last.getDate().compareTo(date) < 0) {
                last = advanceMeasurement;
            }
        }

        return last;
    }

    public BigDecimal getLastPercentage() {
        BigDecimal zero = new BigDecimal(0);
        if (maxValue.compareTo(zero) == 0) {
            return zero;
        }

        AdvanceMeasurement advanceMeasurement = getLastAdvanceMeasurement();
        if (advanceMeasurement == null) {
            return zero;
        }
        return advanceMeasurement.getValue().divide(maxValue);
    }

}
