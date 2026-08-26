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

package org.libreplan.business.workreports.daos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.hibernate.query.Query;
import org.libreplan.business.common.daos.IntegrationEntityDAO;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.reports.dtos.WorkReportLineDTO;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.util.Pair;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dao for {@link WorkReportLineDAO}
 *
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class WorkReportLineDAO extends IntegrationEntityDAO<WorkReportLine>
        implements IWorkReportLineDAO {

    @Override
    public List<WorkReportLine> findByOrderElement(OrderElement orderElement){
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<WorkReportLine> cq = cb.createQuery(WorkReportLine.class);
        Root<WorkReportLine> root = cq.from(WorkReportLine.class);
        cq.where(cb.equal(root.get("orderElement").get("id"), orderElement.getId()));
        return getSession().createQuery(cq).getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<WorkReportLineDTO> findByOrderElementGroupByResourceAndHourTypeAndDate(
            OrderElement orderElement) {

        String strQuery = "SELECT new org.libreplan.business.reports.dtos.WorkReportLineDTO(wrl.resource, wrl.typeOfWorkHours, wrl.date, SUM(wrl.effort)) "
                + "FROM WorkReportLine wrl "
                + "LEFT OUTER JOIN wrl.orderElement orderElement "
                + "WHERE orderElement = :orderElement "
                + "GROUP BY wrl.resource, wrl.typeOfWorkHours, wrl.date "
                + "ORDER BY wrl.resource, wrl.typeOfWorkHours, wrl.date";

        // Set parameters
        Query query = getSession().createQuery(strQuery);
        query.setParameter("orderElement", orderElement);

        return (List<WorkReportLineDTO>) query.list();
    }

    @Override
    public List<WorkReportLine> findByOrderElementAndChildren(
            OrderElement orderElement) {
        if (orderElement.isNewObject()) {
            return new ArrayList<WorkReportLine>();
        }
        return findByOrderElementAndChildren(orderElement, false);
    }

    @Override
    @Transactional(readOnly=true)
    public List<WorkReportLine> findByOrderElementAndChildren(OrderElement orderElement, boolean sortByDate) {
        // Create collection with current orderElement and all its children
        Collection<OrderElement> orderElements = orderElement.getAllChildren();
        orderElements.add(orderElement);

        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<WorkReportLine> cq = cb.createQuery(WorkReportLine.class);
        Root<WorkReportLine> root = cq.from(WorkReportLine.class);
        cq.where(root.get("orderElement").in(orderElements));
        if (sortByDate) {
            cq.orderBy(cb.asc(root.get("date")));
        }
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    public List<WorkReportLine> findFilteredByDate(Date start, Date end) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<WorkReportLine> cq = cb.createQuery(WorkReportLine.class);
        Root<WorkReportLine> root = cq.from(WorkReportLine.class);

        List<Predicate> predicates = new ArrayList<>();
        if(start != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("date"), start));
        }
        if(end != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("date"), end));
        }
        cq.where(predicates.toArray(new Predicate[0]));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    public List<WorkReportLine> findByResources(List<Resource> resourcesList) {
        if (resourcesList.isEmpty()) {
            return Collections.emptyList();
        }
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<WorkReportLine> cq = cb.createQuery(WorkReportLine.class);
        Root<WorkReportLine> root = cq.from(WorkReportLine.class);
        cq.where(root.get("resource").in(resourcesList));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    public List<WorkReportLine> findByResourceFilteredByDateNotInWorkReport(
            Resource resource, Date start, Date end, WorkReport workReport) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<WorkReportLine> cq = cb.createQuery(WorkReportLine.class);
        Root<WorkReportLine> root = cq.from(WorkReportLine.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("resource"), resource));
        predicates.add(cb.greaterThanOrEqualTo(root.get("date"), start));
        predicates.add(cb.lessThanOrEqualTo(root.get("date"), end));

        if (workReport != null) {
            predicates.add(cb.notEqual(root.get("workReport"), workReport));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    public Pair<Date, Date> findMinAndMaxDatesByOrderElement(
            OrderElement orderElement) {

        String strQuery = "SELECT MIN(date) AS min, MAX(date) AS max "
                + "FROM WorkReportLine " + "WHERE orderElement = :orderElement";

        Query query = getSession().createQuery(strQuery);
        query.setParameter("orderElement", orderElement);

        Object[] result = (Object[]) query.uniqueResult();

        Date min = null;
        Date max = null;
        if (result != null) {
            min = (Date) result[0];
            max = (Date) result[1];
        }
        return Pair.create(min, max);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public List<WorkReportLine> findFinishedByOrderElementNotInWorkReportAnotherTransaction(
            OrderElement orderElement, WorkReport workReport) {
        return findFinishedByOrderElementNotInWorkReport(orderElement, workReport);
    }

    private List<WorkReportLine> findFinishedByOrderElementNotInWorkReport(
            OrderElement orderElement, WorkReport workReport) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<WorkReportLine> cq = cb.createQuery(WorkReportLine.class);
        Root<WorkReportLine> root = cq.from(WorkReportLine.class);

        // Compare by id, not by entity reference (same idiom as findByOrderElement() above).
        // orderElement/workReport here often belong to a different Hibernate session than this
        // one (findFinishedByOrderElementNotInWorkReportAnotherTransaction below runs in its own
        // REQUIRES_NEW transaction), so using them directly as Criteria equality parameters makes
        // Hibernate try to determine whether they're transient or detached - which throws
        // TransientObjectException even for an entity with a real, persisted id. Comparing raw
        // ids sidesteps that entirely; getId() needs no session at all.
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("orderElement").get("id"), orderElement.getId()));
        if (!workReport.isNewObject()) {
            predicates.add(cb.notEqual(root.get("workReport").get("id"), workReport.getId()));
        }
        predicates.add(cb.equal(root.get("finished"), true));

        cq.where(predicates.toArray(new Predicate[0]));
        return getSession().createQuery(cq).getResultList();
    }

    @Override
    public Boolean isFinished(OrderElement orderElement) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<WorkReportLine> cq = cb.createQuery(WorkReportLine.class);
        Root<WorkReportLine> root = cq.from(WorkReportLine.class);
        cq.where(
                cb.equal(root.get("orderElement"), orderElement),
                cb.equal(root.get("finished"), true));

        return getSession().createQuery(cq).uniqueResult() != null;
    }

    @Override
    public List<WorkReportLine> findByOrderElementAndWorkReports(
            OrderElement orderElement, List<WorkReport> workReports) {
        if (workReports.isEmpty()) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<WorkReportLine> cq = cb.createQuery(WorkReportLine.class);
        Root<WorkReportLine> root = cq.from(WorkReportLine.class);
        cq.where(
                cb.equal(root.get("orderElement"), orderElement),
                root.get("workReport").in(workReports));

        return getSession().createQuery(cq).getResultList();
    }

    @Transactional(readOnly = true)
    public List<WorkReportLine> findByOrderElementAndChildrenFilteredByDate(
            OrderElement orderElement, Date start, Date end, boolean sortByDate) {

        if (orderElement.isNewObject()) {
            return new ArrayList<WorkReportLine>();
        }

        Collection<OrderElement> orderElements = orderElement.getAllChildren();
        orderElements.add(orderElement);

        CriteriaBuilder cb = getSession().getCriteriaBuilder();
        CriteriaQuery<WorkReportLine> cq = cb.createQuery(WorkReportLine.class);
        Root<WorkReportLine> root = cq.from(WorkReportLine.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(root.get("orderElement").in(orderElements));
        if (start != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("date"), start));
        }
        if (end != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("date"), end));
        }
        cq.where(predicates.toArray(new Predicate[0]));
        if (sortByDate) {
            cq.orderBy(cb.asc(root.get("date")));
        }
        return getSession().createQuery(cq).getResultList();

    }

}
