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
import org.hibernate.validator.Valid;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.orders.entities.SchedulingState.ITypeChangedListener;
import org.navalplanner.business.orders.entities.SchedulingState.Type;
import org.navalplanner.business.scenarios.entities.OrderVersion;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class SchedulingDataForVersion extends BaseEntity {

    public static class Data {

        private static Data from(SchedulingDataForVersion version,
                OrderVersion orderVersion) {
            return new Data(orderVersion, version, version
                    .getTaskSource(), version.getSchedulingStateType());
        }

        private SchedulingDataForVersion originVersion;

        private TaskSource taskSource;

        private SchedulingState.Type schedulingStateType = Type.NO_SCHEDULED;

        private final OrderVersion originOrderVersion;

        private Data(OrderVersion orderVersion,
                SchedulingDataForVersion version,
                TaskSource taskSource,
                Type schedulingStateType) {
            Validate.notNull(schedulingStateType);
            this.originOrderVersion = orderVersion;
            this.originVersion = version;
            this.taskSource = taskSource;
            this.schedulingStateType = schedulingStateType;
        }

        public TaskSource getTaskSource() {
            return taskSource;
        }

        public SchedulingState.Type getSchedulingStateType() {
            return schedulingStateType;
        }

        public void initializeType(Type type) {
            if (schedulingStateType != Type.NO_SCHEDULED) {
                throw new IllegalStateException("already initialized");
            }
            this.schedulingStateType = type;
        }

        public ITypeChangedListener onTypeChangeListener() {
            return new ITypeChangedListener() {
                @Override
                public void typeChanged(Type newType) {
                    schedulingStateType = newType;
                }
            };
        }

        public void taskSourceRemovalRequested() {
            taskSource = null;
        }

        public void requestedCreationOf(TaskSource taskSource) {
            Validate.isTrue(this.taskSource == null,
                    "there must be no task source");
            this.taskSource = taskSource;
        }

        public void replaceCurrentTaskSourceWith(TaskSource newTaskSource) {
            Validate.isTrue(this.taskSource != null,
                    "there must be a task source to replace");
            this.taskSource = newTaskSource;
        }

        public SchedulingDataForVersion getVersion() {
            return originVersion;
        }

        public void writeSchedulingStateChanges() {
            this.originVersion.schedulingStateType = this.schedulingStateType;
            this.originVersion.taskSource = this.taskSource;
        }

        public OrderVersion getOriginOrderVersion() {
            return originOrderVersion;
        }
    }

    public static SchedulingDataForVersion createInitialFor(OrderElement orderElement) {
        Validate.notNull(orderElement);
        SchedulingDataForVersion schedulingDataForVersion = new SchedulingDataForVersion();
        schedulingDataForVersion.orderElement = orderElement;
        return create(schedulingDataForVersion);
    }

    @NotNull
    private SchedulingState.Type schedulingStateType = Type.NO_SCHEDULED;

    @NotNull
    private OrderElement orderElement;

    private TaskSource taskSource;

    public SchedulingState.Type getSchedulingStateType() {
        return schedulingStateType;
    }

    @Valid
    public TaskSource getTaskSource() {
        return taskSource;
    }

    public OrderElement getOrderElement() {
        return orderElement;
    }

    public Data makeAvailableFor(OrderVersion orderVersion) {
        return Data.from(this, orderVersion);
    }

}
