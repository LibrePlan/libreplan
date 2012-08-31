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

package org.libreplan.business.cashflow.entities;

import static org.libreplan.business.i18n.I18nHelper._;

/**
 * Represents the different types of {@link CashflowPlan CashflowPlans}.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public enum CashflowType {

    MANUAL(
            _("Manual"),
            _("Cashflow outputs are defined manually by the user")),
    DEFERRED_PAYMENT(
            _("Defferred payment"),
            _("Cashflow outputs are defined automatically within the configured days taken into account the task expenses"));

    private String name;

    private String description;

    private CashflowType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}