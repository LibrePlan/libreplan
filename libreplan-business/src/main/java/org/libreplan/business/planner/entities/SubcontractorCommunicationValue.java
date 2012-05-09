/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 WirelessGalicia, S.L.
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

package org.libreplan.business.planner.entities;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.validator.AssertTrue;
import org.libreplan.business.INewObject;

/**
 * Entity to represent each {@SubcontractorCommunicationValue}.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia>
 */
public class SubcontractorCommunicationValue implements INewObject {

    public static SubcontractorCommunicationValue create() {
        SubcontractorCommunicationValue subcontractorCommunicationValue = new SubcontractorCommunicationValue();
        subcontractorCommunicationValue.setNewObject(true);
        return subcontractorCommunicationValue;
    }

    public static SubcontractorCommunicationValue create(Date date,
            BigDecimal progress) {
        SubcontractorCommunicationValue subcontractorCommunicationValue = new SubcontractorCommunicationValue(
                date, progress);
        subcontractorCommunicationValue.setNewObject(true);
        return subcontractorCommunicationValue;
    }

    protected SubcontractorCommunicationValue() {

    }

    private SubcontractorCommunicationValue(Date date, BigDecimal progress) {
        this.setDate(date);
        this.setProgress(progress);
    }

    private boolean newObject = false;

    private Date date;

    private BigDecimal progress;

    public boolean isNewObject() {
        return newObject;
    }

    private void setNewObject(boolean newObject) {
        this.newObject = newObject;
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "progress should be greater than 0% and less than 100%")
    public boolean checkConstraintQualityFormItemPercentage() {
        if (getProgress() == null) {
            return true;
        }
        return ((getProgress().compareTo(new BigDecimal(100).setScale(2)) <= 0) && (getProgress()
                .compareTo(new BigDecimal(0).setScale(2)) > 0));
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setProgress(BigDecimal progress) {
        this.progress = progress;
    }

    public BigDecimal getProgress() {
        return progress;
    }

    @Override
    public String toString() {
        String datetime = (date == null) ? "" : new SimpleDateFormat(
                "dd/MM/yyyy").format(date);
        String progress_reported = progress != null ? progress.toString() + "% - " : "";
        return progress_reported + datetime;
    }
}
