package org.navalplanner.business.advance.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.orders.entities.OrderElement;

/**
 * Represents an {@link AdvanceAssignment} that is own of this
 * {@link OrderElement}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class DirectAdvanceAssignment extends AdvanceAssignment {

    public static DirectAdvanceAssignment create() {
        DirectAdvanceAssignment directAdvanceAssignment = new DirectAdvanceAssignment();
        directAdvanceAssignment.setNewObject(true);
        return directAdvanceAssignment;
    }

    public static DirectAdvanceAssignment create(boolean reportGlobalAdvance,
            BigDecimal maxValue) {
        DirectAdvanceAssignment advanceAssignment = new DirectAdvanceAssignment(
                reportGlobalAdvance, maxValue);
        advanceAssignment.setNewObject(true);
        return advanceAssignment;
    }

    @NotNull
    private BigDecimal maxValue;

    private SortedSet<AdvanceMeasurement> advanceMeasurements = new TreeSet<AdvanceMeasurement>(
            new AdvanceMeasurementComparator());

    public DirectAdvanceAssignment() {
        super();
    }

    private DirectAdvanceAssignment(boolean reportGlobalAdvance,
            BigDecimal maxValue) {
        super(reportGlobalAdvance);
        this.maxValue = maxValue;
        this.maxValue.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getMaxValue() {
        return this.maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
        this.maxValue.setScale(2);
    }

    public SortedSet<AdvanceMeasurement> getAdvanceMeasurements() {
        return this.advanceMeasurements;
    }

    public void setAdvanceMeasurements(
            SortedSet<AdvanceMeasurement> advanceMeasurements) {
        this.advanceMeasurements = advanceMeasurements;
    }

    public AdvanceMeasurement getLastAdvanceMeasurement() {
        if (advanceMeasurements.isEmpty()) {
            return null;
        }

        return advanceMeasurements.first();
    }

    public BigDecimal getLastPercentage() {
        if (maxValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        AdvanceMeasurement advanceMeasurement = getLastAdvanceMeasurement();
        if (advanceMeasurement == null) {
            return BigDecimal.ZERO;
        }
        return advanceMeasurement.getValue().setScale(2).divide(maxValue,
                RoundingMode.DOWN);
    }

    public AdvanceMeasurement getAdvanceMeasurement(LocalDate date) {
        if (advanceMeasurements.isEmpty()) {
            return null;
        }

        for (AdvanceMeasurement advanceMeasurement : advanceMeasurements) {
            if (advanceMeasurement.getDate().compareTo(date) <= 0) {
                return advanceMeasurement;
            }
        }

        return null;
    }

    public BigDecimal getAdvancePercentage(LocalDate date) {
        if (maxValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        AdvanceMeasurement advanceMeasurement = getAdvanceMeasurement(date);
        if (advanceMeasurement == null) {
            return BigDecimal.ZERO;
        }
        return advanceMeasurement.getValue().setScale(2).divide(maxValue,
                RoundingMode.DOWN);
    }

}
