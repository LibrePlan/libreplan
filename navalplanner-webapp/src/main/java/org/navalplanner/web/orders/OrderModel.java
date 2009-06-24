package org.navalplanner.web.orders;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.orders.entities.IOrderLineGroup;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.services.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link Order}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OrderModel implements IOrderModel {

    private final IOrderService orderService;

    private Order order;

    private ClassValidator<Order> orderValidator = new ClassValidator<Order>(
            Order.class);

    private OrderElementModel orderElementTreeModel;

    @Autowired
    public OrderModel(IOrderService orderService) {
        Validate.notNull(orderService);
        this.orderService = orderService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrders() {
        return orderService.getOrders();
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareEditFor(Order order) {
        Validate.notNull(order);
        try {
            this.order = orderService.find(order.getId());
            this.orderElementTreeModel = new OrderElementModel(this.order);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void prepareForCreate() {
        this.order = new Order();
        this.orderElementTreeModel = new OrderElementModel(this.order);
        this.order.setInitDate(new Date());
    }

    @Override
    @Transactional
    public void save() throws ValidationException {
        InvalidValue[] invalidValues = orderValidator
                .getInvalidValues(order);
        if (invalidValues.length > 0)
            throw new ValidationException(invalidValues);
        this.orderService.save(order);
    }

    @Override
    public IOrderLineGroup getOrder() {
        return order;
    }

    @Override
    public void remove(Order order) {
        try {
            this.orderService.remove(order);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void prepareForRemove(Order order) {
        this.order = order;
    }

    @Override
    public OrderElementModel getOrderElementTreeModel() {
        return orderElementTreeModel;
    }

}
