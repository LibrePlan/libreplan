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

package org.navalplanner.business.calendars.entities;

import org.navalplanner.business.common.BaseEntity;

/**
 * Type of an exception day.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class CalendarExceptionType extends BaseEntity {

    public static CalendarExceptionType create(String name, String color,
            Boolean notAssignable) {
        return create(new CalendarExceptionType(name, color, notAssignable));
    }

    private String name;
    private String color;
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

    public Boolean isNotAssignable() {
        return notAssignable;
    }

}
