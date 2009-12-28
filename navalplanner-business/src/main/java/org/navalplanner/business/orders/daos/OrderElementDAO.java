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

package org.navalplanner.business.orders.daos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.GenericDAOHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.workreports.daos.IWorkReportLineDAO;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dao for {@link OrderElement}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 **/
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class OrderElementDAO extends GenericDAOHibernate<OrderElement, Long>
        implements IOrderElementDAO {

    @Autowired
    private IWorkReportLineDAO workReportLineDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Override
    public List<OrderElement> findWithoutParent() {
        Criteria c = getSession().createCriteria(OrderElement.class);
        c.add(Restrictions.isNull("parent"));
        return (List<OrderElement>) c.list();
    }

    public List<OrderElement> findByCodeAndParent(OrderElement parent,
            String code) {
        Criteria c = getSession().createCriteria(OrderElement.class);
        c.add(Restrictions.eq("code", code));
        if (parent != null) {
            c.add(Restrictions.eq("parent", parent));
        } else {
            c.add(Restrictions.isNull("parent"));
        }
        return c.list();
    }

    public OrderElement findUniqueByCodeAndParent(OrderElement parent,
            String code) throws InstanceNotFoundException {
        List<OrderElement> list = findByCodeAndParent(parent, code);
        if (list.isEmpty() || list.size() > 1) {
            throw new InstanceNotFoundException(code, OrderElement.class
                    .getName());
        }
        return list.get(0);
    }

    @Override
    public List<OrderElement> findParent(OrderElement orderElement) {
        Criteria c = getSession().createCriteria(OrderElement.class)
                .createCriteria("children").add(
                        Restrictions.idEq(orderElement.getId()));
        return ((List<OrderElement>) c.list());
    }

    @Override
    @Transactional(readOnly = true)
    public int getAssignedHours(OrderElement orderElement) {
        int addAsignedHoursChildren = 0;
        if (orderElement != null) {
            if (!orderElement.getChildren().isEmpty()) {
                List<OrderElement> children = orderElement.getChildren();
                Iterator<OrderElement> iterador = children.iterator();
                while (iterador.hasNext()) {
                    OrderElement w = iterador.next();
                    addAsignedHoursChildren = addAsignedHoursChildren
                            + getAssignedHours(w);
                }
            }
            List<WorkReportLine> listWRL = this.workReportLineDAO
                    .findByOrderElement(orderElement);
            return (getAssignedDirectHours(listWRL) + addAsignedHoursChildren);
        }
        return 0;
    }

    private int getAssignedDirectHours(List<WorkReportLine> listWRL) {
        int asignedDirectHours = 0;
        Iterator<WorkReportLine> iterator = listWRL.iterator();
        while (iterator.hasNext()) {
            asignedDirectHours = asignedDirectHours
                    + iterator.next().getNumHours();
        }
        return asignedDirectHours;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getHoursAdvancePercentage(OrderElement orderElement) {
        BigDecimal assignedHours = new BigDecimal(
                getAssignedHours(orderElement)).setScale(2);
        BigDecimal estimatedHours = new BigDecimal(orderElement.getWorkHours())
                .setScale(2);

        if (estimatedHours.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return assignedHours.divide(estimatedHours, RoundingMode.DOWN);
    }

    @Override
    public void remove(Long id) throws InstanceNotFoundException {
        OrderElement orderElement = find(id);
        for (TaskSource each : orderElement.getTaskSourcesFromBottomToTop()) {
            each.detachAssociatedTaskFromParent();
            taskSourceDAO.remove(each.getId());
        }
        super.remove(id);
    }

    @Override
    public List<OrderElement> findByCode(String code) {
        Criteria c = getSession().createCriteria(OrderElement.class);
        c.add(Restrictions.eq("code", code).ignoreCase());
        return (List<OrderElement>) c.list();
    }

    @Override
    public OrderElement findUniqueByCode(String code)
            throws InstanceNotFoundException {
        Criteria c = getSession().createCriteria(OrderElement.class);
        c.add(Restrictions.eq("code", code));

        OrderElement orderElement = (OrderElement) c.uniqueResult();
        if (orderElement == null) {
            throw new InstanceNotFoundException(code, OrderElement.class
                    .getName());
        } else {
            return orderElement;
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public OrderElement findUniqueByCodeAnotherTransaction(String code)
            throws InstanceNotFoundException {
        return findUniqueByCode(code);
    }

    @Override
    public boolean existsOtherOrderElementByCode(OrderElement orderElement) {
        try {
            OrderElement t = findUniqueByCode(orderElement.getCode());
            return t != null && t != orderElement;
        } catch (InstanceNotFoundException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public boolean existsByCodeAnotherTransaction(OrderElement orderElement) {
        return existsOtherOrderElementByCode(orderElement);
    }

}
