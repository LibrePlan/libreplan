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

package org.navalplanner.business.planner.entities;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.AssertTrue;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.workingday.IntraDayDate;

/**
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Javier Moran Rua <jmoran@igalia.com>
 */
public class TaskMilestone extends TaskElement implements ITaskPositionConstrained {

    public static TaskMilestone create(Date initialDate) {
        Validate.notNull(initialDate);
        TaskMilestone milestone = new TaskMilestone();
        milestone.setStartDate(initialDate);
        milestone.setEndDate(initialDate);
        return createWithoutTaskSource(milestone);
    }

    private CalculatedValue calculatedValue = CalculatedValue.END_DATE;

    private TaskPositionConstraint startConstraint = new TaskPositionConstraint();

    /**
     * Constructor for hibernate. Do not use!
     */
    public TaskMilestone() {

    }

    public Set<ResourceAllocation<?>> getSatisfiedResourceAllocations() {
        return Collections.emptySet();
    }

    @Override
    public Set<ResourceAllocation<?>> getAllResourceAllocations() {
        return Collections.emptySet();
    }

    public int getAssignedHours() {
        return 0;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public List<TaskElement> getChildren() {
        throw new UnsupportedOperationException();
    }

    public CalculatedValue getCalculatedValue() {
        if (calculatedValue == null) {
            return CalculatedValue.END_DATE;
        }
        return calculatedValue;
    }

    public void setCalculatedValue(CalculatedValue calculatedValue) {
        Validate.notNull(calculatedValue);
        this.calculatedValue = calculatedValue;
    }

    public void setDaysDuration(Integer duration) {
        Validate.notNull(duration);
        Validate.isTrue(duration >= 0);
        DateTime endDate = toDateTime(getStartDate()).plusDays(duration);
        setEndDate(endDate.toDate());
    }

    public Integer getDaysDuration() {
        Days daysBetween = Days.daysBetween(toDateTime(getStartDate()),
                toDateTime(getEndDate()));
        return daysBetween.getDays();
    }

    private DateTime toDateTime(Date startDate) {
        return new DateTime(startDate.getTime());
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "order element associated to a milestone must be null")
    private boolean theOrderElementMustBeNull() {
        return getOrderElement() == null;
    }

    @Override
    protected IDatesHandler createDatesHandler(Scenario scenario, IResourceDAO resourceDAO) {
        return new IDatesHandler() {

            @Override
            public void moveTo(IntraDayDate newStartDate) {
                setIntraDayStartDate(newStartDate);
                setIntraDayEndDate(newStartDate);
            }

            @Override
            public void resizeTo(IntraDayDate endDate) {
                moveTo(endDate);
            }

            @Override
            public void moveEndTo(IntraDayDate newEnd) {
                moveTo(newEnd);
            }
        };
    }

    @Override
    protected void initializeDates() {
        // do nothing
    }

    @Override
    protected boolean canBeResized() {
        return false;
    }

    @Override
    public boolean canBeExplicitlyResized() {
        return false;
    }

    @Override
    public boolean isMilestone() {
        return true;
    }

    @Override
    public boolean hasLimitedResourceAllocation() {
        return false;
    }

    public void explicityMoved(LocalDate date) {
        getPositionConstraint().explicityMovedTo(date);
    }

    public TaskPositionConstraint getPositionConstraint() {
        if (startConstraint == null) {
            startConstraint = new TaskPositionConstraint();
        }
        return startConstraint;
    }

}