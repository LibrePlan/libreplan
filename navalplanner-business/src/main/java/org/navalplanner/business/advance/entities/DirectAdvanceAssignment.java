/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.business.advance.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
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

    private BigDecimal maxValue;

    @Valid
    private SortedSet<AdvanceMeasurement> advanceMeasurements = new TreeSet<AdvanceMeasurement>(
            new AdvanceMeasurementComparator());

    private boolean fake = false;

    public DirectAdvanceAssignment() {
        super();
    }

    private DirectAdvanceAssignment(boolean reportGlobalAdvance,
            BigDecimal maxValue) {
        super(reportGlobalAdvance);
        this.maxValue = maxValue;
        this.maxValue.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @NotNull(message = "maximum value not specified")
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

    public BigDecimal getAdvancePercentage() {
        return getAdvancePercentage(null);
    }

    public BigDecimal getAdvancePercentage(LocalDate date) {
        if (maxValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        AdvanceMeasurement advanceMeasurement = (date != null) ? getAdvanceMeasurement(date)
                : getLastAdvanceMeasurement();
        if (advanceMeasurement == null) {
            return BigDecimal.ZERO;
        }
        return advanceMeasurement.getValue().setScale(2).divide(maxValue,
                RoundingMode.DOWN);
    }

    public void addAdvanceMeasurements(AdvanceMeasurement advanceMeasurement) {
        this.advanceMeasurements.add(advanceMeasurement);
        advanceMeasurement.setAdvanceAssignment(this);
    }

    public AdvanceMeasurement getAdvanceMeasurementAtExactDate(LocalDate date) {
        if (advanceMeasurements.isEmpty()) {
            return null;
        }

        for (AdvanceMeasurement advanceMeasurement : advanceMeasurements) {
            if (advanceMeasurement.getDate().equals(date)) {
                return advanceMeasurement;
            }
        }

        return null;
    }


    public void setFake(boolean fake) {
        this.fake = fake;
    }

    public boolean isFake() {
        return fake;
    }

}
