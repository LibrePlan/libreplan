/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

import org.joda.time.DateTime;
import org.zkoss.ganttz.data.GanttDate.CustomDate;
import org.zkoss.ganttz.data.GanttDate.ICases;
import org.zkoss.ganttz.data.GanttDate.LocalDateBased;
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

    private static GanttDate toGanttDate(long milliseconds) {
        return GanttDate.createFrom(new DateTime(milliseconds).toLocalDate());
    }

    private static long toMilliseconds(GanttDate date) {
        return date.byCases(new ICases<Long>() {

            @Override
            public Long on(LocalDateBased localDateBased) {
                return localDateBased.getLocalDate().toDateTimeAtStartOfDay()
                        .getMillis();
            }

            @Override
            public Long on(CustomDate customType) {
                throw new UnsupportedOperationException("no custom "
                        + GanttDate.class.getSimpleName() + " for "
                        + DefaultFundamentalProperties.class.getSimpleName());
            }
        });
    }

    @Override
    public GanttDate getBeginDate() {
        return toGanttDate(beginDate);
    }

    @Override
    public void setBeginDate(GanttDate beginDate) {
        this.beginDate = toMilliseconds(beginDate);
    }

    public long getLengthMilliseconds() {
        return lengthMilliseconds;
    }

    @Override
    public GanttDate getEndDate() {
        return toGanttDate(beginDate + getLengthMilliseconds());
    }

    @Override
    public void setEndDate(GanttDate endDate) {
        this.beginDate = toMilliseconds(endDate) - this.lengthMilliseconds;
    }

    @Override
    public void resizeTo(GanttDate endDate) {
        this.lengthMilliseconds = toMilliseconds(endDate) - beginDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public GanttDate getHoursAdvanceEndDate() {
        return GanttDate.createFrom(new Date(hoursAdvanceEndDate));
    }

    @Override
    public GanttDate getAdvanceEndDate() {
        return advanceEndDate != null ? GanttDate.createFrom(new Date(
                advanceEndDate.getTime()))
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
    public List<Constraint<GanttDate>> getStartConstraints() {
        return Collections.emptyList();
    }

    @Override
    public List<Constraint<GanttDate>> getEndConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void moveTo(GanttDate date) {
        setBeginDate(date);
    }

    @Override
    public Date getDeadline() {
        return null;
    }

    public boolean isSubcontracted() {
        return false;
    }

    public boolean isLimiting() {
        return false;
    }

    public boolean isLimitingAndHasDayAssignments() {
        return false;
    }

    @Override
    public boolean canBeExplicitlyResized() {
        return true;
    }

    public boolean hasConsolidations() {
        return false;
    }

    @Override
    public String getAssignedStatus() {
        return "unassigned";
    }

    @Override
    public boolean isFixed() {
        return false;
    }

    @Override
    public GanttDate getConsolidatedline() {
        return null;
    }

    public String updateTooltipText() {
        return null;
    }

    @Override
    public void setDeadline(Date date) {
    }

    @Override
    public List<Constraint<GanttDate>> getCurrentLengthConstraint() {
        return Collections.emptyList();
    }

    @Override
    public GanttDate getAdvanceEndDate(String progressType) {
        return null;
    }

    @Override
    public String updateTooltipText(String progressType) {
        return "";
    }

}
