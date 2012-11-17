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

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.effortsummary.entities.EffortSummary;
import org.libreplan.business.planner.entities.Task;
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
    public void save(Set<EffortSummary> efforts) {
        for (EffortSummary effort : efforts) {
            save(effort);
        }
    }

    @Override
    public void saveOrUpdate(Set<EffortSummary> efforts) {
        for (EffortSummary effort : efforts) {
            EffortSummary repeatedEffort = findByResourceAndTask(
                    effort.getResource(), effort.getTask());
            if (repeatedEffort != null) {
                // remove the existing EffortSummary
                try {
                    remove(repeatedEffort.getId());
                } catch (InstanceNotFoundException e) {
                    // should never happen, we have just retrieved the instance
                    // from the DAO
                }
            }
            if (!effort.isGlobal()) {
                EffortSummary globalEffort =
                        findGlobalInformationForResource(effort.getResource());
                if (globalEffort == null) {
                    // this shouldn't happen, the global row should have been
                    // created before
                    // TODO: throw proper exception
                }
                if (repeatedEffort != null) {
                    // substract the deleted EffortSummary
                    globalEffort.substractAssignedEffort(repeatedEffort);
                    // FIXME: when we try to update the same object,
                    // repeatedEffort == effort so this substraction is not
                    // performed properly
                }
                // add the new EffortSummary
                globalEffort.addAssignedEffort(effort);
                save(globalEffort);
            }
            // if isGlobal, save operation will overwrite it; we don't need to
            // handle that case explicitly
            save(effort);
        }
    }

    @Override
    public EffortSummary listForResourceBetweenDates(Resource resource,
            LocalDate startDate, LocalDate endDate) {
        EffortSummary effort = (EffortSummary) getSession()
                .createCriteria(EffortSummary.class)
                .add(Restrictions.eq("resource", resource)).uniqueResult();

        return effort.getSubEffortSummary(startDate, endDate);
    }

    @Override
    public EffortSummary findGlobalInformationForResource(Resource resource) {
        return (EffortSummary) getSession().createCriteria(EffortSummary.class)
                .add(Restrictions.eq("resource", resource))
                .add(Restrictions.isNull("task")).uniqueResult();
    }

    @Override
    public EffortSummary findByResourceAndTask(Resource resource, Task task) {
        return (EffortSummary) getSession().createCriteria(EffortSummary.class)
                .add(Restrictions.eq("resource", resource))
                .add(Restrictions.eq("task", task)).uniqueResult();
    }

    @Override
    public List<EffortSummary> findByResource(Resource resource) {
        return (List<EffortSummary>) getSession()
                .createCriteria(EffortSummary.class)
                .add(Restrictions.eq("resource", resource))
                .addOrder(Order.asc("startDate")).list();
    }

}
