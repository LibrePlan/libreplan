package org.navalplanner.business.orders.services;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.navalplanner.business.common.OnTransaction;
import org.navalplanner.business.common.daos.impl.GenericDaoHibernate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link IOrderService} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Transactional
public class OrderService implements IOrderService {

    @Autowired
    private SessionFactory sessionFactory;

    /*
     * Because the dao for orderwork doesn't have special needs, it's not
     * created an interface for defining its contract
     */

    private GenericDaoHibernate<Order, Long> dao = new GenericDaoHibernate<Order, Long>() {

        @Override
        protected Session getSession() {
            return sessionFactory.getCurrentSession();
        }
    };

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Order order) {
        return dao.exists(order.getId());
    }

    @Override
    public void save(Order order) throws ValidationException {
        if (order.isEndDateBeforeStart()) {
            throw new ValidationException("endDate must be after startDate");
        }
        dao.save(order);
    }

    @Override
    public List<Order> getOrders() {
        return dao.list(Order.class);
    }

    @Override
    public Order find(Long orderId)
            throws InstanceNotFoundException {
        return dao.find(orderId);
    }

    @Override
    public void remove(Order order)
            throws InstanceNotFoundException {
        dao.remove(order.getId());
    }

    @Override
    public <T> T onTransaction(OnTransaction<T> onTransaction) {
        return onTransaction.execute();
    }

}
