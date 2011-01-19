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

import java.math.BigDecimal;
import java.util.SortedMap;

import org.joda.time.LocalDate;

/**
 * Interface to calculate the cost of a {@link Task}.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public interface ICostCalculator {

    public SortedMap<LocalDate, BigDecimal> getEstimatedCost(Task task);

    public SortedMap<LocalDate, BigDecimal> getAdvanceCost(Task task);

    public SortedMap<LocalDate, BigDecimal> getWorkReportCost(Task task);

    SortedMap<LocalDate, BigDecimal> getEstimatedCost(Task task,
            LocalDate filterStartDate, LocalDate filterEndDate);

    SortedMap<LocalDate, BigDecimal> getAdvanceCost(Task task,
            LocalDate filterStartDate, LocalDate filterEndDate);

}
