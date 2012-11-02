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

package org.libreplan.business.effortsummary.daos;

import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.libreplan.business.common.daos.IGenericDAO;
import org.libreplan.business.effortsummary.entities.EffortSummary;
import org.libreplan.business.resources.entities.Resource;

public interface IEffortSummaryDAO extends IGenericDAO<EffortSummary, Long> {

    List<EffortSummary> list();

    void save(Set<EffortSummary> efforts);

    EffortSummary listForResourceBetweenDates(Resource resource,
            LocalDate startDate, LocalDate endDate);

    /**
     * Find the EffortSummary row containing the global information about a
     * specific resource. Take into account that there is also one row per
     * allocation of that resource to a task.
     *
     * @param resource
     *            The resource to search by.
     * @return The EffortSummary object corresponding to the resource or null if
     *         it doesn't exist yet.
     */
    EffortSummary findGlobalInformationForResource(Resource resource);
}
