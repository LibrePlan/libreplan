package org.navalplanner.business.orders.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.entities.OrderElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * Dao for {@link OrderElement}
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 **/
@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class OrderElementDao extends GenericDaoHibernate<OrderElement, Long>
        implements IOrderElementDao {

    public OrderElement findByCode(String code) {
        Criteria c = getSession().createCriteria(OrderElement.class);
        c.add(Restrictions.eq("code", code));
        return (OrderElement) c.uniqueResult();
    }

    public OrderElement findByCode(OrderElement orderElement, String code) {
        Criteria c = getSession().createCriteria(OrderElement.class);
        c.add(Restrictions.eq("code", code));
        c.add(Restrictions.eq("parent", orderElement));
        return (OrderElement) c.uniqueResult();
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
