/*
 * This file is part of ###PROJECT_NAME###
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

package org.navalplanner.business.planner.daos;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.planner.entities.DayAssignment;
import org.navalplanner.business.planner.entities.GenericDayAssignment;
import org.navalplanner.business.planner.entities.SpecificDayAssignment;
import org.navalplanner.business.planner.entities.TaskElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class TaskElementDAO extends GenericDAOHibernate<TaskElement, Long>
        implements ITaskElementDAO {

    @Override
    @Transactional
    public void removeOrphanedDayAssignments() {
        deleteAll(getOrphanedDayAssignments());
    }

    private void deleteAll(List<DayAssignment> orphaned) {
        for (DayAssignment dayAssignment : orphaned) {
            getSession().delete(dayAssignment);
        }
    }

    private List<DayAssignment> getOrphanedDayAssignments() {
        List<DayAssignment> orphaned = new ArrayList<DayAssignment>();
        orphaned.addAll(findOrphanedGenericDayAssignments());
        orphaned.addAll(findOrphanedSpecificDayAssignments());
        return orphaned;
    }

    @SuppressWarnings("unchecked")
    private List<GenericDayAssignment> findOrphanedGenericDayAssignments() {
        return getSession().createCriteria(GenericDayAssignment.class).add(
                Restrictions.isNull("genericResourceAllocation")).list();
    }

    @SuppressWarnings("unchecked")
    private List<SpecificDayAssignment> findOrphanedSpecificDayAssignments() {
        return getSession().createCriteria(SpecificDayAssignment.class).add(
                Restrictions.isNull("specificResourceAllocation")).list();
    }
}
