/*
 * This file is part of ###PROJECT_NAME###
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

import java.util.Date;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class DefaultFundamentalProperties implements ITaskFundamentalProperties {

    private String name;

    private Date beginDate = null;

    private long lengthMilliseconds = 0;

    private String notes;

    private Date hoursAdvanceEndDate;

    private Date advanceEndDate;

    public DefaultFundamentalProperties() {
    }

    public DefaultFundamentalProperties(String name, Date beginDate,
            long lengthMilliseconds, String notes,
            Date hoursAdvancePercentage,
            Date advancePercentage) {
        this.name = name;
        this.beginDate = beginDate;
        this.lengthMilliseconds = lengthMilliseconds;
        this.notes = notes;
        this.hoursAdvanceEndDate = hoursAdvancePercentage;
        this.advanceEndDate = advancePercentage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public long getLengthMilliseconds() {
        return lengthMilliseconds;
    }

    public void setLengthMilliseconds(long lengthMilliseconds) {
        if (lengthMilliseconds < 0)
            throw new IllegalArgumentException(
                    "a task must not have a negative length. Received value: "
                            + lengthMilliseconds);
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
        return hoursAdvanceEndDate;
    }

    @Override
    public Date getAdvanceEndDate() {
        return advanceEndDate;
    }

}
