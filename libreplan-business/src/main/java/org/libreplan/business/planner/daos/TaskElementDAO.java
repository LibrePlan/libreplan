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

package org.libreplan.business.planner.daos;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;
import org.libreplan.business.common.daos.GenericDAOHibernate;
import org.libreplan.business.planner.entities.ResourceAllocation;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.planner.entities.TaskGroup;
import org.libreplan.business.workingday.EffortDuration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Jacobo Aragunde Pérez <jaragunde@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class TaskElementDAO extends GenericDAOHibernate<TaskElement, Long>
        implements ITaskElementDAO {

    @SuppressWarnings("unchecked")
    @Override
    public List<TaskElement> findChildrenOf(TaskGroup each) {
        return getSession().createCriteria(TaskElement.class).add(
                Restrictions.eq("parent", each)).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TaskElement> listFilteredByDate(Date start, Date end) {
        Criteria criteria  = getSession().createCriteria(TaskElement.class);
        if(start != null) {
            criteria.add(Restrictions.ge("endDate.date",
                    LocalDate.fromDateFields(start)));
        }
        if(end != null) {
            criteria.add(Restrictions.le("startDate.date",
                    LocalDate.fromDateFields(end)));
        }
        return criteria.list();
    }

    private void updateSumOfAllocatedHours(TaskElement taskElement) {
        EffortDuration assignedEffort = EffortDuration.hours(0);
        EffortDuration oldAssignedEffort = taskElement.getSumOfAssignedEffort();
        for(ResourceAllocation<?> allocation : taskElement.getAllResourceAllocations()) {
            assignedEffort = assignedEffort.plus(allocation.getAssignedEffort());
        }
        if(assignedEffort.compareTo(oldAssignedEffort) != 0) {
            taskElement.setSumOfAssignedEffort(assignedEffort);
            updateSumOfAllocatedHoursToParent(taskElement.getParent(),
                    oldAssignedEffort, assignedEffort);
            updateSumOfAllocatedHoursToChildren(taskElement);
        }
    }

    private void updateSumOfAllocatedHoursToChildren(TaskElement parent) {
        if(parent instanceof TaskGroup) {
            for(TaskElement child : parent.getChildren()) {
                EffortDuration assignedEffort = EffortDuration.hours(0);
                EffortDuration oldAssignedEffort = child.getSumOfAssignedEffort();
                for(ResourceAllocation<?> allocation : child.getAllResourceAllocations()) {
                    assignedEffort = assignedEffort.plus(allocation.getAssignedEffort());
                }
                if(assignedEffort.compareTo(oldAssignedEffort) != 0) {
                    child.setSumOfAssignedEffort(assignedEffort);
                    updateSumOfAllocatedHoursToChildren(child);
                }
            }
        }
    }

    private void updateSumOfAllocatedHoursToParent(TaskGroup taskGroup,
            EffortDuration oldAssignedEffort, EffortDuration newAssignedEffort) {
        if (taskGroup != null) {
            if (!Hibernate.isInitialized(taskGroup)) {
                reattach(taskGroup);
            }
            if(newAssignedEffort.compareTo(oldAssignedEffort) < 0) {
                taskGroup.setSumOfAssignedEffort(taskGroup.getSumOfAssignedEffort().minus(
                        oldAssignedEffort.minus(newAssignedEffort)));
            }
            else {
                taskGroup.setSumOfAssignedEffort(taskGroup.getSumOfAssignedEffort().plus(
                        newAssignedEffort.minus(oldAssignedEffort)));
            }
            updateSumOfAllocatedHoursToParent(taskGroup.getParent(),
                    oldAssignedEffort, newAssignedEffort);
        }
    }

    @Override
    @Transactional
    public void save(TaskElement taskElement) {
        updateSumOfAllocatedHours(taskElement);
        super.save(taskElement);
    }

}
