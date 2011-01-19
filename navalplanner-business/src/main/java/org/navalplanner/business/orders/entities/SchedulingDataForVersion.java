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
package org.navalplanner.business.orders.entities;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.orders.entities.SchedulingState.ITypeChangedListener;
import org.navalplanner.business.orders.entities.SchedulingState.Type;
import org.navalplanner.business.planner.entities.TaskElement;
import org.navalplanner.business.scenarios.entities.OrderVersion;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.navalplanner.business.util.deepcopy.DeepCopy;

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

        private SchedulingState.Type schedulingStateType;

        private final OrderVersion originOrderVersion;

        private boolean hasPendingChanges = false;

        private final Type initialSchedulingStateType;

        private Data(OrderVersion orderVersion,
                SchedulingDataForVersion version,
                TaskSource taskSource,
                Type schedulingStateType) {
            Validate.notNull(schedulingStateType);
            this.originOrderVersion = orderVersion;
            this.originVersion = version;
            this.taskSource = taskSource;
            this.schedulingStateType = schedulingStateType;
            this.initialSchedulingStateType = schedulingStateType;
        }

        public TaskSource getTaskSource() {
            return taskSource;
        }

        public SchedulingState.Type getSchedulingStateType() {
            return schedulingStateType;
        }

        private void setSchedulingStateType(
                SchedulingState.Type schedulingStateType) {
            this.schedulingStateType = schedulingStateType;
            hasPendingChanges = true;
        }

        private void setTaskSource(TaskSource taskSource) {
            hasPendingChanges = true;
            this.taskSource = taskSource;
        }

        public void initializeType(Type type) {
            if (getSchedulingStateType() != initialSchedulingStateType) {
                throw new IllegalStateException("already initialized");
            }
            this.setSchedulingStateType(type);
        }

        public ITypeChangedListener onTypeChangeListener() {
            return new ITypeChangedListener() {
                @Override
                public void typeChanged(Type newType) {
                    setSchedulingStateType(newType);
                }
            };
        }

        public void taskSourceRemovalRequested() {
            setTaskSource(null);
        }

        public void requestedCreationOf(TaskSource taskSource) {
            Validate.isTrue(this.getTaskSource() == null,
                    "there must be no task source");
            this.setTaskSource(taskSource);
        }

        public void replaceCurrentTaskSourceWith(TaskSource newTaskSource) {
            Validate.isTrue(this.getTaskSource() != null,
                    "there must be a task source to replace");
            this.setTaskSource(newTaskSource);
        }

        public SchedulingDataForVersion getVersion() {
            return originVersion;
        }

        public void writeSchedulingDataChanges() {
            this.originVersion.schedulingStateType = this.getSchedulingStateType();
            this.originVersion.taskSource = this.getTaskSource();
            hasPendingChanges = false;
        }

        public OrderVersion getOriginOrderVersion() {
            return originOrderVersion;
        }

        public boolean hasPendingChanges() {
            return hasPendingChanges;
        }

        public Data pointsTo(DeepCopy deepCopy, OrderVersion orderVersion,
                SchedulingDataForVersion schedulingVersion) {
            Validate.isTrue(!this.originVersion.equals(schedulingVersion));
            Data data = new Data(orderVersion, schedulingVersion, copy(
                    deepCopy, taskSource), schedulingStateType);
            data.hasPendingChanges = true;
            return data;
        }

        private static TaskSource copy(DeepCopy deepCopy, TaskSource taskSource) {
            return deepCopy.copy(taskSource);
        }

    }

    public static SchedulingDataForVersion createInitialFor(OrderElement orderElement) {
        Validate.notNull(orderElement);
        SchedulingDataForVersion schedulingDataForVersion = new SchedulingDataForVersion();
        schedulingDataForVersion.orderElement = orderElement;
        schedulingDataForVersion.schedulingStateType = defaultTypeFor(orderElement);
        return create(schedulingDataForVersion);
    }

    private static Type defaultTypeFor(OrderElement orderElement) {
        return orderElement.isLeaf() ? Type.SCHEDULING_POINT
                : Type.NO_SCHEDULED;
    }

    @NotNull
    private SchedulingState.Type schedulingStateType;

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

    void removeSpuriousDayAssignments(Scenario scenario) {
        TaskSource taskSource = getTaskSource();
        if (taskSource != null) {
            TaskElement task = taskSource.getTask();
            task.removeDayAssignmentsFor(scenario);
        }
    }

}
