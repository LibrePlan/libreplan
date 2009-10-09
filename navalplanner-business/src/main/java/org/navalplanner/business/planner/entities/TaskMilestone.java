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

package org.navalplanner.business.planner.entities;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.navalplanner.business.common.IValidable;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;

/**
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class TaskMilestone extends TaskElement implements IValidable {

    public static TaskMilestone create() {
        TaskMilestone milestone = new TaskMilestone();
        milestone.setNewObject(true);
        return milestone;
    }

    @NotNull
    private HoursGroup hoursGroup;

    private CalculatedValue calculatedValue = CalculatedValue.END_DATE;

    private Set<ResourceAllocation<?>> resourceAllocations = new HashSet<ResourceAllocation<?>>();

    /**
     * Constructor for hibernate. Do not use!
     */
    public TaskMilestone() {

    }

    public Set<ResourceAllocation<?>> getResourceAllocations() {
        return Collections.unmodifiableSet(resourceAllocations);
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


    @Override
    public void setOrderElement(OrderElement orderElement)
            throws IllegalStateException {
        throw new IllegalStateException(
                "milestones can't have orderElements associated");
    }

    @Override
    public Integer defaultWorkHours() {
        return new Integer(0);
    }

    @Override
    public void checkValid() throws ValidationException {
        if (super.getOrderElement() != null) {
            throw new ValidationException("Not null milestone orderelement");
        }

    }

}