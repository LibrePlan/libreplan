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
package org.navalplanner.business.planner.entities;

/**
 * Enum with all possible ways of calculating the start of a task <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public enum StartConstraintType {
    AS_SOON_AS_POSSIBLE(false) {
        @Override
        public StartConstraintType newTypeAfterMoved() {
            return START_NOT_EARLIER_THAN;
        }
    },
    START_NOT_EARLIER_THAN(true) {
        @Override
        public StartConstraintType newTypeAfterMoved() {
            return START_NOT_EARLIER_THAN;
        }
    },
    START_IN_FIXED_DATE(true) {
        @Override
        public StartConstraintType newTypeAfterMoved() {
            return START_NOT_EARLIER_THAN;
        }
    },
    AS_LATE_AS_POSSIBLE(false) {
        @Override
        public StartConstraintType newTypeAfterMoved() {
            return FINISH_NOT_LATER_THAN;
        }
    },
    FINISH_NOT_LATER_THAN(true) {
        @Override
        public StartConstraintType newTypeAfterMoved() {
            return FINISH_NOT_LATER_THAN;
        }
    };

    private boolean dateRequired;

    private StartConstraintType(boolean dateRequired) {
        this.dateRequired = dateRequired;
    }

    public abstract StartConstraintType newTypeAfterMoved();

    public boolean isAssociatedDateRequired() {
        return dateRequired;
    }

}
