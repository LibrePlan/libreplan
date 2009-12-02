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

package org.navalplanner.business.resources.daos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.Query;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hibernate DAO for the <code>Resource</code> entity.
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Diego Pino Garcia <dpino@udc.es>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Transactional
public class ResourceDAO extends GenericDAOHibernate<Resource, Long> implements
        IResourceDAO {

    @Override
    public List<Worker> getWorkers() {
        return list(Worker.class);
    }

    @Override
    public List<Resource> findAllSatisfyingCriterions(
            Collection<? extends Criterion> criterions) {
        Validate.notNull(criterions);
        Validate.noNullElements(criterions);
        if (criterions.isEmpty()) {
            return list(Resource.class);
        }
        return selectSatisfiyingAllCriterions(
                findRelatedWithSomeOfTheCriterions(criterions), criterions);
    }

    @SuppressWarnings("unchecked")
    private List<Resource> findRelatedWithSomeOfTheCriterions(
            Collection<? extends Criterion> criterions) {
        String strQuery = "SELECT DISTINCT resource "
                + "FROM Resource resource "
                + "LEFT OUTER JOIN resource.criterionSatisfactions criterionSatisfactions "
                + "LEFT OUTER JOIN criterionSatisfactions.criterion criterion "
                + "WHERE criterion IN (:criterions)";
        Query query = getSession().createQuery(strQuery);
        query.setParameterList("criterions", criterions);
        return (List<Resource>) query.list();
    }

    private List<Resource> selectSatisfiyingAllCriterions(
            List<Resource> resources,
            Collection<? extends Criterion> criterions) {
        List<Resource> result = new ArrayList<Resource>();
        for (Resource each : resources) {
            if (each.satisfiesCriterions(criterions)) {
                result.add(each);
            }
        }
        return result;
    }


    @Override
    public List<Resource> findResourcesRelatedTo(List<Task> taskElements) {
        if (taskElements.isEmpty()) {
            return new ArrayList<Resource>();
        }
        Set<Resource> result = new LinkedHashSet<Resource>();
        result.addAll(findRelatedToSpecific(taskElements));
        result.addAll(findRelatedToGeneric(taskElements));
        return new ArrayList<Resource>(result);
    }

    @SuppressWarnings("unchecked")
    private List<Resource> findRelatedToGeneric(List<Task> taskElements) {
        String query = "SELECT DISTINCT resource FROM GenericResourceAllocation generic"
                + " JOIN generic.genericDayAssignments dayAssignment"
                + " JOIN dayAssignment.resource resource"
                + " WHERE generic.task IN(:taskElements)";
        return getSession().createQuery(query)
                .setParameterList("taskElements",
                taskElements).list();
    }

    @SuppressWarnings("unchecked")
    private List<Resource> findRelatedToSpecific(List<Task> taskElements) {
        List<Resource> list = getSession()
                .createQuery(
                        "SELECT DISTINCT specificAllocation.resource FROM SpecificResourceAllocation specificAllocation "
                                + " WHERE specificAllocation.task IN(:taskElements)")
                .setParameterList(
                "taskElements",
                taskElements).list();
        return list;
    }

}
