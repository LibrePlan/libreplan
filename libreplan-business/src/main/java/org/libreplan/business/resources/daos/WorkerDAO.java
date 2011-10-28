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

package org.libreplan.business.resources.daos;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
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
 *
 */
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class WorkerDAO extends IntegrationEntityDAO<Worker>
    implements IWorkerDAO {

    @Override
    public Worker findUniqueByNif(String nif) throws InstanceNotFoundException {
        Criteria criteria = getSession().createCriteria(Worker.class);
        criteria.add(Restrictions.eq("nif", nif.trim()).ignoreCase());

        List<Worker> list = criteria.list();
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

    @SuppressWarnings("unchecked")
    @Override
    public List<Worker> findByNameSubpartOrNifCaseInsensitive(String name, boolean limitingResource) {
        final String containsName = "%" + name + "%";
        return getSession().createCriteria(Worker.class).add(
                Restrictions.and(
                        Restrictions.eq("limitingResource", limitingResource),
                        Restrictions.or(
                                Restrictions.or(
                                        Restrictions.ilike("firstName", containsName),
                                        Restrictions.ilike("surname", containsName)),
                                Restrictions.like("nif", containsName)))).list();

    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Worker> findByFirstNameCaseInsensitive(String name) {
        return getSession().createCriteria(Worker.class).add(
                Restrictions.ilike("firstName", name)).list();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public List<Worker> findByFirstNameAnotherTransactionCaseInsensitive(String name) {
        return findByFirstNameCaseInsensitive(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Worker> findByFirstNameSecondNameAndNif(String firstname,
            String secondname, String nif) {
        return getSession().createCriteria(Worker.class).add(
                Restrictions.and(Restrictions.ilike("firstName", firstname),
                        Restrictions.and(Restrictions.ilike("surname",
                                secondname), Restrictions.like("nif", nif))))
                .list();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public List<Worker> findByFirstNameSecondNameAndNifAnotherTransaction(
            String firstname, String secondname, String nif) {
        return findByFirstNameSecondNameAndNif(firstname, secondname, nif);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Worker> findByFirstNameSecondName(String firstname,
            String secondname) {
        return getSession().createCriteria(Worker.class).add(
                Restrictions.and(Restrictions.ilike("firstName", firstname),
                        Restrictions.ilike("surname", secondname))).list();
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
            List<String> workerNifs, Date startingDate, Date endingDate) {
        String strQuery = "SELECT worker.nif, SUM(wrl.numHours) "
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
        if (workerNifs != null && !workerNifs.isEmpty()) {
            strQuery += "AND worker.nif IN (:workerNifs) ";
        }

        // Group by
        strQuery += "GROUP BY worker.nif ";

        // Order by
        strQuery += "ORDER BY worker.nif";

        // Set parameters
        Query query = getSession().createQuery(strQuery);
        if (startingDate != null) {
            query.setParameter("startingDate", startingDate);
        }
        if (endingDate != null) {
            query.setParameter("endingDate", endingDate);
        }
        if (workerNifs != null && !workerNifs.isEmpty()) {
            query.setParameterList("workerNifs", workerNifs);
        }

        // Get result
        return query.list();
    }

}