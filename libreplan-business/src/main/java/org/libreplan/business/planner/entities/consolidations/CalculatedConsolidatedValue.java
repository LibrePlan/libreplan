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

package org.libreplan.business.planner.entities.consolidations;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.libreplan.business.util.deepcopy.DeepCopy;
import org.libreplan.business.workingday.IntraDayDate;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class CalculatedConsolidatedValue extends ConsolidatedValue {

    private CalculatedConsolidation consolidation;

    public static CalculatedConsolidatedValue create() {
        return create(new CalculatedConsolidatedValue());
    }

    public static CalculatedConsolidatedValue create(LocalDate date,
            BigDecimal value, IntraDayDate taskEndDate) {
        return create(new CalculatedConsolidatedValue(date, value, taskEndDate));
    }

    protected CalculatedConsolidatedValue(LocalDate date, BigDecimal value,
            IntraDayDate taskEndDate) {
        super(date, value, taskEndDate);
    }

    /**
     * Constructor for {@link DeepCopy}. DO NOT USE!
     */
    public CalculatedConsolidatedValue() {
    }

    public void setConsolidation(CalculatedConsolidation consolidation) {
        this.consolidation = consolidation;
    }

    public CalculatedConsolidation getConsolidation() {
        return consolidation;
    }

    @Override
    public boolean isCalculated() {
        return true;
    }
}
