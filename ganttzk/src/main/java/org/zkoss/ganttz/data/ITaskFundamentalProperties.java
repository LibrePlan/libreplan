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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.zkoss.ganttz.data.constraint.Constraint;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public interface ITaskFundamentalProperties {

    public String getName();

    public void setName(String name);

    /**
     * Sets the beginDate. As result of this, the length of the task can change.
     * So the new value is returned
     * @return the new length
     */
    public long setBeginDate(Date beginDate);

    public Date getBeginDate();

    /**
     * The deadline associated to the task. It can return null if has no
     * deadline associated
     */
    public Date getDeadline();

    public void setLengthMilliseconds(long lengthMilliseconds);

    public long getLengthMilliseconds();

    public String getNotes();

    public void setNotes(String notes);

    public Date getHoursAdvanceEndDate();

    public Date getAdvanceEndDate();

    public BigDecimal getHoursAdvancePercentage();

    public BigDecimal getAdvancePercentage();

    public String getTooltipText();

    public String getLabelsText();

    List<Constraint<Date>> getStartConstraints();

    public void moveTo(Date date);

}
