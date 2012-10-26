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

import org.hibernate.criterion.Restrictions;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.effortsummary.entities.EffortSummary;
import org.libreplan.business.resources.entities.Resource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EffortSummaryDAO extends GenericDAOHibernate<EffortSummary, Long>
        implements IEffortSummaryDAO {

    @Override
    public List<EffortSummary> list() {

        return list(EffortSummary.class);
    }

    @Override
    public EffortSummary listForResourceBetweenDates(Resource resource,
            LocalDate startDate, LocalDate endDate) {
        EffortSummary effort = (EffortSummary) getSession()
                .createCriteria(EffortSummary.class)
                .add(Restrictions.eq("resource", resource)).uniqueResult();
        int startDifference = Days.daysBetween(startDate.toDateMidnight(),
                effort.getStartDate().toDateMidnight()).getDays();
        int length = Days.daysBetween(startDate.toDateMidnight(),
                endDate.plusDays(1).toDateMidnight()).getDays();
        int[] newAssignedEffort = new int[length];
        System.arraycopy(effort.getAssignedEffort(),
                startDifference, newAssignedEffort, 0, length);
        int[] newAvailableEffort = new int[length];
        System.arraycopy(effort.getAvailableEffort(),
                startDifference, newAvailableEffort, 0, length);

        return EffortSummary.create(startDate, endDate, newAvailableEffort,
                newAssignedEffort, effort.getResource());
    }

    @Override
    public EffortSummary findForResource(Resource resource) {
        return (EffortSummary) getSession().createCriteria(EffortSummary.class)
                .add(Restrictions.eq("resource", resource)).uniqueResult();
    }

}
