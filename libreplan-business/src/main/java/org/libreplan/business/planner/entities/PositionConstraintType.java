/*
 * This file is part of LibrePlan
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
package org.libreplan.business.planner.entities;

import org.libreplan.business.orders.entities.Order.SchedulingMode;


/**
 * Enum with all possible ways of calculating the start of a task <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public enum PositionConstraintType {
    AS_SOON_AS_POSSIBLE(false, _("as soon as possible")) {

        @Override
        public PositionConstraintType newTypeAfterMoved(SchedulingMode mode) {
            return START_NOT_EARLIER_THAN;
        }

        @Override
        public boolean appliesToTheStart() {
            return true;
        }
    },
    START_NOT_EARLIER_THAN(true, _("start not earlier than")) {

        @Override
        public PositionConstraintType newTypeAfterMoved(SchedulingMode mode) {
            if(mode == SchedulingMode.FORWARD)
                return START_NOT_EARLIER_THAN;
            else
                return FINISH_NOT_LATER_THAN;
        }

        @Override
        public boolean appliesToTheStart() {
            return true;
        }
    },
    START_IN_FIXED_DATE(true, _("start in fixed date")) {

        @Override
        public PositionConstraintType newTypeAfterMoved(SchedulingMode mode) {
            return START_IN_FIXED_DATE;
        }

        @Override
        public boolean appliesToTheStart() {
            return true;
        }
    },
    AS_LATE_AS_POSSIBLE(false, _("as late as possible")) {

        @Override
        public PositionConstraintType newTypeAfterMoved(SchedulingMode mode) {
            return FINISH_NOT_LATER_THAN;
        }

        @Override
        public boolean appliesToTheStart() {
            return false;
        }
    },
    FINISH_NOT_LATER_THAN(true, _("finish not later than")) {

        @Override
        public PositionConstraintType newTypeAfterMoved(SchedulingMode mode) {
            if(mode == SchedulingMode.FORWARD)
                return START_NOT_EARLIER_THAN;
            else
                return FINISH_NOT_LATER_THAN;
        }

        @Override
        public boolean appliesToTheStart() {
            return false;
        }
    };

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    private boolean dateRequired;
    private String name;

    private PositionConstraintType(boolean dateRequired, String name) {
        this.dateRequired = dateRequired;
        this.name = name;
    }

    public abstract PositionConstraintType newTypeAfterMoved(SchedulingMode mode);

    public boolean isAssociatedDateRequired() {
        return dateRequired;
    }

    public String getName() {
        return name;
    }

    public abstract boolean appliesToTheStart();
}
