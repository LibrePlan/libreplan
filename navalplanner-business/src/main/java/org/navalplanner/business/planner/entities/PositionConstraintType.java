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
package org.navalplanner.business.planner.entities;

/**
 * Enum with all possible ways of calculating the start of a task <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public enum PositionConstraintType {
    AS_SOON_AS_POSSIBLE(false) {

        @Override
        public PositionConstraintType newTypeAfterMoved() {
            return START_NOT_EARLIER_THAN;
        }

        @Override
        public boolean appliesToTheStart() {
            return true;
        }
    },
    START_NOT_EARLIER_THAN(true) {

        @Override
        public PositionConstraintType newTypeAfterMoved() {
            return START_NOT_EARLIER_THAN;
        }

        @Override
        public boolean appliesToTheStart() {
            return true;
        }
    },
    START_IN_FIXED_DATE(true) {

        @Override
        public PositionConstraintType newTypeAfterMoved() {
            return START_NOT_EARLIER_THAN;
        }

        @Override
        public boolean appliesToTheStart() {
            return true;
        }
    },
    AS_LATE_AS_POSSIBLE(false) {

        @Override
        public PositionConstraintType newTypeAfterMoved() {
            return FINISH_NOT_LATER_THAN;
        }

        @Override
        public boolean appliesToTheStart() {
            return false;
        }
    },
    FINISH_NOT_LATER_THAN(true) {

        @Override
        public PositionConstraintType newTypeAfterMoved() {
            return FINISH_NOT_LATER_THAN;
        }

        @Override
        public boolean appliesToTheStart() {
            return false;
        }
    };

    private boolean dateRequired;

    private PositionConstraintType(boolean dateRequired) {
        this.dateRequired = dateRequired;
    }

    public abstract PositionConstraintType newTypeAfterMoved();

    public boolean isAssociatedDateRequired() {
        return dateRequired;
    }

    public abstract boolean appliesToTheStart();
}
