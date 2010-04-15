/*
 * This file is part of NavalPlan
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

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.orders.entities.SchedulingState.Type;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class SchedulingStateForVersion extends BaseEntity {

    public static SchedulingStateForVersion create(OrderElement orderElement) {
        Validate.notNull(orderElement);
        SchedulingStateForVersion schedulingStateForVersion = new SchedulingStateForVersion();
        schedulingStateForVersion.orderElement = orderElement;
        return create(schedulingStateForVersion);
    }

    @NotNull
    private SchedulingState.Type schedulingStateType = Type.NO_SCHEDULED;

    @NotNull
    private OrderElement orderElement;

    private TaskSource taskSource;

    public SchedulingState.Type getSchedulingStateType() {
        return schedulingStateType;
    }

    public TaskSource getTaskSource() {
        return taskSource;
    }

    public OrderElement getOrderElement() {
        return orderElement;
    }

}
