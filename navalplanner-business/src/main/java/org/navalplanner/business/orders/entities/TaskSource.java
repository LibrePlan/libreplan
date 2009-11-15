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
package org.navalplanner.business.orders.entities;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TaskSource extends BaseEntity {

    public static TaskSource withHoursGroupOf(OrderElement orderElement) {
        return create(new TaskSource(orderElement));
    }

    @NotNull
    private OrderElement orderElement;

    private Set<HoursGroup> hoursGroups = new HashSet<HoursGroup>();

    public TaskSource() {
    }

    public TaskSource(OrderElement orderElement) {
        Validate.notNull(orderElement);
        this.orderElement = orderElement;
        this.hoursGroups = new HashSet<HoursGroup>(orderElement
                .getHoursGroups());
    }

    public void added(HoursGroup hoursGroup) {
        hoursGroups.add(hoursGroup);
    }

    public void removed(HoursGroup hoursGroup) {
        hoursGroups.remove(hoursGroup);
    }

}
