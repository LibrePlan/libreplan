package org.navalplanner.web.common.converters;

import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.services.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * A {@link Converter} for {@link Order} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class OrderConverter implements Converter<Order> {

    @Autowired
    private IOrderService orderService;

    @Override
    public Order asObject(String stringRepresentation) {
        try {
            return orderService.find(Long.parseLong(stringRepresentation));
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String asString(Order entity) {
        return entity.getId() + "";
    }

    @Override
    public String asStringUngeneric(Object entity) {
        return asString((Order) entity);
    }

    @Override
    public Class<Order> getType() {
        return Order.class;
    }

}
