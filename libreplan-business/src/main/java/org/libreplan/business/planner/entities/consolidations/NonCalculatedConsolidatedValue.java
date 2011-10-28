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
import org.libreplan.business.advance.entities.AdvanceMeasurement;
import org.libreplan.business.util.deepcopy.DeepCopy;
import org.libreplan.business.util.deepcopy.OnCopy;
import org.libreplan.business.util.deepcopy.Strategy;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class NonCalculatedConsolidatedValue extends ConsolidatedValue {

    private NonCalculatedConsolidation consolidation;

    @OnCopy(Strategy.SHARE)
    private AdvanceMeasurement advanceMeasurement;

    public static NonCalculatedConsolidatedValue create() {
        return create(new NonCalculatedConsolidatedValue());
    }

    public static NonCalculatedConsolidatedValue create(LocalDate date,
            BigDecimal value, LocalDate taskEndDate) {
        return create(new NonCalculatedConsolidatedValue(date, value,
                taskEndDate));
    }

    public static NonCalculatedConsolidatedValue create(LocalDate date,
            BigDecimal value, AdvanceMeasurement advanceMeasurement,
            LocalDate taskEndDate) {
        return create(new NonCalculatedConsolidatedValue(date, value,
                advanceMeasurement, taskEndDate));
    }

    protected NonCalculatedConsolidatedValue(LocalDate date, BigDecimal value,
            AdvanceMeasurement advanceMeasurement, LocalDate taskEndDate) {
        this(date, value, taskEndDate);
        this.advanceMeasurement = advanceMeasurement;
    }

    protected NonCalculatedConsolidatedValue(LocalDate date, BigDecimal value,
            LocalDate taskEndDate) {
        super(date, value, taskEndDate);
    }

    /**
     * Constructor for {@link DeepCopy}. DO NOT USE!
     */
    public NonCalculatedConsolidatedValue() {
    }

    public void setAdvanceMeasurement(AdvanceMeasurement advanceMeasurement) {
        this.advanceMeasurement = advanceMeasurement;
    }

    public AdvanceMeasurement getAdvanceMeasurement() {
        return advanceMeasurement;
    }

    public void setConsolidation(NonCalculatedConsolidation consolidation) {
        this.consolidation = consolidation;
    }

    public NonCalculatedConsolidation getConsolidation() {
        return consolidation;
    }

    @Override
    public boolean isCalculated() {
        return false;
    }
}
