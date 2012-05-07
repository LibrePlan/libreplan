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

package org.libreplan.business.planner.entities;

import java.math.BigDecimal;
import java.util.SortedMap;

import org.joda.time.LocalDate;
import org.libreplan.business.calendars.entities.AvailabilityTimeLine;

/**
 * @author Diego Pino García <dpino@igalia.com>
 *
 *         Utility class for calculating all 'Earned Value' indicators
 */
public interface ICompanyEarnedValueCalculator extends IEarnedValueCalculator {

    SortedMap<LocalDate, BigDecimal> calculateBudgetedCostWorkScheduled(
            AvailabilityTimeLine.Interval interval);

    SortedMap<LocalDate, BigDecimal> calculateActualCostWorkPerformed(
            AvailabilityTimeLine.Interval interval);

    SortedMap<LocalDate, BigDecimal> calculateBudgetedCostWorkPerformed(
            AvailabilityTimeLine.Interval interval);

}
