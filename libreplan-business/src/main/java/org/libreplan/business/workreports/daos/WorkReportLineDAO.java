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

package org.libreplan.business.workreports.daos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
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

    @SuppressWarnings("unchecked")
    @Override
    public List<WorkReportLine> findByOrderElement(OrderElement orderElement){
        Criteria c = getSession().createCriteria(WorkReportLine.class).createCriteria("orderElement");
        c.add(Restrictions.idEq(orderElement.getId()));
        return (List<WorkReportLine>) c.list();
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

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly=true)
    public List<WorkReportLine> findByOrderElementAndChildren(OrderElement orderElement, boolean sortByDate) {
        // Create collection with current orderElement and all its children
        Collection<OrderElement> orderElements = orderElement.getAllChildren();
        orderElements.add(orderElement);

        // Prepare criteria
        final Criteria criteria = getSession().createCriteria(WorkReportLine.class);
        criteria.add(Restrictions.in("orderElement", orderElements));
        if (sortByDate) {
            criteria.addOrder(org.hibernate.criterion.Order.asc("date"));
        }
        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<WorkReportLine> findFilteredByDate(Date start, Date end) {
        Criteria criteria  = getSession().createCriteria(WorkReportLine.class);
        if(start != null) {
            criteria.add(Restrictions.ge("date", start));
        }
        if(end != null) {
            criteria.add(Restrictions.le("date", end));
        }
        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<WorkReportLine> findByResources(List<Resource> resourcesList) {
        if (resourcesList.isEmpty()) {
            return Collections.emptyList();
        }
        return getSession().createCriteria(WorkReportLine.class).add(
                Restrictions.in("resource", resourcesList)).list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<WorkReportLine> findByResourceFilteredByDateNotInWorkReport(
            Resource resource, Date start, Date end, WorkReport workReport) {
        Criteria criteria = getSession().createCriteria(WorkReportLine.class);
        criteria.add(Restrictions.eq("resource", resource));

        criteria.add(Restrictions.ge("date", start));
        criteria.add(Restrictions.le("date", end));

        if (workReport != null) {
            criteria.add(Restrictions.ne("workReport", workReport));
        }

        return criteria.list();
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
    public List<WorkReportLine> findByOrderElementNotInWorkReportAnotherTransaction(
            OrderElement orderElement, WorkReport workReport) {
        return findByOrderElementNotInWorkReport(orderElement, workReport);
    }

    @SuppressWarnings("unchecked")
    private List<WorkReportLine> findByOrderElementNotInWorkReport(
            OrderElement orderElement, WorkReport workReport) {
        Criteria criteria = getSession().createCriteria(WorkReportLine.class);

        criteria.add(Restrictions.eq("orderElement", orderElement));
        if (!workReport.isNewObject()) {
            criteria.add(Restrictions.ne("workReport", workReport));
        }

        return (List<WorkReportLine>) criteria.list();
    }

}
