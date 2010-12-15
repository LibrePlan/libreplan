/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.IntegrationEntityDAO;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.reports.dtos.HoursWorkedPerResourceDTO;
import org.navalplanner.business.reports.dtos.HoursWorkedPerWorkerInAMonthDTO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.LimitingResourceQueue;
import org.navalplanner.business.resources.entities.Machine;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.ResourceType;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.scenarios.IScenarioManager;
import org.navalplanner.business.scenarios.entities.Scenario;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ResourceDAO extends IntegrationEntityDAO<Resource> implements
    IResourceDAO {

    @Autowired
    private IScenarioManager scenarioManager;

    @Override
    public List<Worker> getWorkers() {
        return list(Worker.class);
    }

    @Override
    public List<Worker> getVirtualWorkers() {
        List<Worker> list = getWorkers();
        for (Iterator<Worker> iterator = list.iterator(); iterator.hasNext();) {
            Worker worker = iterator.next();
            if (worker.isReal()) {
                iterator.remove();
            }
        }
        return list;
    }

    @Override
    public List<Worker> getRealWorkers() {
        List<Worker> list = getWorkers();
        for (Iterator<Worker> iterator = list.iterator(); iterator.hasNext();) {
            Worker worker = iterator.next();
            if (worker.isVirtual()) {
                iterator.remove();
            }
        }
        return list;
    }

    @Override
    public List<Resource> findSatisfyingAllCriterionsAtSomePoint(
            Collection<? extends Criterion> criterions) {
        Validate.notNull(criterions);
        Validate.noNullElements(criterions);
        if (criterions.isEmpty()) {
            return list(Resource.class);
        }
        return selectSatisfiyingAllAtSomePoint(
                findRelatedWithSomeOfTheCriterions(criterions), criterions);
    }

    private List<Resource> selectSatisfiyingAllAtSomePoint(
            List<Resource> resources,
            Collection<? extends Criterion> criterions) {
        List<Resource> result = new ArrayList<Resource>();
        for (Resource each : resources) {
            if (each.satisfiesCriterionsAtSomePoint(criterions)) {
                result.add(each);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Resource> findRelatedWithSomeOfTheCriterions(
            Collection<? extends Criterion> criterions) {
        String strQuery = "SELECT DISTINCT resource "
                + "FROM Resource resource "
                + "JOIN resource.criterionSatisfactions criterionSatisfactions "
                + "JOIN criterionSatisfactions.criterion criterion "
                + "WHERE criterion IN (:criterions)";
        Query query = getSession().createQuery(strQuery);
        query.setParameterList("criterions", criterions);
        return (List<Resource>) query.list();
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
                + " JOIN generic.genericDayAssignmentsContainers container "
                + " JOIN container.dayAssignments dayAssignment"
                + " JOIN dayAssignment.resource resource"
                + " WHERE generic.task IN(:taskElements)";
        return getSession().createQuery(query)
                .setParameterList("taskElements",
                taskElements).list();
    }

    @SuppressWarnings("unchecked")
    private List<Resource> findRelatedToSpecific(List<Task> taskElements) {
        Scenario scenario = scenarioManager.getCurrent();
        List<Resource> list = getSession()
                .createQuery(
                        "SELECT DISTINCT specificAllocation.resource "
                                + "FROM SpecificResourceAllocation specificAllocation "
                                + "JOIN specificAllocation.specificDayAssignmentsContainers container "
                                + "WHERE specificAllocation.task IN(:taskElements) "
                                + "and container.scenario = :scenario "
                                + "and container.dayAssignments IS NOT EMPTY")
                .setParameterList("taskElements", taskElements)
                .setParameter("scenario", scenario)
                .list();
        return list;
    }

    public List<Resource> getResources() {
        return list(Resource.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Resource> getAllLimitingResources() {
        return getSession().createCriteria(Resource.class).add(
                Restrictions.eq("limitingResource", true)).list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Resource> getAllNonLimitingResources() {
        return getSession().createCriteria(Resource.class).add(
                Restrictions.eq("limitingResource", false)).list();
    }

    @Override
    public List<Machine> getMachines() {
        return list(Machine.class);
    }

    @Override
    public List<Resource> getRealResources() {
        List<Resource> list = new ArrayList<Resource>();
        list.addAll(getRealWorkers());
        list.addAll(getMachines());
        return list;
    }

    @Override
    public void save(Resource resource) {
        if (resource instanceof Worker || resource instanceof Machine) {
            if (resource.isLimitingResource() && resource.getLimitingResourceQueue() == null) {
                resource.setLimitingResourceQueue(LimitingResourceQueue.create());
            }
        }
        super.save(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoursWorkedPerResourceDTO> getWorkingHoursPerWorker(
            List<Resource> resources, List<Label> labels,
            List<Criterion> criterions, Date startingDate,
            Date endingDate) {

        String strQuery = "SELECT new org.navalplanner.business.reports.dtos.HoursWorkedPerResourceDTO(resource, wrl) "
                + "FROM Resource resource, WorkReportLine wrl "
                + "LEFT OUTER JOIN wrl.resource wrlresource "
                + "WHERE wrlresource.id = resource.id ";

        // Set date range
        if (startingDate != null && endingDate != null) {
            strQuery += "AND wrl.date BETWEEN :startingDate AND :endingDate ";
        }
        if (startingDate != null && endingDate == null) {
            strQuery += "AND wrl.date >= :startingDate ";
        }
        if (startingDate == null && endingDate != null) {
            strQuery += "AND wrl.date <= :endingDate ";
        }

        // Set workers
        if (resources != null && !resources.isEmpty()) {
            strQuery += "AND resource IN (:resources) ";
        }

        // Set labels
        if (labels != null && !labels.isEmpty()) {
            strQuery += " AND ( EXISTS (FROM wrl.labels as etq WHERE etq IN (:labels)) "
                    + "OR EXISTS (FROM wrl.workReport.labels as etqwr WHERE etqwr IN (:labels))) ";
        }

        // Set Criterions
        if (criterions != null && !criterions.isEmpty()) {
            strQuery += "AND EXISTS (FROM resource.criterionSatisfactions as satisfaction "
                    + " WHERE satisfaction.criterion IN (:criterions))";
        }

        // Order by
        strQuery += "ORDER BY resource.id, wrl.date";

        // Set parameters
        Query query = getSession().createQuery(strQuery);
        if (startingDate != null) {
            query.setParameter("startingDate", startingDate);
        }
        if (endingDate != null) {
            query.setParameter("endingDate", endingDate);
        }
        if (resources != null && !resources.isEmpty()) {
            query.setParameterList("resources", resources);
        }

        if (labels != null && !labels.isEmpty()) {
            query.setParameterList("labels", labels);
        }
        if (criterions != null && !criterions.isEmpty()) {
            query.setParameterList("criterions", criterions);
        }

        // Get result
        return query.list();
    }

    @Override
    public List<HoursWorkedPerWorkerInAMonthDTO> getWorkingHoursPerWorker(
            Integer year, Integer month) {

        String strQuery =
            "SELECT wrlresource.id, SUM(wrl.numHours) "
          + "FROM WorkReportLine wrl "
          + "LEFT OUTER JOIN wrl.resource wrlresource ";

        if (year != null) {
            strQuery += "WHERE YEAR(wrl.date) = :year ";
        }
        if (month != null) {
            strQuery += "AND MONTH(wrl.date) = :month ";
        }

        strQuery += "GROUP BY wrlresource.id, MONTH(wrl.date) ";

        Query query = getSession().createQuery(strQuery);
        if (year != null) {
            query.setParameter("year", year);
        }
        if (month != null) {
            query.setParameter("month", month);
        }

        List<HoursWorkedPerWorkerInAMonthDTO> result = toDTO(query.list());
        return result;
    }

    @Override
    public Boolean existStrategicResources() {
        List strategicResources = getSession().createCriteria(Resource.class).add(
                Restrictions.eq("resourceType", ResourceType.STRATEGIC_RESOURCE)).list();
        return !strategicResources.isEmpty();
    }

    private List<HoursWorkedPerWorkerInAMonthDTO> toDTO(List<Object> rows) {
        List<HoursWorkedPerWorkerInAMonthDTO> result = new ArrayList<HoursWorkedPerWorkerInAMonthDTO>();

        for (Object row: rows) {
            Object[] columns = (Object[]) row;
            Worker worker = (Worker) findExistingEntity((Long) columns[0]);
            Long numHours = (Long) columns[1];

            HoursWorkedPerWorkerInAMonthDTO dto = new HoursWorkedPerWorkerInAMonthDTO(
                    worker, numHours);
            result.add(dto);
        }
        return result;
    }

}