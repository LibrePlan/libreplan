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

import org.apache.commons.lang.BooleanUtils;
import org.navalplanner.business.calendars.daos.ICalendarExceptionTypeDAO;
import org.navalplanner.business.common.IntegrationEntity;
import org.navalplanner.business.common.Registry;

/**
 * Type of an exception day.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CalendarExceptionType extends IntegrationEntity {

    public static CalendarExceptionType create(String name, String color,
            Boolean notAssignable) {
        return create(new CalendarExceptionType(name, color, notAssignable));
    }

    public static CalendarExceptionType create(String code, String name,
            String color, Boolean notAssignable) {
        return create(new CalendarExceptionType(name, color, notAssignable),
                code);
    }

    private String name;
    private String color;

    // Beware. Not Assignable was intended to mean not over assignable. This
    // name is kept in order to not break legacy data
    private Boolean notAssignable;

    /**
     * Constructor for hibernate. Do not use!
     */
    public CalendarExceptionType() {
    }

    public CalendarExceptionType(String name, String color,
            Boolean notAssignable) {
        this.name = name;
        this.color = color;
        this.notAssignable = notAssignable;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    /**
     * @return If more hours can be assigned on this day.
     */
    public boolean isOverAssignable() {
        return BooleanUtils.isFalse(notAssignable);
    }

    @Override
    protected ICalendarExceptionTypeDAO getIntegrationEntityDAO() {
        return Registry.getCalendarExceptionTypeDAO();
    }
}
