/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public interface ITaskFundamentalProperties {

    public interface IUpdatablePosition {

        public void setBeginDate(GanttDate beginDate);

        public void setEndDate(GanttDate endDate);

        public void resizeTo(GanttDate endDate);

        public void moveTo(GanttDate newStart);
    }

    /**
     * The position modifications must be wrapped inside this
     */
    public interface IModifications {
        public void doIt(IUpdatablePosition position);
    }

    public void doPositionModifications(IModifications modifications);

    public String getName();

    public void setName(String name);

    public GanttDate getBeginDate();

    /**
     * The deadline associated to the task. It can return null if has no
     * deadline associated
     */
    public Date getDeadline();

    public void setDeadline(Date date);

    public GanttDate getConsolidatedline();

    public GanttDate getEndDate();

    public String getNotes();

    public void setNotes(String notes);

    public GanttDate getHoursAdvanceEndDate();

    public GanttDate getMoneyCostBarEndDate();

    BigDecimal getMoneyCostBarPercentage();

    public GanttDate getAdvanceEndDate();

    public BigDecimal getHoursAdvancePercentage();

    public BigDecimal getAdvancePercentage();

    public String getTooltipText();

    public String getLabelsText();

    public String getResourcesText();

    public List<Constraint<GanttDate>> getStartConstraints();

    public List<Constraint<GanttDate>> getEndConstraints();

    public boolean isSubcontracted();

    public boolean isLimiting();

    public boolean isLimitingAndHasDayAssignments();

    public boolean hasConsolidations();

    public boolean canBeExplicitlyResized();

    public String getAssignedStatus();

    public boolean isFixed();

    public String updateTooltipText();

    public List<Constraint<GanttDate>> getCurrentLengthConstraint();

    public GanttDate getAdvanceEndDate(String progressType);

    String updateTooltipText(String progressType);

    boolean isManualAnyAllocation();

    public boolean belongsClosedProject();

    public boolean isRoot();

    boolean isUpdatedFromTimesheets();

}
