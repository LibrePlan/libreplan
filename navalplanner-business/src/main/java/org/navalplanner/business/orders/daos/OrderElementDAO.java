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
import org.navalplanner.business.workreports.daos.IWorkReportLineDAO;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
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

    public List<OrderElement> findByCode(String code) {
        Criteria c = getSession().createCriteria(OrderElement.class);
        c.add(Restrictions.eq("code", code));
        return (List<OrderElement>) c.list();
    }

    public OrderElement findUniqueByCode(String code)
            throws InstanceNotFoundException {
        List<OrderElement> list = findByCode(code);
        if (list.size() > 1) {
            throw new InstanceNotFoundException(code, OrderElement.class
                    .getName());
        }
        return list.get(0);
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

    public String getDistinguishedCode(OrderElement orderElement)
            throws InstanceNotFoundException {
        String code = orderElement.getCode();

        while (orderElement.getParent() != null) {
            OrderElement parent = find(orderElement.getParent().getId());
            code = parent.getCode() + "-" + code;
            orderElement = parent;
        }
        return code;
    }

    @Override
    @Transactional(readOnly = true)
    public int getAssignedHours(OrderElement orderElement) {
        int addAsignedHoursChildren = 0;
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

}
