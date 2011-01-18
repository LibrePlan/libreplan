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

package org.navalplanner.business.calendars.entities;

import static org.navalplanner.business.i18n.I18nHelper._;

import java.util.EnumMap;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.calendars.daos.ICalendarExceptionTypeDAO;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.workingday.EffortDuration;
import org.navalplanner.business.workingday.EffortDuration.Granularity;

/**
 * Type of an exception day.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CalendarExceptionType extends IntegrationEntity {

    private String name;

    private String color;

    private Capacity capacity = Capacity.zero();

    public static CalendarExceptionType create() {
        return create(new CalendarExceptionType());
    }

    public static CalendarExceptionType create(String name, String color,
            Boolean notAssignable) {
        return create(new CalendarExceptionType(name, color, notAssignable));
    }

    public static CalendarExceptionType create(String code, String name,
            String color, Boolean notAssignable) {
        return create(new CalendarExceptionType(name, color, notAssignable),
                code);
    }

    public static CalendarExceptionType create(String code, String name,
            String color, Boolean notAssignable, EffortDuration duration) {
        CalendarExceptionType calendarExceptionType = new CalendarExceptionType(
                name, color, notAssignable);
        calendarExceptionType.setDuration(duration);
        return create(calendarExceptionType, code);
    }

    /**
     * Constructor for hibernate. Do not use!
     */
    protected CalendarExceptionType() {

    }

    public CalendarExceptionType(String name, String color,
            Boolean notOverAssignable) {
        this.name = name;
        this.color = color;
        this.capacity = Capacity.zero();
        this.capacity = this.capacity.overAssignableWithoutLimit(!BooleanUtils
                .isTrue(notOverAssignable));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @NotNull
    public Capacity getCapacity() {
        return capacity;
    }

    public void setCapacity(Capacity capacity) {
        Validate.notNull(capacity);
        this.capacity = capacity;
    }

    /**
     * @return If more hours can be assigned on this day.
     */
    public boolean isOverAssignableWithoutLimit() {
        return capacity.isOverAssignableWithoutLimit();
    }

    public void setOverAssignable(Boolean overAssignable) {
        this.capacity = capacity.overAssignableWithoutLimit(BooleanUtils
                .isTrue(overAssignable));
    }

    public String getOverAssignableStr() {
        return isOverAssignableWithoutLimit() ? _("Yes") : _("No");
    }

    public EffortDuration getDuration() {
        return capacity.getStandardEffort();
    }

    private String asString(EffortDuration duration) {
        if (duration == null) {
            return "";
        }
        EnumMap<Granularity, Integer> values = duration.decompose();
        Integer hours = values.get(Granularity.HOURS);
        Integer minutes = values.get(Granularity.MINUTES);
        Integer seconds = values.get(Granularity.SECONDS);
        return hours + ":" + minutes + ":" + seconds ;
    }

    public void setDuration(EffortDuration duration) {
        this.capacity = this.capacity.withStandardEffort(duration);
    }

    @Override
    protected ICalendarExceptionTypeDAO getIntegrationEntityDAO() {
        return Registry.getCalendarExceptionTypeDAO();
    }

    @AssertTrue(message = "name is already used")
    public boolean checkConstraintUniqueName() {
        if (StringUtils.isBlank(name)) {
            return true;
        }

        ICalendarExceptionTypeDAO calendarExceptionTypeDAO = getIntegrationEntityDAO();
        if (isNewObject()) {
            return !calendarExceptionTypeDAO.existsByNameAnotherTransaction(
                    name);
        } else {
            try {
                CalendarExceptionType calendarExceptionType = calendarExceptionTypeDAO.findByName(name);
                return calendarExceptionType.getId().equals(getId());
            } catch (InstanceNotFoundException e) {
                return true;
            }
        }
    }

}
