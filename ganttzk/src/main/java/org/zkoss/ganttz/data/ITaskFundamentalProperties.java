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

import org.zkoss.ganttz.ProjectStatusEnum;
import org.zkoss.ganttz.data.constraint.Constraint;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public interface ITaskFundamentalProperties {

    interface IUpdatablePosition {

        void setBeginDate(GanttDate beginDate);

        void setEndDate(GanttDate endDate);

        void resizeTo(GanttDate endDate);

        void moveTo(GanttDate newStart);
    }

    /**
     * The position modifications must be wrapped inside this
     */
    interface IModifications {
        void doIt(IUpdatablePosition position);
    }


    void doPositionModifications(IModifications modifications);

    String getName();

    void setName(String name);

    GanttDate getBeginDate();

    /**
     * The deadline associated to the task. It can return null if has no
     * deadline associated
     */
    Date getDeadline();

    void setDeadline(Date date);

    GanttDate getConsolidatedline();

    GanttDate getEndDate();

    String getNotes();

    void setNotes(String notes);

    GanttDate getHoursAdvanceBarEndDate();

    GanttDate getMoneyCostBarEndDate();

    BigDecimal getMoneyCostBarPercentage();

    GanttDate getAdvanceBarEndDate();

    BigDecimal getHoursAdvanceBarPercentage();

    BigDecimal getAdvancePercentage();

    String getTooltipText();

    String getLabelsText();

    String getResourcesText();

    List<Constraint<GanttDate>> getStartConstraints();

    List<Constraint<GanttDate>> getEndConstraints();

    boolean isSubcontracted();

    boolean isLimiting();

    boolean isLimitingAndHasDayAssignments();

    boolean hasConsolidations();

    boolean canBeExplicitlyResized();

    String getAssignedStatus();

    boolean isFixed();

    String updateTooltipText();

    List<Constraint<GanttDate>> getCurrentLengthConstraint();

    GanttDate getAdvanceBarEndDate(String progressType);

    String updateTooltipText(String progressType);

    boolean isManualAnyAllocation();

    boolean belongsClosedProject();

    boolean isRoot();

    boolean isUpdatedFromTimesheets();

    Date getFirstTimesheetDate();

    Date getLastTimesheetDate();

    String getCode();

    String getProjectCode();

    /**
     * Calculates whether the project is within the estimated hours or not and
     * returns alarm status(color) {@link ProjectStatusEnum} accordingly.
     *
     * Alarm status definition:
     * <ul>
     * <li>{@link ProjectStatusEnum#AS_PLANNED}: everything is OK, project is as
     * planned</li>
     * <li>{@link ProjectStatusEnum#WITHIN_MARGIN}: warning, project exceeded
     * the estimated hours, but still within margin</li>
     * <li>{@link ProjectStatusEnum#MARGIN_EXCEEDED}: Project exceeded the hours
     * estimated with margin</li>
     * </ul>
     *
     * @return {@link ProjectStatusEnum}
     */
    ProjectStatusEnum getProjectHoursStatus();

    /**
     * Calculates whether the project is within the estimated budget or not and
     * returns alarm status(color) {@link ProjectStatusEnum} accordingly.
     *
     * Alarm status definition:
     * <ul>
     * <li>{@link ProjectStatusEnum#AS_PLANNED}: everything is OK, project is as
     * planned</li>
     * <li>{@link ProjectStatusEnum#WITHIN_MARGIN}: warning, project exceeded
     * the estimated budget, but still within margin</li>
     * <li>{@link ProjectStatusEnum#MARGIN_EXCEEDED}: Project exceeded the
     * budget estimated with margin</li>
     * </ul>
     *
     * @return {@link ProjectStatusEnum}
     */
    ProjectStatusEnum getProjectBudgetStatus();

    /**
     * creates and returns tooltiptext for the project's hours status
     */
    String getTooltipTextForProjectHoursStatus();

    /**
     * creates and returns tooltiptext for the project's budget status
     */
    String getTooltipTextForProjectBudgetStatus();

}
