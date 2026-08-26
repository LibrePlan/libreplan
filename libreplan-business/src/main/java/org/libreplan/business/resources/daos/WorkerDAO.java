/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

package org.libreplan.business.resources.daos;

import java.util.Date;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.hibernate.query.Query;
import org.libreplan.business.common.daos.IntegrationEntityDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.resources.entities.Worker;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * Hibernate DAO for the <code>Worker</code> entity.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino Garcia <dpino@igalia.com>
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class WorkerDAO extends IntegrationEntityDAO<Worker>
    implements IWorkerDAO {

    @Override
    public Worker findUniqueByNif(String nif) throws InstanceNotFoundException {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<Worker> cq = cb.createQuery(Worker.class);
        Root<Worker> root = cq.from(Worker.class);
        cq.where(cb.equal(cb.lower(root.get("nif")), nif.trim().toLowerCase()));

        List<Worker> list = getSession().createQuery(cq).getResultList();
        if (list.size() != 1) {
            throw new InstanceNotFoundException(nif, Worker.class.getName());
        }

        return list.get(0);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Worker findByNifAnotherTransaction(String nif)
            throws InstanceNotFoundException {
        return findUniqueByNif(nif);
    }

    @Override
    public List<Worker> getWorkers() {
        return getSession().createQuery(
                "FROM Worker worker WHERE worker NOT IN (FROM VirtualWorker)")
                .list();
    }

    @Override
    public List<Worker> getAll() {
        return list(Worker.class);
    }

    @Override
    public List<Worker> findByFirstNameCaseInsensitive(String name) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<Worker> cq = cb.createQuery(Worker.class);
        Root<Worker> root = cq.from(Worker.class);
        cq.where(cb.equal(cb.lower(root.get("firstName")), name.toLowerCase()));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public List<Worker> findByFirstNameAnotherTransactionCaseInsensitive(String name) {
        return findByFirstNameCaseInsensitive(name);
    }

    @Override
    public List<Worker> findByFirstNameSecondNameAndNif(String firstname,
            String secondname, String nif) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<Worker> cq = cb.createQuery(Worker.class);
        Root<Worker> root = cq.from(Worker.class);
        cq.where(
                cb.like(cb.lower(root.get("firstName")), firstname.toLowerCase()),
                cb.like(cb.lower(root.get("surname")), secondname.toLowerCase()),
                cb.like(root.get("nif"), nif));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public List<Worker> findByFirstNameSecondNameAndNifAnotherTransaction(
            String firstname, String secondname, String nif) {
        return findByFirstNameSecondNameAndNif(firstname, secondname, nif);
    }

    @Override
    public List<Worker> findByFirstNameSecondName(String firstname,
            String secondname) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<Worker> cq = cb.createQuery(Worker.class);
        Root<Worker> root = cq.from(Worker.class);
        cq.where(
                cb.like(cb.lower(root.get("firstName")), firstname.toLowerCase()),
                cb.like(cb.lower(root.get("surname")), secondname.toLowerCase()));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public List<Worker> findByFirstNameSecondNameAnotherTransaction(
            String firstname, String secondname) {
        return findByFirstNameSecondName(firstname, secondname);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getWorkingHoursGroupedPerWorker(
            List<String> workerCodes, Date startingDate, Date endingDate) {
        String strQuery = "SELECT worker.code, SUM(wrl.effort) "
                + "FROM Worker worker, WorkReportLine wrl "
                + "LEFT OUTER JOIN wrl.resource resource "
                + "WHERE resource.id = worker.id ";

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
        if (workerCodes != null && !workerCodes.isEmpty()) {
            strQuery += "AND worker.code IN (:workerCodes) ";
        }

        // Group by
        strQuery += "GROUP BY worker.code ";

        // Order by
        strQuery += "ORDER BY worker.code";

        // Set parameters
        Query query = getSession().createQuery(strQuery);
        if (startingDate != null) {
            query.setParameter("startingDate", startingDate);
        }
        if (endingDate != null) {
            query.setParameter("endingDate", endingDate);
        }
        if (workerCodes != null && !workerCodes.isEmpty()) {
            query.setParameterList("workerCodes", workerCodes);
        }

        // Get result
        return query.list();
    }

    @Override
    public List<Worker> getBound() {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<Worker> cq = cb.createQuery(Worker.class);
        Root<Worker> root = cq.from(Worker.class);
        cq.where(cb.isNotNull(root.get("user")));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Worker getCurrentWorker(Long resourceID) {
        List<Worker> workerList = getWorkers();

        for (Worker worker : workerList) {
            if (worker.getId().equals(resourceID)) {
                return worker;
            }
        }

        return null;
    }
}
