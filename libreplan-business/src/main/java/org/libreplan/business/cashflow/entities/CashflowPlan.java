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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.validator.NotNull;
import org.hibernate.validator.Valid;
import org.joda.time.LocalDate;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.planner.entities.TaskElement;

/**
 * Represents the cashflow plan for a {@link TaskElement}.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
public class CashflowPlan extends BaseEntity {

    private CashflowType type = CashflowType.MANUAL;

    private List<CashflowOutput> outputs = new ArrayList<CashflowOutput>();

    public static CashflowPlan create() {
        return create(new CashflowPlan());
    }

    /**
     * Default constructor for Hibernate. Do not use!
     */
    protected CashflowPlan() {
    }

    @NotNull(message = "type not specified")
    public CashflowType getType() {
        return type;
    }

    public void setType(CashflowType type) {
        this.type = type;
    }

    @Valid
    public List<CashflowOutput> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public void addOutput(LocalDate date, BigDecimal amount) {
        outputs.add(new CashflowOutput(date, amount));
    }

    public void removeOutput(LocalDate date, BigDecimal amount) {
        int index = indexOfOutput(date, amount);
        if (index >= 0) {
            outputs.remove(index);
        }
    }

    private int indexOfOutput(LocalDate date, BigDecimal amount) {
        int i = 0;
        for (CashflowOutput output : outputs) {
            if (output.getDate().equals(date)
                    && output.getAmount().equals(amount)) {
                return i;
            }
            i++;
        }
        return -1;
    }

}