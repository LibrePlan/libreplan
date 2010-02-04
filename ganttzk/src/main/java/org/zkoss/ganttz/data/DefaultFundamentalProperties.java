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

package org.zkoss.ganttz.data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.zkoss.ganttz.data.constraint.Constraint;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DefaultFundamentalProperties implements ITaskFundamentalProperties {

    private String name;

    private long beginDate;

    private long lengthMilliseconds = 0;

    private String notes;

    private long hoursAdvanceEndDate;

    private Date advanceEndDate;

    private BigDecimal hoursAdvancePercentage;

    private BigDecimal advancePercentage;

    private String tooltipText;

    private String labelsText;

    private String resourcesText;

    public DefaultFundamentalProperties() {
    }

    public DefaultFundamentalProperties(String name, Date beginDate,
            long lengthMilliseconds, String notes,
            Date hoursAdvanceEndDate,
            Date advanceEndDate,
			BigDecimal hoursAdvancePercentage, BigDecimal advancePercentage) {
        this.name = name;
        this.beginDate = beginDate.getTime();
        this.lengthMilliseconds = lengthMilliseconds;
        this.notes = notes;
        this.hoursAdvanceEndDate = hoursAdvanceEndDate.getTime();
        this.advanceEndDate = advanceEndDate;
        this.hoursAdvancePercentage = hoursAdvancePercentage;
        this.advancePercentage = advancePercentage;
        this.tooltipText = "Default tooltip";
        this.labelsText = "";
        this.resourcesText = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBeginDate() {
        return new Date(beginDate);
    }

    public long setBeginDate(Date beginDate) {
        this.beginDate = beginDate.getTime();
        return lengthMilliseconds;
    }

    public long getLengthMilliseconds() {
        return lengthMilliseconds;
    }

    public void setLengthMilliseconds(long lengthMilliseconds) {
        if (lengthMilliseconds < 0) {
            throw new IllegalArgumentException(
                    "a task must not have a negative length. Received value: "
                            + lengthMilliseconds);
        }
        this.lengthMilliseconds = lengthMilliseconds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public Date getHoursAdvanceEndDate() {
        return new Date(hoursAdvanceEndDate);
    }

    @Override
    public Date getAdvanceEndDate() {
        return advanceEndDate != null ? new Date(advanceEndDate.getTime())
                : null;
    }
    @Override
    public BigDecimal getHoursAdvancePercentage() {
        return hoursAdvancePercentage;
    }

    @Override
    public BigDecimal getAdvancePercentage() {
        return advancePercentage;
    }

    @Override
    public String getTooltipText() {
        return tooltipText;
    }

    @Override
    public String getLabelsText() {
        return labelsText;
    }

    @Override
    public String getResourcesText() {
        return resourcesText;
    }

    @Override
    public List<Constraint<Date>> getStartConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void moveTo(Date date) {
        setBeginDate(date);
    }

    @Override
    public Date getDeadline() {
        return null;
    }

    public boolean isSubcontracted() {
        return false;
    }

}
