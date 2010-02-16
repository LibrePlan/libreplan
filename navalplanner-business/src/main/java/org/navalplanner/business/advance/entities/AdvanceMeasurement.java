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
import java.util.Date;
import java.util.Set;

import org.hibernate.validator.NotNull;
import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.orders.entities.OrderElement;

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

    private LocalDate date;

    private BigDecimal value;

    private AdvanceAssignment advanceAssignment;

    private Date communicationDate;

    public AdvanceMeasurement() {
    }

    private AdvanceMeasurement(LocalDate date, BigDecimal value) {
        this.date = date;
        this.value = value;
        if (this.value != null) {
            this.value.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }

    public void setDate(LocalDate date) {
        this.date = date;
        resetCommunicationDate();
    }

    @NotNull(message = "date not specified")
    public LocalDate getDate() {
        return this.date;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
        if (value != null) {
            this.value.setScale(2);
        }
        resetCommunicationDate();
    }

    @NotNull(message = "value not specified")
    public BigDecimal getValue() {
        return this.value;
    }

    public void setAdvanceAssignment(AdvanceAssignment advanceAssignment) {
        this.advanceAssignment = advanceAssignment;
    }

    @NotNull(message = "advance assignment not specified")
    public AdvanceAssignment getAdvanceAssignment() {
        return this.advanceAssignment;
    }

    public void setCommunicationDate(Date communicationDate) {
        this.communicationDate = communicationDate;
    }

    public Date getCommunicationDate() {
        return communicationDate;
    }

    /**
     * Just set the communication date if it was <code>null</code>. Otherwise
     * keep the old value stored.
     *
     * @param communicationDate
     */
    public void updateCommunicationDate(Date communicationDate) {
        DirectAdvanceAssignment advanceAssignment = (DirectAdvanceAssignment) getAdvanceAssignment();
        if (advanceAssignment.isFake()) {
            OrderElement orderElement = advanceAssignment.getOrderElement();
            Set<DirectAdvanceAssignment> directAdvanceAssignments = orderElement
                    .getAllDirectAdvanceAssignments(advanceAssignment
                            .getAdvanceType());
            for (DirectAdvanceAssignment directAdvanceAssignment : directAdvanceAssignments) {
                for (AdvanceMeasurement advanceMeasurement : directAdvanceAssignment
                        .getAdvanceMeasurements()) {
                    advanceMeasurement
                            .updateCommunicationDate(communicationDate);
                }
            }
        } else {
            if ((this.communicationDate == null) && (communicationDate != null)) {
                this.communicationDate = communicationDate;
            }
        }
    }

    private void resetCommunicationDate() {
        communicationDate = null;
    }

}
