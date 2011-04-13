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

import static org.navalplanner.business.i18n.I18nHelper._;

import org.navalplanner.business.common.BaseEntity;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class AssignmentFunction extends BaseEntity {

    public static AssignmentFunction create() {
        return create(new AssignmentFunction());
    }

    public AssignmentFunction() {

    }

    /**
     * This method applies the function to the received resourceAllocation
     * <i>This method is intended to be overridden by subclasses</i>
     * @param resourceAllocation
     */
    public void applyTo(ResourceAllocation<?> resourceAllocation) {
        // override at subclasses
    }

    public String getName() {
        // override at subclasses
        return null;
    }

    public enum ASSIGNMENT_FUNCTION_NAME {
        NONE(_("None")),
        STRETCHES(_("Stretches")),
        INTERPOLATION(_("Interporlation")),
        SIGMOID(_("Sigmoid"));

        private String name;

        private ASSIGNMENT_FUNCTION_NAME(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

}
