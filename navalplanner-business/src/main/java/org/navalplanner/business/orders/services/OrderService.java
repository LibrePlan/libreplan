package org.navalplanner.business.orders.services;

import java.util.List;

import org.navalplanner.business.common.OnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.daos.IOrderDao;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link IOrderService} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Transactional
public class OrderService implements IOrderService {

    @Autowired
    private IOrderDao orderDAO;

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Order order) {
        return orderDAO.exists(order.getId());
    }

    @Override
    public void save(Order order) throws ValidationException {
        if (order.isEndDateBeforeStart()) {
            throw new ValidationException("endDate must be after startDate");
        }
        for (OrderElement orderElement : order.getOrderElements()) {
            if (!orderElement.checkAtLeastOneHoursGroup()) {
                throw new ValidationException(
                        "At least one HoursGroup is needed for each OrderElement");
            }
        }
        orderDAO.save(order);
    }

    @Override
    public List<Order> getOrders() {
        return orderDAO.list(Order.class);
    }

    @Override
    public Order find(Long orderId) throws InstanceNotFoundException {
        return orderDAO.find(orderId);
    }

    @Override
    public void remove(Order order) throws InstanceNotFoundException {
        orderDAO.remove(order.getId());
    }

    @Override
    public <T> T onTransaction(OnTransaction<T> onTransaction) {
        return onTransaction.execute();
    }

}
