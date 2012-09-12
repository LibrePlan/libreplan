/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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
package org.libreplan.business.templates.entities;

import static org.libreplan.business.i18n.I18nHelper._;

/**
 * Types of budget line.
 *
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 *
 */
public enum BudgetLineTypeEnum {

    PRICE_PER_SESSION(_("price per session")),
    PRICE_PER_DAY(_("price per day")),
    PRICE_PER_UNIT(_("price per unit")),
    SALARY_PER_SESSION(_("salary per session")) {
        @Override
        public boolean isRelatedToSalary() {
            return true;
        }
    },
    SALARY_PER_DAY(_("salary per day")) {
        @Override
        public boolean isRelatedToSalary() {
            return true;
        }
    },
    TOTAL_SALARY(_("total salary")) {
        @Override
        public boolean isRelatedToSalary() {
            return true;
        }
    };

    private String description;

    private BudgetLineTypeEnum(String description) {
        this.description = description;
    }

    public String toString() {
        return this.description;
    }

    public boolean isRelatedToSalary() {
        return false;
    }

}
