package org.navalplanner.business.orders.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.impl.GenericDAOHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.entities.OrderElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

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
}
