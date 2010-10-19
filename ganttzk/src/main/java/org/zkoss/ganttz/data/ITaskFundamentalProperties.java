/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
     * Sets the beginDate.
     */
    public void setBeginDate(GanttDate beginDate);

    public GanttDate getBeginDate();

    void setBeginDateKeepingSize(GanttDate newStart);

    /**
     * The deadline associated to the task. It can return null if has no
     * deadline associated
     */
    public Date getDeadline();

    public void setDeadline(Date date);

    public GanttDate getConsolidatedline();

    public GanttDate getEndDate();

    public void setEndDate(GanttDate endDate);

    public void setEndDateKeepingSize(GanttDate value);

    public String getNotes();

    public void setNotes(String notes);

    public GanttDate getHoursAdvanceEndDate();

    public GanttDate getAdvanceEndDate();

    public BigDecimal getHoursAdvancePercentage();

    public BigDecimal getAdvancePercentage();

    public String getTooltipText();

    public String getLabelsText();

    public String getResourcesText();

    List<Constraint<GanttDate>> getStartConstraints();

    public void moveTo(GanttDate date);

    public boolean isSubcontracted();

    public boolean isLimiting();

    public boolean isLimitingAndHasDayAssignments();

    public boolean hasConsolidations();

    public boolean canBeExplicitlyResized();

    public String getAssignedStatus();

    public boolean isFixed();

    public String updateTooltipText();

    List<Constraint<GanttDate>> getEndConstraints();

    boolean shouldCalculateEndFirst();

    GanttDate getOrderDeadline();

}
