/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.planner.entities.consolidations;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.navalplanner.business.common.BaseEntity;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public abstract class ConsolidatedValue extends BaseEntity {

    private LocalDate date;
    private BigDecimal value;

    public abstract boolean isCalculated();

    protected ConsolidatedValue() {

    }

    protected ConsolidatedValue(
            LocalDate date,
 BigDecimal value) {
        this.date = date;
        this.value = value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

}
